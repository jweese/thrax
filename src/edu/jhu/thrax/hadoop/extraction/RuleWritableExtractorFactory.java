package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class RuleWritableExtractorFactory {
  public static RuleWritableExtractor create(
      Mapper<LongWritable, Text, RuleWritable, Annotation>.Context context) {
    return new HierarchicalRuleWritableExtractor(context);
  }
}
