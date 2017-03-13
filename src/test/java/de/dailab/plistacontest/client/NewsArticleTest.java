package de.dailab.plistacontest.client;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class NewsArticleTest {
	
	@Test
	public void testComputeFrequencyList() {
		NewsArticle article =
				new NewsArticle(50L, 20L, 10L, "Yes We are all in this together", true);
		
		List<String> words = new ArrayList<String>(Arrays.asList("we", "are", "all",
															"in", "this", "together", "yes"));
		article.computeFrequencyList(words);
		assertEquals("[1, 1, 1, 1, 1, 1, 1]", article.getFrequencyList().toString());
		
		words.add("boga");
		article.computeFrequencyList(words);
		assertEquals("[1, 1, 1, 1, 1, 1, 1, 0]", article.getFrequencyList().toString());
	}
	
}
