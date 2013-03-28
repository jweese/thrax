package edu.jhu.thrax.hadoop.features;

import java.util.Map;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.util.Vocabulary;

public class ConsumeSourceTerminalsFeature implements SimpleFeature {
  private static final Text LABEL = new Text("SourceTerminalsButNoTarget");
  private static final IntWritable ZERO = new IntWritable(0);
  private static final IntWritable ONE = new IntWritable(1);

  public void score(RuleWritable r, Map<Text, Writable> map) {
    for (int tok : r.target) {
      if (!Vocabulary.nt(tok)) {
        map.put(LABEL, ZERO);
        return;
      }
    }
    for (int tok : r.source) {
      if (!Vocabulary.nt(tok)) {
        map.put(LABEL, ONE);
        return;
      }
    }
    map.put(LABEL, ZERO);
    return;
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }
}
