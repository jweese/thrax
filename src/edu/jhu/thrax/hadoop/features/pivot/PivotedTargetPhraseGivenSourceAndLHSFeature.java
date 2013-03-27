package edu.jhu.thrax.hadoop.features.pivot;

import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;

import edu.jhu.thrax.hadoop.datatypes.FeatureMap;

public class PivotedTargetPhraseGivenSourceAndLHSFeature extends PivotedNegLogProbFeature {

  private static final Text LABEL = new Text("p(e|f,LHS)");

  public String getName() {
    return "e_given_f_and_lhs";
  }

  public Text getFeatureLabel() {
    return LABEL;
  }

  public Set<String> getPrerequisites() {
    Set<String> prereqs = new HashSet<String>();
    prereqs.add("e_given_f_and_lhs");
    prereqs.add("f_given_e_and_lhs");
    return prereqs;
  }

  public FloatWritable pivot(FeatureMap src, FeatureMap tgt) {
    float fge = ((FloatWritable) tgt.get(new Text("p(e|f,LHS)"))).get();
    float egf = ((FloatWritable) src.get(new Text("p(f|e,LHS)"))).get();

    return new FloatWritable(egf + fge);
  }

  @Override
  public Set<Text> getLowerBoundLabels() {
    Set<Text> lower_bound_labels = new HashSet<Text>();
    lower_bound_labels.add(new Text("p(e|f,LHS)"));
    lower_bound_labels.add(new Text("p(f|e,LHS)"));
    return lower_bound_labels;
  }

  @Override
  public Set<Text> getUpperBoundLabels() {
    return null;
  }
}
