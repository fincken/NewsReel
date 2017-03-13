package de.dailab.plistacontest.client;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class LanguageProcessorTest {
	
	private static final LanguageProcessor languageProcessor = LanguageProcessor.INSTANCE;
	
	@Test
	public void testloadStopWordsFromFile() {
		languageProcessor.loadStopWordsFromFile(
						getClass().getResourceAsStream("/dummy_stop_words.txt"));
		Set<String> stopWords = languageProcessor.getStopWords();
		assertEquals("Should have read three stop words", 3, stopWords.size());
		String[] words = {"hei", "ok", "hade"};
		for (String word : words) {
			assertTrue("Should have read hei, ok and hade", stopWords.contains(word));
		}
		languageProcessor.loadStopWordsFromFile(null);
		assertTrue("Loading from a null inputstream should clear any loaded stop words",
								languageProcessor.getStopWords().isEmpty());
	}
	
	@Test
	public void testGetWords_noStopWords() {
		String testText = "Bendik is an hei ent.";
		String expectedOutput = "[bendik, is, an, hei, ent]";
		assertEquals("getWords() without loaded stop words should clean and tokenize",
				expectedOutput, languageProcessor.getWords(testText).toString());
	}
	
	@Test
	public void testGetWords_withStopWords() {
		languageProcessor.loadStopWordsFromFile(
				getClass().getResourceAsStream("/dummy_stop_words.txt"));
		String testText = "Bendik is an hei ent.";
		String expectedOutput = "[bendik, is, an, ent]";
		assertEquals("getWords() with loaded stop words should clean, remove stop words "
				+ "and tokenize", expectedOutput, languageProcessor.getWords(testText).toString());
		languageProcessor.loadStopWordsFromFile(null);
	}
	
	@Test
	public void testGetKeywordMap_noStopWords() {
		String testText = "Bendik is an ent.";
		Map<String, Integer> keywords = languageProcessor.getKeywordMap(testText);
		assertFalse("Keywords should not be empty", keywords.isEmpty());
		assertEquals("Count of ent should be 1", 1, (int) keywords.get("ent"));
		for (String word : testText.split(" ")) {
			String clean = languageProcessor.getClean(word);
			assertTrue("Keywords should contain the words \"bendik\", \"is\", \"an\", and \"ent\"",
												keywords.containsKey(clean.toLowerCase()));
		}
	}
	
	@Test
	public void testGetClean() {
		BufferedReader br = null;
		InputStream inputStream = getClass().getResourceAsStream("/dirty_words.txt");
		if (inputStream == null) {
			return;
		}
		try {
			br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
		} catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
		}
		String line;
		try {
			while ((line = br.readLine()) != null) {
				String dirtyWord = line;
				String cleaned = languageProcessor.getClean(dirtyWord);
			}
			if (br != null) {
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
