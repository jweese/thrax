package edu.jhu.thrax.hadoop.features;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.datatypes.Alignment;
import edu.jhu.thrax.hadoop.datatypes.TextPair;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.DoubleWritable;

import java.io.IOException;
import java.util.HashMap;

public class LexicalProbability
{
    public static final Text UNALIGNED = new Text("/UNALIGNED/");
    public static final Text MARGINAL = new Text("/MARGINAL/");
    public static final byte [] MARGINAL_BYTES = MARGINAL.getBytes();
    public static final int MARGINAL_LENGTH = MARGINAL.getLength();

    private static class Map extends Mapper<LongWritable, Text, TextPair, IntWritable>
    {
        private HashMap<TextPair,Integer> counts = new HashMap<TextPair,Integer>();
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
        {
            counts.clear();
            String line = value.toString();
            String [] parts = line.split(ThraxConfig.DELIMITER_REGEX);
            String [] source = parts[0].trim().split("\\s+");
            String [] target = parts[1].trim().split("\\s+");
            Alignment alignment = new Alignment(parts[2].trim());

            for (int i = 0; i < source.length; i++) {
                Text src = new Text(source[i]);
                TextPair marginal = new TextPair(src, MARGINAL);
                if (alignment.sourceIsAligned(i)) {
                    for (int x : alignment.f2e[i]) {
                        Text tgt = new Text(target[x]);
                        TextPair tp = new TextPair(src, tgt);
                        counts.put(tp, counts.containsKey(tp) ? counts.get(tp) + 1 : 1);
                    }
                    counts.put(marginal, counts.containsKey(marginal) ? counts.get(marginal) + alignment.f2e[i].length : alignment.f2e[i].length);
                }
                else {
                    TextPair tp = new TextPair(src, UNALIGNED);
                    counts.put(tp, counts.containsKey(tp) ? counts.get(tp) + 1 : 1);
                    counts.put(marginal, counts.containsKey(marginal) ? counts.get(marginal) + 1 : 1);
                }
            }

            for (TextPair tp : counts.keySet()) {
                context.write(tp, new IntWritable(counts.get(tp)));
            }
        }
    }

    private static class Combine extends Reducer<TextPair, IntWritable, TextPair, IntWritable>
    {
        protected void reduce(TextPair key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
        {
            int total = 0;
            for (IntWritable x : values) {
                total += x.get();
            }
            context.write(key, new IntWritable(total));
        }
    }

    private static class Reduce extends Reducer<TextPair, IntWritable, TextPair, DoubleWritable>
    {
        private Text current = new Text();
        private int marginalCount;

        protected void reduce(TextPair key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException
        {
            if (!key.fst.equals(current)) {
                if (!key.snd.equals(MARGINAL))
                    return;
                current.set(key.fst);
                marginalCount = 0;
                for (IntWritable x : values)
                    marginalCount += x.get();
                return;
            }
            // control only gets here if we are using the same marginal
            int myCount = 0;
            for (IntWritable x : values)
                myCount += x.get();
            context.write(key, new DoubleWritable(-1 * Math.log(myCount / (double) marginalCount)));
        }
    }

    private static class Partition extends Partitioner<TextPair,IntWritable>
    {
        public int getPartition(TextPair key, IntWritable value, int numPartitions)
        {
            return key.fst.hashCode() % numPartitions;
        }
    }

    public static Job getJob() throws IOException
    {
        Job result = new Job();
        result.setMapperClass(Map.class);
        result.setReducerClass(Reduce.class);
        result.setCombinerClass(Combine.class);
        result.setPartitionerClass(Partition.class);
        result.setSortComparatorClass(TextPair.MarginalComparator.class);

        result.setMapOutputKeyClass(TextPair.class);
        result.setMapOutputValueClass(IntWritable.class);
        result.setOutputKeyClass(TextPair.class);
        result.setOutputValueClass(DoubleWritable.class);
        return result;
    }

    public static void main(String [] argv)
    {
        if (argv.length < 2) {
            System.err.println("usage: hadoop jar <jar> <input> <output>");
            return;
        }
        try {
            Job theJob = getJob();
            theJob.setJobName("lexprobs");
            theJob.setJarByClass(LexicalProbability.class);
            FileInputFormat.setInputPaths(theJob, new Path(argv[0]));
            FileOutputFormat.setOutputPath(theJob, new Path(argv[1]));
            theJob.submit();
            return;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

}

