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
import edu.jhu.thrax.hadoop.features.WordLexicalProbabilityCalculator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TargetPhraseGivenSourceFeature extends MapReduceFeature
{
    public String getName()
    {
        return "f2ephrase";
    }

    public Class<? extends WritableComparator> sortComparatorClass()
    {
        return RuleWritable.TargetMarginalComparator.class;
    }

    public Class<? extends Partitioner> partitionerClass()
    {
        return RuleWritable.SourcePartitioner.class;
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
        private static final String MARGINAL_LHS = "[MARGINAL]";

        protected void map(RuleWritable key, IntWritable value, Context context) throws IOException, InterruptedException
        {
            RuleWritable marginal = new RuleWritable(key);
            marginal.target.set(WordLexicalProbabilityCalculator.MARGINAL);
            marginal.lhs.set(MARGINAL_LHS);
            context.write(key, value);
            context.write(marginal, value);
        }
    }

    private static class Reduce extends Reducer<RuleWritable, IntWritable, RuleWritable, NullWritable>
    {
        private int marginal;
        private static final Text NAME = new Text("TargetPhraseGivenSource");

        protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
        {
            if (key.target.equals(WordLexicalProbabilityCalculator.MARGINAL)) {
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
            key.featureLabel.set(NAME);
            key.featureScore.set(-Math.log(count / (double) marginal));
            context.write(key, NullWritable.get());
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

