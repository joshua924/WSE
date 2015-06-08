package edu.nyu.cs.sz1288;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

/**
 * This class is added after the delivery of this project
 * with the purpose to experimenting query processing methods
 * such as spelling correction suggestions.
 * @author shuangzhou
 *
 */
public class QuerySpellingCorrection {
	private String filePosition = "data/simple/smallWordList.txt";
	private ArrayList<String> queryWords = new ArrayList<String>();
	private ArrayList<Short> wordLength = new ArrayList<Short>();
	private ArrayList<Character> startingChar = new ArrayList<Character>();
	private ArrayList<String> sounds = new ArrayList<String>();
	
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
				if (!line.equals("") && !hs.contains(line)){
					hs.add(line);
					queryWords.add(line);
					sounds.add(SoundEx.soundExEncode(line));
					wordLength.add((short) line.length());
					startingChar.add(line.charAt(0));
				}
			}
		sc.close();
		long after = System.currentTimeMillis();
		System.out.println("Loading time " + (after - before) + " for word size " + queryWords.size());
		} catch (FileNotFoundException e) {
			System.out.println("File path not valid!");
		}
	}
	
	public int editDistance (String query, String candidate) {                          
	    int len0 = query.length() + 1;                                                     
	    int len1 = candidate.length() + 1;                                                     
	 
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	 
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) cost[i] = i;                                     
	 
	    // dynamically computing the array of distances                                  
	 
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j;                                                             
	 
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (query.charAt(i - 1) == candidate.charAt(j - 1)) ? 0 : 1;             
	 
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	 
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	 
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; cost = newcost; newcost = swap;                          
	    }                                                                               
	 
	    // the distance is the cost for transforming all letters in both strings        
	    return cost[len0 - 1];                                                          
	}
	
	public ArrayList<String> restrainedGetSuggestion(String query) {
		ArrayList<String> bestMatch1 = new ArrayList<String>();
		HashMap<String, Short> hm = new HashMap<String, Short>();
		short min = Short.MAX_VALUE;
		char s = query.charAt(0);
		short len = (short) query.length();
		for (int i=0; i<queryWords.size(); i++) {
			if (startingChar.get(i) == s){
				if (Math.abs(len - wordLength.get(i)) < min) {
					String w = queryWords.get(i);
					short temp = (short) editDistance(query, w);
					hm.put(queryWords.get(i), temp);
					if (temp < min) {
						min = temp;
					}
				}
			}
		}
		Iterator<String> it = hm.keySet().iterator();
		while(it.hasNext()){
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
		for (int i=0; i<queryWords.size(); i++) {
			if (startingChar.get(i) == s){
				if (Math.abs(len - wordLength.get(i)) < min) {
					String w = queryWords.get(i);
					short temp = (short) editDistance(query, w);
					hm.put(queryWords.get(i), temp);
					if (temp < min) {
						min = temp;
					}
				}
			}
		}
		it = hm.keySet().iterator();
		while(it.hasNext()){
			String key = (String) it.next();
			if (hm.get(key) == min)
				bestMatch2.add(key);
		}
		bestMatch2 = new ArrayList<String>(hm.keySet());
		bestMatch1.retainAll(bestMatch2);
		return bestMatch1;
	}
	
	private static String printList(ArrayList<String> res) {
		String ret = "";
		for (String s : res) {
			ret += s + ", ";
		}
		return ret;
	}
	
	public static void main(String[] args) {
		QuerySpellingCorrection qsc = new QuerySpellingCorrection();
		System.out.print(">");
		Scanner reader = new Scanner(System.in);
		String tmp = "";
		while (!(tmp = reader.nextLine()).equals("quit")){
			long before = System.currentTimeMillis();
			ArrayList<String> res = qsc.restrainedGetSuggestion(tmp);
			long after = System.currentTimeMillis();
			System.out.println("Simple Suggestion running time: " + (after - before));
			System.out.println("Simple Suggestion returns: " + printList(res));
			System.out.print(">");
		}
		reader.close();
	}
}
