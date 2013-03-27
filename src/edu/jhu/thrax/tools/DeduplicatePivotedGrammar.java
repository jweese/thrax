package edu.jhu.thrax.tools;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.FeatureMap;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.features.SimpleFeature;
import edu.jhu.thrax.hadoop.features.SimpleFeatureFactory;
import edu.jhu.thrax.hadoop.features.pivot.PivotedFeature;
import edu.jhu.thrax.hadoop.features.pivot.PivotedFeatureFactory;
import edu.jhu.thrax.util.FormatUtils;
import edu.jhu.thrax.util.io.LineReader;

public class DeduplicatePivotedGrammar {

  private static final Logger logger = Logger.getLogger(DeduplicatePivotedGrammar.class.getName());

  private static List<SimpleFeature> simple;
  private static List<PivotedFeature> pivoted;

  private static boolean sparse;
  private static boolean labeled;

  private static void writeRule(RuleWritable rule) {
    Map<Text, Writable> aggregated = new TreeMap<Text, Writable>();
    for (PivotedFeature pf : pivoted)
      aggregated.put(pf.getFeatureLabel(), pf.finalizeAggregation());
    for (SimpleFeature f : simple)
      f.score(rule, aggregated);
    System.out.println(FormatUtils.ruleToText(rule, aggregated, labeled, sparse));
  }

  @SuppressWarnings("deprecation")
  private static void writeIndex(String index_file) throws IOException, FileNotFoundException {
    OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(index_file), "UTF-8");
    Map<Text, Writable> sorted_features = new TreeMap<Text, Writable>();

    for (PivotedFeature pf : pivoted)
      sorted_features.put(pf.getFeatureLabel(), null);

    RuleWritable dummy = new RuleWritable("X ||| one ||| two ||| Dummy=1");
    for (SimpleFeature f : simple)
      f.score(dummy, sorted_features);

    int index = 0;
    for (Text t : sorted_features.keySet())
      out.write(index++ + "\t" + t + "\n");

    out.close();
  }

  @SuppressWarnings("deprecation")
  public static void main(String[] args) {

    labeled = false;
    sparse = false;

    String grammar_file = null;
    String index_file = null;
    String feature_string = null;

    for (int i = 0; i < args.length; i++) {
      if ("-g".equals(args[i]) && (i < args.length - 1)) {
        grammar_file = args[++i];
      } else if ("-i".equals(args[i]) && (i < args.length - 1)) {
        index_file = args[++i];
      } else if ("-f".equals(args[i]) && (i < args.length - 1)) {
        feature_string += " " + args[++i];
      } else if ("-l".equals(args[i])) {
        labeled = true;
      } else if ("-s".equals(args[i])) {
        sparse = true;
      }
    }

    if (grammar_file == null) {
      logger.severe("No grammar specified.");
      return;
    }
    if (index_file == null) {
      logger.severe("No grammar specified.");
      return;
    }
    if (feature_string == null) {
      logger.severe("No features specified.");
      return;
    }
    if (!labeled && sparse) {
      logger.severe("I cannot condone grammars that are both sparse " + "and unlabeled.");
      return;
    }

    simple = SimpleFeatureFactory.getAll(feature_string);
    pivoted = PivotedFeatureFactory.getAll(feature_string);

    try {
      FeatureMap features = new FeatureMap();
      LineReader reader = new LineReader(grammar_file);

      RuleWritable rule = null;
      RuleWritable last_rule = null;

      for (PivotedFeature pf : pivoted)
        pf.initializeAggregation();

      while (reader.hasNext()) {
        String rule_line = reader.next().trim();
        features.clear();
        String[] feature_entries =
            FormatUtils.P_SPACE.split(FormatUtils.P_DELIM.split(rule_line)[3]);

        int i;
        if (feature_entries.length > 0) {
          if (!feature_entries[0].contains("=")) {
            logger.severe("Expecting labeled features.");
            System.exit(0);
          }
          for (i = 0; i < feature_entries.length; i++) {
            String[] parts = feature_entries[i].split("=");
            Text label = new Text(parts[0]);
            FloatWritable value = new FloatWritable(Float.parseFloat(parts[1]));
            features.put(label, value);
          }
        }
        rule = new RuleWritable(rule_line);

        if (last_rule != null && !last_rule.sameYield(rule)) {
          writeRule(last_rule);
          for (PivotedFeature pf : pivoted)
            pf.initializeAggregation();
        }

        for (PivotedFeature pf : pivoted)
          pf.aggregate(features);

        last_rule = rule;
      }
      writeRule(rule);
      writeIndex(index_file);

      reader.close();
    } catch (IOException e) {
      logger.severe(e.getMessage());
    }
  }

}
