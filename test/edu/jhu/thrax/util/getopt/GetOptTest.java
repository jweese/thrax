package edu.jhu.thrax.util.getopt;

import org.testng.Assert;
import org.testng.annotations.Test;

public class GetOptTest {

	@Test
	public void IsSet_NotPresent_ReturnFalse()
	{
		Assert.assertFalse(GetOpt.isSet("a"));
		return;
	}

	@Test
	public void IsSet_Present_ReturnFalse()
	{
		GetOpt.registerOption("a", "all", false);
		Assert.assertFalse(GetOpt.isSet("a"));
		Assert.assertFalse(GetOpt.isSet("all"));
		return;
	}

	@Test
	public void ValueOf_NotPresent_EmptyString()
	{
		Assert.assertEquals(GetOpt.valueOf("b"), "");
		return;
	}

	@Test
	public void ValueOf_Present_EmptyString()
	{
		GetOpt.registerOption("b", "ball", false);
		Assert.assertEquals(GetOpt.valueOf("b"), "");
		Assert.assertEquals(GetOpt.valueOf("ball"), "");
		return;
	}

	@Test
	public void Parse_LongForm_NoArgumentGiven_IsSet() throws OptionMissingArgumentException
	{
		String [] cmd = { "--charlie" };
		GetOpt.registerOption("c", "charlie", false);
		GetOpt.parse(cmd);
		Assert.assertTrue(GetOpt.isSet("c"));
		Assert.assertTrue(GetOpt.isSet("charlie"));
		return;
	}
}
