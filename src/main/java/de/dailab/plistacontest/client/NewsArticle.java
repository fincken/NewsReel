package de.dailab.plistacontest.client;

public class NewsArticle {
	
	private Long id;
	private Long publisherId;
	private String title;
	private String description;
	private boolean recommendable;
	
	public NewsArticle(Long id, Long publisherId, String title, String description, boolean recommendable) {
		this.id = id;
		this.publisherId = publisherId;
		this.title = title;
		this.description = description;
		this.recommendable = recommendable;
	}

	public Long getId() {
		return id;
	}
	
	public Long getPublisherId() {
		return publisherId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRecommendable() {
		return recommendable;
	}
	
}
