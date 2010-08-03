package edu.jhu.thrax.hadoop.features;

import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.Configuration;

import edu.jhu.thrax.hadoop.datatypes.TextPair;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class LexicalProbabilityFeature extends Feature
{
    public static final String name = "lex";

    public Class<? extends Mapper<RuleWritable, IntWritable, RuleWritable, IntWritable>> mapperClass()
    {
        return Map.class;
    }

    public Class<? extends WritableComparator> sortComparatorClass()
    {
        return RuleWritable.YieldComparator.class;
    }

    public Class<? extends Partitioner<RuleWritable, IntWritable>> partitionerClass()
    {
        return RuleWritable.YieldPartitioner.class;
    }

    private static class Map extends Mapper<RuleWritable, IntWritable, RuleWritable, IntWritable>
    {
        private HashMap<TextPair,Double> f2e;
        private HashMap<TextPair,Double> e2f;
        private HashMap<RuleWritable,IntWritable> ruleCounts;

        private RuleWritable current;
        private double maxf2e;
        private double maxe2f;

        private static final Text SGT_LABEL = new Text("LexprobSourceGivenTarget");
        private static final Text TGS_LABEL = new Text("LexprobTargetGivenSource");

        protected void setup(Context context) throws IOException, InterruptedException
        {
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

        protected void map(RuleWritable key, IntWritable value, Context context) throws IOException, InterruptedException
        {
            if (current != null && !key.sameYield(current)) {
                DoubleWritable tgsWritable = new DoubleWritable(-maxf2e);
                DoubleWritable sgtWritable = new DoubleWritable(-maxe2f);
                for (RuleWritable r : ruleCounts.keySet()) {
                    r.features.put(TGS_LABEL, tgsWritable);
                    r.features.put(SGT_LABEL, sgtWritable);
                    context.write(r, ruleCounts.get(r));
                }
                ruleCounts.clear();
                maxf2e = targetGivenSource(key);
                maxe2f = sourceGivenTarget(key);
                ruleCounts.put(key, value);
                return;
            }
            double sgt = sourceGivenTarget(key);
            double tgs = targetGivenSource(key);
            if (sgt > maxe2f)
                maxe2f = sgt;
            if (tgs > maxf2e)
                maxf2e = tgs;
            ruleCounts.put(key, value);
        }

        protected void cleanup(Context context) throws IOException, InterruptedException
        {
            DoubleWritable tgsWritable = new DoubleWritable(-maxf2e);
            DoubleWritable sgtWritable = new DoubleWritable(-maxe2f);
            for (RuleWritable r : ruleCounts.keySet()) {
                r.features.put(TGS_LABEL, tgsWritable);
                r.features.put(SGT_LABEL, sgtWritable);
                context.write(r, ruleCounts.get(r));
            }
        }

        private double sourceGivenTarget(RuleWritable r)
        {
            double result = 0;
            for (Text [] pairs : (Text [][]) r.e2f.toArray()) {
                double len = Math.log(pairs.length - 1);
                result -= len;
                Text tgt = pairs[0];
                TextPair tp = new TextPair(tgt, new Text());
                for (int j = 1; j < pairs.length; j++) {
                    tp.snd.set(pairs[j]);
                    double prob = e2f.get(tp);
                    result += prob;
                }
            }
            return result;
        }

        private double targetGivenSource(RuleWritable r)
        {
            double result = 0;
            for (Text [] pairs : (Text [][]) r.f2e.toArray()) {
                double len = Math.log(pairs.length - 1);
                result -= len;
                Text src = pairs[0];
                TextPair tp = new TextPair(src, new Text());
                for (int j = 1; j < pairs.length; j++) {
                    tp.snd.set(pairs[j]);
                    double prob = f2e.get(tp);
                    result += prob;
                }
            }
            return result;
        }

        private HashMap<TextPair,Double> readTable(String filename) throws IOException
        {
            HashMap<TextPair,Double> result = new HashMap<TextPair,Double>();
            Scanner scanner = new Scanner(new File(filename));
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

