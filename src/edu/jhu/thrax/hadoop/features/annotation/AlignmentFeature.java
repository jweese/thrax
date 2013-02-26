package edu.jhu.thrax.hadoop.features.annotation;

import java.util.Map;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class AlignmentFeature implements AnnotationFeature {
  private static final Text LABEL = new Text("Alignment");
  private static final IntWritable ZERO = new IntWritable(0);

  public Text getName() {
    return LABEL;
  }

  public Text score(RuleWritable r, Annotation annotation) {
    return new Text(annotation.f2e().toString());
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }
}
