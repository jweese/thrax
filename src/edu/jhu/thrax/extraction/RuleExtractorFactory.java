package edu.jhu.thrax.extraction;

import edu.jhu.thrax.util.UnknownGrammarTypeException;

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
	 * @throws UnknownGrammarTypeException if grammarType is not a known
	 * type of grammar
	 */
	public static RuleExtractor create(String grammarType) throws UnknownGrammarTypeException
	{
		String gt = grammarType.toLowerCase();
		if (gt.equals(HieroRuleExtractor.name)) {
			return new HieroRuleExtractor();
		}
		else if (gt.equals(SAMTRuleExtractor.name)) {
			return new SAMTRuleExtractor();
		}
		// when you create new grammars, add them here.

		else {
			throw new UnknownGrammarTypeException(grammarType);
		}
	}

}
