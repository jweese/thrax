package edu.jhu.thrax.hadoop.features;

import java.util.Map;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.util.Vocabulary;

public class AdjacentNonTerminalsFeature implements SimpleFeature {
  private static final Text LABEL = new Text("Adjacent");
  private static final IntWritable ZERO = new IntWritable(0);
  private static final IntWritable ONE = new IntWritable(1);

  public void score(RuleWritable r, Map<Text, Writable> map) {
    for (int i = 0; i < r.source.length - 1; ++i)
      if (Vocabulary.nt(r.source[i])) {
        if (Vocabulary.nt(r.source[i + 1])) {
          map.put(LABEL, ONE);
          return;
        } else {
          i += 2;
          continue;
        }
      }
    map.put(LABEL, ZERO);
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ONE);
  }
}
