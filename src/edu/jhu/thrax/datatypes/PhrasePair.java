package edu.jhu.thrax.datatypes;

/**
 * This class represents a phrase pair -- that is, an aligned pair of subphrases
 * contained in an aligned pair of sentences. The representation is simple
 * and minimal, backed only by an integer array.
 */
public class PhrasePair {
	/**
	 * The four endpoints of the phrase pair, in this order: start of source
	 * span, end of source span, start of target span, end of target span.
	 */
	public int [] endpoints;

	/**
	 * Constructor.
	 */
	public PhrasePair(int [] ps)
	{
		endpoints = ps.clone();
	}

	/**
	 * Determines if this PhrasePair is disjoint from another; that is,
	 * determines if they intersect or not.
	 *
	 * @param other another PhrasePair of interest
	 * @return true if the two PhraseParis are disjoint, false otherwise
	 */
	public boolean isDisjointFrom(PhrasePair other)
	{
		return false;
	}

	/**
	 * Determines if another PhrasePair is completely contained in this
	 * PhrasePair.
	 *
	 * @param other another PhrasePair of interest
	 * @return true if the other PhrasePair is completely contained in this
	 * one, false otherwise
	 */
	public boolean contains(PhrasePair other)
	{
		return false;
	}
}
