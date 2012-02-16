package edu.jhu.thrax.hadoop.features.mapred;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Partitioner;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.comparators.TextMarginalComparator;
import edu.jhu.thrax.hadoop.comparators.TextFieldComparator;

import java.io.IOException;

public class TargetPhraseGivenSourceandLHSFeature extends MapReduceFeature
{
    public String getName()
    {
        return "e_given_f_and_lhs";
    }

    public Class<? extends WritableComparator> sortComparatorClass()
    {
        return Comparator.class;
    }

    public Class<? extends Partitioner> partitionerClass()
    {
        return SourceandLHSPartitioner.class;
    }

    public Class<? extends Mapper> mapperClass()
    {
        return Map.class;
    }

    public Class<? extends Reducer> reducerClass()
    {
        return Reduce.class;
    }

    private static class Map extends Mapper<RuleWritable, IntWritable, RuleWritable, IntWritable>
    {
        protected void map(RuleWritable key, IntWritable value, Context context) throws IOException, InterruptedException
        {
            RuleWritable marginal = new RuleWritable(key);
            marginal.target.set(TextMarginalComparator.MARGINAL);
            context.write(key, value);
            context.write(marginal, value);
        }
    }

    private static class Reduce extends Reducer<RuleWritable, IntWritable, RuleWritable, NullWritable>
    {
        private int marginal;
        private static final Text NAME = new Text("p(e|f,LHS)");

        protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
        {
            if (key.target.equals(TextMarginalComparator.MARGINAL)) {
                // we only get here if it is the very first time we saw the LHS
                // and source combination
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
            double prob = -Math.log(count / (double) marginal);
            key.featureLabel.set(NAME);
            key.featureScore.set(prob);
            context.write(key, NullWritable.get());
        }

    }

    public static class Comparator extends WritableComparator
    {
        private static final Text.Comparator TEXT_COMPARATOR = new Text.Comparator();
        private static final TextMarginalComparator MARGINAL_COMPARATOR = new TextMarginalComparator();
        private static final TextFieldComparator LHS_COMPARATOR = new TextFieldComparator(0, TEXT_COMPARATOR);
        private static final TextFieldComparator SOURCE_COMPARATOR = new TextFieldComparator(1, TEXT_COMPARATOR);
        private static final TextFieldComparator TARGET_COMPARATOR = new TextFieldComparator(2, MARGINAL_COMPARATOR);

        public Comparator()
        {
            super(RuleWritable.class);
        }

        public int compare(byte [] b1, int s1, int l1,
                           byte [] b2, int s2, int l2)
        {
            try {
                int cmp = LHS_COMPARATOR.compare(b1, s1, l1, b2, s2, l2);
                if (cmp != 0) {
                    return cmp;
                }
                cmp = SOURCE_COMPARATOR.compare(b1, s1, l1, b2, s2, l2);
                if (cmp != 0) {
                    return cmp;
                }
                return TARGET_COMPARATOR.compare(b1, s1, l1, b2, s2, l2);
            }
            catch (IOException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
    }

    public static class SourceandLHSPartitioner extends Partitioner<RuleWritable, IntWritable>
    {
        public int getPartition(RuleWritable key, IntWritable value, int numPartitions)
        {
            int hash = 163;
            hash = 37 * hash + key.lhs.hashCode();
            hash = 37 * hash + key.source.hashCode();
            return (hash & Integer.MAX_VALUE) % numPartitions;
        }
    }

    private static final DoubleWritable ZERO = new DoubleWritable(0.0);
    public void unaryGlueRuleScore(Text nt, java.util.Map<Text,Writable> map)
    {
        map.put(Reduce.NAME, ZERO);
    }

    public void binaryGlueRuleScore(Text nt, java.util.Map<Text,Writable> map)
    {
        map.put(Reduce.NAME, ZERO);
    }
}

