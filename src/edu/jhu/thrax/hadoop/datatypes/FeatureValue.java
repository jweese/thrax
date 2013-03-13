package edu.jhu.thrax.hadoop.datatypes;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.GenericWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class FeatureValue extends GenericWritable {

  @SuppressWarnings("rawtypes")
  private static Class[] TYPES = {DoubleWritable.class, IntWritable.class, Text.class};

  FeatureValue() {}
  
  FeatureValue(Writable val) {
    this.set(val);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected Class<? extends Writable>[] getTypes() {
    return TYPES;
  }
}