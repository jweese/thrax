package edu.jhu.thrax.hadoop.datatypes;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.TwoDArrayWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.jhu.thrax.datatypes.Rule;

public class RuleWritable implements Writable
{
    public IntWritable lhs;
    public ArrayWritable source;
    public ArrayWritable target;
    public TwoDArrayWritable f2e;
    public TwoDArrayWritable e2f;
    public MapWritable features;

    public RuleWritable()
    {
        lhs = new IntWritable();
        source = new ArrayWritable(IntWritable.class);
        target = new ArrayWritable(IntWritable.class);
        f2e = new TwoDArrayWritable(IntWritable.class);
        e2f = new TwoDArrayWritable(IntWritable.class);
        features = new MapWritable();
    }

    public RuleWritable(Rule r)
    {

    }

    public void write(DataOutput out) throws IOException
    {

    }

    public void readFields(DataInput in) throws IOException
    {

    }

}

