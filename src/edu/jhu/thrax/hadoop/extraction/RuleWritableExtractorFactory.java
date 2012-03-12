package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.mapreduce.Mapper;

public class RuleWritableExtractorFactory
{
	public static RuleWritableExtractor create(Mapper.Context context)
	{
		return new HierarchicalRuleWritableExtractor(context);
	}
}

