package de.dailab.plistacontest.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Utility class for basic language processing. Assumes that the recommender wants all
 * lower case words, and that the stop words file is encoded with UTF-8.
 * @author Tobias
 *
 */
public enum LanguageProcessor {
	
	INSTANCE;
	
	private static Set<String> stopWords = new HashSet<String>();
	
	private static final Logger logger = LoggerFactory.getLogger(LanguageProcessor.class);
	
	SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.GERMAN);
	
	/**
	 * Returns a list of clean, stemmed words from a text. Starts with performing tokenization
	 * (assumes text with words separated by single spaces). Each token (word) is cleaned
	 * (according to the getClean() method), converted to lower case, and checked against a set of
	 * stop words to ignore. If the word is not a stop word, it is stemmed before it is added to
	 * the final list.
	 * @param text - text to clean
	 * @return a list of clean, stemmed words
	 */
	public List<String> getWords(String text) {
		List<String> words = new ArrayList<String>();
		String[] tokens = text.split(" ");
		for (String token : tokens) {
			String clean = getClean(token);
			if (clean.length() > 1) {
				String allLowerCase = clean.toLowerCase();
				if (stopWords.isEmpty() || !stopWords.contains(allLowerCase)) {
					String stemmed = stemmer.stem(allLowerCase).toString();
					if (stopWords.isEmpty() || !stopWords.contains(stemmed)) {
						words.add(stemmed);
					}
				}
			}
		}
		return words;
	}
	
	public void loadStopWordsFromFile(InputStream inputStream) {
		stopWords.clear();
		if (inputStream != null) {
			stopWords.addAll(Util.loadData(inputStream));
		}
	}
	
	public Set<String> getStopWords() {
		return new HashSet<String>(stopWords);
	}

	/**
	 * 
	 * @param word
	 * @return
	 */
	public String getClean(String word) {
		return word.replaceAll("[^\\p{L}]", "").trim();
	}
	
	public Map<String, Integer> getKeywordMap(String text) {
		Map<String, Integer> keywords = new LinkedHashMap<String, Integer>();
		for (String word : getWords(text)) {
			keywords.put(word, keywords.containsKey(word) ?
					keywords.get(word) + 1 : 1);
		}
		return keywords;
	}
	
}
