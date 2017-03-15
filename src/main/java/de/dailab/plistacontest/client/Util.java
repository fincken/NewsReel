package de.dailab.plistacontest.client;

import java.util.List;

public class Util {

	public static double cosineSimilarity(List<Integer> frequencyList, List<Integer> frequencyList2) {
		if (frequencyList.size() != frequencyList2.size()) {
			return Double.NaN;
		}
		
        double innerProduct = 0.0, thisPower2 = 0.0, thatPower2 = 0.0;
        for (int i = 0; i < frequencyList.size(); i++) {
            innerProduct += frequencyList.get(i).doubleValue() * frequencyList2.get(i).doubleValue();
            thisPower2 += frequencyList.get(i).doubleValue() * frequencyList.get(i).doubleValue();
            thatPower2 += frequencyList2.get(i).doubleValue() * frequencyList2.get(i).doubleValue();
        }
        return innerProduct / Math.sqrt(thisPower2 * thatPower2);
	}
	
}
