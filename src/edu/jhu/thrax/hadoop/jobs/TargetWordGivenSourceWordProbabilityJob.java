package edu.jhu.thrax.hadoop.jobs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TargetWordGivenSourceWordProbabilityJob extends WordLexprobJob
{
	public TargetWordGivenSourceWordProbabilityJob()
	{
		super(false);
	}

    public Job getJob(Configuration conf) throws IOException
    {
        Job job = super.getJob(conf);
        FileOutputFormat.setOutputPath(job, new Path(conf.get("thrax.work-dir") + "lexprobsf2e"));
        return job;
    }
    
    public String getName() {
    	return "target-word-lexprob";
    }
    
    public String getOutputSuffix() {
    	return "lexprobsf2e";
    }
}

