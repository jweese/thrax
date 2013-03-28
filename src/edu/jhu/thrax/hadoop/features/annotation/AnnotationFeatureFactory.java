package edu.jhu.thrax.hadoop.features.annotation;

import java.util.ArrayList;
import java.util.List;

import edu.jhu.thrax.util.FormatUtils;

public class AnnotationFeatureFactory {

  public static AnnotationFeature get(String name) {
    if (name.equals("unaligned-source"))
      return new UnalignedSourceCounterFeature();
    else if (name.equals("unaligned-target"))
      return new UnalignedTargetCounterFeature();
    else if (name.equals("rarity"))
      return new RarityPenaltyFeature();
    else if (name.equals("count"))
      return new CountFeature();
    else if (name.equals("logcount"))
      return new CountFeature();
    else if (name.equals("f_given_e_lex"))
      return new SourceGivenTargetLexicalProbabilityFeature();
    else if (name.equals("e_given_f_lex"))
      return new TargetGivenSourceLexicalProbabilityFeature();
    else if (name.equals("alignment")) return new AlignmentFeature();

    return null;
  }

  public static List<AnnotationFeature> getAll(String names) {
    String[] feature_names = FormatUtils.P_COMMA_OR_SPACE.split(names);
    List<AnnotationFeature> features = new ArrayList<AnnotationFeature>();

    for (String feature_name : feature_names) {
      AnnotationFeature feature = get(feature_name);
      if (feature != null) features.add(feature);
    }
    return features;
  }
}
