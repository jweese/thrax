package edu.jhu.thrax.hadoop.distributional;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import edu.jhu.jerboa.sim.Signature;

public class SignatureWritable implements WritableComparable<SignatureWritable> {
  public Text key;
  public IntWritable count;
  public BytesWritable bytes;
  public IntWritable strength;


  public SignatureWritable() {
    this.key = new Text();
    this.count = new IntWritable();
    this.bytes = new BytesWritable();
    this.strength = new IntWritable();
  }

  public SignatureWritable(Text key, Signature signature, int strength, int count) {
    this.key = new Text(key);
    this.count = new IntWritable(count);
    this.bytes = new BytesWritable(signature.bytes);
    this.strength = new IntWritable(strength);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    key.readFields(in);
    count.readFields(in);
    bytes.readFields(in);
    strength.readFields(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    key.write(out);
    count.write(out);
    bytes.write(out);
    strength.write(out);
  }

  @Override
  public int compareTo(SignatureWritable that) {
    int cmp = count.compareTo(that.count);
    // Flip sign for descending sort order.
    if (cmp != 0) return -cmp;
    cmp = key.compareTo(that.key);
    if (cmp != 0) return cmp;
    cmp = strength.compareTo(that.strength);
    if (cmp != 0) return cmp;
    return bytes.compareTo(that.bytes);
  }
}
