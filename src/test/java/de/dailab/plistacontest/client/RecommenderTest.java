package de.dailab.plistacontest.client;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RecommenderTest {
	
	Recommender recommender;
	
	@Before
	public void initialize() {
		recommender = new Recommender();
	}
	
	@Test
	public void testAddNewsArticle() {
		NewsArticle article1 = new NewsArticle(10L, 5L, 1L, "Frankie Goes to Hollywood "
				+ "Frank Underwood travelled to Hollywood today.", true);
		recommender.addNewsArticle(article1);
		assertTrue(recommender.getNewsArticles().contains(article1));
		String text1 = "frankie goes to hollywood frank underwood travelled to today";
		for (String word : text1.split(" ")) {
			assertTrue(recommender.getWords().contains(word));
		}
		
		NewsArticle article2 = new NewsArticle(1L, 5L, 10L, "But black dog Hollywood "
				+ "is cool", true);
		recommender.addNewsArticle(article2);
		assertTrue(recommender.getNewsArticles().contains(article2));
		assertTrue(article1.getFrequencyList().size() == 13);
		String text2 = "but black dog is cool";
		for (String word : text2.split(" ")) {
			assertTrue(recommender.getWords().contains(word));
		}
	}

}
