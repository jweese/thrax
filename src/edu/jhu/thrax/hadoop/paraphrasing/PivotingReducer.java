package edu.jhu.thrax.hadoop.paraphrasing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.features.pivot.PivotedFeature;
import edu.jhu.thrax.hadoop.features.pivot.PivotedFeatureFactory;
import edu.jhu.thrax.util.Vocabulary;

public class PivotingReducer extends Reducer<RuleWritable, MapWritable, RuleWritable, MapWritable> {

  private static enum PivotingCounters {
    F_READ, EF_READ, EF_PRUNED, EE_PRUNED, EE_WRITTEN
  };

  private int[] currentSrc;
  private int currentLhs;

  private int[] nts;
  private int lhs;

  private List<ParaphrasePattern> targets;
  private List<PivotedFeature> features;

  private Map<Text, PruningRule> translationPruningRules;
  private Map<Text, PruningRule> pivotedPruningRules;

  protected void setup(Context context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    String vocabulary_path = conf.getRaw("thrax.work-dir") + "vocabulary/part-r-00000";
    Vocabulary.read(conf, vocabulary_path);
    
    features = PivotedFeatureFactory.getAll(conf.get("thrax.features", ""));

    currentLhs = 0;
    currentSrc = null;

    lhs = 0;
    nts = null;
    targets = new ArrayList<ParaphrasePattern>();

    translationPruningRules = getTranslationPruningRules(conf.get("thrax.pruning", ""));
    pivotedPruningRules = getPivotedPruningRules(conf.get("thrax.pruning", ""));
  }

  protected void reduce(RuleWritable key, Iterable<MapWritable> values, Context context)
      throws IOException, InterruptedException {
    if (currentLhs == 0 || !(key.lhs == currentLhs && Arrays.equals(key.source, currentSrc))) {
      if (currentLhs != 0) pivotAll(context);
      currentLhs = key.lhs;
      currentSrc = key.source;
      // TODO: not sure why this check is here.
      if (currentLhs == 0 || currentSrc.length == 0) return;
      lhs = currentLhs;
      nts = extractNonterminals(currentSrc);
      targets.clear();
    }
    for (MapWritable features : values)
      if (!prune(features, translationPruningRules))
        targets.add(new ParaphrasePattern(key.target, nts, lhs, key.monotone, features));
      else
        context.getCounter(PivotingCounters.EF_PRUNED).increment(1);
  }

  protected void cleanup(Context context) throws IOException, InterruptedException {
    if (currentLhs != 0) pivotAll(context);
  }

  protected void pivotAll(Context context) throws IOException, InterruptedException {
    context.getCounter(PivotingCounters.F_READ).increment(1);
    context.getCounter(PivotingCounters.EF_READ).increment(targets.size());

    for (int i = 0; i < targets.size(); i++) {
      for (int j = i; j < targets.size(); j++) {
        pivotOne(targets.get(i), targets.get(j), context);
        if (i != j) pivotOne(targets.get(j), targets.get(i), context);
      }
    }
  }

  protected void pivotOne(ParaphrasePattern src, ParaphrasePattern tgt, Context context)
      throws IOException, InterruptedException {
    RuleWritable pivoted_rule = new RuleWritable();
    MapWritable pivoted_features = new MapWritable();

    pivoted_rule.lhs = src.lhs;
    pivoted_rule.source = src.rhs;
    pivoted_rule.target = tgt.rhs;
    pivoted_rule.monotone = (src.monotone == tgt.monotone);

    try {
      // Compute the features.
      for (PivotedFeature f : features)
        pivoted_features.put(f.getFeatureLabel(), f.pivot(src.features, tgt.features));
    } catch (Exception e) {
      StringBuilder src_f = new StringBuilder();
      for (Writable w : src.features.keySet())
        src_f.append(w.toString() + "=" + src.features.get(w) + " ");
      StringBuilder tgt_f = new StringBuilder();
      for (Writable w : tgt.features.keySet())
        tgt_f.append(w.toString() + "=" + tgt.features.get(w) + " ");

      e.printStackTrace();
      
      throw new RuntimeException(Vocabulary.getWords(src.rhs) + " \n "
          + Vocabulary.getWords(tgt.rhs) + " \n " + src_f.toString() + " \n " + tgt_f.toString()
          + " \n");
    }


    if (!prune(pivoted_features, pivotedPruningRules)) {
      context.write(pivoted_rule, pivoted_features);
      context.getCounter(PivotingCounters.EE_WRITTEN).increment(1);
    } else {
      context.getCounter(PivotingCounters.EE_PRUNED).increment(1);
    }
  }

