package edu.nyu.cs.sz1288;

public class SoundEx {
	public static String soundExEncode(String word) {
		if (word.length() == 1) return word.toUpperCase();
		else{
			String code = "";
			// keep the first letter in uppercase
			char ch = Character.toUpperCase(word.charAt(0));
			// convert characters to hyphen or numbers
			word = word.substring(1).replaceAll("[aeiouyhw]", "-").replaceAll("[bfpv]", "1").replaceAll("[cgjkqsxz]", "2").replaceAll("[dt]", 
					"3").replace("l", "4").replaceAll("[mn]", "5").replace("r", "6");
			// remove adjacent repeats of a number
			word = word.replaceAll("(.)(\\1)+", "$1").replace("-", "");
			// keep first three number or pad out
			if (word.length() > 3)
				code = word.substring(0, 3);
			else
				code = String.format("%-3s", word).replace(' ', '0');
			return String.valueOf(ch) + code;
		}
	}
}
