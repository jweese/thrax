package edu.jhu.thrax.hadoop.distributional;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.ArrayPrimitiveWritable;

/**
 * A union-like writable that contains a set of context features.
 * 
 * @author Juri Ganitkevitch
 * 
 */
public class ContextWritable implements Writable {
  public IntWritable strength;
  public BooleanWritable compacted;
  public MapWritable map;
  public ArrayPrimitiveWritable sums;

  public ContextWritable() {
    this(false);
  }
  
  public ContextWritable(boolean compacted) {
    this.strength = new IntWritable(0);
    this.compacted = new BooleanWritable(compacted);
    if (compacted) {
      this.map = null;
      this.sums = new ArrayPrimitiveWritable(float.class);
    } else {
      this.map = new MapWritable();
      this.sums = null;
    }
  }

  public ContextWritable(int strength, MapWritable map) {
    this.strength = new IntWritable(strength);
    this.compacted = new BooleanWritable(false);
    this.map = new MapWritable(map);
    this.sums = null;
  }

  public ContextWritable(int strength, float[] sums) {
    this.strength = new IntWritable(strength);
    this.compacted = new BooleanWritable(true);
    this.map = null;
    this.sums = new ArrayPrimitiveWritable(sums);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    strength.readFields(in);
    compacted.readFields(in);
    if (compacted.get()) {
      map = null;
      if (sums == null) sums = new ArrayPrimitiveWritable(float.class);
      sums.readFields(in);
    } else {
      if (map == null) map = new MapWritable();
      map.readFields(in);
      sums = null;
    }
  }

  @Override
  public void write(DataOutput out) throws IOException {
    strength.write(out);
    compacted.write(out);
    if (compacted.get()) {
      sums.write(out);
    } else {
      map.write(out);
    }
  }
}
