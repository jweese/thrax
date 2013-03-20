package edu.jhu.thrax.hadoop.features.mapred;

import java.util.ArrayList;
import java.util.List;

import edu.jhu.thrax.util.FormatUtils;

public class MapReduceFeatureFactory {

  public static MapReduceFeature get(String name) {
    if (name.equals("f_given_e_phrase"))
      return new SourcePhraseGivenTargetFeature();
    else if (name.equals("e_given_f_phrase"))
      return new TargetPhraseGivenSourceFeature();
    else if (name.equals("f_given_lhs"))
      return new SourcePhraseGivenLHSFeature();
    else if (name.equals("lhs_given_f"))
      return new LhsGivenSourcePhraseFeature();
    else if (name.equals("f_given_e_and_lhs"))
      return new SourcePhraseGivenTargetandLHSFeature();
    else if (name.equals("e_given_lhs"))
      return new TargetPhraseGivenLHSFeature();
    else if (name.equals("lhs_given_e"))
      return new LhsGivenTargetPhraseFeature();
    else if (name.equals("e_inv_given_lhs"))
      return new InvariantTargetPhraseGivenLHSFeature();
    else if (name.equals("lhs_given_e_inv"))
      return new InvariantLhsGivenTargetPhraseFeature();
    else if (name.equals("e_given_f_and_lhs")) return new TargetPhraseGivenSourceandLHSFeature();

    return null;
  }

  public static List<MapReduceFeature> getAll(String names) {
    String[] feature_names = FormatUtils.P_COMMA_OR_SPACE.split(names);
    List<MapReduceFeature> features = new ArrayList<MapReduceFeature>();

    for (String feature_name : feature_names) {
      MapReduceFeature feature = get(feature_name);
      if (feature != null) features.add(feature);
    }
    return features;
  }
}
