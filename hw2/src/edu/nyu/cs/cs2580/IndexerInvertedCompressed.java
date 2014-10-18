package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import javafx.util.Pair;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedCompressed extends Indexer {
  private int SKIPSIZE = 10;
  private HashMap<String, Integer> _dictionary = new HashMap<String, Integer>();
  private Vector<String> _terms = new Vector<String>();
  private Vector<Vector<Vector<Pair<Integer, Integer>>>> uncompressedList =
      new Vector<Vector<Vector<Pair<Integer, Integer>>>>();
  private Vector<Vector<Byte>> _postingLists = new Vector<Vector<Byte>>();
  private Vector<Vector<Pair<Integer, Integer>>> _skipLists =
      new Vector<Vector<Pair<Integer, Integer>>>();

  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @Override
  public void constructIndex() throws IOException {
  }

  @Override
  public void loadIndex() throws IOException, ClassNotFoundException {
  }

  @Override
  public Document getDoc(int docid) {
    return null;
  }

  /**
   * In HW2, you should be using {@link DocumentIndexed}
   */
  @Override
  public Document nextDoc(Query query, int docid) {
    // Check if all the query words exist
    Vector<Integer> nextList = new Vector<Integer>();
    Vector<Integer> freqList = new Vector<Integer>();
    Vector<String> phrases = new Vector<String>();
    for (String token : query._tokens) {
      String[] words = token.split(" ");
      for (String word : words) {
        if (words.length > 1) phrases.add(token);
        Pair<Integer, Vector<Integer>> idPosList = nextPosList(word, docid);
        if (idPosList == null) return null;
        nextList.add(idPosList.getKey());
        freqList.add(idPosList.getValue().size());
      }
    }
    int maxId = Collections.max(nextList);
    if (maxId == Collections.min(nextList)) {
      // check if the document contains the phrases
      for (String phrase : phrases) {
        if (!phraseSearch(phrase, maxId)) return nextDoc(query, maxId - 1);
      }
      // query is satisfied!!!
      DocumentIndexed doc = (DocumentIndexed) getDoc(maxId);
      doc.setTermFrequencyList(freqList);
      return doc;
    }
    return nextDoc(query, maxId - 1);
  }

  private boolean phraseSearch(String phrase, int docid) {
    Vector<Vector<Integer>> phraseList = new Vector<Vector<Integer>>();
    for (String word : phrase.split(" ")) {
      phraseList.add(nextPosList(word, docid - 1).getValue());
    }
    int prevPos = -1;
    int pos = -1;
    while (pos < Integer.MAX_VALUE) {
      boolean result = true;
      for (int i = 0; i < phraseList.size(); i++) {
        if (i == 0) prevPos = nextPos(phraseList.get(i), prevPos);
        else {
          pos = nextPos(phraseList.get(i), prevPos);
          if (pos != prevPos - 1) {
            prevPos = pos - i;
            result = false;
            break;
          }
        }
      }
      if (result) return true;
    }
    return false;
  }

  private Pair<Integer, Vector<Integer>> nextPosList(String word, int docid) {
    if (!_dictionary.containsKey(word)) return null;
    Vector<Pair<Integer, Integer>> docList = _skipLists.get(_dictionary.get(word));
    if (docList.get(docList.size() - 1).getKey() <= docid) return null;
    if (docList.get(0).getKey() > docid) {
      return decodePosList(_postingLists.get(_dictionary.get(word)), 0, docid);
    }
    // Returns the index of the skip pointer.
    int startPos = binarySearch(docList, 0, docList.get(docList.size() - 1).getKey(), docid);
    return decodePosList(_postingLists.get(_dictionary.get(word)), startPos, docid);
  }

  private int binarySearch(Vector<Pair<Integer, Integer>> docList, int low, int high, int docid) {
    while (high - low > 1) {
      int mid = (high - low) / 2;
      if (docList.get(mid).getKey() < docid) { low = mid; }
      else { high = mid; }
    }
    return low;
  }

  private int nextPos(Vector<Integer> posList, int pos) {
    if (posList.get(posList.size() - 1) <= pos) return Integer.MAX_VALUE;
    if (posList.get(0) > pos) return Integer.MAX_VALUE;
    return posList.get(binarySearchPos(posList, 0, posList.get(0), pos));
  }

  private int binarySearchPos(Vector<Integer> posList, int low, int high, int pos) {
    while (high - low > 1) {
      int mid = (high - low) / 2;
      if (posList.get(mid) < pos) { low = mid; }
      else { high = mid; }
    }
    return high;
  }

  @Override
  public int corpusDocFrequencyByTerm(String term) {
    return 0;
  }

  @Override
  public int corpusTermFrequency(String term) {
    return 0;
  }

  /**
   * @CS2580: Implement this for bonus points.
   */
  @Override
  public int documentTermFrequency(String term, String url) {
    return 0;
  }

  private Vector<Byte> encodePosList(Vector<Vector<Pair<Integer, Integer>>> docList,
                                     Vector<Pair<Integer, Integer>> skipPointer) {
    Vector<Integer> output = new Vector<Integer>();
    int pointer = 0;
    int prevId = 0;
    int counter = 0;
    for (int i = 0; i < docList.size(); i++) {
      Vector<Pair<Integer, Integer>> posList = docList.get(i);
      int currentId = posList.get(0).getKey();
      output.add(currentId - prevId);
      prevId = currentId;
      output.add(posList.size());
      if (counter % SKIPSIZE == 0) {
        skipPointer.add(new Pair<Integer, Integer>(currentId, pointer));
      }
      int prevPos = 0;
      for (Pair<Integer, Integer> pos : posList) {
        output.add(pos.getValue() - prevPos);
        prevPos = pos.getValue();
      }
      pointer += 2 + posList.size();
      counter++;
    }
    return encode(output);
  }

  private Vector<Byte> encode(Vector<Integer> input) {
    Vector<Byte> output = new Vector<Byte>();
    for (int i : input) {
      while (i >= 128) {
        output.add((byte) (i & 0x7F));
        i >>>= 7;
      }
      output.add((byte) (i | 0x80));
    }
    return output;
  }

  private int[] decode(Vector<Byte> input, int startPos) {
    int i = startPos;
    int result = ((int) input.get(i) & 0x7F);
    while (((int) input.get(i) & 0x80) == 0) {
      i += 1;
      int unsignedByte = (int) input.get(i) & 0x7F;
      result |= (unsignedByte << (7 * (i - startPos)));
    }
    int[] posResult = {i + 1, result};
    return posResult;
  }

  private Pair<Integer, Vector<Integer>> decodePosList(Vector<Byte> input, int startPos, int docid) {
    Vector<Integer> posList = new Vector<Integer>();
    int pos = startPos;
    while (decode(input, pos)[1] <= docid) {
      int[] posId = decode(input, pos);
      int[] posSize = decode(input, posId[0]);
      pos = posSize[0];
      for (int i = 0; i < posSize[1]; i++) {
        int[] posPos = decode(input, pos);
        pos = posPos[0];
      }
    }
    int[] posId = decode(input, pos);
    int[] posSize = decode(input, posId[0]);
    int[] prevPos = {posSize[0], 0};
    for (int i = 0; i < posSize[1]; i++) {
      int[] posPos = decode(input, prevPos[0]);
      prevPos[0] = posPos[0];
      prevPos[1] = posPos[1] + prevPos[1];
      posList.add(prevPos[1]);
    }
    return new Pair<Integer, Vector<Integer>>(posId[1], posList);
  }
}
