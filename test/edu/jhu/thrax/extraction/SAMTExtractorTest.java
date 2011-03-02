package edu.jhu.thrax.extraction;

import org.testng.annotations.Test;
import org.testng.Assert;
import edu.jhu.thrax.util.exceptions.*;
import edu.jhu.thrax.datatypes.Rule;
import java.util.List;

public class SAMTExtractorTest
{
    @Test(expectedExceptions = { EmptySourceSentenceException.class })
    public void emptySource_ThrowsException() throws MalformedInputException
    {
        SAMTExtractor e = new SAMTExtractor();
        e.extract("||| (S world) ||| 0-0");
    }

    @Test(expectedExceptions = { EmptyTargetSentenceException.class })
    public void emptyTarget_ThrowsException() throws MalformedInputException
    {
        SAMTExtractor e = new SAMTExtractor();
        e.extract("hello ||| ||| 0-0");
    }

    @Test(expectedExceptions = { EmptyTargetSentenceException.class })
    public void emptyParse_ThrowsException() throws MalformedInputException
    {
        SAMTExtractor e = new SAMTExtractor();
        e.extract("hello ||| () ||| 0-0");
    }

    @Test(expectedExceptions = { EmptyTargetSentenceException.class })
    public void doubleEmptyParse_ThrowsException() throws MalformedInputException
    {
        SAMTExtractor e = new SAMTExtractor();
        e.extract("hello ||| (()) ||| 0-0");
    }

    @Test(expectedExceptions = { EmptyAlignmentException.class })
    public void emptyAlignment_ThrowsException() throws MalformedInputException
    {
        SAMTExtractor e = new SAMTExtractor();
        e.extract("hello ||| (S world) ||| ");
    }

    @Test(expectedExceptions = { InconsistentAlignmentException.class })
    public void inconsistentAlignment_ThrowsException() throws MalformedInputException
    {
        SAMTExtractor e = new SAMTExtractor();
        e.extract("hello ||| (S world) ||| 2-3");
    }

    @Test(expectedExceptions = { NotEnoughFieldsException.class })
    public void emptyInput_ThrowsException() throws MalformedInputException
    {
        SAMTExtractor e = new SAMTExtractor();
        e.extract("");
    }

    @Test(expectedExceptions = { NotEnoughFieldsException.class })
    public void whitespaceInput_ThrowsException() throws MalformedInputException
    {
        SAMTExtractor e = new SAMTExtractor();
        e.extract("          ");
    }

    @Test(expectedExceptions = { MalformedParseException.class })
    public void unfinishedParse_ThrowsException() throws MalformedInputException
    {
        SAMTExtractor e = new SAMTExtractor();
        e.extract("hello ||| (S world ||| 0-0");
    }

    @Test(expectedExceptions = { MalformedParseException.class })
    public void extraRRBParse_ThrowsException() throws MalformedInputException
    {
        SAMTExtractor e = new SAMTExtractor();
        e.extract("hello ||| (S world)) ||| 0-0");
    }

    @Test
    public void emptyParse_YieldHasLengthZero() throws MalformedParseException
    {
        Assert.assertEquals(SAMTExtractor.yield("()").length, 0);
    }

}

