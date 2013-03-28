package edu.jhu.thrax.hadoop.features.pivot;

import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;

import edu.jhu.thrax.hadoop.datatypes.FeatureMap;

public class PivotedTargetPhraseGivenLHSFeature extends NonAggregatingPivotedFeature {

  private static final Text LABEL = new Text("p(e|LHS)");

  public String getName() {
    return "e_given_lhs";
  }

  public Text getFeatureLabel() {
    return LABEL;
  }

  public Set<String> getPrerequisites() {
    Set<String> prereqs = new HashSet<String>();
    prereqs.add("e_given_lhs");
    return prereqs;
  }

  public FloatWritable pivot(FeatureMap src, FeatureMap tgt) {
    return new FloatWritable(((FloatWritable) tgt.get(new Text("p(e|LHS)"))).get());
  }

  @Override
  public Set<Text> getLowerBoundLabels() {
    Set<Text> lower_bound_labels = new HashSet<Text>();
    lower_bound_labels.add(new Text("p(e|LHS)"));
    return lower_bound_labels;
  }

  @Override
  public Set<Text> getUpperBoundLabels() {
    return null;
  }
}
