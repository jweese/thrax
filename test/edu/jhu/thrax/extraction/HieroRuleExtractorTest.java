package edu.jhu.thrax.extraction;

import org.testng.annotations.Test;
import edu.jhu.thrax.util.exceptions.*;

import org.apache.hadoop.conf.Configuration;

public class HieroRuleExtractorTest
{
	private Configuration dummy_conf;
	
	public HieroRuleExtractorTest() {
		dummy_conf = new Configuration();
	}
	
    @Test(expectedExceptions = { EmptySentenceException.class })
    public void emptySource_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor(dummy_conf);
        e.extract("||| world ||| 0-0");
    }

    @Test(expectedExceptions = { EmptySentenceException.class })
    public void emptyTarget_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor(dummy_conf);
        e.extract("hello ||| ||| 0-0");
    }

    @Test(expectedExceptions = { EmptyAlignmentException.class })
    public void emptyAlignment_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor(dummy_conf);
        e.extract("hello ||| world ||| ");
    }

    @Test(expectedExceptions = { InconsistentAlignmentException.class })
    public void inconsistentAlignment_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor(dummy_conf);
        e.extract("hello ||| world ||| 2-3");
    }

    @Test(expectedExceptions = { NotEnoughFieldsException.class })
    public void emptyInput_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor(dummy_conf);
        e.extract("");
    }

    @Test(expectedExceptions = { NotEnoughFieldsException.class })
    public void whitespaceInput_ThrowsException() throws MalformedInputException
    {
        HieroRuleExtractor e = new HieroRuleExtractor(dummy_conf);
        e.extract("          ");
    }

}

