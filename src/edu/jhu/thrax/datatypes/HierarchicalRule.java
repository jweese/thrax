package edu.jhu.thrax.datatypes;

import java.util.Arrays;
import java.util.Iterator;

import edu.jhu.thrax.extraction.SpanLabeler;
import edu.jhu.thrax.hadoop.features.WordLexicalProbabilityCalculator;

public class HierarchicalRule
{
	private final PhrasePair lhs;
	private final PhrasePair [] nts;

	private final static PhrasePair [] EMPTY_NT_ARRAY = new PhrasePair[0];

	public HierarchicalRule(PhrasePair leftHandSide, PhrasePair [] nonterms)
	{
		lhs = leftHandSide;
		nts = nonterms;
	}

	public HierarchicalRule(PhrasePair leftHandSide)
	{
		this(leftHandSide, EMPTY_NT_ARRAY);
	}

	public int arity()
	{
		return nts.length;
	}

	public int numSourceTerminals()
	{
		int result = lhs.sourceLength();
		for (PhrasePair pp : nts)
			result -= pp.sourceLength();
		return result;
	}

	public int numTargetTerminals()
	{
		int result = lhs.targetLength();
		for (PhrasePair pp : nts)
			result -= pp.targetLength();
		return result;
	}

	public int numAlignmentPoints(Alignment a)
	{
		int result = lhs.numAlignmentPoints(a);
		for (PhrasePair pp : nts)
			result -= pp.numAlignmentPoints(a);
		return result;
	}

	public PhrasePair getLhs()
	{
		return lhs;
	}

	public PhrasePair getNonterminal(int index)
	{
		if (index < 0 || index > nts.length)
			return null;
		return nts[index];
	}

	public HierarchicalRule addNonterminal(PhrasePair pp)
	{
		PhrasePair [] theNTs = new PhrasePair[nts.length + 1];
		for (int i = 0; i < nts.length; i++)
			theNTs[i] = nts[i];
		theNTs[nts.length] = pp;
		return new HierarchicalRule(lhs, theNTs);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("HierarchicalRule { ");
		sb.append(String.format("lhs:%s ", lhs));
		for (int i = 0; i < nts.length; i++)
			sb.append(String.format("%d:%s ", i, nts[i]));
		sb.append("}");
		return sb.toString();
	}

	public String toString(String [] sourceWords, String [] targetWords, SpanLabeler labeler, boolean useSourceSpansForLabels)
	{
		String result = lhsLabel(labeler, useSourceSpansForLabels);
		result += " ||| ";
		result += sourceString(sourceWords, labeler, useSourceSpansForLabels);
		result += " ||| ";
		result += targetString(targetWords, labeler, useSourceSpansForLabels);
		return result;
	}

	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (!(o instanceof HierarchicalRule))
			return false;
		HierarchicalRule other = (HierarchicalRule) o;
		return lhs.equals(other.lhs)
			&& Arrays.equals(nts, other.nts);
	}

	public int hashCode()
	{
		int result = 137;
		result = result * 37 + lhs.hashCode();
		result = result * 37 + Arrays.hashCode(nts);
		return result;
	}

	public String lhsLabel(SpanLabeler labeler, boolean useSource)
	{
		return lhs.getLabel(labeler, useSource);
	}

	private String ntLabel(int i, SpanLabeler labeler, boolean useSource)
	{
		if (i < 0 || i >= nts.length)
			return null;
		String raw = nts[i].getLabel(labeler, useSource);
		return raw == null ? null : String.format("[%s,%d]", raw, i + 1);
	}

	private int [] sourceTerminalIndices()
	{
		int [] result = new int[numSourceTerminals()];
		int j = 0;
		int currNT = 0;
		for (int i = lhs.sourceStart; i < lhs.sourceEnd; i++) {
			if (currNT < nts.length && i == nts[currNT].sourceStart) {
				i = nts[currNT].sourceEnd - 1;
				currNT++;
			}
			else {
				result[j] = i;
				j++;
			}
		}
		return result;
	}

	public String sourceString(String [] sourceWords, SpanLabeler labeler, boolean useSource)
	{
		String result = "";
		int currNT = 0;
		for (int i = lhs.sourceStart; i < lhs.sourceEnd; i++) {
			if (i != lhs.sourceStart)
				result += " ";
			if (currNT < nts.length && i == nts[currNT].sourceStart) {
				result += ntLabel(currNT, labeler, useSource);
				i = nts[currNT].sourceEnd - 1;
				currNT++;
			}
			else {
				result += sourceWords[i];
			}
		}
		return result;
	}

	private int [] targetTerminalIndices()
	{
		int [] result = new int[numTargetTerminals()];
		int curr = 0;
		for (int i = lhs.targetStart; i < lhs.targetEnd; i++) {
			boolean nt = false;
			for (int j = 0; j < arity(); j++) {
				if (i == nts[j].targetStart) {
					i = nts[j].targetEnd - 1;
					nt = true;
					break;
				}
			}
			if (!nt) {
				result[curr] = i;
				curr++;
			}
		}
		return result;
	}

	public String targetString(String [] targetWords, SpanLabeler labeler, boolean useSource)
	{
		String result = "";
		for (int i = lhs.targetStart; i < lhs.targetEnd; i++) {
			if (i != lhs.targetStart)
				result += " ";
			boolean nt = false;
			for (int j = 0; j < arity(); j++) {
				if (i == nts[j].targetStart) {
					result += ntLabel(j, labeler, useSource);
					i = nts[j].targetEnd - 1;
					nt = true;
					break;
				}
			}
			if (!nt) {
				result += targetWords[i];
			}
		}
		return result;
	}

	public String [][] sourceAlignmentArray(String [] sourceWords, String [] targetWords, Alignment alignment)
	{
		String [][] result = new String[numSourceTerminals()][];
		int currTerminal = 0;
		for (int i : sourceTerminalIndices()) {
			Iterator<Integer> indices = alignment.targetIndicesAlignedTo(i);
			int len = alignment.numTargetWordsAlignedTo(i);
			result[currTerminal] = singleAlignmentArray(sourceWords[i], targetWords, indices, len);
			currTerminal++;
		}
		return result;
	}

	public String [][] targetAlignmentArray(String [] sourceWords, String [] targetWords, Alignment alignment)
	{
		String [][] result = new String[numTargetTerminals()][];
		int currTerminal = 0;
		for (int i : targetTerminalIndices()) {
			Iterator<Integer> indices = alignment.sourceIndicesAlignedTo(i);
			int len = alignment.numSourceWordsAlignedTo(i);
			result[currTerminal] = singleAlignmentArray(targetWords[i], sourceWords, indices, len);
			currTerminal++;
		}
		return result;
	}


	private static String [] singleAlignmentArray(String word, String [] targetWords, Iterator<Integer> indices, int len)
	{
		String [] result = new String[len == 0 ? 2 : len + 1];
		result[0] = word;
		if (len == 0) {
			result[1] = WordLexicalProbabilityCalculator.UNALIGNED.toString();
			return result;
		}
		int j = 1;
		while (indices.hasNext()) {
			result[j] = targetWords[indices.next()];
			j++;
		}
		return result;
	}
}

