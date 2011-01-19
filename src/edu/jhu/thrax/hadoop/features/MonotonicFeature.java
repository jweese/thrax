package edu.jhu.thrax.hadoop.features;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.util.Map;

public class MonotonicFeature extends SimpleFeature
{
    private static final Text LABEL = new Text("Monotonic");
    private static final IntWritable ZERO = new IntWritable(0);
    private static final IntWritable ONE = new IntWritable(1);

    public MonotonicFeature()
    {
        super("monotonic");
    }

    public void score(RuleWritable r)
    {
        r.features.put(LABEL, r.target.toString().matches("2\\].*1\\]") ? ZERO : ONE);
        return;
    }

    public void score(RuleWritable r, Map<Text,Writable> map)
    {
        map.put(LABEL, r.target.toString().matches("2\\].*1\\]") ? ZERO : ONE);
        return;
    }
}



