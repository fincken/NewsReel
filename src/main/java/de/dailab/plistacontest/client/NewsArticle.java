package de.dailab.plistacontest.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewsArticle {
	
	private Long id;
	private Long publisherId;
	
	/**
	 * Category id. Note that a category id of 0 most likely represents an unknown category.
	 */
	private Long categoryId;
	private boolean recommendable;
	
	/**
	 * The counts of the keywords of this article's description concatenated with
	 * it's title.
	 */
	private Map<String, Integer> keywords;
	
	/**
	 * A list of word frequencies of this article, according to the global list of words.
	 */
	private List<Integer> frequencyList;
	
	private static final LanguageProcessor languageProcessor = LanguageProcessor.INSTANCE;
		
	public NewsArticle(Long id, Long publisherId, Long categoryId, String text,
															boolean recommendable) {
		this.id = id;
		this.publisherId = publisherId;
		this.categoryId = categoryId;
		this.recommendable = recommendable;
	
		frequencyList = new ArrayList<Integer>();
		keywords = languageProcessor.getKeywordMap(text);
		
		// also include publisher and category ids as words
		if (publisherId != 0) {
			keywords.put("<publisherId>" + publisherId, 1);
		}
		if (categoryId != 0) {
			keywords.put("<categoryId>" + categoryId, 1);
		}
	}

	public Long getId() {
		return id;
	}
	
	public Long getPublisherId() {
		return publisherId;
	}
	
	public Long getCategoryId() {
		return categoryId;
	}

	public boolean isRecommendable() {
		return recommendable;
	}
	
	public Set<String> getKeywords() {
		return new HashSet<String>(keywords.keySet());
	}
	
	/**
	 * Computes the frequency list of this article in accordance with some list of words.
	 * @param words - list of words from which to compute the frequency list
	 */
	public void computeFrequencyList(Collection<String> words) {
		frequencyList.clear();
		for (String word : words) {
			frequencyList.add(keywords.containsKey(word) ? keywords.get(word) : 0);
		}
	}
	
	/**
	 * Pads the frequency list with zeroes.
	 * @param n - the number of zeroes to pad with
	 */
	public void padFrequencyList(int n) {
		for (int i = 0; i < n; i++) {
			frequencyList.add(0);
		}
	}
	
	public List<Integer> getFrequencyList() {
		return new ArrayList<Integer>(frequencyList);
	}
	
}
