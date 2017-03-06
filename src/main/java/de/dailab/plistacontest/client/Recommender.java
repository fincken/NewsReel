package de.dailab.plistacontest.client;

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
			
	private Map<Long, NewsArticle> newsArticleById;
	
	private Map<Long, Set<NewsArticle>> newsArticlesByPublisher;
	
	/**
	 * All the articles read by a user. Stored as a map that maps from the user ID to a set of
	 * NewsArticle objects representing the news articles.
	 */
	private Map<Long, Set<NewsArticle>> readByUser;
	
	private Map<Long, Set<Long>> userPreferences;
	
	private Map<Long, Set<NewsArticle>> recommendedToUser;
	
	private final static Logger logger = LoggerFactory.getLogger(Recommender.class);
	
	public Recommender() {
		newsArticles = new ArrayList<NewsArticle>();
		readByUser = new HashMap<Long, Set<NewsArticle>>();
		newsArticleById = new HashMap<Long, NewsArticle>();
		userPreferences = new HashMap<Long, Set<Long>>();
		newsArticlesByPublisher = new HashMap<Long, Set<NewsArticle>>();
	}
	
	/**
	 * Add a news article to the system.
	 * @param newsArticle - article that is to be added to the system
	 */
	public void addNewsArticle(NewsArticle newsArticle) {
		// get the ids of the article
		Long id = newsArticle.getId();
		Long publisherId = newsArticle.getPublisherId();
		
		// check if the article already exists in the system
		if (newsArticleById.containsKey(id)) {
			// the article is in the system, so remove previous reference
			logger.info("Updating existing article {}", id);
			newsArticles.remove(newsArticle);
		}
		// add the article to the system
		newsArticles.add(newsArticle);
		newsArticleById.put(id, newsArticle);
		
		// put it in the publisherId -> Set<Article> map for easy lookup later
		if (newsArticlesByPublisher.containsKey(publisherId)) {
			newsArticlesByPublisher.get(publisherId).add(newsArticle);
		} else {
			newsArticlesByPublisher.put(publisherId,
					new HashSet<NewsArticle>(Arrays.asList(newsArticle)));
		}
	}
	
	/**
	 * Register that a user clicks on a recommendation and reads an article.
	 * @param click - event with relevant information about the click
	 */
	public void userReadArticle(RecommenderItem click) {
		// get info about the click
		Long readArticleId = click.getListOfDisplayedRecs().get(0);
		Long userId = click.getUserID();
		
		// check if the user is known to the data provider (user id != 0)
		if (userId == 0) {
			return;
		}
		
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
		if (userPreferences.containsKey(userId)) {
			userPreferences.get(userId).add(click.getDomainID());
		} else {
			userPreferences.put(userId, new HashSet<Long>(Arrays.asList(click.getDomainID())));
		}
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	public List<Long> recommend(RecommenderItem request) {
		// get info about the incoming request
		Long requestPublisherId = request.getDomainID();
		Long requestUserId = request.getUserID();
		int limit = request.getNumberOfRequestedResults();
		
		// check if the system knows anything about the user's preferences
		// note that a user id of 0 indicates an unknown user
		boolean userInSystem = requestUserId != 0 && (userPreferences.containsKey(requestUserId)
				|| readByUser.containsKey(requestUserId));
		
		List<NewsArticle> recommendations;
		List<NewsArticle> matches = new ArrayList<NewsArticle>();
		
		if (userInSystem) {
			for (NewsArticle article : newsArticles) {
				if (userPreferences.get(requestUserId).contains(article.getPublisherId())) {
					matches.add(article);
				}
			}

			if (matches.size() > limit) {
				recommendations = getNRandomArticles(matches, limit);
			} else {
				recommendations = matches.isEmpty() ?
						getNRandomArticles(newsArticles, limit) : matches;
			}
		} else {
			// the system knows nothing about the user, the best it can do is 
			// to recommend random items

			if (newsArticles.size() > limit) {
				recommendations = getNRandomArticles(newsArticles, limit);
			} else {
				recommendations = newsArticles;
			}
		}
		
		return getArticleIds(recommendations);
	}
	
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
	
	private static List<Long> getArticleIds(List<NewsArticle> articles) {
		List<Long> result = new ArrayList<Long>();
		for (NewsArticle article : articles) {
			result.add(article.getId());
		}
		return result;
	}

}
