package edu.jhu.thrax.hadoop.distributional;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.jerboa.sim.SLSH;
import edu.jhu.jerboa.sim.Signature;

public class DistributionalContextReducer
    extends Reducer<Text, ContextWritable, SignatureWritable, NullWritable> {

  private int minCount;
  private SLSH slsh;

  public void setup(Context context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    minCount = conf.getInt("thrax.min-phrase-count", 3);

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
    Signature in_signature = new Signature();
    Signature out_signature = new Signature();
    int strength = 0;
    for (ContextWritable input : values) {
      if (!input.compacted.get())
        continue;
      strength += input.strength.get();
      in_signature.sums = (float[]) input.sums.get();
      slsh.updateSignature(out_signature, in_signature);
    }

    if (strength >= minCount) {
      slsh.buildSignature(out_signature, false);
      context.write(new SignatureWritable(key, out_signature, strength),
          NullWritable.get());
      slsh.clear();
    }

    return;
  }
}
