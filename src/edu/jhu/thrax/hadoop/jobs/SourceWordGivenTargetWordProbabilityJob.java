package edu.jhu.thrax.hadoop.jobs;

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.DoubleWritable;

import edu.jhu.thrax.hadoop.datatypes.TextPair;

import edu.jhu.thrax.hadoop.features.WordLexicalProbabilityCalculator;

import java.io.IOException;

public class SourceWordGivenTargetWordProbabilityJob extends WordLexprobJob
{

	public SourceWordGivenTargetWordProbabilityJob()
	{
		super(true);
	}

    public Job getJob(Configuration conf) throws IOException
    {
        Job job = super.getJob(conf);
        FileOutputFormat.setOutputPath(job, new Path(conf.get("thrax.work-dir") + "lexprobse2f"));
        return job;
    }
    
    public String getOutputSuffix() {
    	return "lexprobse2f";
    }
}

