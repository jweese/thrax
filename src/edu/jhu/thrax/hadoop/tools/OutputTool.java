package edu.jhu.thrax.hadoop.tools;

import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.features.mapred.MapReduceFeature;
import edu.jhu.thrax.hadoop.features.mapred.MapReduceFeatureFactory;
import edu.jhu.thrax.hadoop.output.OutputReducer;
import edu.jhu.thrax.util.BackwardsCompatibility;
import edu.jhu.thrax.util.ConfFileParser;
import edu.jhu.thrax.util.FormatUtils;

public class OutputTool extends Configured implements Tool
{
    public int run(String [] argv) throws Exception
    {
        if (argv.length < 1) {
            System.err.println("usage: OutputTool <conf file>");
            return 1;
        }
        String confFile = argv[0];
        Map<String,String> options = ConfFileParser.parse(confFile);
        Configuration conf = getConf();
        for (String opt : options.keySet()) {
            conf.set("thrax." + opt, options.get(opt));
        }
        String workDir = conf.get("thrax.work-dir");
        if (workDir == null) {
            System.err.println("Set work-dir key in conf file " + confFile + "!");
            return 1;
        }
        if (!workDir.endsWith(Path.SEPARATOR)) {
            workDir += Path.SEPARATOR;
            conf.set("thrax.work-dir", workDir);
        }
        Job job = new Job(conf, "thrax-collect");
        job.setJarByClass(OutputReducer.class);

        job.setMapperClass(Mapper.class);
        job.setReducerClass(OutputReducer.class);

        job.setInputFormatClass(SequenceFileInputFormat.class);

        job.setMapOutputKeyClass(RuleWritable.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setOutputKeyClass(RuleWritable.class);
        job.setOutputValueClass(NullWritable.class);

        String features = BackwardsCompatibility.equivalent(conf.get("thrax.features", ""));
        for (String feature : FormatUtils.P_SPACE.split(features)) {
            if (MapReduceFeatureFactory.get(feature) instanceof MapReduceFeature) {
                FileInputFormat.addInputPath(job, new Path(workDir + feature));
            }
        }
        if (FileInputFormat.getInputPaths(job).length == 0)
            FileInputFormat.addInputPath(job, new Path(workDir + "rules"));
        FileOutputFormat.setOutputPath(job, new Path(workDir + "final"));

        job.submit();
        return 0;
    }

    public static void main(String [] argv) throws Exception
    {
        int exit_code = ToolRunner.run(null, new OutputTool(), argv);
        System.exit(exit_code);
    }
}
