package edu.jhu.thrax.hadoop.jobs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import edu.jhu.thrax.hadoop.distributional.DistributionalContextCombiner;
import edu.jhu.thrax.hadoop.distributional.DistributionalContextMapper;
import edu.jhu.thrax.hadoop.distributional.DistributionalContextReducer;
import edu.jhu.thrax.hadoop.distributional.SignatureWritable;

public class DistributionalContextExtractionJob extends ThraxJob {
	
	public Job getJob(Configuration conf) throws IOException {
		Job job = new Job(conf, "distributional");
		job.setJarByClass(DistributionalContextMapper.class);
		job.setMapperClass(DistributionalContextMapper.class);
		job.setCombinerClass(DistributionalContextCombiner.class);
		job.setReducerClass(DistributionalContextReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(MapWritable.class);
		job.setOutputKeyClass(SignatureWritable.class);
		job.setOutputValueClass(NullWritable.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		int numReducers = conf.getInt("thrax.reducers", 4);
		job.setNumReduceTasks(numReducers);

		FileInputFormat.setInputPaths(job, new Path(conf.get("thrax.input-file")));
		int maxSplitSize = conf.getInt("thrax.max-split-size", 0);
		if (maxSplitSize != 0)
			FileInputFormat.setMaxInputSplitSize(job, maxSplitSize);
		
		String workDir = conf.get("thrax.work-dir");
		
		String outputPath = workDir + "signatures";
		FileOutputFormat.setOutputPath(job, new Path(outputPath));
    		
		return job;
	}
	
	public String getName() {
  	return "distributional";
  }

	public String getOutputSuffix() {
		return null;
	}
}
