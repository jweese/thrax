package edu.jhu.thrax.features;

import edu.jhu.thrax.datatypes.Rule;

public class LexicalProbabilityFeature implements Feature {

    private CollocationTable table;

    public LexicalProbabilityFeature()
    {
        table = new CollocationTable();
    }

    public void noteExtraction(Rule r)
    {
        table.add(r.source, r.target, r.alignment);
        return;
    }

    public double [] score(Rule r)
    {
        double [] ret = new double[2];
        ret[0] = targetGivenSource(r);
        ret[1] = sourceGivenTarget(r);
        return ret;
    }

    public int length()
    {
        return 2;
    }

    private double targetGivenSource(Rule r)
    {
        double ret = 1.0;
        for (int i = 0; i < r.target.length; i++) {
            if (!r.targetLexical.get(i))
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
            if (!r.lexical.get(i))
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
