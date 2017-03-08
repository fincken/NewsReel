package de.dailab.plistacontest.client;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class LanguageProcessorTest {
		
	@Test
	public void testloadStopWordsFromFile() {
		LanguageProcessor.loadStopWordsFromFile(
						getClass().getResourceAsStream("/dummy_stop_words.txt"));
		Set<String> stopWords = LanguageProcessor.getStopWords();
		assertEquals("Should have read three stop words", 3, stopWords.size());
		String[] words = {"hei", "ok", "hade"};
		for (String word : words) {
			assertTrue("Should have read hei, ok and hade", stopWords.contains(word));
		}
	}
	
	@Test
	public void testGetWords() {
		LanguageProcessor.loadStopWordsFromFile(
				getClass().getResourceAsStream("/dummy_stop_words.txt"));
		
		String testText = "Bendik is an hei ent.";
		String expectedOutput = "[bendik, is, an, ent]";
		assertEquals("getWords() should remove stop words, clean and tokenize", expectedOutput,
				LanguageProcessor.getWords(testText).toString());
	}
	
	@Test
	public void testGetKeywordMap() {
		String testText = "Bendik is an ent.";
		Map<String, Integer> keywords = LanguageProcessor.getKeywordMap(testText);
		assertFalse("Keywords should not be empty", keywords.isEmpty());
		assertEquals("Count of ent should be 1", 1, (int) keywords.get("ent"));
	}
	
	@Test
	public void testGetClean() {
		
	}

}
