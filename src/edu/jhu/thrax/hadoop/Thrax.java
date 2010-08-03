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
import edu.jhu.thrax.hadoop.features.Feature;
import edu.jhu.thrax.hadoop.features.FeatureFactory;

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
        String thraxConf = conf.getRaw("thrax_work");
        if (thraxConf.endsWith(Path.SEPARATOR)) {
            thraxConf += "thrax.config";
        }
        else {
            thraxConf += Path.SEPARATOR + "thrax.config";
        }
        ThraxConfig.configure(thraxConf);
        job.setJarByClass(Thrax.class);
        job.setMapperClass(ExtractionMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);

        job.setMapOutputKeyClass(RuleWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(RuleWritable.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.setInputPaths(job, new Path(argv[0]));
        if (!argv[1].endsWith(Path.SEPARATOR)) {
            argv[1] += Path.SEPARATOR;
        }
        String outputPath = argv[1] + "rules";
        String inputPath;
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.waitForCompletion(true);

        Feature [] features = FeatureFactory.getAll(ThraxConfig.FEATURES.split("\\s+"));
        for (int j = 0; j < features.length; j++) {
            Feature f = features[j];
            Job fjob = new Job(conf, String.format("thrax-%d-%s", j, f.name));
            fjob.setMapOutputKeyClass(RuleWritable.class);
            fjob.setMapOutputValueClass(IntWritable.class);
            fjob.setOutputKeyClass(RuleWritable.class);
            fjob.setOutputValueClass(IntWritable.class);
            fjob.setMapperClass(f.mapperClass());
            fjob.setCombinerClass(f.combinerClass());
            fjob.setPartitionerClass(f.partitionerClass());
            fjob.setReducerClass(f.reducerClass());

            inputPath = outputPath;
            outputPath = argv[1] + String.format("feature-%d-%s", j, f.name);
            FileInputFormat.setInputPaths(fjob, new Path(inputPath));
            FileOutputFormat.setOutputPath(fjob, new Path(outputPath));
            fjob.waitForCompletion(true);
        }
        Job printjob = new Job(conf, "thrax-print");
        printjob.setMapOutputKeyClass(RuleWritable.class);
        printjob.setMapOutputValueClass(IntWritable.class);
        printjob.setOutputKeyClass(RuleWritable.class);
        printjob.setOutputValueClass(NullWritable.class);
        printjob.setCombinerClass(IntSumReducer.class);
        printjob.setReducerClass(OutputReducer.class);

        inputPath = outputPath;
        outputPath = argv[1] + "final";
        FileInputFormat.setInputPaths(printjob, new Path(inputPath));
        FileOutputFormat.setOutputPath(printjob, new Path(outputPath));
        printjob.waitForCompletion(true);
        return 0;
    }

    public static void main(String [] argv) throws Exception
    {
        int exitCode = ToolRunner.run(null, new Thrax(), argv);
        System.exit(exitCode);
    }
}

