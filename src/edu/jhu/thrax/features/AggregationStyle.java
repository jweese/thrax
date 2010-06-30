package edu.jhu.thrax.features;

/**
 * This enum determines how feature scores should be combined when the scorer
 * sees multiple copies of the same Rule. It was introduced because the
 * lexical probability features needs to be aggregated with MIN.
 */
public enum AggregationStyle {
    NONE, MAX, MIN
}
