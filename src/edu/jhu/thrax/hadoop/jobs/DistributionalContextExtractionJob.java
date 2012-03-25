package edu.jhu.thrax.hadoop.jobs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import edu.jhu.thrax.hadoop.distributional.DistributionalContextMapper;
import edu.jhu.thrax.hadoop.distributional.DistributionalContextReducer;

public class DistributionalContextExtractionJob extends ThraxJob {
	
	public Job getJob(Configuration conf) throws IOException {
		Job job = new Job(conf, "distributional");
		job.setJarByClass(DistributionalContextMapper.class);
		job.setMapperClass(DistributionalContextMapper.class);
		job.setReducerClass(DistributionalContextReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(MapWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);

		int numReducers = conf.getInt("thrax.reducers", 4);
		job.setNumReduceTasks(numReducers);

		FileInputFormat.setInputPaths(job, new Path(conf.get("thrax.input-file")));
		int maxSplitSize = conf.getInt("thrax.max-split-size", 0);
		if (maxSplitSize != 0)
			FileInputFormat.setMaxInputSplitSize(job, maxSplitSize);
		
		String outputPath = conf.get("thrax.outputPath", "");
    FileOutputFormat.setOutputPath(job, new Path(outputPath));
    
    FileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
    FileOutputFormat.setCompressOutput(job, true);
		
		return job;
	}

	public String getOutputSuffix() {
		return null;
	}
}
