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

public class SourcePhraseGivenTargetFeature extends MapReduceFeature
{
    public SourcePhraseGivenTargetFeature()
    {
        super("phrasal-e2f");
    }

    public Class<? extends WritableComparator> sortComparatorClass()
    {
        return RuleWritable.SourceMarginalComparator.class;
    }

    public Class<? extends Partitioner> partitionerClass()
    {
        return RuleWritable.TargetPartitioner.class;
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
            marginal.source.set(WordLexicalProbabilityCalculator.MARGINAL);
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
        private static final Text NAME = new Text("SourcePhraseGivenTarget");

        protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
        {
            if (key.source.equals(WordLexicalProbabilityCalculator.MARGINAL)) {
                DoubleWritable result = new DoubleWritable(-Math.log(count / (double) marginal));
                for (RuleWritable r : rules.keySet()) {
                    IntWritable cnt = rules.get(r);
                    r.featureLabel.set(NAME);
                    r.featureScore.set(-Math.log(count / (double) marginal));
                    context.write(r, cnt);
                }
                currentSource.set(WordLexicalProbabilityCalculator.MARGINAL);
                if (!key.target.equals(currentTarget)) {
                    marginal = 0;
                    count = 0;
                    currentTarget.set(key.target);
                    rules.clear();
                }
                for (IntWritable x : values)
                    marginal += x.get();
                return;
            }
            
            // control only gets here if we are using the same marginal
            if (!key.source.equals(currentSource)) {
                DoubleWritable result = new DoubleWritable(-Math.log(count / (double) marginal));
                for (RuleWritable r : rules.keySet()) {
                    IntWritable cnt = rules.get(r);
                    r.featureLabel.set(NAME);
                    r.featureScore.set(-Math.log(count / (double) marginal));
                    context.write(r, cnt);
                }
                rules.clear();
                count = 0;
                currentSource.set(key.source);
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
                IntWritable cnt = rules.get(r);
                r.featureLabel.set(NAME);
                r.featureScore.set(-Math.log(count / (double) marginal));
                context.write(r, cnt);
            }
        }
    }
}

