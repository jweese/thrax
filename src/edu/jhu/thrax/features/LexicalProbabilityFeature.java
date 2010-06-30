package edu.jhu.thrax.features;

import edu.jhu.thrax.datatypes.Rule;

import java.util.Map;
import java.util.HashMap;

public class LexicalProbabilityFeature implements Feature {

    public static final String name = "lex";

    public int length()
    {
        return 2;
    }

    private CollocationTable table;
    private Map<Rule,Double> maxf2e;
    private Map<Rule,Double> maxe2f;

    public LexicalProbabilityFeature()
    {
        table = new CollocationTable();
        maxf2e = new HashMap<Rule,Double>();
        maxe2f = new HashMap<Rule,Double>();
    }

    public void noteExtraction(Rule r)
    {
        table.add(r.source, r.target, r.alignment);
        return;
    }

    public double [] score(Rule r)
    {
        double [] ret = new double[2];
        ret[0] = -Math.log(targetGivenSource(r));
        ret[1] = -Math.log(sourceGivenTarget(r));
        if (maxf2e.containsKey(r) && ret[0] > maxf2e.get(r))
            ret[0] = maxf2e.get(r);
        else
            maxf2e.put(r, ret[0]);
        if (maxe2f.containsKey(r) && ret[1] > maxe2f.get(r))
            ret[1] = maxe2f.get(r);
        else
            maxe2f.put(r, ret[1]);
        return ret;
    }



    private double targetGivenSource(Rule r)
    {
        double ret = 1.0;
        for (int i = 0; i < r.target.length; i++) {
            if (r.targetLex[i] != 0)
                continue;
            if (i >= r.alignment.e2f.length || r.alignment.e2f[i].length == 0)
                ret *= table.targetGivenSource(CollocationTable.ID_UNALIGNED,
                        r.target[i]);
            else {
                ret /= r.alignment.e2f[i].length;
                double sum = 0;
                for (int x : r.alignment.e2f[i])
                    sum += table.targetGivenSource(r.source[x], r.target[i]);
                ret *= sum;
            }
        }
        return ret;
    }

    private double sourceGivenTarget(Rule r)
    {
        double ret = 1.0;
        for (int i = 0; i < r.source.length; i++) {
            if (r.sourceLex[i] != 0)
                continue;
            if (i >= r.alignment.f2e.length || r.alignment.f2e[i].length == 0)
                ret *= table.sourceGivenTarget(r.source[i],
                        CollocationTable.ID_UNALIGNED);
            else {
                ret /= r.alignment.f2e[i].length;
                double sum = 0;
                for (int x : r.alignment.f2e[i])
                    sum += table.sourceGivenTarget(r.source[i], r.target[x]);
                ret *= sum;
            }
        }
        return ret;
    }
}
