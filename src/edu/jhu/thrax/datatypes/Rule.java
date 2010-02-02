package edu.jhu.thrax.datatypes;

/**
 * This class represents a synchronous context-free grammar production rule.
 */
public class Rule {
	/**
	 * The left hand side nonterminal symbol of the rule.
	 */
	private int lhs;

	/**
	 * The source-side right hand side of the rule.
	 */
	private int [] sourceRhs;

	/**
	 * The target-side right hand side of the rule.
	 */
	private int [] targetRhs;

	/**
	 * A map indicating the one-to-one correspondence between nonterminals
	 * on the two right hand sides. For example, if nonTerminalAlignment[0]
	 * = 2, this means that the first nonterminal on the source side
	 * (reading left-to-right) corresponds to the third nonterminal on the
	 * target side.
	 */
	private int [] nonTerminalAlignment;

}
