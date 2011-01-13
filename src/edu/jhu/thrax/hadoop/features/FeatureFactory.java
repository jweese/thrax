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
        else if (name.equals("lexical")) {
            return new LexicalityFeature();
        }
        else if (name.equals("abstract")) {
            return new AbstractnessFeature();
        }
        else if (name.equals("adjacent")) {
            return new AdjacentNonTerminalsFeature();
        }
        else if (name.equals("x-rule")) {
            return new XRuleFeature();
        }
        else if (name.equals("source-terminals-without-target")) {
            return new ConsumeSourceTerminalsFeature();
        }
        else if (name.equals("target-terminals-without-source")) {
            return new ProduceTargetTerminalsFeature();
        }
        else if (name.equals("monotonic")) {
            return new MonotonicFeature();
        }
        else if (name.equals("phrase-penalty")) {
            return new PhrasePenaltyFeature();
        }
        else if (name.equals("target-word-count")) {
            return new TargetWordCounterFeature();
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

