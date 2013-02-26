package edu.jhu.thrax.hadoop.extraction;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.util.Vocabulary;

public class ExtractionMapper extends Mapper<LongWritable, Text, RuleWritable, Annotation> {
  private RuleWritableExtractor extractor;

  protected void setup(Context context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    String vocabulary_path = conf.getRaw("thrax.work-dir") + "vocabulary/part-r-00000";
    Vocabulary.read(conf, vocabulary_path);
    
    // TODO: static initializer call for what annotations ExtractionStuff carries would go here.

    extractor = RuleWritableExtractorFactory.create(context);
    if (extractor == null) {
      System.err.println("WARNING: could not create rule extractor as configured!");
    }
  }

  protected void map(LongWritable key, Text value, Context context) throws IOException,
      InterruptedException {
    if (extractor == null) return;
    for (AnnotatedRule ar : extractor.extract(value)) {
      context.write(ar.rule, ar.annotation);
    }
    context.progress();
  }
}
