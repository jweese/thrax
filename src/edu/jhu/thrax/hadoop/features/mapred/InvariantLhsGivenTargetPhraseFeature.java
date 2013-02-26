package edu.jhu.thrax.hadoop.features.mapred;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
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
import edu.jhu.thrax.util.FormatUtils;

@SuppressWarnings("rawtypes")
public class InvariantLhsGivenTargetPhraseFeature extends MapReduceFeature {

  public String getName() {
    return "lhs_given_e_inv";
  }

  public Class<? extends WritableComparator> sortComparatorClass() {
    return Comparator.class;
  }

  public Class<? extends Partitioner> partitionerClass() {
    return RuleWritable.TargetPartitioner.class;
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
      RuleWritable target_marginal = new RuleWritable(key);
      RuleWritable lhs_target_marginal = new RuleWritable(key);
      RuleWritable invariant_key = new RuleWritable(key);

      boolean monotonic = true;
      boolean seen_first = false;
      int[] zeroed = new int[key.target.length];
      for (int i = 0; i < zeroed.length; ++i) {
        if (key.target[i] < 0) {
          zeroed[i] = -1;
          if (key.target[i] == -1) seen_first = true;
          if (key.target[i] == -2 && !seen_first) monotonic = false;
        } else {
          zeroed[i] = key.target[i];
        }
      }

      target_marginal.target = zeroed;
      target_marginal.source = PrimitiveArrayMarginalComparator.MARGINAL;
      target_marginal.lhs = PrimitiveUtils.MARGINAL_ID;

      lhs_target_marginal.target = zeroed;
      lhs_target_marginal.source = PrimitiveArrayMarginalComparator.MARGINAL;

      invariant_key.target = zeroed;

      IntWritable count = new IntWritable(value.count());

      context.write(invariant_key, new IntWritable(monotonic ? 1 : 60000));
      context.write(lhs_target_marginal, count);
      context.write(target_marginal, count);
    }
  }

  private static class Reduce
      extends Reducer<RuleWritable, IntWritable, RuleWritable, FeaturePair<DoubleWritable>> {

    private int marginal;
    private DoubleWritable prob;
    private static final Text NAME = new Text("p(LHS|e_inv)");

    protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      if (key.lhs == PrimitiveUtils.MARGINAL_ID) {
        // We only get here if it is the very first time we saw the source.
        marginal = 0;
        for (IntWritable c : values)
          marginal += c.get();
        return;
      }

      // Control only gets here if we are using the same marginal.
      if (key.source.equals(PrimitiveArrayMarginalComparator.MARGINAL)) {
        // We only get in here if it's a new LHS.
        int count = 0;
        for (IntWritable c : values)
          count += c.get();
        prob = new DoubleWritable(-Math.log(count / (double) marginal));
        return;
      }
      RuleWritable result = new RuleWritable(key);


      for (IntWritable s : values) {
        int signal = s.get();
        if (signal % 60000 >= 1) {
          result.target = FormatUtils.applyIndices(key.target, true);
          context.write(result, new FeaturePair<DoubleWritable>(NAME, prob));
        }
        if (signal / 60000 >= 1) {
          result.target = FormatUtils.applyIndices(key.target, false);
          context.write(result, new FeaturePair<DoubleWritable>(NAME, prob));
        }

      }
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

        int cmp = TARGET_COMP.compare(b1, s1 + h1, l1 - h1, b2, s2 + h2, l2 - h2);
        if (cmp != 0) return cmp;

        int lhs1 = WritableComparator.readVInt(b1, s1 + 1);
        int lhs2 = WritableComparator.readVInt(b2, s2 + 1);
        cmp = Integer.compare(lhs1, lhs2);
        if (cmp != 0) return cmp;

        return SOURCE_COMP.compare(b1, s1 + h1, l1 - h1, b2, s2 + h2, l2 - h2);
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  private static final DoubleWritable ZERO = new DoubleWritable(0.0);

  public void unaryGlueRuleScore(Text nt, java.util.Map<Text, Writable> map) {
    map.put(Reduce.NAME, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, java.util.Map<Text, Writable> map) {
    map.put(Reduce.NAME, ZERO);
  }
}
