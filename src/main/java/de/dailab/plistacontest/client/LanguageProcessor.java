package de.dailab.plistacontest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	 * Get the clean words from a text.
	 * @param text - the text to clean
	 * @return
	 */
	public List<String> getWords(String text) {
		List<String> words = new ArrayList<String>();
		String[] tokens = text.split(" ");
		for (String token : tokens) {
			String clean = getClean(token);
			if (clean.length() > 1 &&
					(stopWords.isEmpty() || !stopWords.contains(clean.toLowerCase()))) {
				words.add(stemmer.stem(clean.toLowerCase()).toString());
			}
		}
		return words;
	}
	
	public void loadStopWordsFromFile(InputStream inputStream) {
		stopWords.clear();
		if (inputStream == null) {
			return;
		}
		
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
		} catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
			br = new BufferedReader(new InputStreamReader(inputStream));
		}
		
		String line; // lines read from file go here
		try {
			while ((line = br.readLine()) != null) {
				stopWords.add(line);
			}
			if (br != null) {
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
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
		Map<String, Integer> keywords = new HashMap<String, Integer>();
		for (String word : getWords(text)) {
			keywords.put(word, keywords.containsKey(word) ?
					keywords.get(word) + 1 : 1);
		}
		return keywords;
	}
	
}
