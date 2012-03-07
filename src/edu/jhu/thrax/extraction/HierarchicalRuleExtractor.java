package edu.jhu.thrax.extraction;

import edu.jhu.thrax.datatypes.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.IOException;

public class HierarchicalRuleExtractor
{
	private int arityLimit = 2;
	private int initialPhraseSourceLimit = 10;
	private int initialPhraseTargetLimit = 10;
	private boolean requireMinimalPhrases = true;
	private int minimumInitialAlignmentPoints = 1;
	private boolean allowAdjacent = false;
	private int sourceSymbolLimit = 5;
	private int targetSymbolLimit = 1000;
	private int minimumRuleAlignmentPoints = 1;

	public List<HierarchicalRule> extract(int sourceLength, int targetLength, Alignment alignment)
	{
		List<PhrasePair> initialPhrasePairs = initialPhrasePairs(sourceLength, targetLength, alignment);

		HierarchicalRule [][] rulesByArity = new HierarchicalRule[arityLimit+1][];
		// we have one arity-0 rule for each initial phrase pair
		rulesByArity[0] = new HierarchicalRule[initialPhrasePairs.size()];
		for (int i = 0; i < initialPhrasePairs.size(); i++)
			rulesByArity[0][i] = new HierarchicalRule(initialPhrasePairs.get(i));
		// rules of arity j can be created from rules of arity j - 1 using the
		// initial phrase pairs
		for (int j = 1; j <= arityLimit; j++) {
			rulesByArity[j] = addNonterminalsTo(rulesByArity[j-1], initialPhrasePairs);
		}
		return removeIfNotValid(rulesByArity, alignment);
	}

	private List<PhrasePair> initialPhrasePairs(int sourceLength, int targetLength, Alignment a)
	{
		List<PhrasePair> result = new ArrayList<PhrasePair>();
		for (int i = 0; i < sourceLength; i++) {
			for (int x = 1; x <= initialPhraseSourceLimit; x++) {
				if (i + x > sourceLength)
					break;
				for (int j = 0; j < targetLength; j++) {
					for (int y = 1; y <= initialPhraseTargetLimit; y++) {
						if (j + y > targetLength)
							break;
						PhrasePair pp = new PhrasePair(i, i + x, j, j + y);
						if (pp.isInitialPhrasePair(a, !requireMinimalPhrases, minimumInitialAlignmentPoints)) {
							result.add(pp);
						}
					}
				}
			}
		}
		return result;
	}

	private HierarchicalRule [] addNonterminalsTo(HierarchicalRule [] rules, List<PhrasePair> initialPhrasePairs)
	{
		List<HierarchicalRule> result = new ArrayList<HierarchicalRule>();
		for (HierarchicalRule r : rules) {
			int start = getStart(r, allowAdjacent);
			int end = r.getLhs().sourceEnd;
			for (PhrasePair pp : initialPhrasePairs) {
				if (pp.sourceStart < start)
					continue;
				if (pp.sourceStart >= end)
					break;
				if (r.getLhs().contains(pp))
					result.add(r.addNonterminal(pp));
			}
		}
		HierarchicalRule [] resultArray = new HierarchicalRule[result.size()];
		return result.toArray(resultArray);
	}

	private static int getStart(HierarchicalRule r, boolean allowAdjacent)
	{
		int arity = r.arity();
		if (arity == 0)
			return r.getLhs().sourceStart;
		int start = r.getNonterminal(arity - 1).sourceEnd;
		int offset = allowAdjacent ? 0 : 1;
		return start + offset;
	}

	private List<HierarchicalRule> removeIfNotValid(HierarchicalRule [][] rules, Alignment a)
	{
		List<HierarchicalRule> result = new ArrayList<HierarchicalRule>();
		for (HierarchicalRule [] rs : rules) {
			for (HierarchicalRule r : rs) {
				if (isValid(r, a))
					result.add(r);
			}
		}
		return result;
	}

	private boolean isValid(HierarchicalRule r, Alignment a)
	{
		// conditions:
		// 1) limit of the total number of symbols on the source side
		// 2) limit of the total number of symbols on the target side
		// 3) minimum number of alignment points
		// This is where you add more!
		if (r.arity() + r.numSourceTerminals() > sourceSymbolLimit)
			return false;
		if (r.arity() + r.numTargetTerminals() > targetSymbolLimit)
			return false;
		if (r.numAlignmentPoints(a) < minimumRuleAlignmentPoints)
			return false;
		return true;
	}

	public static void main(String [] argv) throws IOException
	{
		Scanner scanner = new Scanner(System.in, "utf-8");
		HierarchicalRuleExtractor extractor = new HierarchicalRuleExtractor();
		SpanLabeler labeler = null;
		if (argv.length > 0) {
			if (argv[0].equals("--hiero"))
				labeler = new HieroLabeler("X");
		}
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String [] parts = line.split(" \\|\\|\\| ");
			if (parts.length >= 3) {
				String [] source = parts[0].split("\\s+");
				String [] target = parts[1].split("\\s+");
				Alignment alignment = ArrayAlignment.fromString(parts[2], false);
				for (HierarchicalRule r : extractor.extract(source.length, target.length, alignment)) {
					if (labeler != null)
						System.out.println(r.toString(source, target, labeler, true));
					else
						System.out.println(r);
				}
			}
		}
		return;
	}

}

