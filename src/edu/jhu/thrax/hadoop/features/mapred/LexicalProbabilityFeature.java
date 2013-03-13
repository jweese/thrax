package edu.jhu.thrax.hadoop.features.mapred;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.thrax.hadoop.datatypes.AlignmentWritable;
import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.FeaturePair;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.jobs.SourceWordGivenTargetWordProbabilityJob;
import edu.jhu.thrax.hadoop.jobs.TargetWordGivenSourceWordProbabilityJob;
import edu.jhu.thrax.hadoop.jobs.ThraxJob;
import edu.jhu.thrax.lexprob.DoubleTrieTable;
import edu.jhu.thrax.lexprob.LexicalProbabilityTable;
import edu.jhu.thrax.lexprob.TrieLexprobTable;
import edu.jhu.thrax.util.Vocabulary;

@SuppressWarnings("rawtypes")
public class LexicalProbabilityFeature extends MapReduceFeature {

  public String getName() {
    return "lexprob";
  }

  public Class<? extends Reducer> combinerClass() {
    return Reducer.class;
  }

  public Class<? extends Mapper> mapperClass() {
    return Mapper.class;
  }

  public Class<? extends WritableComparator> sortComparatorClass() {
    return RuleWritable.YieldComparator.class;
  }

  public Class<? extends Partitioner<RuleWritable, Writable>> partitionerClass() {
    return RuleWritable.YieldPartitioner.class;
  }

  public Class<? extends Reducer<RuleWritable, Annotation, RuleWritable, FeaturePair>> reducerClass() {
    return Reduce.class;
  }

  protected void setMapOutputFormat(Job job) {
    job.setMapOutputKeyClass(RuleWritable.class);
    job.setMapOutputValueClass(Annotation.class);
  }

  private static class Reduce extends Reducer<RuleWritable, Annotation, RuleWritable, FeaturePair> {

    private LexicalProbabilityTable table;

    private static final double DEFAULT_PROB = 10e-7;

    private static final Text SGT_LABEL = new Text("Lex(f|e)");
    private static final Text TGS_LABEL = new Text("Lex(e|f)");

    protected void setup(Context context) throws IOException, InterruptedException {
      Configuration conf = context.getConfiguration();
      String vocabulary_path = conf.getRaw("thrax.work-dir") + "vocabulary/part-r-00000";

      Vocabulary.read(conf, vocabulary_path);

      String workDir = conf.getRaw("thrax.work-dir");
      String e2fpath = workDir + "lexprobse2f/part-*";
      String f2epath = workDir + "lexprobsf2e/part-*";

      TrieLexprobTable e2f = new TrieLexprobTable(conf, e2fpath);
      context.progress();
      TrieLexprobTable f2e = new TrieLexprobTable(conf, f2epath);
      context.progress();
      table = new DoubleTrieTable(e2f, f2e);
    }

    protected void reduce(RuleWritable key, Iterable<Annotation> values, Context context)
        throws IOException, InterruptedException {
      for (Annotation annotation : values) {
        double sgt = sourceGivenTarget(key, annotation.f2e());
        double tgs = targetGivenSource(key, annotation.e2f());
        context.write(key, new FeaturePair(SGT_LABEL, new DoubleWritable(-sgt)));
        context.write(key, new FeaturePair(TGS_LABEL, new DoubleWritable(-tgs)));
      }
      context.progress();
    }

    private double sourceGivenTarget(RuleWritable rule, AlignmentWritable f2e) {
      byte[] points = f2e.points;
      int[] source = rule.source;
      int[] target = rule.target;

      double total = 0, prob = 0;
      int prev = -1;
      int n = points.length / 2;
      int m = 0;
      for (int i = 0; i < n; ++i) {
        int f = points[2 * i];
        int e = points[2 * i + 1];
        if (f != prev && prev != -1) {
          total += Math.log(prob) - Math.log(m);
          prob = 0;
          m = 0;
        }
        prev = f;
        m++;
        double p = table.logpSourceGivenTarget(source[f], target[e]);
        prob += (p < 0 ? DEFAULT_PROB : p);
        if (p < 0)
          System.err.printf("WARNING: could not read lexprob p(%s|%s)\n",
              Vocabulary.word(source[f]), Vocabulary.word(target[e]));
      }
      total += Math.log(prob) - Math.log(m);
      return total;
    }

    private double targetGivenSource(RuleWritable rule, AlignmentWritable e2f) {
      byte[] points = e2f.points;
      int[] source = rule.source;
      int[] target = rule.target;

      double total = 0, prob = 0;
      int prev = -1;
      int n = points.length / 2;
      int m = 0;
      for (int i = 0; i < n; ++i) {
        int e = points[2 * i];
        int f = points[2 * i + 1];
        if (e != prev && prev != -1) {
          total += Math.log(prob) - Math.log(m);
          prob = 0;
          m = 0;
        }
        prev = e;
        m++;
        double p = table.logpTargetGivenSource(source[f], target[e]);
        prob += (p < 0 ? DEFAULT_PROB : p);
        if (p <= 0)
          System.err.printf("WARNING: could not read lexprob p(%s|%s)\n",
              Vocabulary.word(target[e]), Vocabulary.word(source[f]));
      }
      total += Math.log(prob) - Math.log(m);
      return total;
    }
  }

  public Set<Class<? extends ThraxJob>> getPrerequisites() {
    Set<Class<? extends ThraxJob>> pqs = super.getPrerequisites();
    pqs.add(TargetWordGivenSourceWordProbabilityJob.class);
    pqs.add(SourceWordGivenTargetWordProbabilityJob.class);
    return pqs;
  }

  private static final DoubleWritable ONE_PROB = new DoubleWritable(0.0);

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(Reduce.SGT_LABEL, ONE_PROB);
    map.put(Reduce.TGS_LABEL, ONE_PROB);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(Reduce.SGT_LABEL, ONE_PROB);
    map.put(Reduce.TGS_LABEL, ONE_PROB);
  }
}
