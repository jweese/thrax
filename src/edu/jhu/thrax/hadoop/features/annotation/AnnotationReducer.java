package edu.jhu.thrax.hadoop.features.annotation;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.FeaturePair;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class AnnotationReducer extends Reducer<RuleWritable, Annotation, RuleWritable, FeaturePair> {

  private List<AnnotationFeature> features;
  
  public AnnotationReducer() {}

  protected void setup(Context context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    features = AnnotationFeatureFactory.getAll(conf.get("thrax.features", ""));
  }

  protected void reduce(RuleWritable key, Iterable<Annotation> values, Context context)
      throws IOException, InterruptedException {
    for (Annotation annotation : values) {
      for (AnnotationFeature f : features) {
        context.write(key, new FeaturePair(f.getName(), f.score(key, annotation)));
      }
    }
  }
}