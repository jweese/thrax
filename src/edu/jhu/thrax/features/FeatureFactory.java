package edu.jhu.thrax.features;

import edu.jhu.thrax.util.UnknownFeatureException;

public class FeatureFactory {

    /**
     * Given the name of a feature function, return an instance of that Feature.
     * If you create a new feature function, you should be sure that it is
     * checked for in this method.
     *
     * @param feat the name of the feature function
     * @return a Feature of the named type
     * @throws UnknownFeatureException if the feature is of an unknown type
     */
    public static Feature create(String feat) throws UnknownFeatureException
    {
        String fname = feat.toLowerCase();

        // when you write new feature functions, put them in this block
        if (fname.equals(LexicalProbabilityFeature.name)) {
            return new LexicalProbabilityFeature();
        }
        else if (fname.equals(RuleProbabilityFeature.name)) {
            return new RuleProbabilityFeature();
        }
        else if (fname.equals(PhrasalProbabilityFeature.name)) {
            return new PhrasalProbabilityFeature();
        }
        else if (fname.equals(UnalignedWordCountFeature.name)) {
            return new UnalignedWordCountFeature();
        }

        // put the new names here
        else {
            throw new UnknownFeatureException(feat);
        }

    }

    public static Feature [] createAll(String [] feats) throws UnknownFeatureException
    {
        Feature [] ret = new Feature[feats.length];
        for (int i = 0; i < feats.length; i++) {
            ret[i] = create(feats[i]);
        }
        return ret;
    }

}
