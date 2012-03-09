package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

import edu.jhu.thrax.util.io.InputUtilities;
import edu.jhu.thrax.util.exceptions.MalformedInputException;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.datatypes.*;

import java.util.Collections;

public class HierarchicalRuleWritableExtractor implements RuleWritableExtractor
{
	private Mapper.Context context;

	private boolean sourceParsed;
	private boolean targetParsed;
	private boolean reverse;

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
		return null;
	}
}

