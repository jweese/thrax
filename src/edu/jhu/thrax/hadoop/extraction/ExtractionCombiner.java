package edu.jhu.thrax.hadoop.extraction;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.util.Vocabulary;

public class ExtractionCombiner
    extends Reducer<RuleWritable, Annotation, RuleWritable, Annotation> {

  private int minCount;

  protected void setup(Context context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    minCount = conf.getInt("thrax.min-rule-count", 1);
  }

  protected void reduce(RuleWritable key, Iterable<Annotation> values, Context context)
      throws IOException, InterruptedException {
    context.progress();
    Annotation merged = null;
    for (Annotation a : values) {
      if (merged == null)
        merged = a;
      else
        merged.merge(a);
    }
    if (merged.count() >= minCount || isUnigramRule(key)) context.write(key, merged);
  }

  private static boolean isUnigramRule(RuleWritable rule) {
    if (rule.source.length == 1) return !Vocabulary.nt(rule.source[0]);
    return rule.target.length == 1 && !Vocabulary.nt(rule.target[0]);
  }
}
