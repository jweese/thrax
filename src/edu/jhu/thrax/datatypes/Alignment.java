package edu.jhu.thrax.datatypes;

import java.util.Arrays;

public class Alignment {

	private int [][] map;

	public Alignment(int [][] m)
	{
		map = m;
	}

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
		}
		return hasAlignment;
	}
}
