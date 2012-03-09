package edu.jhu.thrax.util.io;

import org.testng.annotations.Test;
import org.testng.Assert;
import edu.jhu.thrax.util.exceptions.*;

public class InputUtilitiesTest
{
    @Test
    public void parseYield_EmptyString_ReturnsZeroLengthArray() throws MalformedInputException
    {
        Assert.assertEquals(InputUtilities.parseYield("").length, 0);
    }

    @Test
    public void parseYield_Whitespace_ReturnsZeroLengthArray() throws MalformedInputException
    {
        Assert.assertEquals(InputUtilities.parseYield("        ").length, 0);
    }
    @Test
    public void parseYield_EmptyParse_ReturnsZeroLengthArray() throws MalformedInputException
    {
        Assert.assertEquals(InputUtilities.parseYield("()").length, 0);
    }

    @Test(expectedExceptions = { MalformedInputException.class })
    public void parseYield_UnbalancedLeft_ThrowsException() throws MalformedInputException
    {
        InputUtilities.parseYield("(S (DT the) (NP dog)");
    }

    @Test(expectedExceptions = { MalformedInputException.class })
    public void parseYield_UnbalancedRight_ThrowsException() throws MalformedInputException
    {
        InputUtilities.parseYield("(S (DT the) (NP dog)))");
    }

    @Test
    public void getWords_EmptyString_ReturnsZeroLengthArray() throws MalformedInputException
    {
        Assert.assertEquals(InputUtilities.getWords("", false).length, 0);
        Assert.assertEquals(InputUtilities.getWords("", true).length, 0);
    }

    @Test
    public void getWords_Whitespace_ReturnsZeroLengthArray() throws MalformedInputException
    {
        Assert.assertEquals(InputUtilities.getWords("    ", false).length, 0);
        Assert.assertEquals(InputUtilities.getWords("    ", true).length, 0);
    }

	@Test
    public void getWords_PlainWords_ReturnsStringArray() throws MalformedInputException
    {
        String [] tokens = { "hello", ",", "world" };
        Assert.assertEquals(InputUtilities.getWords("hello , world", false), tokens);
    }

}

