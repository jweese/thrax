package edu.jhu.thrax.hadoop.distributional;

import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.jerboa.sim.SLSH;
import edu.jhu.jerboa.sim.Signature;

public class DistributionalContextCombiner
    extends Reducer<Text, ContextWritable, Text, ContextWritable> {

  private SLSH slsh;

  public void setup(Context context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    try {
      slsh = new SLSH();
      slsh.initialize(conf.getInt("thrax.lsh-num-bits", 256),
          conf.getInt("thrax.lsh-pool-size", 100000), conf.getInt("thrax.lsh-random-seed", 42));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return;
  }

  protected void reduce(Text key, Iterable<ContextWritable> values, Context context)
      throws IOException, InterruptedException {
    HashMap<String, Integer> output_map = new HashMap<String, Integer>();
    int strength = 0;
    for (ContextWritable input : values) {
      if (input.compacted.get())
        continue;
      for (Writable feature_text : input.map.keySet()) {
        String feature_string = ((Text) feature_text).toString();
        int feature_value = ((IntWritable) input.map.get(feature_text)).get();
        Integer current_value = output_map.get(feature_string);
        if (current_value != null)
          output_map.put(feature_string, current_value + feature_value);
        else
          output_map.put(feature_string, feature_value);
      }
      strength++;
    }

    Signature signature = new Signature();
    for (String feature_name : output_map.keySet()) {
      slsh.updateSignature(signature, feature_name, output_map.get(feature_name).doubleValue(), 1);
    }
    context.write(key, new ContextWritable(strength, signature.sums));
    return;
  }
}
