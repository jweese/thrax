package edu.jhu.thrax.hadoop.features;

import java.util.List;
import java.util.ArrayList;

public class FeatureFactory
{
    public static void get(List<Feature> list, String name)
    {
        if (name.equals("lex")) {
            list.add(new LexicalProbabilityFeature());
        }
        else if (name.equals("phrase")) {
            list.add(new SourcePhraseGivenTargetFeature());
            list.add(new TargetPhraseGivenSourceFeature());
        }
        else if (name.equals("samt")) {
            list.add(new SAMTFeatureSet());
        }
    }

    public static List<Feature> getAll(String [] names)
    {
        List<Feature> result = new ArrayList<Feature>();
        for (String s : names)
            get(result, s);
        return result;
    }
}
