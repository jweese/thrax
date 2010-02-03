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
	 * The phrase is represented by an array of int, where the first two
	 * integers are the bounds of the source side, and the last two are
	 * the bounds of the target side of the phrase.
	 *
	 * @param p a PhrasePair of interest
	 * @return true if the two phrases form an initial phrase pair, false
	 * otherwise
	 */
	public boolean isInitialPhrasePair(PhrasePair p)
	{
		int x = p.endpoints[0];
		int y = p.endpoints[1];
		int a = p.endpoints[2];
		int b = p.endpoints[3];
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

	/**
	 * Determines if a particular source word is aligned to a particular
	 * target word.
	 *
	 * @param source the index of the source word
	 * @param target the index of the target word
	 * @return true if the words are aligned under this Alignment, false
	 * otherwise
	 */
	public boolean isAligned(int source, int target)
	{
		for (int [] link : map) {
			if ((link[0] == source) && (link[1] == target)) {
				return true;
			}
		}
		return false;
	}

}
