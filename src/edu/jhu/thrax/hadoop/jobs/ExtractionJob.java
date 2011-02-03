package edu.jhu.thrax.hadoop.jobs;

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.extraction.ExtractionMapper;

import java.io.IOException;

public class ExtractionJob extends ThraxJob
{
    public Job getJob(Configuration conf) throws IOException
    {
        Job job = new Job(conf, "extraction");
        job.setJarByClass(ExtractionMapper.class);
        job.setMapperClass(ExtractionMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setMapOutputKeyClass(RuleWritable.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(RuleWritable.class);
        job.setOutputValueClass(IntWritable.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path(conf.get("thrax.input-file")));
        int maxSplitSize = conf.getInt("thrax.max-split-size", 0);
        if (maxSplitSize != 0) {
//            System.err.println("* changing max split size from " + FileInputFormat.getMaxSplitSize(job) + " to " + maxSplitSize);
            FileInputFormat.setMaxInputSplitSize(job, maxSplitSize);
        }
        FileOutputFormat.setOutputPath(job, new Path(conf.get("thrax.work-dir") + "rules"));

        return job;
    }
}

