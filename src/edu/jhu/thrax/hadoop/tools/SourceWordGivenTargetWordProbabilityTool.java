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
import org.apache.hadoop.io.DoubleWritable;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.datatypes.TextPair;

import edu.jhu.thrax.hadoop.features.Feature;
import edu.jhu.thrax.hadoop.features.WordLexicalProbabilityCalculator;

public class SourceWordGivenTargetWordProbabilityTool extends Configured implements Tool
{
    public int run(String [] argv) throws Exception
    {
        if (argv.length < 2) {
            System.err.println("usage: SourceWordGivenTargetWordProbabilityTool <input file> <work directory>");
            return 1;
        }
        String input = argv[0];
        String workDir = argv[1];
        Configuration conf = getConf();
        Job job = new Job(conf, "thrax-sgt-word-lexprob");

        job.setJarByClass(Feature.class);
        job.setMapperClass(WordLexicalProbabilityCalculator.SourceGivenTargetMap.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setSortComparatorClass(TextPair.SndMarginalComparator.class);
        job.setPartitionerClass(WordLexicalProbabilityCalculator.Partition.class);
        job.setReducerClass(WordLexicalProbabilityCalculator.Reduce.class);

        job.setMapOutputKeyClass(TextPair.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(TextPair.class);
        job.setOutputValueClass(DoubleWritable.class);

        if (!workDir.endsWith(Path.SEPARATOR))
            workDir += Path.SEPARATOR;
        FileInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(workDir + "lexprobse2f"));

        job.submit();
        return 0;
    }

    public static void main(String [] argv) throws Exception
    {
        int exitCode = ToolRunner.run(null, new SourceWordGivenTargetWordProbabilityTool(), argv);
        return;
    }
}
