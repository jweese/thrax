package edu.jhu.thrax.hadoop.datatypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

public class AlignmentWritable implements Writable {

  public byte[] points;
  // public float[] counts;
  public int total;

  public static final Text SGT_KEY = new Text("e2f_align");
  public static final Text TGS_KEY = new Text("f2e_align");

  public AlignmentWritable() {
    points = new byte[0];
    // counts = new float[0];
    total = 1;
  }

  public AlignmentWritable(byte[] p) {
    points = p;
    // counts = new float[p.length / 2];
    total = 1;
  }

  public AlignmentWritable(byte[] p, float[] c) {
    points = p;
    // counts = c;
    total = 1;
  }

  public AlignmentWritable(byte[] p, float[] c, int t) {
    points = p;
    // counts = c;
    total = t;
  }

  public AlignmentWritable(AlignmentWritable r) {
    this.set(r);
  }

  public void set(AlignmentWritable r) {
    points = r.points;
    // counts = r.counts;
    total = r.total;
  }

  public void write(DataOutput out) throws IOException {
    PrimitiveUtils.writeByteArray(out, points);
    WritableUtils.writeVInt(out, total);
    // if (total > 1) PrimitiveUtils.writeFloatArray(out, counts);
  }

  public void readFields(DataInput in) throws IOException {
    points = PrimitiveUtils.readByteArray(in);
    total = WritableUtils.readVInt(in);
    // if (total > 1) counts = PrimitiveUtils.readFloatArray(in);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < points.length / 2; ++i) {
      if (i != 0) sb.append(" ");
      sb.append(points[2 * i]);
      sb.append("-");
      sb.append(points[2 * i + 1]);
    }
    return sb.toString();
    // return new String(points, Charset.forName("UTF-8"));
  }
}
