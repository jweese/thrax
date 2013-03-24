package edu.jhu.thrax.hadoop.datatypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

public class Annotation implements Writable {

  private static final int MAX_NUM_ALIGNMENTS = 50;
  private static final int MIN_ALIGNMENT_COUNT = 3;

  // Internal alignments seen with the rule, and their occurrence counts.
  private Map<AlignmentWritable, Integer> e2f_alignments = null;
  private Map<AlignmentWritable, Integer> f2e_alignments = null;

  private boolean maxed = false;

  private AlignmentWritable e2f = null;
  private AlignmentWritable f2e = null;

  // Rule occurrence count.
  private int count;

  public Annotation() {
    e2f_alignments = new HashMap<AlignmentWritable, Integer>();
    f2e_alignments = new HashMap<AlignmentWritable, Integer>();

    count = 0;
  }

  public Annotation(AlignmentWritable f2e, AlignmentWritable e2f) {
    e2f_alignments = new HashMap<AlignmentWritable, Integer>();
    f2e_alignments = new HashMap<AlignmentWritable, Integer>();

    count = 1;

    e2f_alignments.put(e2f, 1);
    f2e_alignments.put(f2e, 1);
  }

  public void merge(Annotation that) {
    this.count += that.count;

    if (!maxed) {
      mergeAlignments(this.e2f_alignments, that.e2f_alignments);
      mergeAlignments(this.f2e_alignments, that.f2e_alignments);
    }
  }

  public void retainMaxAlignment() {
    if (!maxed) {
      e2f = getMaxAlignment(e2f_alignments);
      f2e = getMaxAlignment(f2e_alignments);
      e2f_alignments = null;
      f2e_alignments = null;
    }
    maxed = true;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    maxed = false;
    count = WritableUtils.readVInt(in);
    if (count < 0) {
      count = -count;
      maxed = true;
    }
    if (!maxed) {
      e2f_alignments = readAlignments(in);
      f2e_alignments = readAlignments(in);
    } else {
      e2f = new AlignmentWritable();
      f2e = new AlignmentWritable();
      e2f.readFields(in);
      f2e.readFields(in);
    }
  }

  @Override
  public void write(DataOutput out) throws IOException {
    WritableUtils.writeVInt(out, (maxed ? -count : count));
    if (!maxed) {
      writeAlignments(out, e2f_alignments);
      writeAlignments(out, f2e_alignments);
    } else {
      e2f.write(out);
      f2e.write(out);
    }
  }

  public AlignmentWritable e2f() {
    return e2f;
  }

  public AlignmentWritable f2e() {
    return f2e;
  }

  public int count() {
    return count;
  }

  private static Map<AlignmentWritable, Integer> readAlignments(DataInput in) throws IOException {
    HashMap<AlignmentWritable, Integer> alignments = new HashMap<AlignmentWritable, Integer>();
    int align_count = WritableUtils.readVInt(in);
    for (int i = 0; i < align_count; ++i) {
      int aw_count = WritableUtils.readVInt(in);
      AlignmentWritable aw = new AlignmentWritable();
      aw.readFields(in);
      alignments.put(aw, aw_count);
    }
    return alignments;
  }

  private static void writeAlignments(DataOutput out, Map<AlignmentWritable, Integer> alignments)
      throws IOException {
    WritableUtils.writeVInt(out, alignments.size());
    for (AlignmentWritable aw : alignments.keySet()) {
      WritableUtils.writeVInt(out, alignments.get(aw));
      aw.write(out);
    }
  }

  private static void mergeAlignments(Map<AlignmentWritable, Integer> to,
      Map<AlignmentWritable, Integer> from) {
    for (Entry<AlignmentWritable, Integer> aw : from.entrySet()) {
      Integer to_count = to.get(aw.getKey());
      if (to_count == null) {
        to.put(aw.getKey(), aw.getValue());
      } else {
        to.put(aw.getKey(), to_count + aw.getValue());
      }
    }
    // TODO: awful heuristic.
    if (to.size() > MAX_NUM_ALIGNMENTS) {
      Iterator<Entry<AlignmentWritable, Integer>> iter = to.entrySet().iterator();
      while (iter.hasNext()) {
        Entry<AlignmentWritable, Integer> entry = iter.next();
        if (entry.getValue() < MIN_ALIGNMENT_COUNT) iter.remove();
      }
    }
  }

  private static AlignmentWritable getMaxAlignment(Map<AlignmentWritable, Integer> alignments) {
    AlignmentWritable best = null;
    int max = Integer.MIN_VALUE;
    for (Entry<AlignmentWritable, Integer> e : alignments.entrySet()) {
      if (e.getValue() > max) {
        max = e.getValue();
        best = e.getKey();
      }
    }
    return best;
  }
}
