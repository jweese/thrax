package edu.jhu.thrax.extraction;

import java.util.Set;
import java.util.HashSet;
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

	private static final int INIT_LENGTH_LIMIT = 10;
	private static final int SOURCE_LENGTH_LIMIT = 5;
	private static final int NT_LIMIT = 2;

	public Set<Rule> extract(Object [] inputs)
	{
		if (inputs.length < 3) {
			return null;
		}

		int [] source = (int []) inputs[0];
		int [] target = (int []) inputs[1];
		Alignment alignment = (Alignment) inputs[2];

		Set<Rule> rules = new HashSet<Rule>();

		return rules;
	}

	
	/**
	 * Determines if one phrase is contained completely within another.
	 * The phrases are represented by arrays of int, delimiting the
	 * source and target side spans of each phrase.
	 *
	 * @param child the possible subphrase
	 * @param parent the possible larger phrase
	 * @return true if child is contained in parent, false otherwise
	 */
	private static boolean containedIn(int [] child, int [] parent)
	{
		return ((child[0] >= parent[0]) && (child[1] <= parent[1]) && (child[2] >= parent[2]) && (child[3] <= parent[3]));
	}

	/**
	 * Determines if two phrases are disjoint. Again, the phrases are
	 * represented by an array of int holding the spans of the phrase.
	 *
	 * @param p1 a phrase
	 * @param p2 a phrase
	 * @return true if the phrases do not overlap, false otherwise
	 */
	private static boolean disjoint(int [] p1, int [] p2)
	{
		if (((p1[0] >= p2[0]) && (p1[0] <= p2[1])) || 
		    ((p1[1] >= p2[0]) && (p1[1] <= p2[1]))) {
			return !(((p1[2] >= p2[2]) && (p1[2] <= p2[3])) ||
			         ((p1[3] >= p2[2]) && (p1[3] <= p2[3])));
		}
		return false;
	}
}
