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

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.extraction.ExtractionMapper;

public class ExtractionTool extends Configured implements Tool
{
    public int run(String [] argv) throws Exception
    {
        Configuration conf = getConf();
        Job job = new Job(conf, "thrax");

        String thraxConf = conf.getRaw("thrax_work");
        if (thraxConf.endsWith(Path.SEPARATOR))
            thraxConf += "thrax.config";
        else
            thraxConf += Path.SEPARATOR + "thrax.config";
        ThraxConfig.configure(thraxConf);

        job.setJarByClass(ExtractionMapper.class);
        job.setMapperClass(ExtractionMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);

        job.setMapOutputKeyClass(RuleWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(RuleWritable.class);
        job.setOutputValueClass(IntWritable.class);

        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path(argv[0]));
        if (!argv[1].endsWith(Path.SEPARATOR))
            argv[1] += Path.SEPARATOR;
        FileOutputFormat.setOutputPath(job, new Path(argv[1] + "rules"));

        job.submit();
        return 0;
    }

    public static void main(String [] argv) throws Exception
    {
        int exitCode = ToolRunner.run(null, new ExtractionTool(), argv);
        System.exit(exitCode);
    }
}
