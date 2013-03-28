package edu.jhu.thrax.hadoop.features.pivot;

import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;

import edu.jhu.thrax.hadoop.datatypes.FeatureMap;

public class PivotedTargetPhraseGivenSourceFeature extends PivotedNegLogProbFeature {

  private static final Text LABEL = new Text("p(e|f)");

  public String getName() {
    return "e_given_f";
  }

  public Text getFeatureLabel() {
    return LABEL;
  }

  public Set<String> getPrerequisites() {
    Set<String> prereqs = new HashSet<String>();
    prereqs.add("e2fphrase");
    prereqs.add("f2ephrase");
    return prereqs;
  }

  public FloatWritable pivot(FeatureMap src, FeatureMap tgt) {
    float tgt_f = ((FloatWritable) tgt.get(new Text("p(e|f)"))).get();
    float f_src = ((FloatWritable) src.get(new Text("p(f|e)"))).get();

    return new FloatWritable(tgt_f + f_src);
  }

  @Override
  public Set<Text> getLowerBoundLabels() {
    Set<Text> lower_bound_labels = new HashSet<Text>();
    lower_bound_labels.add(new Text("p(e|f)"));
    lower_bound_labels.add(new Text("p(f|e)"));
    return lower_bound_labels;
  }

  @Override
  public Set<Text> getUpperBoundLabels() {
    return null;
  }
}
