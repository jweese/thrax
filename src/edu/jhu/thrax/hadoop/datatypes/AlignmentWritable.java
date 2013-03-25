package edu.jhu.thrax.hadoop.datatypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class AlignmentWritable implements WritableComparable<AlignmentWritable> {

  public static final Text SGT_KEY = new Text("e2f_align");
  public static final Text TGS_KEY = new Text("f2e_align");

  public byte[] points;

  // Cached target-to-source alignment.
  private AlignmentWritable flipped = null;

  public AlignmentWritable() {
    points = new byte[0];
  }

  public AlignmentWritable(byte[] p) {
    points = p;
  }

  public AlignmentWritable(byte[] p, float[] c) {
    points = p;
  }

  public AlignmentWritable(byte[] p, float[] c, int t) {
    points = p;
  }

  public AlignmentWritable(AlignmentWritable r) {
    this.set(r);
  }

  public void set(AlignmentWritable r) {
    points = r.points;
  }

  public AlignmentWritable flip() {
    if (flipped == null) {
      Integer[] flipside_points = new Integer[points.length / 2];
      for (int i = 0; i < flipside_points.length; ++i)
        flipside_points[i] = i;
      Arrays.sort(flipside_points, new Comparator<Integer>() {
        public int compare(Integer a, Integer b) {
          return PrimitiveUtils.compare(points[2 * a + 1], points[2 * b + 1]);
        }
      });

      byte[] flipside = new byte[points.length];
      for (int i = 0; i < flipside_points.length; ++i) {
        flipside[2 * i] = points[2 * flipside_points[i] + 1];
        flipside[2 * i + 1] = points[2 * flipside_points[i]];
      }
      flipped = new AlignmentWritable(flipside);
    }
    return flipped;
  }

  public void write(DataOutput out) throws IOException {
    PrimitiveUtils.writeByteArray(out, points);
  }

  public void readFields(DataInput in) throws IOException {
    points = PrimitiveUtils.readByteArray(in);
  }

  public String toString(String glue) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < points.length / 2; ++i) {
      if (i != 0) sb.append(glue);
      sb.append(points[2 * i]);
      sb.append("-");
      sb.append(points[2 * i + 1]);
    }
    return sb.toString();
  }

  public String toString() {
    return toString(" ");
  }

  public int compareTo(AlignmentWritable that) {
    return PrimitiveUtils.compareByteArrays(this.points, that.points);
  }

  static {
    WritableComparator.define(AlignmentWritable.class, new AlignmentComparator());
  }

  public static final class AlignmentComparator extends WritableComparator {

    public AlignmentComparator() {
      super(AlignmentWritable.class);
    }

    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
      return WritableComparator.compareBytes(b1, s1, l1, b2, s2, l2);
    }
  }
}
