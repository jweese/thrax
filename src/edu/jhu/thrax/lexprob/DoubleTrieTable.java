package edu.jhu.thrax.lexprob;


public class DoubleTrieTable implements LexicalProbabilityTable {
  private final TrieLexprobTable e2f;
  private final TrieLexprobTable f2e;

  public DoubleTrieTable(TrieLexprobTable _e2f, TrieLexprobTable _f2e) {
    e2f = _e2f;
    f2e = _f2e;
  }

  public double logpSourceGivenTarget(int source, int target) {
    return e2f.get(target, source);
  }

  public double logpTargetGivenSource(int source, int target) {
    return f2e.get(source, target);
  }
}
