package edu.jhu.thrax.features;

import edu.jhu.thrax.datatypes.Rule;

public class UnalignedWordCountFeature implements Feature {

    public int length()
    {
        return 2;
    }

    public UnalignedWordCountFeature()
    {
        // do nothing
    }

    public void noteExtraction(Rule r)
    {
        // do nothing
    }

    public double [] score(Rule r)
    {
        double [] ret = new double[2];
        for (int i = 0; i < r.source.length; i++)
            if (r.sourceLex[i] != 0 && r.alignment.f2e[i].length == 0) ret[0]++;
        for (int j = 0; j < r.target.length; j++)
            if (r.targetLex[j] != 0 && r.alignment.e2f[j].length == 0) ret[1]++;

        return ret;
    }

}
