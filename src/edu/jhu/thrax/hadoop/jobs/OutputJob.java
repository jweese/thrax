package edu.jhu.thrax.hadoop.jobs;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.fs.Path;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.output.*;
import edu.jhu.thrax.hadoop.features.mapred.MapReduceFeature;

public class OutputJob extends ThraxJob
{
    private static HashSet<Class<? extends ThraxJob>> prereqs = new HashSet<Class<? extends ThraxJob>>();

    public static void addPrerequisite(Class<? extends ThraxJob> c)
    {
        prereqs.add(c);
    }

    public Job getJob(Configuration conf) throws IOException
    {
        Job job = new Job(conf, "collect");
        String workDir = conf.get("thrax.work-dir");
        job.setJarByClass(OutputMapper.class);
        job.setMapperClass(OutputMapper.class);
        job.setReducerClass(OutputReducer.class);

        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setMapOutputKeyClass(RuleWritable.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(RuleWritable.class);
        job.setOutputValueClass(NullWritable.class);

        for (String feature : conf.get("thrax.features", "").split("\\s+")) {
            if (FeatureJobFactory.get(feature) instanceof MapReduceFeature) {
                FileInputFormat.addInputPath(job, new Path(workDir + feature));
            }
        }
        if (FileInputFormat.getInputPaths(job).length == 0)
            FileInputFormat.addInputPath(job, new Path(workDir + "rules"));
        FileOutputFormat.setOutputPath(job, new Path(workDir + "final"));

        return job;
    }

    public Set<Class<? extends ThraxJob>> getPrerequisites()
    {
        prereqs.add(ExtractionJob.class);
        return prereqs;
    }
}

