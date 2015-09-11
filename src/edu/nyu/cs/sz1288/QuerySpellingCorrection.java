package edu.nyu.cs.sz1288;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * This class is added after the delivery of this project with the purpose to
 * experimenting query processing methods such as spelling correction
 * suggestions.
 * 
 * @author shuang zhou
 *
 */
public class QuerySpellingCorrection {
	private String filePosition = "data/words/single_word.txt";
	private List<String> queryWords = new ArrayList<String>();
	private List<Short> wordLength = new ArrayList<Short>();
	private List<Character> startingChar = new ArrayList<Character>();
	private List<String> sounds = new ArrayList<String>();

	public QuerySpellingCorrection() {
		System.out.println("Loading word file ...");
		long before = System.currentTimeMillis();
		HashSet<String> hs = new HashSet<String>();
		// read in query log or dictionary
		File f = new File(filePosition);
		try {
			Scanner sc = new Scanner(f);
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (!line.equals("") && !hs.contains(line)) {
					hs.add(line);
					queryWords.add(line);
					sounds.add(SoundEx.soundExEncode(line));
					wordLength.add((short) line.length());
					startingChar.add(line.charAt(0));
				}
			}
			sc.close();
			long after = System.currentTimeMillis();
			System.out.println("Loading time " + (after - before)
					+ " for word size " + queryWords.size());
		} catch (FileNotFoundException e) {
			System.out.println("File path not valid!");
		}
	}

	public ArrayList<String> restrainedGetSuggestion(String query) {
		ArrayList<String> bestMatch1 = new ArrayList<String>();
		HashMap<String, Short> hm = new HashMap<String, Short>();
		short min = Short.MAX_VALUE;
		char s = query.charAt(0);
		short len = (short) query.length();
		for (int i = 0; i < queryWords.size(); i++) {
			if (startingChar.get(i) == s) {
				if (Math.abs(len - wordLength.get(i)) < min) {
					String w = queryWords.get(i);
					short temp = (short) EditDistance.editDistance(query, w);
					hm.put(queryWords.get(i), temp);
					if (temp < min) {
						min = temp;
					}
				}
			}
		}
		Iterator<String> it = hm.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (hm.get(key) == min)
				bestMatch1.add(key);
		}
		// use sound code
		ArrayList<String> bestMatch2 = new ArrayList<String>();
		hm = new HashMap<String, Short>();
		min = Short.MAX_VALUE;
		s = query.charAt(0);
		len = (short) query.length();
		for (int i = 0; i < sounds.size(); i++) {
			if (startingChar.get(i) == s) {
				if (Math.abs(len - wordLength.get(i)) < min) {
					String w = sounds.get(i);
					short temp = (short) EditDistance.editDistance(
							SoundEx.soundExEncode(query), w);
					hm.put(queryWords.get(i), temp);
					if (temp < min) {
						min = temp;
					}
				}
			}
		}
		it = hm.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (hm.get(key) == min) {
				bestMatch2.add(key);
			}
		}
		bestMatch2.removeAll(bestMatch1);
		bestMatch1.addAll(bestMatch2);
		return bestMatch1;
	}

	public static void main(String[] args) {
		QuerySpellingCorrection qsc = new QuerySpellingCorrection();
		System.out.print(">");
		Scanner reader = new Scanner(System.in);
		String tmp = "";
		while (!(tmp = reader.nextLine()).equals("quit")) {
			long before = System.currentTimeMillis();
			ArrayList<String> res = qsc.restrainedGetSuggestion(tmp);
			long after = System.currentTimeMillis();
			System.out.println("Simple Suggestion running time: "
					+ (after - before));
			if (res == null) {
				System.out.println("Do you mean " + String.join(", ", res)
						+ "?");
			} else {
				System.out.println("Find exact match, proceed ..");
			}
			System.out.print(">");
		}
		reader.close();
	}
}
