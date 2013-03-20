package edu.jhu.thrax.hadoop.features.annotation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer.Context;

import edu.jhu.thrax.hadoop.datatypes.AlignmentWritable;
import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.jobs.SourceWordGivenTargetWordProbabilityJob;
import edu.jhu.thrax.hadoop.jobs.TargetWordGivenSourceWordProbabilityJob;
import edu.jhu.thrax.hadoop.jobs.ThraxJob;
import edu.jhu.thrax.lexprob.TrieLexprobTable;
import edu.jhu.thrax.util.Vocabulary;

@SuppressWarnings("rawtypes")
public class SourceGivenTargetLexicalProbabilityFeature implements AnnotationFeature {

  private TrieLexprobTable table;

  private static final double DEFAULT_PROB = 10e-7;
  private static final Text LABEL = new Text("Lex(f|e)");

  public Text getName() {
    return LABEL;
  }

  public void init(Context context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    String work_dir = conf.getRaw("thrax.work-dir");
    String sgt_path = work_dir + "lexprobs_sgt/part-*";
    table = new TrieLexprobTable(conf, sgt_path);
    context.progress();
  }

  public Writable score(RuleWritable key, Annotation annotation) {
    return new DoubleWritable(sourceGivenTarget(key, annotation.f2e()));
  }

  private double sourceGivenTarget(RuleWritable rule, AlignmentWritable f2e) {
    byte[] points = f2e.points;
    int[] source = rule.source;
    int[] target = rule.target;
    
    double total = 0, prob = 0;
    int prev = -1;
    int n = points.length / 2;
    int m = 0;
    int expected = 0;
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

      while (expected < f) {
        if (!Vocabulary.nt(source[expected])) {
          double p = table.get(Vocabulary.getUnknownId(), source[expected]);
          total += (p < 0 ? Math.log(DEFAULT_PROB) : Math.log(p));
        }
        ++expected;
      }
      expected = f + 1;

      double p = table.get(target[e], source[f]);
      prob += (p < 0 ? DEFAULT_PROB : p);
      if (p < 0)
        System.err.printf("WARNING: could not read lexprob p(%s|%s)\n", Vocabulary.word(source[f]),
            Vocabulary.word(target[e]));
    }
    if (m != 0)
      total += Math.log(prob) - Math.log(m);
    
    while (expected < source.length) {
      if (!Vocabulary.nt(source[expected])) {
        double p = table.get(Vocabulary.getUnknownId(), source[expected]);
        total += (p < 0 ? Math.log(DEFAULT_PROB) : Math.log(p));
      }
      ++expected;
    }
    return -total;
  }


  public Set<Class<? extends ThraxJob>> getPrerequisites() {
    Set<Class<? extends ThraxJob>> pqs = new HashSet<Class<? extends ThraxJob>>();
    pqs.add(TargetWordGivenSourceWordProbabilityJob.class);
    pqs.add(SourceWordGivenTargetWordProbabilityJob.class);
    return pqs;
  }

  private static final DoubleWritable ONE_PROB = new DoubleWritable(0.0);

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ONE_PROB);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ONE_PROB);
  }
}
