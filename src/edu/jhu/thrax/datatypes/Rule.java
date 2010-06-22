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

        rhs.sourceStart = appendPoint;
        rhs.sourceEnd = appendPoint;
        rhs.targetStart = -1;
        rhs.targetEnd = -1;
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

    public static final String FIELD_SEPARATOR = " |||";
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s]", Vocabulary.getWord(lhs)));
        sb.append(FIELD_SEPARATOR);

        sb.append(FIELD_SEPARATOR);

        sb.append(FIELD_SEPARATOR);
        for (double s : scores)
            sb.append(String.format(" %.6f", s));

        return sb.toString();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Rule))
            return false;
        return true;
    }

}
