package edu.jhu.thrax.hadoop.features.mapred;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.FeaturePair;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class RarityPenaltyFeature extends MapReduceFeature {

  public String getName() {
    return "rarity";
  }

  public Class<? extends WritableComparator> sortComparatorClass() {
    return RuleWritable.YieldComparator.class;
  }

  public Class<? extends Partitioner<RuleWritable, Writable>> partitionerClass() {
    return RuleWritable.YieldPartitioner.class;
  }

  public Class<? extends Mapper<RuleWritable, Annotation, RuleWritable, IntWritable>> mapperClass() {
    return Map.class;
  }

  public Class<? extends Reducer<RuleWritable, IntWritable, RuleWritable, FeaturePair>> reducerClass() {
    return Reduce.class;
  }

  private static class Map extends Mapper<RuleWritable, Annotation, RuleWritable, IntWritable> {

    protected void map(RuleWritable key, Annotation value, Context context)
        throws IOException, InterruptedException {
      context.write(key, new IntWritable(value.count()));
    }
  }

  private static class Reduce
      extends Reducer<RuleWritable, IntWritable, RuleWritable, FeaturePair> {
    private static final Text LABEL = new Text("RarityPenalty");

    protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      int count = 0;
      for (IntWritable x : values)
        count += x.get();
      context.write(key,
          new FeaturePair(LABEL, new DoubleWritable(Math.exp(1 - count))));
    }
  }

  private static final DoubleWritable ZERO = new DoubleWritable(0.0);

  public void unaryGlueRuleScore(Text nt, java.util.Map<Text, Writable> map) {
    map.put(Reduce.LABEL, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, java.util.Map<Text, Writable> map) {
    map.put(Reduce.LABEL, ZERO);
  }
}
