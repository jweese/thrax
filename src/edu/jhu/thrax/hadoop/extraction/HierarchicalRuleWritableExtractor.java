package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

import edu.jhu.thrax.util.io.InputUtilities;
import edu.jhu.thrax.util.FormatUtils;
import edu.jhu.thrax.util.exceptions.MalformedInputException;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.datatypes.AlignmentArray;
import edu.jhu.thrax.datatypes.*;
import edu.jhu.thrax.extraction.*;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class HierarchicalRuleWritableExtractor implements RuleWritableExtractor
{
	private Mapper.Context context;

	private boolean sourceParsed;
	private boolean targetParsed;
	private boolean reverse;
	private boolean sourceLabels;
	private String defaultLabel;
	private boolean allowDefaultLHSOnNonlexicalRules;

	private HierarchicalRuleExtractor extractor;

	public HierarchicalRuleWritableExtractor(Mapper.Context c)
	{
		context = c;
		Configuration conf = c.getConfiguration();
		sourceParsed = conf.getBoolean("thrax.source-is-parsed", false);
		targetParsed = conf.getBoolean("thrax.target-is-parsed", false);
		reverse = conf.getBoolean("thrax.reverse", false);
		// TODO: this configuration key needs a more general name now
		sourceLabels = ! conf.getBoolean("thrax.target-is-samt-syntax", true);
		defaultLabel = conf.get("thrax.default-nt", "X");
		allowDefaultLHSOnNonlexicalRules = conf.getBoolean("thrax.allow-nonlexical-x", true);
		extractor = getExtractor(conf);
	}

	private static HierarchicalRuleExtractor getExtractor(Configuration conf)
	{
		int arity = conf.getInt("thrax.arity", 2);
		int initialPhraseSource = conf.getInt("thrax.initial-phrase-length", 10);
		int initialPhraseTarget = conf.getInt("thrax.initial-phrase-length", 10);
		int initialAlignment = conf.getInt("thrax.initial-lexicality", 1);
		boolean initialAligned = ! conf.getBoolean("thrax.loose", false);
		int sourceLimit = conf.getInt("thrax.nonlex-source-length", 5);
		int targetLimit = conf.getInt("thrax.nonlex-target-length", 5);
		int ruleAlignment = conf.getInt("thrax.lexicality", 1);
		boolean adjacent = conf.getBoolean("thrax.adjacent-nts", false);
		boolean abs = conf.getBoolean("thrax.allow-abstract-rules", false);
		boolean mixed = conf.getBoolean("thrax.allow-mixed-rules", true);
		return new HierarchicalRuleExtractor(arity,
											 initialPhraseSource,
											 initialPhraseTarget,
											 initialAlignment,
											 initialAligned,
											 sourceLimit,
											 targetLimit,
											 ruleAlignment,
											 adjacent,
											 abs,
											 mixed);
	}

	public Iterable<RuleWritable> extract(Text line)
	{
		AlignedSentencePair sentencePair;
		try {
			sentencePair = InputUtilities.alignedSentencePair(line.toString(), sourceParsed, targetParsed, reverse);
		}
		catch (MalformedInputException e) {
			context.getCounter("input errors", e.getMessage()).increment(1);
			return Collections.<RuleWritable>emptyList();
		}
		String [] source = sentencePair.source;
		String [] target = sentencePair.target;
		Alignment alignment = sentencePair.alignment;
		List<HierarchicalRule> rules = extractor.extract(source.length, target.length, alignment);
		List<RuleWritable> result = new ArrayList<RuleWritable>(rules.size());
		SpanLabeler labeler = getSpanLabeler(line, context.getConfiguration());
		for (HierarchicalRule r : rules) {
			RuleWritable writable = toRuleWritable(r, labeler, source, target, alignment);
			if (writable != null) {
				result.add(writable);
			}
		}
		return result;
	}

	private RuleWritable toRuleWritable(HierarchicalRule r, SpanLabeler spanLabeler, String [] source, String [] target, Alignment alignment)
	{
		String lhs = r.lhsLabel(spanLabeler, sourceLabels);
		String src = r.sourceString(source, spanLabeler, sourceLabels);
		String tgt = r.targetString(target, spanLabeler, sourceLabels);
		if (!isValidLabeling(lhs, src, tgt, spanLabeler))
			return null;
		String [][] sourceAlignment = r.sourceAlignmentArray(source, target, alignment);
		String [][] targetAlignment = r.targetAlignmentArray(source, target, alignment);
		return new RuleWritable(new Text("[" + lhs + "]"), new Text(src), new Text(tgt), new AlignmentArray(sourceAlignment), new AlignmentArray(targetAlignment));
	}

	private SpanLabeler getSpanLabeler(Text line, Configuration conf)
	{
		String labelType = conf.get("thrax.grammar", "hiero");
		if (labelType.equalsIgnoreCase("hiero")) {
			return new HieroLabeler(defaultLabel);
		}
		else if (labelType.equalsIgnoreCase("samt")) {
			String [] fields = line.toString().split(FormatUtils.DELIMITER_REGEX);
			if (fields.length < 2)
				return new HieroLabeler(defaultLabel);
			String parse = fields[sourceLabels ? 0 : 1].trim();
			boolean constituent = conf.getBoolean("thrax.allow-constituent-label", true);
			boolean ccg = conf.getBoolean("thrax.allow-ccg-label", true);
			boolean concat = conf.getBoolean("thrax.allow-concat-label", true);
			boolean doubleConcat = conf.getBoolean("thrax.allow-double-plus", true);
			String unary = conf.get("thrax.unary-category-handler", "all");
			return new SAMTLabeler(parse, constituent, ccg, concat, doubleConcat, unary, defaultLabel);
		}
		else if (labelType.equalsIgnoreCase("manual")) {
			String [] fields = line.toString().split(FormatUtils.DELIMITER_REGEX);
			if (fields.length < 4)
				return new HieroLabeler(defaultLabel);
			String [] labels = fields[3].trim().split("\\s+");
			return new ManualSpanLabeler(labels, defaultLabel);
		}
		else {
			return new HieroLabeler(defaultLabel);
		}
	}

	private boolean isValidLabeling(String lhs, String source, String target, SpanLabeler labeler)
	{
		if (!allowDefaultLHSOnNonlexicalRules) {
			if (defaultLabel.equals(lhs) && hasNonterminal(source) && hasNonterminal(target))
				return false;
			else if (source.indexOf("[" + defaultLabel + ",") >= 0)
				return false;
			else if (target.indexOf("[" + defaultLabel + ",") >= 0)
				return false;
		}
		return true;
	}

	private static boolean hasNonterminal(String s)
	{
		return s.indexOf("1]") >= 0;
	}
}

