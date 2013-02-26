package edu.jhu.thrax.lexprob;


public class DoubleHashMapTable implements LexicalProbabilityTable {
  private final HashMapLexprobTable e2f;
  private final HashMapLexprobTable f2e;

  public DoubleHashMapTable(HashMapLexprobTable _e2f, HashMapLexprobTable _f2e) {
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
