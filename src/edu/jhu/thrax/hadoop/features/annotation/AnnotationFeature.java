package edu.jhu.thrax.hadoop.features.annotation;

import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public interface AnnotationFeature {
  
  public Text getName();
  
  public Writable score(RuleWritable r, Annotation annotation);

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map);

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map);
}
