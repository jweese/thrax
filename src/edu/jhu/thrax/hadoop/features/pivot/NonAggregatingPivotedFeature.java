package edu.jhu.thrax.hadoop.features.pivot;

import java.util.Map;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.FeatureMap;

public abstract class NonAggregatingPivotedFeature implements PivotedFeature {

  private static final FloatWritable ZERO = new FloatWritable(0.0f);

  private float value;

  public void initializeAggregation() {
    value = Float.MAX_VALUE;
  }

  public void aggregate(FeatureMap features) {
    FloatWritable val = (FloatWritable) features.get(getFeatureLabel());
    if (value == Float.MAX_VALUE) {
      value = val.get();
    } else {
      if (value != val.get()) {
        throw new RuntimeException("Diverging values in pseudo-aggregation: " + value + " versus "
            + val.get());
      }
    }
  }

  public FloatWritable finalizeAggregation() {
    return new FloatWritable(value);
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(getFeatureLabel(), ZERO);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(getFeatureLabel(), ZERO);
  }
}
