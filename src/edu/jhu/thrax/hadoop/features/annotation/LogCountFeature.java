package edu.jhu.thrax.hadoop.features.annotation;

import java.util.Set;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer.Context;

import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.jobs.ThraxJob;

@SuppressWarnings("rawtypes")
public class LogCountFeature implements AnnotationFeature {

  private static final Text LABEL = new Text("LogCount");
  private static final FloatWritable ZERO = new FloatWritable(0);

  public Text getName() {
    return LABEL;
  }

  public void unaryGlueRuleScore(Text nt, java.util.Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, java.util.Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  @Override
  public Writable score(RuleWritable r, Annotation annotation) {
    return new FloatWritable((float) Math.log(annotation.count()));
  }

  @Override
  public void init(Context context) {}

  @Override
  public Set<Class<? extends ThraxJob>> getPrerequisites() {
    return null;
  }
}
