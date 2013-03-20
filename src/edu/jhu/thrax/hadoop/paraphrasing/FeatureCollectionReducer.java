package edu.jhu.thrax.hadoop.paraphrasing;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.thrax.hadoop.datatypes.FeaturePair;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.util.Vocabulary;

public class FeatureCollectionReducer
    extends Reducer<RuleWritable, FeaturePair, RuleWritable, MapWritable> {

  protected void setup(Context context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    String vocabulary_path = conf.getRaw("thrax.work-dir") + "vocabulary/part-r-00000";
    Vocabulary.read(conf, vocabulary_path);
  }

  protected void reduce(RuleWritable key, Iterable<FeaturePair> values, Context context)
      throws IOException, InterruptedException {
    MapWritable features = new MapWritable();
    for (FeaturePair fp : values)
      features.put(new Text(fp.key), fp.val.get());

    System.err.println("PIVOTING: " + key.toString());
    for (Writable k : features.keySet()) {
      System.err.println("FEAT: " + ((Text) k).toString() + " = "
          + features.get(k).toString());
    }

    context.write(key, features);
  }
}
