package edu.jhu.thrax.hadoop.features.mapred;

import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.io.SequenceFile;

import edu.jhu.thrax.hadoop.datatypes.TextPair;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.jobs.TargetWordGivenSourceWordProbabilityJob;
import edu.jhu.thrax.hadoop.jobs.SourceWordGivenTargetWordProbabilityJob;
import edu.jhu.thrax.hadoop.jobs.ThraxJob;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.io.IOException;

public class LexicalProbabilityFeature extends MapReduceFeature
{
    public String getName()
    {
        return "lexprob";
    }

    public Class<? extends Mapper> mapperClass()
    {
        return Mapper.class;
    }

    public Class<? extends WritableComparator> sortComparatorClass()
    {
        return RuleWritable.YieldAndAlignmentComparator.class;
    }

    public Class<? extends Partitioner<RuleWritable, IntWritable>> partitionerClass()
    {
        return RuleWritable.YieldPartitioner.class;
    }

    public Class<? extends Reducer<RuleWritable, IntWritable, RuleWritable, NullWritable>> reducerClass()
    {
        return Reduce.class;
    }

    private static class Reduce extends Reducer<RuleWritable, IntWritable, RuleWritable, NullWritable>
    {
        private HashMap<TextPair,Double> f2e;
        private HashMap<TextPair,Double> e2f;

        private RuleWritable current;
        private double maxf2e;
        private double maxe2f;

        private static final double DEFAULT_PROB = 10e-7;

        private static final Text SGT_LABEL = new Text("Lex(f|e)");
        private static final Text TGS_LABEL = new Text("Lex(e|f)");

        protected void setup(Context context) throws IOException, InterruptedException
        {
            Configuration conf = context.getConfiguration();
//            Path [] localFiles = DistributedCache.getLocalCacheFiles(conf);
//            if (localFiles != null) {
                // we are in distributed mode
//                f2e = readTable("lexprobs.f2e");
//                e2f = readTable("lexprobs.e2f");
//            }
//            else {
                // in local mode; distributed cache does not work
//                String localWorkDir = conf.getRaw("thrax_work");
//                if (!localWorkDir.endsWith(Path.SEPARATOR))
//                    localWorkDir += Path.SEPARATOR;
//                f2e = readTable(localWorkDir + "lexprobs.f2e");
//                e2f = readTable(localWorkDir + "lexprobs.e2f");
//            }
            String workDir = conf.getRaw("thrax.work-dir");
            String e2fpath = workDir + "lexprobse2f/part-*";
            String f2epath = workDir + "lexprobsf2e/part-*";

            FileStatus [] e2ffiles = FileSystem.get(conf).globStatus(new Path(e2fpath));
            if (e2ffiles.length == 0)
                throw new IOException("no files found in e2f word level lexprob glob: " + e2fpath);
            FileStatus [] f2efiles = FileSystem.get(conf).globStatus(new Path(f2epath));
            if (e2ffiles.length == 0)
                throw new IOException("no files found in f2e word level lexprob glob: " + f2epath);

            e2f = readWordLexprobTable(e2ffiles, conf);
            f2e = readWordLexprobTable(f2efiles, conf);
        }

        protected void reduce(RuleWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
        {
            if (current == null) {
                current = new RuleWritable(key);
                maxe2f = sourceGivenTarget(key);
                maxf2e = targetGivenSource(key);
                return;
            }
            if (!key.sameYield(current)) {
                current.featureLabel.set(TGS_LABEL);
                current.featureScore.set(-maxf2e);
                context.write(current, NullWritable.get());
                current.featureLabel.set(SGT_LABEL);
                current.featureScore.set(-maxe2f);
                context.write(current, NullWritable.get());
                current.set(key);
                maxe2f = sourceGivenTarget(key);
                maxf2e = targetGivenSource(key);
            }

            double sgt = sourceGivenTarget(key);
            double tgs = targetGivenSource(key);
            if (sgt > maxe2f)
                maxe2f = sgt;
            if (tgs > maxf2e)
                maxf2e = tgs;
        }

        protected void cleanup(Context context) throws IOException, InterruptedException
        {
            if (current == null) {
                System.err.println("Lexical probability cleanup(): current was null");
                System.err.println("Lexical probability cleanup(): there may be no output from this reducer");
                return;
            }
            current.featureLabel.set(TGS_LABEL);
            current.featureScore.set(-maxf2e);
            context.write(current, NullWritable.get());
            current.featureLabel.set(SGT_LABEL);
            current.featureScore.set(-maxe2f);
            context.write(current, NullWritable.get());
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

        private HashMap<TextPair,Double> readWordLexprobTable(FileStatus [] files, Configuration conf) throws IOException
        {
            HashMap<TextPair,Double> result = new HashMap<TextPair,Double>();
            for (FileStatus stat : files) {
                SequenceFile.Reader reader = new SequenceFile.Reader(FileSystem.get(conf), stat.getPath(), conf);
                TextPair tp = new TextPair();
                DoubleWritable d = new DoubleWritable(0.0);
                while (reader.next(tp, d)) {
                    Text car = new Text(tp.fst);
                    Text cdr = new Text(tp.snd);
                    TextPair entry = new TextPair(car, cdr);
                    result.put(entry , d.get());
                }
            }
            return result;
        }
    }

    public Set<Class<? extends ThraxJob>> getPrerequisites()
    {
        Set<Class<? extends ThraxJob>> pqs = super.getPrerequisites();
        pqs.add(TargetWordGivenSourceWordProbabilityJob.class);
        pqs.add(SourceWordGivenTargetWordProbabilityJob.class);
        return pqs;
    }

    private static final DoubleWritable ONE_PROB = new DoubleWritable(0.0);
    public void unaryGlueRuleScore(Text nt, Map<Text,Writable> map)
    {
        map.put(Reduce.SGT_LABEL, ONE_PROB);
        map.put(Reduce.TGS_LABEL, ONE_PROB);
    }

    public void binaryGlueRuleScore(Text nt, Map<Text,Writable> map)
    {
        map.put(Reduce.SGT_LABEL, ONE_PROB);
        map.put(Reduce.TGS_LABEL, ONE_PROB);
    }
}

