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

/**
 * Utility class for basic language processing. Assumes that the recommender wants all
 * lower case words, and that the stop words file is encoded with UTF-8.
 * @author Tobias
 *
 */
public class LanguageProcessor {
	
	private static Set<String> stopWords = new HashSet<String>();
	
	private final static Logger logger = LoggerFactory.getLogger(LanguageProcessor.class);
	
	/**
	 * Get the clean words from a text.
	 * @param text - the text to clean
	 * @return
	 */
	public static List<String> getWords(String text) {
		List<String> words = new ArrayList<String>();
		String[] tokens = text.split(" ");
		for (String token : tokens) {
			String clean = getClean(token);
			if (clean.length() > 1 &&
					(stopWords.isEmpty() || !stopWords.contains(clean.toLowerCase()))) {
				words.add(clean.toLowerCase());
			}
		}
		return words;
	}
	
	public static void loadStopWordsFromFile(InputStream inputStream) {
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
		logger.info("Stop words are {}", stopWords);
	}
	
	public static Set<String> getStopWords() {
		return stopWords;
	}

	/**
	 * 
	 * @param word
	 * @return
	 */
	public static String getClean(String word) {
		// TODO: look at getting rid of "s
		return word.replaceAll("[\u00ad\\p{Punct}]", "").trim();
	}
	
	public static Map<String, Integer> getKeywordMap(String text) {
		Map<String, Integer> keywords = new HashMap<String, Integer>();
		for (String word : getWords(text)) {
			keywords.put(word, keywords.containsKey(word) ?
					keywords.get(word) + 1 : 1);
		}
		return keywords;
	}
	
}
