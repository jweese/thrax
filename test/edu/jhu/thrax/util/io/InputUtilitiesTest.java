package edu.jhu.thrax.util.io;

import org.testng.annotations.Test;
import org.testng.Assert;
import edu.jhu.thrax.util.exceptions.*;

public class InputUtilitiesTest
{
    @Test(expectedExceptions = { MalformedParseException.class })
    public void parseYield_EmptyString_ThrowsException() throws MalformedParseException
    {
        InputUtilities.parseYield("");
    }

    @Test
    public void parseYield_EmptyParse_ReturnsZeroLengthArray() throws MalformedParseException
    {
        Assert.assertEquals(InputUtilities.parseYield("()"), new String[0]);
    }

    @Test(expectedExceptions = { MalformedParseException.class })
    public void parseYield_UnbalancedLeft_ThrowsException() throws MalformedParseException
    {
        InputUtilities.parseYield("(S (DT the) (NP dog)");
    }

    @Test(expectedExceptions = { MalformedParseException.class })
    public void parseYield_UnbalancedRight_ThrowsException() throws MalformedParseException
    {
        InputUtilities.parseYield("(S (DT the) (NP dog)))");
    }
}
