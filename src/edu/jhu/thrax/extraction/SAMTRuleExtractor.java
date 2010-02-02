package edu.jhu.thrax.extraction;

import java.util.ArrayList;

import edu.jhu.thrax.datatypes.*;

/**
 * This class extract syntax-aided machine translation rules from provided
 * inputs. The necessary inputs are source and target sides of a parallel
 * corpus, the alignment between the parallel sentences, and target-side
 * parse trees.
 */
public class SAMTRuleExtractor implements RuleExtractor {

	public SAMTRuleExtractor()
	{

	}

	public String [] requiredInputs()
	{
		String [] reqs = { "source", "target", "alignment", "parse" };
		return reqs;
	}

	public ArrayList<Rule> extract(Object [] inputs)
	{
		if (inputs.length < 4) {
			return null;
		}

		int [] source = (int []) inputs[0];
		int [] target = (int []) inputs[1];
		Alignment alignment = (Alignment) inputs[2];
		ParseTree parse = (ParseTree) inputs[3];

		return new ArrayList<Rule>();
	}

}
