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

	@Test
	public void Parse_LongForm_ArgumentWithEquals() throws OptionMissingArgumentException
	{
		String [] cmd = { "--delta=value" };
		GetOpt.registerOption("d", "delta", true);
		GetOpt.parse(cmd);
		Assert.assertTrue(GetOpt.isSet("d"));
		Assert.assertTrue(GetOpt.isSet("delta"));
		Assert.assertEquals(GetOpt.valueOf("d"), "value");
		Assert.assertEquals(GetOpt.valueOf("delta"), "value");
		return;
	}

	@Test
	public void Parse_LongForm_ArgumentNoEquals() throws OptionMissingArgumentException
	{
		String [] cmd = { "--echo" , "value" };
		GetOpt.registerOption("e", "echo", true);
		GetOpt.parse(cmd);
		Assert.assertTrue(GetOpt.isSet("e"));
		Assert.assertTrue(GetOpt.isSet("echo"));
		Assert.assertEquals(GetOpt.valueOf("e"), "value");
		Assert.assertEquals(GetOpt.valueOf("echo"), "value");
		return;
	}

	@Test
	public void Parse_ShortForm_Single() throws OptionMissingArgumentException
	{
		String [] cmd = { "-f" };
		GetOpt.registerOption("f", "foxtrot", false);
		GetOpt.parse(cmd);
		Assert.assertTrue(GetOpt.isSet("f"));
		Assert.assertTrue(GetOpt.isSet("foxtrot"));
		return;
	}

	@Test
	public void Parse_ShortForm_Many() throws OptionMissingArgumentException
	{
		String [] cmd = { "-ghi" };
		GetOpt.registerOption("g", "golf", false);
		GetOpt.registerOption("h", "hotel", false);
		GetOpt.registerOption("i", "india", false);
		GetOpt.parse(cmd);
		Assert.assertTrue(GetOpt.isSet("g"));
		Assert.assertTrue(GetOpt.isSet("h"));
		Assert.assertTrue(GetOpt.isSet("i"));
		Assert.assertTrue(GetOpt.isSet("golf"));
		Assert.assertTrue(GetOpt.isSet("hotel"));
		Assert.assertTrue(GetOpt.isSet("india"));
		return;
	}

	@Test
	public void Parse_ShortForm_WithArgument() throws OptionMissingArgumentException
	{
		String [] cmd = { "-jvalue" };
		GetOpt.registerOption("j", "juliet", true);
		GetOpt.parse(cmd);
		Assert.assertTrue(GetOpt.isSet("j"));
		Assert.assertTrue(GetOpt.isSet("juliet"));
		Assert.assertEquals(GetOpt.valueOf("j"), "value");
		Assert.assertEquals(GetOpt.valueOf("juliet"), "value");
		return;
	}

	@Test
	public void Parse_ShortForm_SeparateArgument() throws OptionMissingArgumentException
	{
		String [] cmd = { "-k" , "value" };
		GetOpt.registerOption("k", "kilo", true);
		GetOpt.parse(cmd);
		Assert.assertTrue(GetOpt.isSet("k"));
		Assert.assertTrue(GetOpt.isSet("kilo"));
		Assert.assertEquals(GetOpt.valueOf("k"), "value");
		Assert.assertEquals(GetOpt.valueOf("kilo"), "value");
		return;
	}

}
