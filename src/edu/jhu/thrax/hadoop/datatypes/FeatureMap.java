package edu.jhu.thrax.hadoop.datatypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

import edu.jhu.thrax.hadoop.features.annotation.AnnotationPassthroughFeature;

public class FeatureMap extends HashMap<Text, Writable> implements Writable {

  private static final long serialVersionUID = 219677543309018120L;

  public FeatureMap() {
    super();
  }
  
  public FeatureMap(FeatureMap fm) {
    super();
    for (Text key : fm.keySet())
      this.put(key, fm.get(key));
  }
  
  
  @Override
  public void readFields(DataInput in) throws IOException {
    this.clear();
    int size = WritableUtils.readVInt(in);
    for (int i = 0; i < size; ++i) {
      Text key = new Text();
      Writable val = null;
      key.readFields(in);
      if (key.equals(AnnotationPassthroughFeature.LABEL)) {
        val = new Annotation();
        val.readFields(in);
      } else {
        val = new FloatWritable();
        val.readFields(in);
      }
      this.put(key, val);
    }
  }

  @Override
  public void write(DataOutput out) throws IOException {
    WritableUtils.writeVInt(out, this.size());
    for (Text key : this.keySet()) {
      key.write(out);
      if (key.equals(AnnotationPassthroughFeature.LABEL)) {
        ((Annotation) this.get(key)).write(out);
      } else {
        ((FloatWritable) this.get(key)).write(out);
      }
    }
  }
}
