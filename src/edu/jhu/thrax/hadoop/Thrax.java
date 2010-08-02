package edu.jhu.thrax.hadoop;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.IntWritable;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.extraction.ExtractionMapper;
import edu.jhu.thrax.hadoop.output.OutputReducer;

public class Thrax extends Configured implements Tool
{
    public int run(String [] argv) throws Exception
    {
        Configuration conf = getConf();
        Job job = new Job(conf, "thrax");

        // do some command-line stuff
        if (argv.length < 2) {
            return 1;
        }
        job.setJarByClass(Thrax.class);
        job.setMapperClass(ExtractionMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(OutputReducer.class);

        job.setMapOutputKeyClass(RuleWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.setInputPaths(job, new Path(argv[0]));
        String sep = argv[1].endsWith(Path.SEPARATOR) ? "" : Path.SEPARATOR;
        FileOutputFormat.setOutputPath(job, new Path(argv[1] + sep + "rules"));

        job.waitForCompletion(true);
        return 0;
    }

    public static void main(String [] argv) throws Exception
    {
        int exitCode = ToolRunner.run(null, new Thrax(), argv);
        System.exit(exitCode);
    }
}

