package edu.jhu.thrax.hadoop.tools;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import edu.jhu.thrax.hadoop.features.Feature;
import edu.jhu.thrax.hadoop.features.MapReduceFeature;
import edu.jhu.thrax.hadoop.features.FeatureFactory;

import edu.jhu.thrax.hadoop.output.OutputMapper;
import edu.jhu.thrax.hadoop.output.OutputReducer;

public class OutputTool extends Configured implements Tool
{
    public int run(String [] argv) throws Exception
    {
        if (argv.length < 2) {
            System.err.println("usage: OutputTool <true|false> <work dir> [f1 f2 ...]");
            return 1;
        }
        boolean label = Boolean.parseBoolean(argv[0]);
        String workDir = argv[1];
        if (!workDir.endsWith(Path.SEPARATOR))
            workDir += Path.SEPARATOR;
        Configuration conf = getConf();
        conf.setBoolean("thrax.label.features", label);
        conf.setStrings("thrax.features", argv);
        Job job = new Job(conf, "thrax-collect");

        job.setMapperClass(OutputMapper.class);
        job.setReducerClass(OutputReducer.class);

        job.setInputFormatClass(SequenceFileInputFormat.class);

        job.setMapOutputKeyClass(RuleWritable.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setOutputKeyClass(RuleWritable.class);
        job.setOutputValueClass(NullWritable.class);

        for (String feature : conf.getStrings("thrax.features", "")) {
            if (FeatureFactory.get(feature) instanceof MapReduceFeature) {
                FileInputFormat.addInputPath(job, new Path(workDir + feature));
            }
        }
        FileOutputFormat.setOutputPath(job, new Path(workDir + "final"));

        job.submit();
        return 0;
    }

    public static void main(String [] argv) throws Exception
    {
        int exitCode = ToolRunner.run(null, new OutputTool(), argv);
        return;
    }
}
