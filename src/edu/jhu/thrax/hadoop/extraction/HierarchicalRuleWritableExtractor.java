package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

import edu.jhu.thrax.util.io.InputUtilities;
import edu.jhu.thrax.util.exceptions.MalformedInputException;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
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

	private HierarchicalRuleExtractor extractor;
	private SpanLabeler labeler;

	public HierarchicalRuleWritableExtractor(Mapper.Context c)
	{
		context = c;
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
		for (HierarchicalRule r : rules)
			result.add(toRuleWritable(r, labeler, source, target));
		return result;
	}

	private static RuleWritable toRuleWritable(HierarchicalRule r, SpanLabeler spanLabeler, String [] source, String [] target)
	{
		return null;
	}
}

