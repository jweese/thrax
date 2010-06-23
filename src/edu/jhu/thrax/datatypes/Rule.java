package edu.jhu.thrax.datatypes;

import edu.jhu.thrax.util.Vocabulary;

import java.util.Arrays;

public class Rule {

    public int lhs;
    public PhrasePair rhs;

    // backing data, from sentence. shared among all rules extracted from
    // this sentence.
    public int [] source;
    public int [] target;
    public Alignment alignment;

    public int [] nts;
    public byte numNTs;

    public boolean sourceEndsWithNT;

    public double [] scores;

    public int appendPoint;
    public byte [] sourceLex;
    public byte [] targetLex; 

    public int alignedWords;
    public int numTerminals;


    private Rule()
    {
    }

    public Rule(int [] f, int [] e, Alignment a, int start, int arity)
    {
        source = f;
        target = e;
        alignment = a;

        nts = new int[arity];
        numNTs = 0;
        sourceEndsWithNT = false;

        sourceLex = new byte[f.length];
        Arrays.fill(sourceLex, (byte) -1);
        targetLex = new byte[e.length];
        Arrays.fill(targetLex, (byte) -1);

        appendPoint = start;
        alignedWords = 0;
        numTerminals = 0;

        rhs = new PhrasePair(start, start + 1, -1, -1);
    }

    /**
     * Makes an almost-deep copy of this rule, where the backing datatypess are
     * not cloned, but the rule-specific data is cloned so that it can be 
     * modified. The modifiable fields are nts, ntAlignment, numNTs, and the
     * lexicalization bitsets and lhs.
     *
     * @return a copy of this Rule, suitable for modifying
     */
    public Rule copy()
    {
        Rule ret = new Rule();
        ret.lhs = this.lhs;
        ret.rhs = (PhrasePair) this.rhs.clone();

        ret.source = this.source;
        ret.target = this.target;
        ret.alignment = this.alignment;

        ret.nts = (int []) this.nts.clone();
        ret.numNTs = this.numNTs;
        ret.sourceEndsWithNT = this.sourceEndsWithNT;

        ret.sourceLex = (byte []) this.sourceLex.clone();
        ret.targetLex = (byte []) this.targetLex.clone();

        ret.appendPoint = this.appendPoint;
        ret.alignedWords = this.alignedWords;
        ret.numTerminals = this.numTerminals;
        return ret;
    }

    public void extendWithNonterminal(PhrasePair pp)
    {
        numNTs++;
        for (; appendPoint < pp.sourceEnd; appendPoint++)
            sourceLex[appendPoint] = numNTs;
        for (int idx = pp.targetStart; idx < pp.targetEnd; idx++)
            targetLex[idx] = numNTs;
        rhs.sourceEnd = pp.sourceEnd;
        if (rhs.targetStart < 0 || pp.targetStart < rhs.targetStart)
            rhs.targetStart = pp.targetStart;
        if (pp.targetEnd > rhs.targetEnd)
            rhs.targetEnd = pp.targetEnd;
        sourceEndsWithNT = true;
    }

    public void extendWithTerminals(PhrasePair pp)
    {
        for (; appendPoint < pp.sourceEnd; appendPoint++) {
            sourceLex[appendPoint] = 0;
            numTerminals++;
            if (alignment.sourceIsAligned(appendPoint))
                alignedWords++;
        }
        for (int idx = pp.targetStart; idx < pp.targetEnd; idx++)
            targetLex[idx] = 0;
        rhs.sourceEnd = pp.sourceEnd;
        if (rhs.targetStart < 0 || pp.targetStart < rhs.targetStart)
            rhs.targetStart = pp.targetStart;
        if (pp.targetEnd > rhs.targetEnd)
            rhs.targetEnd = pp.targetEnd;
        sourceEndsWithNT = false;
    }

    public void extendWithUnalignedTerminal()
    {
        sourceLex[appendPoint] = 0;
        appendPoint++;
        numTerminals++;
        rhs.sourceEnd++;
    }

    public static final String FIELD_SEPARATOR = " |||";
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s]", Vocabulary.getWord(lhs)));
        sb.append(FIELD_SEPARATOR);
        int last = -1;
        for (int i = 0; i < sourceLex.length; i++) {
            int x = sourceLex[i];
            if (x < 0)
                continue;
            if (x == 0)
                sb.append(String.format(" %s", Vocabulary.getWord(source[i])));
            else if (x != last) {
                sb.append(String.format(" [%s,%d]", Vocabulary.getWord(nts[x-1]), x));
                last = x;
            }
        }

        sb.append(FIELD_SEPARATOR);
        last = -1;
        for (int i = 0; i < targetLex.length; i++) {
            int x = targetLex[i];
            if (x < 0)
                continue;
            if (x == 0)
                sb.append(String.format(" %s", Vocabulary.getWord(target[i])));
            else if (x != last) {
                sb.append(String.format(" [%s,%d]", Vocabulary.getWord(nts[x-1]), x));
                last = x;
            }
        }

        sb.append(FIELD_SEPARATOR);
        for (double s : scores)
            sb.append(String.format(" %.6f", s));

        return sb.toString();
    }

    /**
     * Two rules are considered equal if they have the same textual
     * representation.
     *
     * @param o the object to compare to
     * @return true if these objects are equal, false otherwise
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof Rule))
            return false;
        Rule other = (Rule) o;
        if (this.lhs != other.lhs)
            return false;
        if (!sameRepresentation(this.source, other.source, this.sourceLex, other.sourceLex))
            return false;
        if (!sameRepresentation(this.target, other.target, this.targetLex, other.targetLex))
            return false;
        return true;
    }

    private boolean sameRepresentation(int [] a, int [] b, byte [] alex, byte [] blex) {
        int last = -1;
        int i = 0;
        int j = 0;
        while (alex[i] < 0) i++;
        while (blex[j] < 0) j++;
        while (alex[i] >= 0) {
            if (alex[i] == 0) {
                if (a[i] != b[j])
                    return false;
                i++;
                j++;
            }
            else {
                if (alex[i] != blex[j])
                    return false;
                while (blex[j] == alex[i])
                    j++;
                last = alex[i];
                while (alex[i] == last)
                    i++;
            }

        }
        return (blex[j] < 0);
    }

}
