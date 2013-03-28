package edu.jhu.thrax.hadoop.features.annotation;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer.Context;

import edu.jhu.thrax.hadoop.datatypes.AlignmentWritable;
import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.jobs.ThraxJob;

@SuppressWarnings("rawtypes")
public class AlignmentFeature implements AnnotationFeature {
  private static final Text LABEL = new Text("Alignment");
  private static final IntWritable ZERO = new IntWritable(0);

  public Text getName() {
    return LABEL;
  }

  public AlignmentWritable score(RuleWritable r, Annotation annotation) {
    return annotation.f2e();
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  @Override
  public void init(Context context) throws IOException, InterruptedException {}

  @Override
  public Set<Class<? extends ThraxJob>> getPrerequisites() {
    return null;
  }
}
