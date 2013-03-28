package edu.jhu.thrax.hadoop.features;

import java.util.Map;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.util.Vocabulary;

public class WordCountDifferenceFeature implements SimpleFeature {

  private static final Text LABEL = new Text("WordCountDiff");
  private static final IntWritable ZERO = new IntWritable(0);

  public void score(RuleWritable r, Map<Text, Writable> map) {
    int word_difference = 0;
    for (int tok : r.source)
      if (!Vocabulary.nt(tok)) word_difference--;
    for (int tok : r.target)
      if (!Vocabulary.nt(tok)) word_difference++;
    map.put(LABEL, new IntWritable(word_difference));
    return;
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }
}
