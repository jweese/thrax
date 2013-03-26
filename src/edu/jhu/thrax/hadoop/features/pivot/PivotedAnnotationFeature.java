package edu.jhu.thrax.hadoop.features.pivot;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.AlignmentWritable;
import edu.jhu.thrax.hadoop.datatypes.Annotation;
import edu.jhu.thrax.hadoop.datatypes.FeatureMap;
import edu.jhu.thrax.hadoop.features.annotation.AnnotationPassthroughFeature;

public class PivotedAnnotationFeature implements PivotedFeature {

  private Annotation aggregated = null;

  public String getName() {
    return "alignment";
  }

  public Text getFeatureLabel() {
    return AnnotationPassthroughFeature.LABEL;
  }

  public Set<String> getPrerequisites() {
    Set<String> prereqs = new HashSet<String>();
    prereqs.add("alignment");
    return prereqs;
  }

  public Annotation pivot(FeatureMap src, FeatureMap tgt) {
    AlignmentWritable src_f2e = ((AlignmentWritable) src.get(new Text("Alignment")));
    AlignmentWritable tgt_f2e = ((AlignmentWritable) tgt.get(new Text("Alignment")));

    return new Annotation(src_f2e.join(tgt_f2e));
  }

  public void unaryGlueRuleScore(Text nt, Map<Text, Writable> map) {}

  public void binaryGlueRuleScore(Text nt, Map<Text, Writable> map) {}

  public void initializeAggregation() {
    aggregated = null;
  }

  public void aggregate(FeatureMap a) {
    Annotation annotation = (Annotation) a.get(AnnotationPassthroughFeature.LABEL);
    if (aggregated == null) {
      aggregated = new Annotation(annotation);
    } else {
      aggregated.setAlignment(aggregated.f2e().intersect(annotation.f2e()));
      aggregated.merge(annotation);
    }
  }

  public Annotation finalizeAggregation() {
    return aggregated;
  }

  @Override
  public Set<Text> getLowerBoundLabels() {
    return null;
  }

  @Override
  public Set<Text> getUpperBoundLabels() {
    return null;
  }
}
