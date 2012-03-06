package edu.jhu.thrax.datatypes;

/**
 * This class represents a phrase pair. Essentially it is four integers
 * describing the boundaries of the source and target sides of the phrase pair.
 */
public class PhrasePair 
{
    /**
     * The index of the start of the source side of this PhrasePair.
     */
    public final int sourceStart;
    /**
     * One plus the index of the end of the source side of this PhrasePair.
     */
    public final int sourceEnd;
    /**
     * The index of the start of the target side of this PhrasePair.
     */
    public final int targetStart;
    /**
     * One plus the index of the end of the target side of this PhrasePair.
     */
    public final int targetEnd;

    /**
     * Constructor.
     *
     * @param ss source start
     * @param se source end
     * @param ts target start
     * @param te target end
     */
    public PhrasePair(int ss, int se, int ts, int te)
    {
        sourceStart = ss;
        sourceEnd = se;
        targetStart = ts;
        targetEnd = te;
    }

    public String toString()
    {
        return String.format("[%d,%d)+[%d,%d)", sourceStart, sourceEnd, targetStart, targetEnd);
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof PhrasePair))
            return false;
        PhrasePair p = (PhrasePair) o;
        return sourceStart == p.sourceStart
            && sourceEnd == p.sourceEnd
            && targetStart == p.targetStart
            && targetEnd == p.targetEnd;
    }

    public int hashCode()
    {
        int result = 37;
        result *= 163 + sourceStart;
        result *= 163 + sourceEnd;
        result *= 163 + targetStart;
        result *= 163 + targetEnd;
        return result;
    }
}
