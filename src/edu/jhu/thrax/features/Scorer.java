package edu.jhu.thrax.features;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.datatypes.Rule;

public class Scorer {

    private ArrayList<Feature> features;
    private int featureLength;

    private Map<Rule,double []> rules;

    public Scorer()
    {
        features = new ArrayList<Feature>();
        featureLength = 0;
        rules = new HashMap<Rule,double []>();
    }

    public List<Feature> features()
    {
        return features;
    }

    public void addFeature(Feature f)
    {
        features.add(f);
        featureLength += f.length();
    }

    public void noteExtraction(Rule r)
    {
        for (Feature f : features)
            f.noteExtraction(r);
    }

    public void score(Rule r)
    {
        int idx = 0;
        if (rules.containsKey(r) && rules.get(r) != null) {
            double [] currScores = rules.get(r);
            for (Feature f : features) {
                double [] scores = null;
                int scoreIdx = 0;
                for (AggregationStyle style : f.aggregationStyles()) {
                    switch (style) {
                    case NONE:
                        break;
                    case MAX:
                        if (scores == null) scores = f.score(r);
                        if (scores[scoreIdx] > currScores[idx])
                            currScores[idx] = scores[scoreIdx];
                        break;
                    case MIN:
                        if (scores == null) scores = f.score(r);
                        if (scores[scoreIdx] < currScores[idx])
                            currScores[idx] = scores[scoreIdx];
                        break;
                    default:
                        break;
                    }
                    idx++;
                    scoreIdx++;
                }
            }
        }
        else {
            double [] scores = new double[featureLength];
            for (Feature f : features) {
                System.arraycopy(f.score(r), 0, scores, idx, f.length());
                idx += f.length();
            }
            rules.put(r, scores);
        }
    }

    public Set<Rule> rules()
    {
        return rules.keySet();
    }

    public String ruleScoreString(Rule r)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(r);
        if (!rules.containsKey(r))
            return sb.toString();
        sb.append(String.format(" %s", ThraxConfig.DELIMITER));
        for (double d : rules.get(r))
            sb.append(String.format(" %.6f", d));
        return sb.toString();
    }
}

