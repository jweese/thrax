package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.io.Text;

import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public interface RuleWritableExtractor {
  public Iterable<AnnotatedRule> extract(Text line);
}


class AnnotatedRule {
  public RuleWritable rule;
  public Annotation annotation;

  public AnnotatedRule(RuleWritable r, Annotation a) {
    rule = r;
    annotation = a;
  }
}
