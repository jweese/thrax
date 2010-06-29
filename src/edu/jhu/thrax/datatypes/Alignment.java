package edu.jhu.thrax.datatypes;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class represents a word-level alignment of a sentence.
 */
public class Alignment {

    /**
     * A unique identifier for this Alignment.
     */
    public final int id;

    private static int currentId = 0;

    /**
     * A list of alignment points, arranged by source-side index.
     */
    public int [][] f2e;
    /**
     * A list of alignment points, arranged by target-side index.
     */
    public int [][] e2f;

    private static final int [] UNALIGNED = new int[0];

    /**
     * This constructor takes a String in Berkeley alignment format. That is,
     * the string "0-0 1-2" means that the first word of the source is aligned
     * to the first word of the target, and the second word of the source is
     * aligned to the third word of the target.
     *
     * @param s a string in Berkeley alignment format
     */
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

    /**
     * Determines whether the given word of the source side is aligned.
     *
     * @param i the index of the word on the source side
     * @return true if the word is aligned, false if it is unaligned
     */
    public boolean sourceIsAligned(int i)
    {
        return i < f2e.length && f2e[i].length > 0;
    }

    /**
     * Determines whether the given word of the target side is aligned.
     *
     * @param i the index of the word on the target side
     * @return true if the word is aligned, false otherwise
     */
    public boolean targetIsAligned(int i)
    {
        return i < e2f.length && e2f[i].length > 0;
    }

    /**
     * Returns a phrase pair with the given source side, that is consistent
     * with this alignment.
     *
     * @param sourceStart the index of the first word of the source side
     * @param sourceEnd 1 + the index of the last word of the source side
     * @return a consistent phrase pair with the given source, or null if no
     * such phrase pair exists.
     */
    public PhrasePair getPairFromSource(int sourceStart, int sourceEnd)
    {
        int targetStart = -1;
        int targetEnd = -1;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (!sourceIsAligned(i))
                continue;
            int min = f2e[i][0];
            int max = f2e[i][f2e[i].length-1] + 1;
            if (targetStart < 0 || min < targetStart)
                targetStart = min;
            if (max > targetEnd)
                targetEnd = max;
        }
        if (targetStart < 0) // there are no aligned words on the source side
            return null;
        for (int j = targetStart; j < targetEnd; j++) {
            for (int k : e2f[j]) {
                if (k < sourceStart || k >= sourceEnd)
                    return null;
            }
        }
        return new PhrasePair(sourceStart, sourceEnd, targetStart, targetEnd);
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
