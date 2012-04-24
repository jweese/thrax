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
    ContextWritable reduced = new ContextWritable();
    for (ContextWritable input : values) {
      reduced.merge(input, slsh);
    }
    if (!reduced.compacted.get()) reduced.compact(slsh);
    if (reduced.strength.get() >= minCount) {
      Signature reduced_signature = new Signature();
      reduced_signature.sums = (float[]) reduced.sums.get();
      slsh.buildSignature(reduced_signature, false);
      context.write(new SignatureWritable(key, reduced_signature, reduced.strength.get()),
          NullWritable.get());
    }
    return;
  }
}
