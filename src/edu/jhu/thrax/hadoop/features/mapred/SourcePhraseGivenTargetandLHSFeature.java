package edu.jhu.thrax.hadoop.features.mapred;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.thrax.hadoop.comparators.FieldComparator;
import edu.jhu.thrax.hadoop.comparators.PrimitiveArrayMarginalComparator;
import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.FeaturePair;
import edu.jhu.thrax.hadoop.datatypes.PrimitiveUtils;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

@SuppressWarnings("rawtypes")
public class SourcePhraseGivenTargetandLHSFeature extends MapReduceFeature {

  public String getName() {
    return "f_given_e_and_lhs";
  }

  public Class<? extends WritableComparator> sortComparatorClass() {
    return Comparator.class;
  }

  public Class<? extends Partitioner> partitionerClass() {
    return TargetandLHSPartitioner.class;
  }

  public Class<? extends Mapper> mapperClass() {
    return Map.class;
  }

  public Class<? extends Reducer> reducerClass() {
    return Reduce.class;
  }

  private static class Map extends Mapper<RuleWritable, Annotation, RuleWritable, IntWritable> {

    protected void map(RuleWritable key, Annotation value, Context context)
        throws IOException, InterruptedException {
      RuleWritable lhs_target_marginal = new RuleWritable(key);
      lhs_target_marginal.source = PrimitiveArrayMarginalComparator.MARGINAL;
      IntWritable count = new IntWritable(value.count());
      context.write(key, count);
      context.write(lhs_target_marginal, count);
    }
  }

  private static class Reduce
      extends Reducer<RuleWritable, IntWritable, RuleWritable, FeaturePair> {
    private int marginal;
    private static final Text NAME = new Text("p(f|e,LHS)");

    protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      if (Arrays.equals(key.source, PrimitiveArrayMarginalComparator.MARGINAL)) {
        // we only get here if it is the very first time we saw the LHS
        // and target combination
        marginal = 0;
        for (IntWritable x : values)
          marginal += x.get();
        return;
      }

      // control only gets here if we are using the same marginal
      int count = 0;
      for (IntWritable x : values) {
        count += x.get();
      }
      FloatWritable prob = new FloatWritable((float) -Math.log(count / (float) marginal));
      context.write(key, new FeaturePair(NAME, prob));
    }
  }

  public static class Comparator extends WritableComparator {

    private static final WritableComparator PARRAY_COMP = new PrimitiveArrayMarginalComparator();
    private static final FieldComparator SOURCE_COMP = new FieldComparator(0, PARRAY_COMP);
    private static final FieldComparator TARGET_COMP = new FieldComparator(1, PARRAY_COMP);

    public Comparator() {
      super(RuleWritable.class);
    }

    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
      try {
        int h1 = WritableUtils.decodeVIntSize(b1[s1 + 1]) + 1;
        int h2 = WritableUtils.decodeVIntSize(b2[s2 + 1]) + 1;

        int lhs1 = Math.abs(WritableComparator.readVInt(b1, s1 + 1));
        int lhs2 = Math.abs(WritableComparator.readVInt(b2, s2 + 1));
        int cmp = PrimitiveUtils.compare(lhs1, lhs2);
        if (cmp != 0) return cmp;

        cmp = TARGET_COMP.compare(b1, s1 + h1, l1 - h1, b2, s2 + h2, l2 - h2);
        if (cmp != 0) return cmp;

        cmp = PrimitiveUtils.compare(b1[s1], b2[s2]);
        if (cmp != 0) return cmp;

        return SOURCE_COMP.compare(b1, s1 + h1, l1 - h1, b2, s2 + h2, l2 - h2);
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  public static class TargetandLHSPartitioner extends Partitioner<RuleWritable, IntWritable> {
    public int getPartition(RuleWritable key, IntWritable value, int numPartitions) {
      int hash = 163;
      hash = 37 * hash + key.lhs;
      hash = 37 * hash + Arrays.hashCode(key.target);
      return (hash & Integer.MAX_VALUE) % numPartitions;
    }
  }

  private static final FloatWritable ZERO = new FloatWritable(0.0f);

  public void unaryGlueRuleScore(Text nt, java.util.Map<Text, Writable> map) {
    map.put(Reduce.NAME, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, java.util.Map<Text, Writable> map) {
    map.put(Reduce.NAME, ZERO);
  }
}
