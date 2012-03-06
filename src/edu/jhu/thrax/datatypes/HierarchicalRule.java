package edu.jhu.thrax.datatypes;

import java.util.Arrays;

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

	public PhrasePair getNonterminal(int index)
	{
		if (index < 0 || index > nts.length)
			return null;
		return nts[index];
	}

	public boolean lhsContains(PhrasePair pp)
	{
		return lhs.contains(pp);
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
}

