package edu.jhu.thrax.tools;

class ScoredEntry implements Comparable<ScoredEntry> {
  String pair;
  double score;

  public ScoredEntry(String p, double s) {
    pair = p;
    score = s;
  }

  @Override
  public int compareTo(ScoredEntry that) {
    return Double.compare(this.score, that.score);
  }
}