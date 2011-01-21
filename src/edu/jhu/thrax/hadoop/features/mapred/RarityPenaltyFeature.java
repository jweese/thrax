package edu.jhu.thrax.hadoop.features.mapred;

import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.Configuration;

import edu.jhu.thrax.hadoop.datatypes.TextPair;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class RarityPenaltyFeature extends MapReduceFeature
{
    public Class<? extends Mapper> mapperClass()
    {
        return Mapper.class;
    }

    public Class<? extends WritableComparator> sortComparatorClass()
    {
        return RuleWritable.YieldComparator.class;
    }

    public Class<? extends Partitioner<RuleWritable, IntWritable>> partitionerClass()
    {
        return RuleWritable.YieldPartitioner.class;
    }

    public Class<? extends Reducer<RuleWritable, IntWritable, RuleWritable, IntWritable>> reducerClass()
    {
        return Reduce.class;
    }

    private static class Reduce extends Reducer<RuleWritable, IntWritable, RuleWritable, IntWritable>
    {
        private HashMap<RuleWritable,IntWritable> ruleCounts;
        private int totalCount;

        private RuleWritable current;

        private static final Text LABEL = new Text("RarityPenalty");

        protected void setup(Context context) throws IOException, InterruptedException
        {
            current = new RuleWritable();
            ruleCounts = new HashMap<RuleWritable,IntWritable>();
            totalCount = 0;
        }

        protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
        {
            if (current == null || !key.sameYield(current)) {
                current.set(key);
                DoubleWritable score = new DoubleWritable(Math.exp(1 - totalCount));
                for (RuleWritable r : ruleCounts.keySet()) {
                    IntWritable cnt = ruleCounts.get(r);
                    r.featureLabel.set(LABEL);
                    r.featureScore.set(Math.exp(1 - totalCount));
                    context.write(r, cnt);
                }
                ruleCounts.clear();
                int count = 0;
                for (IntWritable x : values)
                    count += x.get();
                ruleCounts.put(new RuleWritable(key), new IntWritable(count));
                totalCount = count;
                return;
            }
            int count = 0;
            for (IntWritable x : values)
                count += x.get();
            ruleCounts.put(new RuleWritable(key), new IntWritable(count));
            totalCount += count;
            return;
        }

        protected void cleanup(Context context) throws IOException, InterruptedException
        {
            DoubleWritable score = new DoubleWritable(Math.exp(1 - totalCount));
            for (RuleWritable r : ruleCounts.keySet()) {
                IntWritable cnt = ruleCounts.get(r);
                r.featureLabel.set(LABEL);
                r.featureScore.set(Math.exp(1 - totalCount));
                context.write(r, cnt);
            }
        }

    }
}

