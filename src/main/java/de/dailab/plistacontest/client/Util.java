package de.dailab.plistacontest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Util {
	
	public static <T> List<T> getNRandomElements(List<T> list, int n) {
		if (n >= list.size()) {
			return list;
		}
		List<T> result = new ArrayList<T>();
		Random rand = new Random();
		while (result.size() < n) {
			int idx = rand.nextInt(list.size());
			while (result.contains(list.get(idx))) {
				idx = rand.nextInt(list.size());
			}
			result.add(list.get(idx));
		}
		return result;
	}
	
	public static double cosineSimilarity(List<Double> list, List<Double> list2) {
		if (list.size() != list2.size()) {
			return Double.NaN;
		}
		
        double innerProduct = 0.0, thisPower2 = 0.0, thatPower2 = 0.0;
        for (int i = 0; i < list.size(); i++) {
            innerProduct += list.get(i).doubleValue() * list2.get(i).doubleValue();
            thisPower2 += list.get(i).doubleValue() * list.get(i).doubleValue();
            thatPower2 += list2.get(i).doubleValue() * list2.get(i).doubleValue();
        }
        return innerProduct / Math.sqrt(thisPower2 * thatPower2);
	}
	
	public static Collection<String> loadData(InputStream inputStream) {
		Collection<String> result = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
		} catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
			br = new BufferedReader(new InputStreamReader(inputStream));
		}
		
		String line; // line read from stream
		try {
			// read lines from stream
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					result.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// close the reader
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
}
