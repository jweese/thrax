package edu.jhu.thrax.util.getopt;

import org.testng.Assert;
import org.testng.annotations.Test;

public class OptionTest {
	private Option testOption;

	@Test
	public void Option()
	{
		testOption = new Option(true);
		Assert.assertEquals(testOption.value(), "");
		Assert.assertFalse(testOption.isSet());
		Assert.assertTrue(testOption.requiresArgument());
	}

	@Test(dependsOnMethods = { "Option" })
	public void Set() {
		testOption.set();
		Assert.assertTrue(testOption.isSet());
	}

	@Test(dependsOnMethods = { "Option" })
	public void SetString() {
		testOption.set("value");
		Assert.assertTrue(testOption.isSet());
		Assert.assertEquals(testOption.value(), "value");
	}

}
