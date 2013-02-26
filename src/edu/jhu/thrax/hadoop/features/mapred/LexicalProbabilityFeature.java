package edu.jhu.thrax.hadoop.features.mapred;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparator;
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

  public Class<? extends Mapper> mapperClass() {
    return Mapper.class;
  }

  public Class<? extends WritableComparator> sortComparatorClass() {
    return RuleWritable.YieldComparator.class;
  }

  public Class<? extends Partitioner<RuleWritable, Writable>> partitionerClass() {
    return RuleWritable.YieldPartitioner.class;
  }

  public Class<? extends Reducer<RuleWritable, Annotation, RuleWritable, FeaturePair<DoubleWritable>>> reducerClass() {
    return Reduce.class;
  }

  private static class Reduce
      extends Reducer<RuleWritable, Annotation, RuleWritable, FeaturePair<DoubleWritable>> {

    private LexicalProbabilityTable table;

    private static final double DEFAULT_PROB = 10e-7;

    private static final Text SGT_LABEL = new Text("Lex(f|e)");
    private static final Text TGS_LABEL = new Text("Lex(e|f)");

    protected void setup(Context context) throws IOException, InterruptedException {
      Configuration conf = context.getConfiguration();
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
      // TODO: check for multiple maps? shouldn't happen.
      for (Annotation annotation : values) {
        double sgt = sourceGivenTarget(key, annotation.e2f());
        double tgs = targetGivenSource(key, annotation.f2e());
        context.write(key, new FeaturePair<DoubleWritable>(SGT_LABEL, new DoubleWritable(-sgt)));
        context.write(key, new FeaturePair<DoubleWritable>(TGS_LABEL, new DoubleWritable(-tgs)));
        context.progress();
      }
    }

    private double sourceGivenTarget(RuleWritable rule, AlignmentWritable e2f) {
      byte[] points = e2f.points;
      int[] source = rule.source;
      int[] target = rule.target;

      int prev = -1;
      int n = points.length / 2;
      int m = 0;
      double total = 0, prob = 0;
      for (int i = 0; i < n; ++i) {
        int e = points[2 * i];
        int f = points[2 * i + 1];
        if (e != prev && prev != -1) {
          total += Math.log(prob / (double) m);
          prob = 0;
          m = 0;
          continue;
        }
        m++;
        double p = table.logpSourceGivenTarget(source[f], target[e]);
        if (p < 0) {
          System.err.printf("WARNING: could not read lexprob p(%s|%s)\n",
              Vocabulary.word(source[f]), Vocabulary.word(target[e]));
          prob += DEFAULT_PROB;
        } else {
          prob += p;
        }
        prev = e;
      }
      return total;
    }

    private double targetGivenSource(RuleWritable rule, AlignmentWritable f2e) {
      byte[] points = f2e.points;
      int[] source = rule.source;
      int[] target = rule.target;

      int prev = -1;
      int n = points.length / 2;
      int m = 0;
      double total = 0, prob = 0;
      for (int i = 0; i < n; ++i) {
        int f = points[2 * i];
        int e = points[2 * i + 1];
        if (e != prev && prev != -1) {
          total += Math.log(prob / (double) m);
          prob = 0;
          m = 0;
          continue;
        }
        m++;
        double p = table.logpTargetGivenSource(target[e], source[f]);
        if (p < 0) {
          System.err.printf("WARNING: could not read lexprob p(%s|%s)\n",
              Vocabulary.word(source[f]), Vocabulary.word(target[e]));
          prob += DEFAULT_PROB;
        } else {
          prob += p;
        }
        prev = e;
      }
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
