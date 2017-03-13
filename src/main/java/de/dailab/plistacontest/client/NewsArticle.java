package de.dailab.plistacontest.client;

import java.util.ArrayList;
import java.util.Collection;
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
		
		this.frequencyList = new ArrayList<Integer>();
		this.keywords = languageProcessor.getKeywordMap(text);
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
		return keywords.keySet();
	}
	
	public void computeFrequencyList(Collection<String> words) {
		List<Integer> frequencies = new ArrayList<Integer>();
		for (String word : words) {
			frequencies.add(keywords.containsKey(word) ? keywords.get(word) : 0);
		}
		frequencyList = frequencies;
	}
	
	/**
	 * Pad the frequency list with zeroes.
	 * @param n - the number of zeroes to pad with
	 */
	public void padFrequencyList(int n) {
		for (int i = 0; i < n; i++) {
			frequencyList.add(0);
		}
	}
	
	public List<Integer> getFrequencyList() {
		return frequencyList;
	}
	
}
