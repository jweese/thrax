package edu.jhu.thrax.hadoop.extraction;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class ExtractionMapper extends Mapper<LongWritable, Text,
                                             RuleWritable, IntWritable>
{
    private RuleWritableExtractor extractor;
    private IntWritable one = new IntWritable(1);

    protected void setup(Context context) throws IOException, InterruptedException
    {
		extractor = RuleWritableExtractorFactory.create(context);
		if (extractor == null) {
			System.err.println("WARNING: could not create rule extractor as configured!");
		}
    }

    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
        if (extractor == null)
            return;
		for (RuleWritable r : extractor.extract(value))
			context.write(r, one);
		context.progress();
    }
}

