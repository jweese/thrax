package edu.jhu.thrax.features;

import java.util.HashMap;
import java.util.ArrayList;
import edu.jhu.thrax.datatypes.Rule;


public class PhrasalProbabilityFeature implements Feature {

    public static final String name = "phrase";

    public int length()
    {
        return 2;
    }

    public AggregationStyle [] aggregationStyles()
    {
        return aggregationStyles;
    }

    private static final AggregationStyle [] aggregationStyles = new AggregationStyle [] { AggregationStyle.NONE };

    private HashMap<ArrayList<Integer>,Integer> yieldCounts;
    private HashMap<ArrayList<Integer>,Integer> collocation;

    public PhrasalProbabilityFeature() {
        yieldCounts = new HashMap<ArrayList<Integer>,Integer>();
        collocation = new HashMap<ArrayList<Integer>,Integer>();
    }

    public synchronized void noteExtraction(Rule r)
    {
        ArrayList<Integer> sy = r.sourceYield();
        ArrayList<Integer> ty = r.targetYield();
        ArrayList<Integer> both = new ArrayList<Integer>();
        both.addAll(sy);
        both.addAll(ty);
        int currSourceCount = yieldCounts.containsKey(sy)
            ? yieldCounts.get(sy) : 0;
        int currTargetCount = yieldCounts.containsKey(ty)
            ? yieldCounts.get(ty) : 0;
        int currRuleCount = collocation.containsKey(both)
            ? collocation.get(both) : 0;

        yieldCounts.put(sy, currSourceCount + 1);
        yieldCounts.put(ty, currTargetCount + 1);
        collocation.put(both, currRuleCount + 1);
        return;
    }

    public double [] score(Rule r)
    {
        ArrayList<Integer> sy = r.sourceYield();
        ArrayList<Integer> ty = r.targetYield();
        ArrayList<Integer> both = new ArrayList<Integer>();
        both.addAll(sy);
        both.addAll(ty);
        int bothCount = collocation.get(both);
        double [] ret = new double[2];
        ret[0] = -Math.log((double) bothCount / yieldCounts.get(sy));
        ret[1] = -Math.log((double) bothCount / yieldCounts.get(ty));
        return ret;
    }
}
