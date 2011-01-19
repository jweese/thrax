package edu.jhu.thrax.hadoop.output;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;

import java.io.IOException;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class OutputMapper extends Mapper<RuleWritable, IntWritable, RuleWritable, NullWritable>
{
    protected void map(RuleWritable key, IntWritable value,
                          Context context) throws IOException, InterruptedException
    {
        context.write(key, NullWritable.get());
    }

}

