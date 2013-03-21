package edu.jhu.thrax.util;

public class BackwardsCompatibility {

  public static String equivalent(String features) {
    features = features.replace("e2fphrase", "f_given_e_phrase");
    features = features.replace("f2ephrase", "e_given_f_phrase");

    features = features.replace("lexprob", "e_given_f_lex f_given_e_lex");
    features = features.replace("unaligned-count", "unaligned-source unaligned-target");

    return features;
  }
}
