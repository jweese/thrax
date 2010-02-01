package edu.jhu.thrax.extraction;

import edu.jhu.thrax.util.UnknownGrammarTypeException;

public class RuleExtractorFactory {

	public static RuleExtractor create(String grammarType) throws UnknownGrammarTypeException
	{
		String gt = grammarType.toLowerCase();
		if (gt.equals("hiero")) {
			return new HieroRuleExtractor();
		}
		else if (gt.equals("samt")) {
			return new SAMTRuleExtractor();
		}
		// when you create new grammars, add them here.

		else {
			throw new UnknownGrammarTypeException(grammarType);
		}
	}

}
