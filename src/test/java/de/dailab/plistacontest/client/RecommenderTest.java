package de.dailab.plistacontest.client;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class RecommenderTest {
	
	Recommender recommender;
	
	@Before
	public void initialize() {
		recommender = new Recommender();
	}
	
	@Test
	public void testAddNewsArticle_noPreviousRecord() {
		NewsArticle article1 = new NewsArticle(10L, 5L, 1L, "Frankie Goes to Hollywood "
				+ "Frank Underwood travelled to Hollywood today.", true);
		recommender.addNewsArticle(article1);
		
		assertTrue(recommender.getNewsArticles().contains(article1));
		String text1 = "franki goes to hollywood frank underwood travelled to today";
		for (String word : text1.split(" ")) {
			assertTrue(recommender.getWords().contains(word));
		}
		
		NewsArticle article2 = new NewsArticle(1L, 5L, 10L, "But black dog Hollywood "
				+ "is cool frank", true);
		recommender.addNewsArticle(article2);
		
		assertTrue(recommender.getNewsArticles().contains(article2));
				
		String text2 = "but black dog is cool";
		for (String word : text2.split(" ")) {
			assertTrue(recommender.getWords().contains(word));
		}
	}
	
	@Test
	public void testRecommend_withPreviousRecord() {
		// create test articles
		NewsArticle article1 = new NewsArticle(1L, 1L, 1L,
				"messi ronaldo madrid barcelona", true);
		NewsArticle article2 = new NewsArticle(2L, 1L, 2L,
				"dylan barcelona guitar audience", true);
		NewsArticle article3 = new NewsArticle(3L, 2L, 1L,
				"messi ronaldo madrid spain", true);
		NewsArticle article4 = new NewsArticle(4L, 2L, 3L,
				"trump usa election obama", true);
		
		// add articles to system
		recommender.addNewsArticle(article1);
		recommender.addNewsArticle(article2);
		recommender.addNewsArticle(article3);
		recommender.addNewsArticle(article4);
		
		// simulate that user 1 read article 1
		recommender.userReadArticle(1L, 1L);
		
		// get recommendations for user 1		
		assertEquals("[3, 2, 4]", recommender.recommend(1L, 3).toString());
		
		// request more articles than available
		assertEquals("[3, 2, 4]", recommender.recommend(1L, 10).toString());
	}

}
