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
	private String text;
	
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
		this.text = text;
	
		updateKeywords();
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
	
	public String getText() {
		return text;
	}
	
	public void setPublisherId(Long publisherId) {
		this.publisherId = publisherId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public void setRecommendable(boolean recommendable) {
		this.recommendable = recommendable;
	}

	public void setText(String text) {
		this.text = text;
		updateKeywords();
	}
	
	private void updateKeywords() {
		keywords = languageProcessor.getKeywordMap(text);
		
		// also include publisher and category ids as words
		if (publisherId != 0) {
			keywords.put("<publisherId>" + publisherId, 1);
		}
		if (categoryId != 0) {
			keywords.put("<categoryId>" + categoryId, 1);
		}
	}

	@Override
	public String toString() {
		return "[NewsArticle id=" + id + ", text=" + text + "]";
	}

}
