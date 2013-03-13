package edu.jhu.thrax.hadoop.paraphrasing;

import java.io.IOException;

import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.thrax.hadoop.datatypes.FeaturePair;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class FeatureCollectionReducer
    extends Reducer<RuleWritable, FeaturePair, RuleWritable, MapWritable> {

  protected void reduce(RuleWritable key, Iterable<FeaturePair> values, Context context)
      throws IOException, InterruptedException {
    MapWritable features = new MapWritable();
    for (FeaturePair fp : values)
      features.put(fp.key, fp.val);
    context.write(key, features);
  }
}
