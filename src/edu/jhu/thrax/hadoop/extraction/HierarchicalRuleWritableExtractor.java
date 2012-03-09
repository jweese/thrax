package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class HierarchicalRuleWritableExtractor implements RuleWritableExtractor
{
	private Mapper.Context context;

	public HierarchicalRuleWritableExtractor(Mapper.Context c)
	{
		context = c;
	}

	public Iterable<RuleWritable> extract(Text line)
	{
		return null;
	}
}

