package edu.jhu.thrax.hadoop.features;

import java.util.Map;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.util.Vocabulary;

public class XRuleFeature implements SimpleFeature {
  private static final Text LABEL = new Text("ContainsX");
  private static final IntWritable ZERO = new IntWritable(0);
  private static final IntWritable ONE = new IntWritable(1);
  // TODO: should be default nonterminal and not explicitly X.
  private final int PATTERN = Vocabulary.id("[X]");

  public void score(RuleWritable r, Map<Text, Writable> map) {
    map.put(LABEL, r.lhs == PATTERN ? ONE : ZERO);
    return;
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }
}
