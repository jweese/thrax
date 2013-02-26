package edu.jhu.thrax.hadoop.jobs;

import edu.jhu.thrax.hadoop.features.mapred.InvariantLhsGivenTargetPhraseFeature;
import edu.jhu.thrax.hadoop.features.mapred.InvariantTargetPhraseGivenLHSFeature;
import edu.jhu.thrax.hadoop.features.mapred.LexicalProbabilityFeature;
import edu.jhu.thrax.hadoop.features.mapred.LhsGivenSourcePhraseFeature;
import edu.jhu.thrax.hadoop.features.mapred.LhsGivenTargetPhraseFeature;
import edu.jhu.thrax.hadoop.features.mapred.MapReduceFeature;
import edu.jhu.thrax.hadoop.features.mapred.RarityPenaltyFeature;
import edu.jhu.thrax.hadoop.features.mapred.SourcePhraseGivenLHSFeature;
import edu.jhu.thrax.hadoop.features.mapred.SourcePhraseGivenTargetFeature;
import edu.jhu.thrax.hadoop.features.mapred.SourcePhraseGivenTargetandLHSFeature;
import edu.jhu.thrax.hadoop.features.mapred.TargetPhraseGivenLHSFeature;
import edu.jhu.thrax.hadoop.features.mapred.TargetPhraseGivenSourceFeature;
import edu.jhu.thrax.hadoop.features.mapred.TargetPhraseGivenSourceandLHSFeature;

public class FeatureJobFactory {

  public static MapReduceFeature get(String name) {
    if (name.equals("e2fphrase"))
      return new SourcePhraseGivenTargetFeature();
    else if (name.equals("f2ephrase"))
      return new TargetPhraseGivenSourceFeature();
    else if (name.equals("rarity"))
      return new RarityPenaltyFeature();
    else if (name.equals("lexprob"))
      return new LexicalProbabilityFeature();
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
    else if (name.equals("e_given_f_and_lhs")) 
      return new TargetPhraseGivenSourceandLHSFeature();

    return null;
  }
}
