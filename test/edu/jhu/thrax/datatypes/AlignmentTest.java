package edu.jhu.thrax.datatypes;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AlignmentTest {
	// my favorite map!
	// it's the alignment for "je lui ai pose' une question"
	// with the english "i asked him a question"
	private static int [][] map = { {0, 0},
	                                {1, 2},
					{2, 1},
					{3, 1},
					{4, 3},
					{5, 4} };
	
	private static Alignment al = new Alignment(map);

	@Test
	public void IPP_TruePhrases_ReturnTrue()
	{

		Assert.assertTrue(al.isInitialPhrasePair(0, 0, 0, 0));
		Assert.assertTrue(al.isInitialPhrasePair(1, 1, 2, 2));
		Assert.assertTrue(al.isInitialPhrasePair(2, 3, 1, 1));
		Assert.assertTrue(al.isInitialPhrasePair(4, 4, 3, 3));
		Assert.assertTrue(al.isInitialPhrasePair(5, 5, 4, 4));

		Assert.assertTrue(al.isInitialPhrasePair(0, 3, 0, 2));
		Assert.assertTrue(al.isInitialPhrasePair(0, 4, 0, 3));
		Assert.assertTrue(al.isInitialPhrasePair(0, 5, 0, 4));

		Assert.assertTrue(al.isInitialPhrasePair(1, 3, 1, 2));
		Assert.assertTrue(al.isInitialPhrasePair(1, 4, 1, 3));
		Assert.assertTrue(al.isInitialPhrasePair(1, 5, 1, 4));
		return;
	}

	@Test
	public void IPP_NotPhrases_ReturnFalse()
	{

		Assert.assertFalse(al.isInitialPhrasePair(0, 1, 0, 1));
		Assert.assertFalse(al.isInitialPhrasePair(0, 1, 0, 2));
		Assert.assertFalse(al.isInitialPhrasePair(0, 2, 0, 1));
		Assert.assertFalse(al.isInitialPhrasePair(0, 3, 0, 1));
		Assert.assertFalse(al.isInitialPhrasePair(0, 4, 0, 2));
		return;
	}

	@Test
	public void IsAligned_Yes_ReturnTrue()
	{

		Assert.assertTrue(al.isAligned(0, 0));
		Assert.assertTrue(al.isAligned(1, 2));
		Assert.assertTrue(al.isAligned(2, 1));
		Assert.assertTrue(al.isAligned(3, 1));
		Assert.assertTrue(al.isAligned(4, 3));
		Assert.assertTrue(al.isAligned(5, 4));
		return;
	}
}
