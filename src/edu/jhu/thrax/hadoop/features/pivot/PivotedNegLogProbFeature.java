package edu.jhu.thrax.hadoop.features.pivot;

import java.util.Map;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.FeatureMap;
import edu.jhu.thrax.util.NegLogMath;

public abstract class PivotedNegLogProbFeature implements PivotedFeature {

  private static final FloatWritable ONE_PROB = new FloatWritable(0.0f);

  private float aggregated;

  public void initializeAggregation() {
    aggregated = 64.0f;
  }

  public void aggregate(FeatureMap features) {
    FloatWritable val = (FloatWritable) features.get(getFeatureLabel());
    aggregated = NegLogMath.logAdd(aggregated, val.get());
  }

  public FloatWritable finalizeAggregation() {
    return new FloatWritable(aggregated);
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(getFeatureLabel(), ONE_PROB);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(getFeatureLabel(), ONE_PROB);
  }
}
