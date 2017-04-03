package de.dailab.plistacontest.client;

import java.util.LinkedHashMap;
import java.util.Map;

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
	
	private static final LanguageProcessor languageProcessor = LanguageProcessor.INSTANCE;
		
	public NewsArticle(Long id, Long publisherId, Long categoryId, String text,
															boolean recommendable) {
		this.id = id;
		this.publisherId = publisherId;
		this.categoryId = categoryId;
		this.recommendable = recommendable;
	
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
	
	public Map<String, Integer> getKeywords() {
		return new LinkedHashMap<String, Integer>(keywords);
	}
	
	@Override
	public String toString() {
		return "[NewsArticle id=" + id + "]";
	}

}
