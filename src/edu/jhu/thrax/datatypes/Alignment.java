package edu.jhu.thrax.datatypes;

import java.util.Arrays;

/**
 * This class represents an alignment between two parallel sentences.
 */
public class Alignment {

	private int [][] map;
	
	/**
	 * Constructor. It should be given an n-by-2 array of int m, where
	 * the source word at m[i][0] is aligned with the target word at
	 * m[i][1] for all i.
	 *
	 * @param m the alignment map
	 */
	public Alignment(int [][] m)
	{
		map = m;
	}

	/**
	 * Determines if a source and target phrase form an initial phrase pair
	 * (in the sense of Chiang). Chiang (2007) writes that two phrases are
	 * an initial phrase pair if (1) some source word is aligned to some
	 * target word, (2) no source word is aligned outside the target phrase,
	 * and (3) no target word is aligned outside the source phrase.
	 *
	 * @param x the index of the start of the source phrase
	 * @param y the index of the end of the source phrase
	 * @param a the index of the start of the target phrase
	 * @param b the index of the end of the target phrase
	 * @return true if the two phrases form an initial phrase pair, false
	 * otherwise
	 */
	public boolean isInitialPhrasePair(int x, int y, int a, int b)
	{
		boolean hasAlignment = false;
		for (int i = 0; i < map.length; i++) {
			int f = map[i][0];
			int e = map[i][1];
			if ((f >= x) && (f <= y)) {
				if ((e < a) || (e > b)) {
					return false;
				}
				else {
					hasAlignment = true;
				}
			}
			else {
				if ((e >= a) && (e <= b)) {
					return false;
				}
			}
		}
		return hasAlignment;
	}
}
