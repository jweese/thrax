package edu.jhu.thrax.hadoop.features;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.jhu.thrax.datatypes.AlignedSentencePair;
import edu.jhu.thrax.datatypes.Alignment;
import edu.jhu.thrax.hadoop.jobs.WordLexprobJob;
import edu.jhu.thrax.util.exceptions.MalformedInputException;
import edu.jhu.thrax.util.io.InputUtilities;

public class WordLexicalProbabilityCalculator extends Configured implements Tool {
  public static final int UNALIGNED = 0xFFFFFFFF;
  public static final int MARGINAL = 0x00000000;

  public static class Map extends Mapper<LongWritable, Text, LongWritable, IntWritable> {
    private HashMap<Long, Integer> counts = new HashMap<Long, Integer>();
    private boolean sourceParsed;
    private boolean targetParsed;
    private boolean reverse;
    private boolean sourceGivenTarget;

    protected void setup(Context context) throws IOException, InterruptedException {
      Configuration conf = context.getConfiguration();
      sourceParsed = conf.getBoolean("thrax.source-is-parsed", false);
      targetParsed = conf.getBoolean("thrax.target-is-parsed", false);
      // TODO: Drop this backwards compatibility hack.
      if (conf.get("thrax.english-is-parsed") != null)
        targetParsed = conf.getBoolean("thrax.english-is-parsed", false);
      reverse = conf.getBoolean("thrax.reverse", false);
      sourceGivenTarget = conf.getBoolean(WordLexprobJob.SOURCE_GIVEN_TARGET, false);
    }

    public void map(LongWritable key, Text value, Context context) throws IOException,
        InterruptedException {
      counts.clear();
      String line = value.toString();
      AlignedSentencePair sentencePair;
      try {
        sentencePair =
            InputUtilities.alignedSentencePair(line, sourceParsed, targetParsed, reverse
                ^ sourceGivenTarget);
      } catch (MalformedInputException e) {
        context.getCounter("input errors", e.getMessage()).increment(1);
        return;
      }
      int[] source = sentencePair.source;
      int[] target = sentencePair.target;
      Alignment alignment = sentencePair.alignment;

      for (int i = 0; i < source.length; i++) {
        int src = source[i];
        long marginal = ((long) src << 32) | MARGINAL;
        if (alignment.sourceIndexIsAligned(i)) {
          Iterator<Integer> targetIndices = alignment.targetIndicesAlignedTo(i);
          while (targetIndices.hasNext()) {
            int tgt = target[targetIndices.next()];
            long pair = ((long) src << 32) | tgt;
            counts.put(pair, counts.containsKey(pair) ? counts.get(pair) + 1 : 1);
          }
          int numWords = alignment.numTargetWordsAlignedTo(i);
          counts.put(marginal, counts.containsKey(marginal)
              ? counts.get(marginal) + numWords
              : numWords);
        } else {
          long pair = ((long) src << 32) | UNALIGNED;
          counts.put(pair, counts.containsKey(pair) ? counts.get(pair) + 1 : 1);
          counts.put(marginal, counts.containsKey(marginal) ? counts.get(marginal) + 1 : 1);
        }
      }

      for (long pair : counts.keySet()) {
        context.write(new LongWritable(pair), new IntWritable(counts.get(pair)));
      }
    }
  }

  public static class Reduce
      extends Reducer<LongWritable, IntWritable, LongWritable, DoubleWritable> {
    private int current;
    private int marginalCount;

    protected void reduce(LongWritable key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      long pair = key.get();
      int first = (int) (pair >> 32);
      int second = (int) pair;
      if (first == current) {
        if (second == MARGINAL) return;
        current = first;
        marginalCount = 0;
        for (IntWritable x : values)
          marginalCount += x.get();
        return;
      }
      // control only gets here if we are using the same marginal
      int my_count = 0;
      for (IntWritable x : values)
        my_count += x.get();
      context.write(key, new DoubleWritable(my_count / (double) marginalCount));
    }
  }

  public static class Partition extends Partitioner<LongWritable, IntWritable> {
    public int getPartition(LongWritable key, IntWritable value, int numPartitions) {
      return ((int) (key.get() >> 32) & Integer.MAX_VALUE) % numPartitions;
    }
  }

  public int run(String[] argv) throws Exception {
    String f2eOut;
    String e2fOut;
    if (argv[1].endsWith(Path.SEPARATOR)) {
      f2eOut = argv[1] + "f2e";
      e2fOut = argv[1] + "e2f";
    } else {
      f2eOut = argv[1] + Path.SEPARATOR + "f2e";
      e2fOut = argv[1] + Path.SEPARATOR + "e2f";
    }
    Configuration conf = getConf();
    Job f2ejob = new Job(conf, "lexprob-f2e");
    f2ejob.setJarByClass(WordLexicalProbabilityCalculator.class);
    f2ejob.setMapperClass(Map.class);
    f2ejob.setReducerClass(Reduce.class);
    f2ejob.setCombinerClass(IntSumReducer.class);
    f2ejob.setPartitionerClass(Partition.class);

    f2ejob.setMapOutputKeyClass(LongWritable.class);
    f2ejob.setMapOutputValueClass(IntWritable.class);
    f2ejob.setOutputKeyClass(LongWritable.class);
    f2ejob.setOutputValueClass(DoubleWritable.class);

    FileInputFormat.setInputPaths(f2ejob, new Path(argv[0]));
    FileOutputFormat.setOutputPath(f2ejob, new Path(f2eOut));
    f2ejob.waitForCompletion(true);

    Configuration newConf = new Configuration(conf);
    newConf.setBoolean("thrax.__wordlexprob_sgt", true);
    Job e2fjob = new Job(newConf, "lexprob-e2f");
    e2fjob.setJarByClass(WordLexicalProbabilityCalculator.class);
    e2fjob.setMapperClass(Map.class);
    e2fjob.setReducerClass(Reduce.class);
    e2fjob.setCombinerClass(IntSumReducer.class);
    e2fjob.setPartitionerClass(Partition.class);

    e2fjob.setMapOutputKeyClass(LongWritable.class);
    e2fjob.setMapOutputValueClass(IntWritable.class);
    e2fjob.setOutputKeyClass(LongWritable.class);
    e2fjob.setOutputValueClass(DoubleWritable.class);

    FileInputFormat.setInputPaths(e2fjob, new Path(argv[0]));
    FileOutputFormat.setOutputPath(e2fjob, new Path(e2fOut));
    e2fjob.waitForCompletion(true);

    return 0;
  }

  public static void main(String[] argv) throws Exception {
    if (argv.length < 2) {
      System.err.println("usage: hadoop jar <jar> <input> <output>");
      return;
    }
    int result = ToolRunner.run(new Configuration(), new WordLexicalProbabilityCalculator(), argv);
    System.exit(result);
  }
}
