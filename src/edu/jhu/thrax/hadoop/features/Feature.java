package edu.jhu.thrax.hadoop.features;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class Feature extends Mapper<RuleWritable, IntWritable,
                                    RuleWritable, IntWritable>
{
    // do nothing
}

