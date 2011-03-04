package edu.jhu.thrax.extraction;

import edu.jhu.thrax.util.exceptions.ConfigurationException;

import org.apache.hadoop.conf.Configuration;

/**
 * This class provides specific kinds of rule extractors, depending on the type
 * of grammar that the caller wants to extract.
 */
public class RuleExtractorFactory {

    /**
     * Creates a rule extractor depending on the type of grammar to extract.
     *
     * @param grammarType the name of the grammar type
     * @return a <code>RuleExtractor</code> for that type of grammar
     * @throws ConfigurationException if the current configuration will not
     *                                allow an extractor to be created
     * type of grammar
     */
    public static RuleExtractor create(Configuration conf) throws ConfigurationException
    {
        String gt = conf.get("thrax.grammar", "NONE");
        boolean SOURCE_IS_PARSED = conf.getBoolean("thrax.source-is-parsed", false);
        boolean TARGET_IS_PARSED = conf.getBoolean("thrax.target-is-parsed", false);
        if (gt.equals(HieroRuleExtractor.name)) {
            return new HieroRuleExtractor(conf);
        }
        else if (gt.equals(SAMTExtractor.name)) {
            if (!(SOURCE_IS_PARSED || TARGET_IS_PARSED))
                throw new ConfigurationException("SAMT requires that either the source or target sentences be parsed");
            return new SAMTExtractor(conf);
        }
        // when you create new grammars, add them here.

        else {
            throw new ConfigurationException("unknown grammar type: " + gt);
        }
    }

}
