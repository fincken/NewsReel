package de.dailab.plistacontest.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recommender {
	
	/**
	 * All the news articles in the system.
	 */
	private List<NewsArticle> newsArticles;
	
	/**
	 * ArticleId -> Article map for quick look up. Used primarily when the system needs
	 * to look up an article after a click. 
	 */
	private Map<Long, NewsArticle> newsArticleById;
	
	/**
	 * PublisherId -> all articles published by the publisher known to the system.
	 */
	private Map<Long, Set<NewsArticle>> newsArticlesByPublisher;
	
	/**
	 * The keywords the system knows about. Used to compute cosine similarities between articles.
	 */
	private List<String> words;
	
	/**
	 * All the articles read by a user. Stored as a map that maps from the user ID to a set of
	 * NewsArticle objects representing the news articles.
	 */
	private Map<Long, Set<NewsArticle>> readByUser;
	
	/**
	 * UserId -> all known publisher id preferences for user with id = UserId.
	 */
	private Map<Long, Set<Long>> userPublisherPreferences;
	
	/**
	 * UserId -> articles recommended over the course of the system's lifetime. Will be used to
	 * avoid recommending the same articles repeatedly. 
	 */
	private Map<Long, Set<NewsArticle>> recommendedToUser;
	
	private final static Logger logger = LoggerFactory.getLogger(Recommender.class);
	
	public Recommender() {
		// initialize data structures
		newsArticles = new ArrayList<NewsArticle>();
		readByUser = new HashMap<Long, Set<NewsArticle>>();
		newsArticleById = new HashMap<Long, NewsArticle>();
		userPublisherPreferences = new HashMap<Long, Set<Long>>();
		newsArticlesByPublisher = new HashMap<Long, Set<NewsArticle>>();
		words = new ArrayList<String>();
		
		// load the stop words of the language processor
		try {
			LanguageProcessor.loadStopWordsFromFile(new FileInputStream("stop_words.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a news article to the system.
	 * @param newsArticle - article that is to be added to the system
	 */
	public void addNewsArticle(NewsArticle newsArticle) {
		// get the ids of the article
		Long id = newsArticle.getId();
		Long publisherId = newsArticle.getPublisherId();
		
		// TODO: store categoryId -> set of articles?
		
		// check if the article already exists in the system
		if (newsArticleById.containsKey(id)) {
			// if it does, simply remove the old reference
			// it will be a different article object in memory, but with a matching id
			logger.info("Updating existing article {}", id);
			newsArticles.remove(newsArticleById.get(id));
		}
		
		// add the article to the system
		newsArticles.add(newsArticle);
		newsArticleById.put(id, newsArticle);
		
		// add the words from the article to the global list of words
		for (String word : newsArticle.getKeywords()) {
			if (!words.contains(word)) {
				words.add(word);
			}
		}
		
		if (newsArticles.size() > 30) {
			logger.info("Global list of words is {}", words);
		}
		
		// compute the frequency list of the new article
		newsArticle.computeFrequencyList(words);
		
		// pad the frequency lists of all the articles now so they are ready when a
		// recommendation request arrives
		for (NewsArticle article : newsArticles) {
			article.padFrequencyList(words.size() - article.getFrequencyList().size());
		}
		
		// put it in the publisherId -> Set<Article> map for easy lookup later
		if (newsArticlesByPublisher.containsKey(publisherId)) {
			newsArticlesByPublisher.get(publisherId).add(newsArticle);
		} else {
			newsArticlesByPublisher.put(publisherId,
					new HashSet<NewsArticle>(Arrays.asList(newsArticle)));
		}
	}
	
	/**
	 * Registers that a user has clicked on a recommendation.
	 * @param click - event with relevant information about the click
	 */
	public void userReadArticle(RecommenderItem click) {
		// get the id of the article that the user read
		// note: clicks are represented with a recs json field, which contains
		// the id, and only the id, of the read article
		Long readArticleId = click.getListOfDisplayedRecs().get(0);
		
		Long userId = click.getUserID();
		
		// TODO: look at context keywords?
		
		// check if the user is known to the data provider (user id != 0)
		if (userId == 0) {
			return;
		}
		
		// since the system only receives the id of the read article, try to gain
		// more information by finding the actual article object. Note that this might not
		// exist in the system.
		
		// check if the system knows about the article
		if (newsArticleById.containsKey(readArticleId)) {
			logger.info("User {} read article {} that is in the system", userId, readArticleId);
			
			// look up the article from it's id
			NewsArticle article = newsArticleById.get(readArticleId);
			
			// check if the system knows about the user
			if (readByUser.containsKey(userId)) {
				readByUser.get(userId).add(article); // add article to set of articles read by user
			} else {
				// the system does not know about the user yet
				// register with new list
				readByUser.put(userId, new HashSet<NewsArticle>(Arrays.asList(article)));
			}
		}
		// register publisher preferences
		if (userPublisherPreferences.containsKey(userId)) {
			userPublisherPreferences.get(userId).add(click.getDomainID());
		} else {
			userPublisherPreferences.put(userId,
										new HashSet<Long>(Arrays.asList(click.getDomainID())));
		}
	}
	
	/**
	 * Recommends articles to a user.
	 * @param request - recommendation request
	 * @return List of IDs of recommended articles
	 */
	public List<Long> recommend(RecommenderItem request) {
		// get info about the incoming request
		Long requestPublisherId = request.getDomainID();
		Long requestUserId = request.getUserID();
		int limit = request.getNumberOfRequestedResults();
		
		// check if the system knows anything about the user's preferences
		// note that a user id of 0 indicates an unknown user
		boolean userInSystem = requestUserId != 0 &&
										userPublisherPreferences.containsKey(requestUserId);
		
		List<NewsArticle> recommendations; // recommended articles goes here
		// ids are extracted before returning them
				
		if (userInSystem) {
			// the system knows something about the user's preferences
			
			// articles that match user preferences
			List<NewsArticle> matches = new ArrayList<NewsArticle>();
			// this might end up empty, which is why it is stored in a separate list
			
			// first, go through publisher preferences
			for (Long publisherId : userPublisherPreferences.get(requestUserId)) {
				if (newsArticlesByPublisher.containsKey(publisherId)) {
					for (NewsArticle article : newsArticlesByPublisher.get(publisherId)) {
						matches.add(article);
					}
				}
			}
			
			// next, check if the system knows about any articles the user has read
			if (readByUser.containsKey(requestUserId)) {
				logger.info("System got a request from a known user, computing cosines...");
				for (NewsArticle article : readByUser.get(requestUserId)) {
					for (NewsArticle articleInSystem : newsArticles) {
						if (article != articleInSystem) {
							double sim = cosineSimilarity(article.getFrequencyList(),
									articleInSystem.getFrequencyList());
							if (sim > 0.1) {
								logger.info("Article 1: {}", article.getKeywords());
								logger.info("Article 2: {}", articleInSystem.getKeywords());
							}
						}
					}
				}
			}
			// TODO: look at categories?
			// done searching for matches, check how many we got
			
			if (matches.size() > limit) {
				recommendations = getNRandomArticles(matches, limit);
			} else {
				recommendations = matches.isEmpty() ?
						getNRandomArticles(newsArticles, limit) : matches;
			}
			
		} else {
			// the system knows nothing about the user, the best it can do is 
			// to recommend random items
			
			// TODO: look at the most popular categories and/or publishers?
			
			if (newsArticles.size() > limit) {
				recommendations = getNRandomArticles(newsArticles, limit);
			} else {
				recommendations = newsArticles;
			}
		}
		return getArticleIds(recommendations);
	}
	
	private double cosineSimilarity(List<Integer> frequencyList, List<Integer> frequencyList2) {
		if (frequencyList.size() != frequencyList2.size()) {
			return Double.NaN;
		}
		
        double innerProduct = 0.0, thisPower2 = 0.0, thatPower2 = 0.0;
        for (int i = 0; i < frequencyList.size(); i++) {
            innerProduct += frequencyList.get(i).doubleValue() * frequencyList2.get(i).doubleValue();
            thisPower2 += frequencyList.get(i).doubleValue() * frequencyList.get(i).doubleValue();
            thatPower2 += frequencyList2.get(i).doubleValue() * frequencyList2.get(i).doubleValue();
        }
        return innerProduct / Math.sqrt(thisPower2 * thatPower2);
	}

	/**
	 * Returns n random, unique articles from a list of articles.
	 * @param articles - articles to pick from
	 * @param n - number of articles
	 * @return List of n random, unique articles from articles
	 */
	private static List<NewsArticle> getNRandomArticles(List<NewsArticle> articles, int n) {
		List<NewsArticle> result = new ArrayList<NewsArticle>();
		Random rand = new Random();
		while (result.size() < n) {
			int idx = rand.nextInt(articles.size());
			while (result.contains(articles.get(idx))) {
				idx = rand.nextInt(articles.size());
			}
			result.add(articles.get(idx));
		}
		return result;
	}
	
	/**
	 * Extract IDs from articles.
	 * @param articles - the articles to extract the IDs from
	 * @return The IDs of the articles
	 */
	private static List<Long> getArticleIds(List<NewsArticle> articles) {
		List<Long> result = new ArrayList<Long>();
		for (NewsArticle article : articles) {
			result.add(article.getId());
		}
		return result;
	}

}
