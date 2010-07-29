package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.datatypes.Rule;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.extraction.RuleExtractor;
import edu.jhu.thrax.extraction.RuleExtractorFactory;
import edu.jhu.thrax.util.UnknownGrammarTypeException;

import java.io.IOException;

public class ExtractionMapper extends Mapper<LongWritable, Text,
                                             RuleWritable, IntWritable>
{
    private RuleExtractor extractor;
    private IntWritable one = new IntWritable(1);

    protected void setup(Context context)
    {
        try {
            extractor = RuleExtractorFactory.create(ThraxConfig.GRAMMAR);
        }
        catch (UnknownGrammarTypeException ex) {
            // do nothing? I don't know what to do here
        }
    }

    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
        String line = value.toString();
        String [] inputs = line.split(ThraxConfig.DELIMITER_REGEX);
        for (Rule r : extractor.extract(inputs))
            context.write(new RuleWritable(r), one);
    }
}

