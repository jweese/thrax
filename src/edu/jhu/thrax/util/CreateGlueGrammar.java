package edu.jhu.thrax.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.features.SimpleFeature;
import edu.jhu.thrax.hadoop.features.SimpleFeatureFactory;
import edu.jhu.thrax.hadoop.features.mapred.MapReduceFeature;
import edu.jhu.thrax.hadoop.jobs.FeatureJobFactory;
import edu.jhu.thrax.util.io.LineReader;

public class CreateGlueGrammar {

	private static final Logger logger =
			Logger.getLogger(CreateGlueGrammar.class.getName());

	private static final String UNARY = "[%1$s] ||| [%2$s,1] ||| [%2$s,1] ||| 0";
	private static final String BINARY = "[%1$s] ||| [%1$s,1] [%2$s,2] ||| [%1$s,1] [%2$s,2] ||| 0";

	private static final IntWritable ZERO = new IntWritable(0);

	public static void main(String[] args) throws IOException {
		String grammar_file = null;
		String config_file = null;
		String feature_string = "";
		String external_string = "";
		String nt_file = null;

		String goal_symbol = "GOAL";
		String[] features;
		String[] external;

		boolean labeled = false;
		boolean sparse = false;

		for (int i = 0; i < args.length; i++) {
			if ("-g".equals(args[i]) && (i < args.length - 1)) {
				grammar_file = args[++i];
			} else if ("-c".equals(args[i]) && (i < args.length - 1)) {
				config_file = args[++i];
			} else if ("-n".equals(args[i]) && (i < args.length - 1)) {
				nt_file = args[++i];
			} else if ("-f".equals(args[i]) && (i < args.length - 1)) {
				feature_string += " " + args[++i];
			} else if ("-e".equals(args[i]) && (i < args.length - 1)) {
				external_string += " " + args[++i];
			} else if ("-l".equals(args[i])) {
				labeled = true;
			} else if ("-s".equals(args[i])) {
				sparse = true;
			}
		}

		if (grammar_file == null && nt_file == null) {
			logger.severe("Neither grammar nor NT file specified.");
			return;
		}
		if (config_file == null && feature_string.isEmpty()) {
			logger.severe("Neither configuration file nor features specified.");
			return;
		}

		if (config_file != null) {
			Map<String, String> opts = ConfFileParser.parse(config_file);
			if (opts.containsKey("goal-symbol"))
				goal_symbol = opts.get("goal-symbol");
			if (opts.containsKey("features"))
				feature_string += " " + opts.get("features");
		}

		features = feature_string.trim().split("\\s+|,");
		external = external_string.trim().split("\\s+|,");

		Set<String> nts = new HashSet<String>();
		if (nt_file != null) {
			LineReader nt_reader = new LineReader(nt_file);
			while (nt_reader.hasNext()) {
				String lhs = nt_reader.next().trim();
				if (lhs.startsWith("[") && lhs.endsWith("]"))
					lhs = lhs.substring(1, lhs.length() - 1);
				nts.add(lhs);
			}
		} else {
			LineReader grammar_reader = new LineReader(grammar_file);
			while (grammar_reader.hasNext()) {
				String line = grammar_reader.next();
				int lhsStart = line.indexOf("[") + 1;
				int lhsEnd = line.indexOf("]");
				if (lhsStart < 1 || lhsEnd < 0) {
					logger.warning("Malformed rule: " + line);
					continue;
				}
				String lhs = line.substring(lhsStart, lhsEnd);
				nts.add(lhs);
			}
		}

		Map<Text, Writable> unary_map = new TreeMap<Text, Writable>();
		Map<Text, Writable> binary_map = new TreeMap<Text, Writable>();
		for (String nonterminal : nts) {
			Text nt = new Text(nonterminal);
			unary_map.clear();
			binary_map.clear();

			for (String e : external) {
				Text extrenal_label = new Text(e);
				unary_map.put(extrenal_label, ZERO);
				binary_map.put(extrenal_label, ZERO);
			}
			for (String f : features) {
				SimpleFeature sf = SimpleFeatureFactory.get(f);
				if (sf != null) {
					sf.unaryGlueRuleScore(nt, unary_map);
					sf.binaryGlueRuleScore(nt, binary_map);
					continue;
				}
				MapReduceFeature mrf = FeatureJobFactory.get(f);
				if (mrf != null) {
					mrf.unaryGlueRuleScore(nt, unary_map);
					mrf.binaryGlueRuleScore(nt, binary_map);
				}
			}
			System.out.println(FormatUtils.ruleToText(
					new RuleWritable(String.format(UNARY, goal_symbol, nt)),
					unary_map, labeled, sparse));
			System.out.println(FormatUtils.ruleToText(
					new RuleWritable(String.format(BINARY, goal_symbol, nt)),
					binary_map, labeled, sparse));
		}
	}

}
