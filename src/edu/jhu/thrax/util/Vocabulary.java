package edu.jhu.thrax.util;

import java.util.HashMap;

/**
 * This class represents all words and nonterminals that are used in input
 * or output of Thrax as integers. It provides facilities for looking up the
 * word ID of a string, or looking up the String representation of an integer.
 */
public class Vocabulary {

	/**
	 * This map maps words to unique integers.
	 */
	private static HashMap<String,Integer> ids = new HashMap<String,Integer>();
	/**
	 * This map maps integers to unique Strings.
	 */
	private static HashMap<Integer,String> words = new HashMap<Integer,String>();

	/**
	 * The number of words in the vocabulary.
	 */
	private static int size;

	/**
	 * Looks up the unique integer associated with this String. If there
	 * is no integer yet associated, the maps are updated with a new
	 * String-integer pair.
	 *
	 * @param w the word to look up
	 * @return the unique integer associated with that word
	 */
	public static int getWordID(String w)
	{
		if (ids.containsKey(w)) {
			return ids.get(w);
		}
		else {
			ids.put(w, size);
			words.put(size, w);
			size++;
			return (size - 1);
		}
	}

	/**
	 * Looks up the unique integer associated with each String in an array,
	 * and returns those integers in an array. The first integer corresponds
	 * to the first string, the second to the second, and so on.
	 *
	 * @param ws an array holding the Strings to look up
	 * @return an array of integers corresponding to those Strings
	 */
	public static int [] getWordIDs(String [] ws)
	{
		int [] ret = new int[ws.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = getWordID(ws[i]);
		}
		return ret;
	}

}
