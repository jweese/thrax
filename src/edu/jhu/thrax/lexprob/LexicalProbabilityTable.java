package edu.jhu.thrax.lexprob;

import org.apache.hadoop.io.Text;

/**
 * A data structure holding word-level lexical probabilities. The table only
 * needs to support two operations: determining whether a particular pair is
 * present in the table, and returning the probability associated with the 
 * pair.
 */
public interface LexicalProbabilityTable
{
    /**
     * Determine whether a word pair is present in the table.
     *
     * @param car word A
     * @param cdr word B
     * @return true if p(B|A) is in the table, false otherwise
     */
    public boolean contains(Text car, Text cdr);

    /**
     * Return the lexical probability of a word pair.
     *
     * @param car word A
     * @param cdr word B
     * @return the probability -logp(B|A) if present, -1 otherwise
     */
    public double get(Text car, Text cdr);
}

