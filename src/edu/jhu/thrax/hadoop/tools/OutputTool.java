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

import edu.jhu.thrax.hadoop.output.OutputReducer;

public class OutputTool extends Configured implements Tool
{
    public int run(String [] argv) throws Exception
    {
        if (argv.length < 2) {
            System.err.println("usage: OutputTool <work dir> <f1,f2,...>");
            return 1;
        }
        Configuration conf = getConf();
        Job job = new Job(conf, "thrax-collect");

        String thraxConf = conf.getRaw("thrax_work");
        if (thraxConf.endsWith(Path.SEPARATOR))
            thraxConf += "thrax.config";
        else
            thraxConf += Path.SEPARATOR + "thrax.config";
        ThraxConfig.configure(thraxConf);

        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(OutputReducer.class);

        job.setInputFormatClass(SequenceFileInputFormat.class);

        job.setMapOutputKeyClass(RuleWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(RuleWritable.class);
        job.setOutputValueClass(NullWritable.class);

        if (!argv[0].endsWith(Path.SEPARATOR))
            argv[0] += Path.SEPARATOR;
        for (String x : argv[1].split(",")) {
            FileInputFormat.addInputPath(job, new Path(argv[0] + x));
        }
        FileOutputFormat.setOutputPath(job, new Path(argv[0] + "final"));

        job.submit();
        return 0;
    }

    public static void main(String [] argv) throws Exception
    {
        int exitCode = ToolRunner.run(null, new OutputTool(), argv);
        return;
    }
}
