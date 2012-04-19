package edu.jhu.thrax.hadoop.features.pivot;

import java.util.Map;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public abstract class NonAggregatingPivotedFeature implements PivotedFeature {

  private static final DoubleWritable ZERO = new DoubleWritable(0.0);

  private double value;

  public void initializeAggregation() {
    value = Double.MAX_VALUE;
  }

  public void aggregate(MapWritable features) {
    DoubleWritable val = (DoubleWritable) features.get(getFeatureLabel());
    if (value == Double.MAX_VALUE) {
      value = val.get();
    } else {
      if (value != val.get())
        throw new RuntimeException("Diverging values in pseudo-aggregation.");
    }
  }

  public DoubleWritable finalizeAggregation() {
    return new DoubleWritable(value);
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(getFeatureLabel(), ZERO);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(getFeatureLabel(), ZERO);
  }
}
