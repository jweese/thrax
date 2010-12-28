package edu.jhu.thrax.hadoop.features;

import java.util.List;
import java.util.ArrayList;

public class FeatureFactory
{
    private List<SimpleFeature> simpleFeatures;
    private List<MapReduceFeature> mapReduceFeatures;

    public FeatureFactory(String feats)
    {
        simpleFeatures = new ArrayList<SimpleFeature>();
        mapReduceFeatures = new ArrayList<MapReduceFeature>();

        String [] fs = feats.split("\\s+");
        for (String f : fs) {
            Feature curr = get(f);
            if (curr instanceof SimpleFeature) {
                simpleFeatures.add((SimpleFeature) curr);
            }
            else if (curr instanceof MapReduceFeature) {
                mapReduceFeatures.add((MapReduceFeature) curr);
            }
        }
    }

    private Feature get(String name)
    {
        if (name.equals("lexprob")) {
            return new LexicalProbabilityFeature();
        }
        else if (name.equals("e2fphrase")) {
            return new SourcePhraseGivenTargetFeature();
        }
        else if (name.equals("f2ephrase")) {
            return new TargetPhraseGivenSourceFeature();
        }
        else if (name.equals("samt")) {
            return new SAMTFeatureSet();
        }
        return null;
    }

    public List<SimpleFeature> getSimpleFeatures()
    {
        return simpleFeatures;
    }

    public List<MapReduceFeature> getMapReduceFeatures()
    {
        return mapReduceFeatures;
    }
}

