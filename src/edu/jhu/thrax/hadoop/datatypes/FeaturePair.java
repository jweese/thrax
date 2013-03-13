package edu.jhu.thrax.hadoop.datatypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class FeaturePair implements Writable {
  public Text key;
  public FeatureValue val;

  public FeaturePair() {
    key = new Text();
    val = new FeatureValue();
  }

  public FeaturePair(Text k, Writable v) {
    key = k;
    val = new FeatureValue(v);
  }

  public void write(DataOutput out) throws IOException {
    key.write(out);
    val.write(out);
  }

  public void readFields(DataInput in) throws IOException {
    key.readFields(in);
    val.readFields(in);
  }

  public int hashCode() {
    return key.hashCode() * 163 + val.hashCode();
  }

  public boolean equals(Object o) {
    if (o instanceof FeaturePair) {
      FeaturePair that = (FeaturePair) o;
      return key.equals(that.key) && val.equals(that.val);
    }
    return false;
  }

  public String toString() {
    return key.toString() + "=" + val.toString();
  }
}
