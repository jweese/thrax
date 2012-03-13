package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.io.Text;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public interface RuleWritableExtractor
{
	public Iterable<RuleWritable> extract(Text line);
}

