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

import edu.jhu.thrax.hadoop.features.Feature;
import edu.jhu.thrax.hadoop.features.MapReduceFeature;
import edu.jhu.thrax.hadoop.features.FeatureFactory;

public class FeatureTool extends Configured implements Tool
{
    public int run(String [] argv) throws Exception
    {
        Configuration conf = getConf();
        Job job = new Job(conf, String.format("thrax-%s", argv[1]));

        String thraxConf = conf.getRaw("thrax_work");
        if (thraxConf.endsWith(Path.SEPARATOR))
            thraxConf += "thrax.config";
        else
            thraxConf += Path.SEPARATOR + "thrax.config";
        ThraxConfig.configure(thraxConf);

        Feature feat = FeatureFactory.get(argv[1]);
        if (!(feat instanceof MapReduceFeature)) {
            System.err.println("Not a MapReduceFeature: " + argv[1]);
            return 1;
        }
        MapReduceFeature f = (MapReduceFeature) feat;

//        job.setJarByClass(Feature.class);
        job.setMapperClass(f.mapperClass());
        job.setCombinerClass(f.combinerClass());
        job.setSortComparatorClass(f.sortComparatorClass());
        job.setPartitionerClass(f.partitionerClass());
        job.setReducerClass(f.reducerClass());

        job.setInputFormatClass(SequenceFileInputFormat.class);

        job.setMapOutputKeyClass(RuleWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(RuleWritable.class);
        job.setOutputValueClass(IntWritable.class);

        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        if (!argv[0].endsWith(Path.SEPARATOR))
            argv[0] += Path.SEPARATOR;
        FileInputFormat.setInputPaths(job, new Path(argv[0] + "rules"));
        FileOutputFormat.setOutputPath(job, new Path(argv[0] + argv[1]));

        job.submit();
        return 0;
    }

    public static void main(String [] argv) throws Exception
    {
        int exitCode = ToolRunner.run(null, new FeatureTool(), argv);
        return;
    }
}
