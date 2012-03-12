package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

import edu.jhu.thrax.util.io.InputUtilities;
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
			result.add(toRuleWritable(r, labeler, source, target, alignment));
		return result;
	}

	private RuleWritable toRuleWritable(HierarchicalRule r, SpanLabeler spanLabeler, String [] source, String [] target, Alignment alignment)
	{
		String lhs = r.lhsLabel(spanLabeler, sourceLabels);
		String src = r.sourceString(source, spanLabeler, sourceLabels);
		String tgt = r.targetString(target, spanLabeler, sourceLabels);
		String [][] sourceAlignment = r.sourceAlignmentArray(source, target, alignment);
		String [][] targetAlignment = r.targetAlignmentArray(source, target, alignment);
		return new RuleWritable(new Text(lhs), new Text(src), new Text(tgt), new AlignmentArray(sourceAlignment), new AlignmentArray(targetAlignment));
	}
}

