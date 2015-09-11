package edu.nyu.cs.cs2580;

import java.util.*;
import java.util.Map.Entry;

public class QueryExpander {
	public static String expandQuery(Vector<ScoredDocument> docs, int numTerms,
			IndexerInvertedCompressed indexer) {
		// dictionary contains all words and their count
		HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
		// get top docs
		for (int i = 0; i < docs.size(); i++) {
			int docid = docs.get(i).getDoc()._docid;
			int[] body = indexer._docBody[docid];
			for (int j = 0; j < body.length; j++) {
				// get the term by looking up _terms
				String term = indexer._terms.get(body[j]);
				j++;
				// get the number of appearances
				int count = body[j];
				// if exists, add the count, else create an entry
				if (dictionary.containsKey(term)) {
					dictionary.put(term, dictionary.get(term) + count);
				} else
					dictionary.put(term, count);
			}
		}
		// get top terms
		dictionary = sortByValues(dictionary);
		Set<Map.Entry<String, Integer>> set = dictionary.entrySet();
		Iterator<Map.Entry<String, Integer>> iterator = set.iterator();
		int totalFrequency = 0;
		for (int i = 0; i < numTerms; i++) {
			Map.Entry<String, Integer> me = iterator.next();
			totalFrequency += me.getValue();
		}

		// write results into a string to return to user
		String result = "";
		iterator = set.iterator();
		for (int i = 0; i < numTerms; i++) {
			Map.Entry<String, Integer> me = iterator.next();
			result += (me.getKey() + "\t" + (double) me.getValue()
					/ totalFrequency + "\n");
		}
		return result;
	}

	private static HashMap<String, Integer> sortByValues(HashMap<String, Integer> map) {
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(map.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o1.getValue() - o2.getValue();
			}
		});
		HashMap<String, Integer> sortedHashMap = new HashMap<String, Integer>();
		for (Iterator<Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Entry<String, Integer> entry = it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}
}
