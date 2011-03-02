package edu.jhu.thrax.extraction;

import org.testng.annotations.Test;
import edu.jhu.thrax.util.exceptions.*;

public class HieroRuleExtractorTest
{
    @Test(expectedExceptions = { EmptySourceSentenceException.class })
    public void emptySource_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor();
        e.extract("||| world ||| 0-0");
    }

    @Test(expectedExceptions = { EmptyTargetSentenceException.class })
    public void emptyTarget_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor();
        e.extract("hello ||| ||| 0-0");
    }

    @Test(expectedExceptions = { EmptyAlignmentException.class })
    public void emptyAlignment_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor();
        e.extract("hello ||| world ||| ");
    }

    @Test(expectedExceptions = { InconsistentAlignmentException.class })
    public void inconsistentAlignment_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor();
        e.extract("hello ||| world ||| 2-3");
    }

    @Test(expectedExceptions = { NotEnoughFieldsException.class })
    public void emptyInput_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor();
        e.extract("");
    }

    @Test(expectedExceptions = { NotEnoughFieldsException.class })
    public void whitespaceInput_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor();
        e.extract("          ");
    }

}

