package edu.jhu.thrax.hadoop.features;

import java.util.Map;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.util.Vocabulary;

public class WordCompressionRatioFeature implements SimpleFeature {
  private static final Text LABEL = new Text("WordLogCR");
  private static final IntWritable ZERO = new IntWritable(0);

  public void score(RuleWritable r, Map<Text, Writable> map) {
    int src_count = 0;
    for (int tok : r.source)
      if (!Vocabulary.nt(tok)) src_count++;
    int tgt_count = 0;
    for (int tok : r.target)
      if (!Vocabulary.nt(tok)) tgt_count++;
    if (src_count == 0 || tgt_count == 0) {
      map.put(LABEL, ZERO);
    } else {
      map.put(LABEL, new FloatWritable((float) Math.log((float) tgt_count / src_count)));
    }
    return;
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }
}
