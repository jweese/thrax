package edu.jhu.thrax.features;

import java.util.HashMap;
import java.util.HashSet;

import edu.jhu.thrax.datatypes.IntPair;
import edu.jhu.thrax.datatypes.Alignment;
import edu.jhu.thrax.util.Vocabulary;

public class CollocationTable {

    private static final String UNALIGNED = "CT_UNALIGNED";
    public static final int ID_UNALIGNED = Vocabulary.getId(UNALIGNED);
    private HashMap<Integer,Integer> sourceCounts;
    private HashMap<Integer,Integer> targetCounts;
    private HashMap<IntPair,Integer> pairCounts;

    private HashSet<Integer> seen;

    public CollocationTable()
    {
        sourceCounts = new HashMap<Integer,Integer>();
        targetCounts = new HashMap<Integer,Integer>();
        pairCounts = new HashMap<IntPair,Integer>();
        seen = new HashSet<Integer>();
    }

    public void add(int [] f, int [] e, Alignment a)
    {
        if (seen.contains(a.id))
            return;

        seen.add(a.id);
        for (int i = 0; i < f.length; i++) {
            if (i >= a.f2e.length || a.f2e[i].length == 0)
                addPair(f[i], ID_UNALIGNED);
            else {
                for (int curre : a.f2e[i])
                    addPair(f[i], e[curre]);
            }
        }

        for (int j = 0; j < e.length; j++) {
            if (j >= a.e2f.length || a.e2f[j].length == 0)
                addPair(ID_UNALIGNED, e[j]);
        }
        return;
    }

    private void addPair(int f, int e)
    {
        IntPair p = new IntPair(f, e);

        sourceCounts.put(f, sourceCounts.containsKey(f) ?
                sourceCounts.get(f) + 1 : 1);
        targetCounts.put(e, targetCounts.containsKey(e) ?
                targetCounts.get(e) + 1 : 1);
        pairCounts.put(p, pairCounts.containsKey(p) ?
                pairCounts.get(p) + 1 : 1);
    }

    public double targetGivenSource(int f, int e)
    {
        IntPair p = new IntPair(f, e);
        if (!pairCounts.containsKey(p)) {
            return 0;
        }

        return (double) pairCounts.get(p) / sourceCounts.get(f);
    }

    public double sourceGivenTarget(int f, int e)
    {
        IntPair p = new IntPair(f, e);
        if (!pairCounts.containsKey(p))
            return 0;

        return (double) pairCounts.get(p) / targetCounts.get(e);
    }
}
