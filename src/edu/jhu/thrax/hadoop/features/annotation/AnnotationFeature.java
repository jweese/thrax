package edu.jhu.thrax.hadoop.features.annotation;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer.Context;

import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.jobs.ThraxJob;

public interface AnnotationFeature {

  @SuppressWarnings("rawtypes")
  public void init(Context context) throws IOException, InterruptedException;

  public Text getName();

  public Writable score(RuleWritable r, Annotation annotation);

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map);

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map);
  
  // TODO: move this into its own interface, have AF extend it.
  public Set<Class<? extends ThraxJob>> getPrerequisites();
}
