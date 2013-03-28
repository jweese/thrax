package edu.jhu.thrax.hadoop.features;

import java.util.Map;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class PhrasePenaltyFeature implements SimpleFeature {
  private static final Text LABEL = new Text("PhrasePenalty");
  private static final IntWritable ONE = new IntWritable(1);

  public void score(RuleWritable r, Map<Text, Writable> map) {
    map.put(LABEL, ONE);
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ONE);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ONE);
  }
}
