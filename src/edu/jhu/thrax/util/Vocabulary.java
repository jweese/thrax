package edu.jhu.thrax.util;

import java.util.HashMap;

public class Vocabulary {

	private static HashMap<String,Integer> ids = new HashMap<String,Integer>();
	private static HashMap<Integer,String> words = new HashMap<Integer,String>();

	private static int size;

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

	public static int [] getWordIDs(String [] ws)
	{
		int [] ret = new int[ws.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = getWordID(ws[i]);
		}
		return ret;
	}

}
