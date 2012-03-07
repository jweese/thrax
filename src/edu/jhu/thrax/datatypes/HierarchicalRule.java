package edu.jhu.thrax.datatypes;

import java.util.Arrays;

import edu.jhu.thrax.extraction.SpanLabeler;

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
		result += " |||";
		result += sourceString(sourceWords, labeler, useSourceSpansForLabels);
		result += " |||";
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

	private String lhsLabel(SpanLabeler labeler, boolean useSource)
	{
		String raw = lhs.getLabel(labeler, useSource);
		return raw == null ? null : "[" + raw + "]";
	}

	private String ntLabel(int i, SpanLabeler labeler, boolean useSource)
	{
		if (i < 0 || i >= nts.length)
			return null;
		String raw = nts[i].getLabel(labeler, useSource);
		return raw == null ? null : String.format("[%s,%d]", raw, i + 1);
	}

	private String sourceString(String [] sourceWords, SpanLabeler labeler, boolean useSource)
	{
		String result = "";
		int currNT = 0;
		for (int i = lhs.sourceStart; i < lhs.sourceEnd; i++) {
			if (currNT < nts.length && i == nts[currNT].sourceStart) {
				result += " " + ntLabel(currNT, labeler, useSource);
				i = nts[currNT].sourceEnd - 1;
				currNT++;
			}
			else {
				result += " " + sourceWords[i];
			}
		}
		return result;
	}

	private String targetString(String [] targetWords, SpanLabeler labeler, boolean useSource)
	{
		String result = "";
		for (int i = lhs.targetStart; i < lhs.targetEnd; i++) {
			boolean nt = false;
			for (int j = 0; j < arity(); j++) {
				if (i == nts[j].targetStart) {
					result += " " + ntLabel(j, labeler, useSource);
					i = nts[j].targetEnd - 1;
					nt = true;
					break;
				}
			}
			if (!nt) {
				result += " " + targetWords[i];
			}
		}
		return result;
	}

}

