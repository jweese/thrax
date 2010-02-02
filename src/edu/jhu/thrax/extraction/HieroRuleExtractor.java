package edu.jhu.thrax.extraction;

import java.util.ArrayList;

import edu.jhu.thrax.datatypes.*;
import edu.jhu.thrax.ThraxConfig;

/**
 * This class extracts Hiero-style SCFG rules. The inputs that are needed
 * are "source" "target" and "alignment", which are the source and target
 * sides of a parallel corpus, and an alignment between each of the sentences.
 */
public class HieroRuleExtractor implements RuleExtractor {

	public HieroRuleExtractor()
	{

	}

	public static String name = "hiero";
	public static String [] requiredInputs = { ThraxConfig.SOURCE,
	                                           ThraxConfig.TARGET,
						   ThraxConfig.ALIGNMENT };

	public ArrayList<Rule> extract(Object [] inputs)
	{
		if (inputs.length < 3) {
			return null;
		}

		int [] source = (int []) inputs[0];
		int [] target = (int []) inputs[1];
		Alignment alignment = (Alignment) inputs[2];

		return new ArrayList<Rule>();
	}
}
