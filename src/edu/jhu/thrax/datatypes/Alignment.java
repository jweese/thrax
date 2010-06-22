package edu.jhu.thrax.datatypes;

import java.util.ArrayList;
import java.util.Arrays;

public class Alignment {

    public final int id;

    private static int currentId = 0;

    public int [][] f2e;
    public int [][] e2f;

    private static final int [] UNALIGNED = new int[0];

    public Alignment(String s)
    {
        String [] ts = s.trim().split("\\s+");
        IntPair [] ips = new IntPair[ts.length];
        int i = 0;
        for (String t : ts) {
            ips[i++] = IntPair.alignmentFormat(t);
        }

        Arrays.sort(ips);
        f2e = convertIntPairsTo2DArray(ips);
        for (IntPair ip : ips)
            ip.reverse();
        Arrays.sort(ips);
        e2f = convertIntPairsTo2DArray(ips);

        this.id = currentId++;
    }

    private static int [][] convertIntPairsTo2DArray(IntPair [] ips)
    {
        int [][] ret = new int[ips[ips.length-1].fst + 1][];
        ArrayList<IntPair> list = new ArrayList<IntPair>();
        int currfst = 0;
        for (IntPair ip : ips) {
            if (ip.fst == currfst) {
                list.add(ip);
            }
            else {
                if (list.size() == 0) {
                    ret[currfst] = UNALIGNED;
                }
                else {
                    ret[currfst] = new int[list.size()];
                }
                int i = 0;
                for (IntPair x : list)
                    ret[currfst][i++] = x.snd;
                for (int j = currfst + 1; j < ip.fst; j++)
                    ret[j] = UNALIGNED;
                list.clear();
                list.add(ip);
                currfst = ip.fst;
            }
        }
        ret[currfst] = new int[list.size()];
        int i = 0;
        for (IntPair x : list)
            ret[currfst][i++] = x.snd;
        return ret;
    }

    public boolean sourceIsAligned(int i)
    {
        return i < f2e.length && f2e[i].length > 0;
    }

    public boolean targetIsAligned(int i)
    {
        return i < e2f.length && e2f[i].length > 0;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < f2e.length; i++) {
            if (f2e[i] == null) {
                System.err.println("warning: null array in Alignment");
                continue;
            }
            sb.append(String.format("%d->{", i));
            for (int j : f2e[i]) {
                sb.append(j);
                sb.append(",");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }

    public static void main(String [] argv)
    {
        Alignment a = new Alignment("3-3 2-4");
        String x = a.toString();
        System.out.print(a);
        return;
    }
}
