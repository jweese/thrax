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
import edu.jhu.thrax.util.ConfFileParser;

import java.util.Map;

public class ExtractionTool extends Configured implements Tool
{
    public int run(String [] argv) throws Exception
    {
        if (argv.length < 3) {
            System.err.println("USAGE: ExtractionTool <conf file> <input path> <work directory>");
            return 1;
        }
        String thraxConf = argv[0];
        String inputPath = argv[1];
        String workDir = argv[2];
        Configuration conf = getConf();

        Map<String,String> options = ConfFileParser.parse(thraxConf);
        for (String opt : options.keySet()) {
            conf.set("thrax." + opt, options.get(opt));
        }

        Job job = new Job(conf, "thrax");
        job.setJarByClass(ExtractionMapper.class);
        job.setMapperClass(ExtractionMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);

        job.setMapOutputKeyClass(RuleWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(RuleWritable.class);
        job.setOutputValueClass(IntWritable.class);

        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path(inputPath));
        if (!workDir.endsWith(Path.SEPARATOR))
            workDir += Path.SEPARATOR;
        FileOutputFormat.setOutputPath(job, new Path(workDir + "rules"));

        job.submit();
        return 0;
    }

    public static void main(String [] argv) throws Exception
    {
        int exitCode = ToolRunner.run(null, new ExtractionTool(), argv);
        return;
    }
}
