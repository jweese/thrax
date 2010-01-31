package edu.jhu.thrax.util.getopt;

import org.testng.Assert;
import org.testng.annotations.Test;

public class OptionTest {
	private Option testOption;

	@Test
	public void Option()
	{
		testOption = new Option(true);
		Assert.assertTrue(testOption.requiresArgument());
	}

}
