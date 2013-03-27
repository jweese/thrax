package edu.jhu.thrax.hadoop.features.pivot;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.FeatureMap;

public class PivotedRarityPenaltyFeature implements PivotedFeature {

  private static final Text LABEL = new Text("RarityPenalty");

  private static final FloatWritable ZERO = new FloatWritable(0.0f);
  
  private static final float RENORMALIZE = (float) Math.exp(-1);

  private float aggregated_rp;

  public String getName() {
    return "rarity";
  }

  public Text getFeatureLabel() {
    return LABEL;
  }

  public Set<String> getPrerequisites() {
    Set<String> prereqs = new HashSet<String>();
    prereqs.add("rarity");
    return prereqs;
  }

  public FloatWritable pivot(FeatureMap a, FeatureMap b) {
    float a_rp = ((FloatWritable) a.get(new Text("RarityPenalty"))).get();
    float b_rp = ((FloatWritable) b.get(new Text("RarityPenalty"))).get();
    return new FloatWritable(Math.max(a_rp, b_rp));
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {
    map.put(LABEL, ZERO);
  }

  public void initializeAggregation() {
    aggregated_rp = -1;
  }

  public void aggregate(FeatureMap a) {
    float rp = ((FloatWritable) a.get(LABEL)).get();
    if (aggregated_rp == -1) {
      aggregated_rp = rp;
    } else {
      // Rarity is exp(1 - count). To compute rarity over a sum of counts:
      // rarity_{1+2} = exp(1 - (count_1 + count_2)) = exp(1 - count_1) * exp(-count_2) = 
      //     = exp(1 - count_1) * exp(1 - count_2) * exp(-1) = rarity_1 * rarity_2 * exp(-1) 
      aggregated_rp *= rp * RENORMALIZE;
    }
  }

  public FloatWritable finalizeAggregation() {
    return new FloatWritable(aggregated_rp);
  }

  @Override
  public Set<Text> getLowerBoundLabels() {
    Set<Text> lower_bound_labels = new HashSet<Text>();
    lower_bound_labels.add(new Text("RarityPenalty"));
    return lower_bound_labels;
  }

  @Override
  public Set<Text> getUpperBoundLabels() {
    return null;
  }
}
