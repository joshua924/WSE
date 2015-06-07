package edu.nyu.cs.sz1288;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * This class is added after the delivery of this project
 * with the purpose to experimenting query processing methods
 * such as spelling correction suggestions.
 * @author shuangzhou
 *
 */
public class QuerySpellingCorrection {
	private String filePosition = "queries.tsv";
	private ArrayList<String> queryWords = new ArrayList<String>();
	private ArrayList<Short> wordLength = new ArrayList<Short>();
	private ArrayList<Character> startingChar = new ArrayList<Character>();;
	
	public QuerySpellingCorrection() {
		// read in query log or dictionary
		File f = new File(filePosition);
		try {
			Scanner sc = new Scanner(f);
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (!line.equals("")){
					queryWords.add(line);
					wordLength.add((short) line.length());
					startingChar.add(line.charAt(0));
				}
			}
			sc.close();
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
	
	public String simpleGetSuggestions(String query) {
		String bestMatch = "";
		short min = Short.MAX_VALUE;
		for (String w : queryWords) {
			short temp = (short) editDistance(query, w);
			if (temp <= min) {
				min = temp;
				bestMatch = w;
			}
		}
		return bestMatch;
	}
	
	public static void main(String[] args) {
		QuerySpellingCorrection qsc = new QuerySpellingCorrection();
		long before = System.currentTimeMillis();
		String res = qsc.simpleGetSuggestions("salal");
		long after = System.currentTimeMillis();
		System.out.println("Simple Suggestion running time: " + (after - before));
		System.out.println("Simple Suggestion returns: " + res);
	}

}
