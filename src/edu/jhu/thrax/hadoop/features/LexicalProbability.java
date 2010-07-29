package edu.jhu.thrax.hadoop.features;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.hadoop.datatypes.IntPair;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;

import java.io.IOException;

public class LexicalProbability
{
    public static final Text UNALIGNED = new Text("/UNALIGNED/");
    public static final Text MARGINAL = new Text("/MARGINAL/");

    private static class Map extends Mapper<LongWritable, Text, IntPair, IntWritable>
    {
        public void map(LongWritable key, Text value, Context context)
        {
            String line = value.toString();
            String [] parts = line.split(ThraxConfig.DELIMITER);
        }
    }

}

