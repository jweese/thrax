package edu.jhu.thrax.hadoop.features;

import java.util.Map;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.util.Vocabulary;

public class CharacterCompressionRatioFeature implements SimpleFeature {

  private static final Text LABEL = new Text("CharLogCR");
  private static final DoubleWritable ZERO = new DoubleWritable(0);

  public void score(RuleWritable r, Map<Text, Writable> map) {
    int src_length = 0;
    for (int tok : r.source) {
      if (!Vocabulary.nt(tok)) {
        src_length += Vocabulary.word(tok).length();
      }
    }
    src_length += r.source.length - 1;

    int tgt_length = 0;
    for (int tok : r.target) {
      if (!Vocabulary.nt(tok)) {
        tgt_length += Vocabulary.word(tok).length();
      }
    }
    tgt_length += r.target.length - 1;

    if (src_length == 0 || tgt_length == 0) {
      map.put(LABEL, ZERO);
    } else {
      map.put(LABEL, new DoubleWritable(Math.log((double) tgt_length / src_length)));
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
