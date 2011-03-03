package edu.jhu.thrax.extraction;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.util.exceptions.ConfigurationException;

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
    public static RuleExtractor create(String grammarType) throws ConfigurationException
    {
        String gt = grammarType.toLowerCase();
        if (gt.equals(HieroRuleExtractor.name)) {
            return new HieroRuleExtractor();
        }
        else if (gt.equals(SAMTExtractor.name)) {
            if (!(ThraxConfig.SOURCE_IS_PARSED || ThraxConfig.TARGET_IS_PARSED))
                throw new ConfigurationException("SAMT requires that either the source or target sentences be parsed");
            return new SAMTExtractor();
        }
        // when you create new grammars, add them here.

        else {
            throw new ConfigurationException("unknown grammar type: " + grammarType);
        }
    }

}
