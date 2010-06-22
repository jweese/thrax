package edu.jhu.thrax.datatypes;

public class PhrasePair implements Cloneable {

    public int sourceStart;
    public int sourceEnd;
    public int targetStart;
    public int targetEnd;

    public PhrasePair(int ss, int se, int ts, int te)
    {
        sourceStart = ss;
        sourceEnd = se;
        targetStart = ts;
        targetEnd = te;
    }

    public boolean consistentWith(Alignment a)
    {
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (i >= a.f2e.length)
                break;
            for (int x : a.f2e[i]) {
                if (x < targetStart || x >= targetEnd)
                    return false;
            }
        }

        for (int j = targetStart; j < targetEnd; j++) {
            if (j >= a.e2f.length)
                break;
            for (int x : a.e2f[j]) {
                if (x < sourceStart || x >= sourceEnd)
                    return false;
            }
        }

        return true;
    }

    public String toString()
    {
        return String.format("[%d,%d)+[%d,%d)", sourceStart, sourceEnd, targetStart, targetEnd);
    }

    public Object clone()
    {
        return new PhrasePair(sourceStart, sourceEnd, targetStart, targetEnd);
    }

}
