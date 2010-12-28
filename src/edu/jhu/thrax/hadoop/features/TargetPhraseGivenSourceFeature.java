package edu.jhu.thrax.hadoop.features;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Partitioner;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import java.io.IOException;
import java.util.HashMap;

public class TargetPhraseGivenSourceFeature extends Feature
{
    public TargetPhraseGivenSourceFeature()
    {
        super("phrasal-f2e");
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
        protected void map(RuleWritable key, IntWritable value, Context context) throws IOException, InterruptedException
        {
            RuleWritable marginal = new RuleWritable(key);
            marginal.target.set(WordLexicalProbabilityCalculator.MARGINAL);
            context.write(key, value);
            context.write(marginal, value);
        }
    }

    private static class Reduce extends Reducer<RuleWritable, IntWritable, RuleWritable, IntWritable>
    {
        private Text currentTarget = new Text();
        private Text currentSource = new Text();
        private HashMap<RuleWritable,IntWritable> rules = new HashMap<RuleWritable,IntWritable>();
        private int marginal;
        private int count;
        private static final Text NAME = new Text("TargetPhraseGivenSource");

        protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
        {
            if (key.target.equals(WordLexicalProbabilityCalculator.MARGINAL)) {
                DoubleWritable result = new DoubleWritable(-Math.log(count / (double) marginal));
                for (RuleWritable r : rules.keySet()) {
                    r.features.put(NAME, result);
                    context.write(r, rules.get(r));
                }
                currentTarget.set(WordLexicalProbabilityCalculator.MARGINAL);
                if (!key.source.equals(currentSource)) {
                    marginal = 0;
                    count = 0;
                    currentSource.set(key.source);
                    rules.clear();
                }
                for (IntWritable x : values)
                    marginal += x.get();
                return;
            }
            
            // control only gets here if we are using the same marginal
            if (!key.target.equals(currentTarget)) {
                DoubleWritable result = new DoubleWritable(-Math.log(count / (double) marginal));
                for (RuleWritable r : rules.keySet()) {
                    r.features.put(NAME, result);
                    context.write(r, rules.get(r));
                }
                rules.clear();
                count = 0;
                currentTarget.set(key.target);
            }
            int myCount = 0;
            for (IntWritable x : values) {
                myCount += x.get();
                count += x.get();
            }
            rules.put(new RuleWritable(key), new IntWritable(myCount));
        }

        protected void cleanup(Context context) throws IOException, InterruptedException
        {
            DoubleWritable result = new DoubleWritable(-Math.log(count / (double) marginal));
            for (RuleWritable r : rules.keySet()) {
                r.features.put(NAME, result);
                context.write(r, rules.get(r));
            }
        }
    }
}

