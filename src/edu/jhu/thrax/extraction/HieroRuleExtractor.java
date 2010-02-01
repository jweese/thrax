package edu.jhu.thrax.extraction;

import java.util.ArrayList;

import edu.jhu.thrax.datatypes.*;

public class HieroRuleExtractor implements RuleExtractor {

	public HieroRuleExtractor()
	{

	}

	public String [] requiredInputs()
	{
		String [] reqs = { "source" , "target" , "alignment" };
		return reqs;
	}

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
