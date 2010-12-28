package edu.jhu.thrax.hadoop.features;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class XRuleFeature extends SimpleFeature
{
    private static final Text LABEL = new Text("ContainsX");
    private static final IntWritable ZERO = new IntWritable(0);
    private static final IntWritable ONE = new IntWritable(1);

    public XRuleFeature()
    {
        super("xrule");
    }

    public void score(RuleWritable r)
    {
        r.features.put(LABEL, r.source.toString().matches("\\[X") ? ONE : ZERO);
        return;
    }
}

