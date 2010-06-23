package edu.jhu.thrax.extraction;

import edu.jhu.thrax.ThraxConfig;

public class SAMTExtractor extends HieroRuleExtractor {

    public static String name = "samt";

    public String [] requiredInputs()
    {
        return new String [] { ThraxConfig.SOURCE,
                               ThraxConfig.PARSE,
                               ThraxConfig.ALIGNMENT };
    }

    public SAMTExtractor()
    {
        super();
    }

}
