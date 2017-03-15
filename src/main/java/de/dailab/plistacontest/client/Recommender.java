package de.dailab.plistacontest.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recommender implements Comparator<NewsArticle> {
	
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
	private Collection<String> words;
	
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
		
	private static final LanguageProcessor languageProcessor = LanguageProcessor.INSTANCE;
	
	private static final Logger logger = LoggerFactory.getLogger(Recommender.class);
	
	private Collection<NewsArticle> readByActiveUser;
		
	public Recommender() {
		// initialize data structures
		newsArticles = new ArrayList<NewsArticle>();
		readByUser = new HashMap<Long, Set<NewsArticle>>();
		newsArticleById = new HashMap<Long, NewsArticle>();
		userPublisherPreferences = new HashMap<Long, Set<Long>>();
		newsArticlesByPublisher = new HashMap<Long, Set<NewsArticle>>();
		words = new LinkedHashSet<String>();
		
		// load the stop words of the language processor
		try {
			languageProcessor.loadStopWordsFromFile(new FileInputStream("stop_words.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public List<NewsArticle> getNewsArticles() {
		return new ArrayList<NewsArticle>(newsArticles);
	}
	
	public Collection<String> getWords() {
		return new LinkedHashSet<String>(words);
	}
	
	/**
	 * Adds a news article to the system. Removes any existing reference to the article if
	 * necessary. Maintains the global word list with any new words from the article.
	 * Computes the frequency list of the article, and pads the frequency lists of all the other
	 * articles with zeroes for any new words. 
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
		
		if (newsArticles.size() > 35) {
			logger.info("Global word list is: {}", words);
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
	 * Registers that a user has clicked on a recommendation, and read an article.
	 * Maintains a UserId -> Set<Article> map that is used to build user profiles.
	 * @param userId - id of the user that read the article
	 * @param articleId - id of the read article
	 * @param publisherId - id of the publisher of the read article
	 */
	public void userReadArticle(Long userId, Long articleId, Long publisherId) {
		// TODO: look at context keywords?
		
		// ignore the click if the user is unknown to the system
		if (userId == 0) {
			return;
		}
		
		// since the system only receives the id of the read article, try to gain
		// more information by finding the actual article object. Note that this might not
		// exist in the system.
		
		// check if the system knows about the article
		if (newsArticleById.containsKey(articleId)) {
			logger.info("User {} read article {} that is in the system", userId, articleId);
			
			// look up the article from it's id
			NewsArticle article = newsArticleById.get(articleId);
			
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
			userPublisherPreferences.get(userId).add(publisherId);
		} else {
			userPublisherPreferences.put(userId,
										new HashSet<Long>(Arrays.asList(publisherId)));
		}
	}
	
	/**
	 * Recommends articles to a user. If the system has a profile of the user, use k-nearest
	 * neighbors with cosine similarity.
	 * @param userId - id of the user to recommend articles to
	 * @param limit - number of articles to recommend
	 * @return a List of the IDs of the recommended articles
	 */
	public List<Long> recommend(Long userId, int limit) {		
		List<NewsArticle> recommendations; // recommended articles goes here
		// ids are extracted before returning them
				
		// check if the system knows anything about the user's preferences
		if (readByUser.containsKey(userId)) {
			recommendations = recommendKArticles(userId, limit);
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
	
	/**
	 * Returns n random, unique articles from a list of articles.
	 * @param articles - articles to pick from
	 * @param n - number of articles
	 * @return List of n random, unique articles from articles
	 */
	private List<NewsArticle> getNRandomArticles(List<NewsArticle> articles, int n) {
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
	
	private List<NewsArticle> recommendKArticles(Long userId, int k) {
		setActiveUser(userId);
		List<NewsArticle> candidates = new ArrayList<NewsArticle>(newsArticles);
		
		// do not recommend articles that the user has already read
		for (NewsArticle article : readByActiveUser) {
			candidates.remove(article);
		}
		
		// TODO: filter candidates? It may very well be naive and inefficient to look at all
		// articles, since most articles will have a cos. sim. of 0 anyways.
		// Implement straightforward thresholding?
		
		// sort the candidates in descending order according to average cosine similarity
		// to articles read by user
		Collections.sort(candidates, this);
		
		return candidates.subList(0, Math.min(candidates.size(), k));
	}
	
	/**
	 * Calculates the average similarity of an article compared to the articles read by the
	 * current active user. This assumes that the articles read by the active user has already
	 * been set.
	 * @param article - article to predict a rating for
	 * @return the predicted rating for the article
	 */
	private double predictRating(NewsArticle article) {
		double sum = 0;
		for (NewsArticle readArticle : readByActiveUser) {
			sum += Util.cosineSimilarity(article.getFrequencyList(),
					readArticle.getFrequencyList());
		}
		return sum / readByActiveUser.size();
	}
	
	/**
	 * Sets the articles to use for predicting ratings.
	 * @param userId - id of the user to predict ratings for
	 */
	private void setActiveUser(Long userId) {
		readByActiveUser = readByUser.get(userId);
	}
	
	/**
	 * Extracts IDs from articles.
	 * @param articles - the articles to extract the IDs from
	 * @return The IDs of the articles
	 */
	private List<Long> getArticleIds(List<NewsArticle> articles) {
		List<Long> result = new ArrayList<Long>();
		for (NewsArticle article : articles) {
			result.add(article.getId());
		}
		return result;
	}

	@Override
	public int compare(NewsArticle article, NewsArticle otherArticle) {
		double artclPredRating = predictRating(article);
		double othArtclPredRating = predictRating(otherArticle);
		int diff;
		if (artclPredRating > othArtclPredRating) {
			diff = -1; // want descending order
		} else if (artclPredRating < othArtclPredRating) {
			diff = 1;
		} else {
			diff = 0;
		}
		return diff;
	}

}
