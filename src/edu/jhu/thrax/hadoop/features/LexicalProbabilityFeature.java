package edu.jhu.thrax.hadoop.features;

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

public class LexicalProbabilityFeature extends MapReduceFeature
{
    public LexicalProbabilityFeature()
    {
        super("lexprob");
    }

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
        private HashMap<TextPair,Double> f2e;
        private HashMap<TextPair,Double> e2f;
        private HashMap<RuleWritable,IntWritable> ruleCounts;

        private RuleWritable current;
        private double maxf2e;
        private double maxe2f;

        private static final double DEFAULT_PROB = 10e-7;

        private static final Text SGT_LABEL = new Text("LexprobSourceGivenTarget");
        private static final Text TGS_LABEL = new Text("LexprobTargetGivenSource");

        protected void setup(Context context) throws IOException, InterruptedException
        {
            current = new RuleWritable();
            ruleCounts = new HashMap<RuleWritable,IntWritable>();
            Configuration conf = context.getConfiguration();
            Path [] localFiles = DistributedCache.getLocalCacheFiles(conf);
            if (localFiles != null) {
                // we are in distributed mode
                f2e = readTable("lexprobs.f2e");
                e2f = readTable("lexprobs.e2f");
            }
            else {
                // in local mode; distributed cache does not work
                String localWorkDir = conf.getRaw("thrax_work");
                if (!localWorkDir.endsWith(Path.SEPARATOR))
                    localWorkDir += Path.SEPARATOR;
                f2e = readTable(localWorkDir + "lexprobs.f2e");
                e2f = readTable(localWorkDir + "lexprobs.e2f");
            }
        }

        protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
        {
            if (current == null || !key.sameYield(current)) {
                current.set(key);
                DoubleWritable tgsWritable = new DoubleWritable(-maxf2e);
                DoubleWritable sgtWritable = new DoubleWritable(-maxe2f);
                for (RuleWritable r : ruleCounts.keySet()) {
                    IntWritable cnt = ruleCounts.get(r);
                    r.features.put(TGS_LABEL, tgsWritable);
                    r.features.put(SGT_LABEL, sgtWritable);
                    r.featureLabel.set(TGS_LABEL);
                    r.featureScore.set(-maxf2e);
                    context.write(r, cnt);
                    RuleWritable r2 = new RuleWritable(r);
                    r2.featureLabel.set(SGT_LABEL);
                    r2.featureScore.set(-maxe2f);
                    context.write(r2, cnt);
                }
                ruleCounts.clear();
                maxe2f = sourceGivenTarget(key);
                maxf2e = targetGivenSource(key);
                int count = 0;
                for (IntWritable x : values)
                    count += x.get();
                ruleCounts.put(new RuleWritable(key), new IntWritable(count));
                return;
            }
            double sgt = sourceGivenTarget(key);
            double tgs = targetGivenSource(key);
            if (sgt > maxe2f)
                maxe2f = sgt;
            if (tgs > maxf2e)
                maxf2e = tgs;
            int count = 0;
            for (IntWritable x : values)
                count += x.get();
            ruleCounts.put(new RuleWritable(key), new IntWritable(count));
        }

        protected void cleanup(Context context) throws IOException, InterruptedException
        {
            DoubleWritable tgsWritable = new DoubleWritable(-maxf2e);
            DoubleWritable sgtWritable = new DoubleWritable(-maxe2f);
            for (RuleWritable r : ruleCounts.keySet()) {
                IntWritable cnt = ruleCounts.get(r);
                r.features.put(TGS_LABEL, tgsWritable);
                r.features.put(SGT_LABEL, sgtWritable);
                r.featureLabel.set(TGS_LABEL);
                r.featureScore.set(-maxf2e);
                context.write(r, cnt);
                RuleWritable r2 = new RuleWritable(r);
                r2.featureLabel.set(SGT_LABEL);
                r2.featureScore.set(-maxe2f);
                context.write(r2, cnt);
            }
        }

        private double sourceGivenTarget(RuleWritable r)
        {
            double result = 0;
            for (Text [] pairs : r.e2f.get()) {
                double len = Math.log(pairs.length - 1);
                result -= len;
                double prob = 0;
                Text tgt = pairs[0];
                TextPair tp = new TextPair(tgt, new Text());
                for (int j = 1; j < pairs.length; j++) {
                    tp.snd.set(pairs[j]);
                    Double currP = e2f.get(tp);
                    if (currP == null) {
                        System.err.println("WARNING: could not read word-level lexprob for pair ``" + tp + "''");
                        System.err.println(String.format("Assuming prob is %f", DEFAULT_PROB));
                        prob += DEFAULT_PROB;
                    }
                    else {
                        prob += currP;
                    }
                }
                result += Math.log(prob);
            }
            return result;
        }

        private double targetGivenSource(RuleWritable r)
        {
            double result = 0;
            for (Text [] pairs : r.f2e.get()) {
                double len = Math.log(pairs.length - 1);
                result -= len;
                double prob = 0;
                Text src = pairs[0];
                TextPair tp = new TextPair(src, new Text());
                for (int j = 1; j < pairs.length; j++) {
                    tp.snd.set(pairs[j]);
                    Double currP = f2e.get(tp);
                    if (currP == null) {
                        System.err.println("WARNING: could not read word-level lexprob for pair ``" + tp + "''");
                        System.err.println(String.format("Assuming prob is %f", DEFAULT_PROB));
                        prob += DEFAULT_PROB;
                    }
                    else {
                        prob += currP;
                    }
                }
                result += Math.log(prob);
            }
            return result;
        }

        private HashMap<TextPair,Double> readTable(String filename) throws IOException
        {
            HashMap<TextPair,Double> result = new HashMap<TextPair,Double>();
            Scanner scanner = new Scanner(new File(filename), "UTF-8");
            while (scanner.hasNextLine()) {
                String [] tokens = scanner.nextLine().split("\\s+");
                if (tokens.length != 3)
                    continue;
                TextPair tp = new TextPair(new Text(tokens[0]),
                                           new Text(tokens[1]));
                double score = Double.parseDouble(tokens[2]);
                result.put(tp, score);
            }
            return result;
        }
    }
}

