package edu.jhu.thrax.hadoop.features;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.util.Map;

public class AdjacentNonTerminalsFeature extends SimpleFeature
{
    private static final Text LABEL = new Text("Adjacent");
    private static final IntWritable ZERO = new IntWritable(0);
    private static final IntWritable ONE = new IntWritable(1);

    public AdjacentNonTerminalsFeature()
    {
        super("adjacent");
    }

    public void score(RuleWritable r, Map<Text,Writable> map)
    {
        map.put(LABEL, r.source.toString().indexOf("] [") == -1 ? ZERO : ONE);
        return;
    }
}

