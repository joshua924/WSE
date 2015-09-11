package edu.nyu.cs.cs2580;

import java.util.*;
import java.io.*;

class SuffixTrie extends Trie implements Serializable {

	SuffixTrie() {
		super();
	}

	@Override
	public void insert(String s) {
		s = s.toLowerCase();
		if (!isValidWord(s))
			return;
		for (int suf = 0; suf < s.length() - 1; ++suf) {
			// Threr might be conflictions at the leave.
			insertIntoTrie(s, suf);
		}
	}
}