  protected Map<Text, PruningRule> getPivotedPruningRules(String conf_string) {
    Map<Text, PruningRule> rules = new HashMap<Text, PruningRule>();
    // TODO: use patterns for this.
    String[] rule_strings = conf_string.split("\\s*,\\s*");
    for (String rule_string : rule_strings) {
      String[] f;
      boolean smaller;
      if (rule_string.contains("<")) {
        f = rule_string.split("<");
        smaller = true;
      } else if (rule_string.contains(">")) {
        f = rule_string.split(">");
        smaller = false;
      } else {
        continue;
      }
      Text label = PivotedFeatureFactory.get(f[0]).getFeatureLabel();
      rules.put(label, new PruningRule(smaller, Double.parseDouble(f[1])));
    }
    return rules;
  }

  protected Map<Text, PruningRule> getTranslationPruningRules(String conf_string) {
    Map<Text, PruningRule> rules = new HashMap<Text, PruningRule>();
    String[] rule_strings = conf_string.split("\\s*,\\s*");
    for (String rule_string : rule_strings) {
      String[] f;
      boolean smaller;
      if (rule_string.contains("<")) {
        f = rule_string.split("<");
        smaller = true;
      } else if (rule_string.contains(">")) {
        f = rule_string.split(">");
        smaller = false;
      } else {
        continue;
      }
      Double threshold = Double.parseDouble(f[1]);

      Set<Text> lower_bound_labels = PivotedFeatureFactory.get(f[0]).getLowerBoundLabels();
      if (lower_bound_labels != null) for (Text label : lower_bound_labels)
        rules.put(label, new PruningRule(smaller, threshold));

      Set<Text> upper_bound_labels = PivotedFeatureFactory.get(f[0]).getUpperBoundLabels();
      if (upper_bound_labels != null) for (Text label : upper_bound_labels)
        rules.put(label, new PruningRule(!smaller, threshold));
    }
    return rules;
  }

  protected boolean prune(MapWritable features, final Map<Text, PruningRule> rules) {
    for (Map.Entry<Text, PruningRule> e : rules.entrySet()) {
      if (features.containsKey(e.getKey())
          && e.getValue().applies((DoubleWritable) features.get(e.getKey()))) return true;
    }
    return false;
  }

  protected int[] extractNonterminals(int[] source) {
    int first_nt = 0;
    for (int token : source)
      if (Vocabulary.nt(token)) {
        if (first_nt == 0)
          first_nt = token;
        else
          return new int[] {first_nt, token};
      }
    return (first_nt == 0 ? new int[0] : new int[] {first_nt});
  }

  class ParaphrasePattern {
    int arity;
    private int lhs;
    private int[] rhs;
    boolean monotone;

    private MapWritable features;

    public ParaphrasePattern(int[] target, int[] nts, int lhs, boolean mono, MapWritable features) {
      this.arity = nts.length;

      this.lhs = lhs;
      this.rhs = target;
      this.monotone = mono;
      this.features = new MapWritable(features);
    }
  }

  class PruningRule {
    private boolean smaller;
    private double threshold;

    PruningRule(boolean smaller, double threshold) {
      this.smaller = smaller;
      this.threshold = threshold;
    }

    protected boolean applies(DoubleWritable value) {
      return (smaller ? value.get() < threshold : value.get() > threshold);
    }
  }
}
