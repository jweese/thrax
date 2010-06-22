package edu.jhu.thrax.datatypes;

import edu.jhu.thrax.datatypes.IntPair;
import edu.jhu.thrax.util.Vocabulary;
import java.util.BitSet;

public class Rule {

    public int lhs;

    public int [] source;
    public int [] target;

    PhrasePair rhs;

    public int [] nts;
    int [] ntAlignment;
    int [] targetNTs;
    int [] targetNTEnds;
    public int numNTs;

    public BitSet lexical;
    public BitSet targetLexical;

    public Alignment alignment;

    public double [] scores;

    private Rule()
    {
    }

    public Rule(int [] f, int [] e, PhrasePair pp, Alignment a)
    {
        source = f;
        target = e;

        rhs = pp;

        nts = new int[f.length];
        ntAlignment = new int[f.length];
        targetNTs = new int[f.length];
        targetNTEnds = new int[f.length];
        numNTs = 0;

        lexical = new BitSet(f.length);
        targetLexical = new BitSet(e.length);

        lexical.set(pp.sourceStart, pp.sourceEnd);
        targetLexical.set(pp.targetStart, pp.targetEnd);

        this.alignment = a;
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

        ret.source = this.source;
        ret.target = this.target;

        ret.rhs = this.rhs;

        ret.nts = (int []) this.nts.clone();
        ret.ntAlignment = (int []) this.ntAlignment.clone();
        ret.targetNTs = (int []) this.targetNTs.clone();
        ret.targetNTEnds = (int []) this.targetNTEnds.clone();
        ret.numNTs = this.numNTs;

        ret.lexical = (BitSet) this.lexical.clone();
        ret.targetLexical = (BitSet) this.targetLexical.clone();

        ret.alignment = this.alignment;

        // ret.scores = (double []) this.scores.clone();
        return ret;
    }

    public boolean canElide(PhrasePair pp)
    {
        if (!pp.consistentWith(this.alignment))
            return false;

        if (pp.sourceStart > rhs.sourceStart && !lexical.get(pp.sourceStart - 1))
            return false;
        if (pp.sourceEnd < rhs.sourceEnd && !lexical.get(pp.sourceEnd))
            return false;
        for (int i = pp.sourceStart; i < pp.sourceEnd; i++)
            if (!lexical.get(i)) return false;

        for (int j = pp.targetStart; j < pp.targetEnd; j++)
            if (!targetLexical.get(j)) return false;
        /*
        int nextClear = lexical.nextClearBit(pp.sourceStart);
        if (nextClear != -1 && nextClear < pp.sourceEnd)
            return false;
        nextClear = targetLexical.nextClearBit(pp.targetStart);
        if (nextClear != -1 && nextClear < pp.targetEnd)
            return false;
        */
        return true;
    }


    /**
     * Remove a phrase pair from this rule, replacing it with a nonterminal
     * symbol.
     *
     * @return the index int nts of the new nonterminal, or -1 if the phrase
     * pair cannot be elided.
     */
    public int elide(PhrasePair pp, int nt)
    {

        int [] index = ntIndex(pp);
        for (int k = 0; k < ntAlignment.length; k++) {
            if (ntAlignment[k] >= index[0])
                ntAlignment[k]++;
        }
        shiftArray(nts, index[0]);
        shiftArray(ntAlignment, index[1]);
        shiftArray(targetNTs, index[1]);
        shiftArray(targetNTEnds, index[1]);
        nts[index[0]] = nt;
        ntAlignment[index[1]] = index[0];
        targetNTs[index[1]] = pp.targetStart;
        targetNTEnds[index[1]] = pp.targetEnd;

        numNTs++;
        lexical.clear(pp.sourceStart, pp.sourceEnd);
        targetLexical.clear(pp.targetStart, pp.targetEnd);
        return index[0];
    }

    private int [] ntIndex(PhrasePair pp)
    {
        int [] ret = new int[2];
        boolean lex = true;
        for (int j = rhs.sourceStart; j < pp.sourceStart; j++) {
            if (!lexical.get(j) && lex) ret[0]++;
            lex = lexical.get(j);
        }
        for (int k = rhs.targetStart; k < pp.targetStart; k++) {
            if (ret[1] == numNTs)
                break;
            if (k == targetNTs[ret[1]])
                ret[1]++;
        }
        return ret;
    }

    private void shiftArray(int [] arr, int idx)
    {
        System.arraycopy(arr, idx, arr, idx+1, numNTs - idx);
        return;
    }

    public static final String FIELD_SEPARATOR = " |||";
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s]", Vocabulary.getWord(lhs)));
        sb.append(FIELD_SEPARATOR);
        boolean lex = true;
        int nt = 0;
        for (int i = rhs.sourceStart; i < rhs.sourceEnd; i++) {
            if (lexical.get(i)) {
                sb.append(String.format(" %s", Vocabulary.getWord(source[i])));
            }
            else if (lex) {
                sb.append(String.format(" [%s,%d]", Vocabulary.getWord(nts[nt]), nt+1));
                nt++;
            }
            lex = lexical.get(i);
        }

        sb.append(FIELD_SEPARATOR);
        lex = true;
        int targetNt = 0;
        for (int j = rhs.targetStart; j < rhs.targetEnd; j++) {
            if (targetLexical.get(j)) {
                sb.append(String.format(" %s", Vocabulary.getWord(target[j])));
            }
            else if (j == targetNTs[targetNt]) {
                int al = ntAlignment[targetNt];
                sb.append(String.format(" [%s,%d]", Vocabulary.getWord(nts[al]), al+1));
                targetNt++;
            }
            lex = targetLexical.get(j);
        }

        sb.append(FIELD_SEPARATOR);
        for (double s : scores)
            sb.append(String.format(" %.6f", s));

        return sb.toString();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Rule))
            return false;
        Rule other = (Rule) o;
        if (this.lhs != other.lhs)
            return false;
        if (this.numNTs != other.numNTs)
            return false;
        if (this.lexical.cardinality() != other.lexical.cardinality() ||
            this.targetLexical.cardinality() != other.targetLexical.cardinality())
            return false;
        int i = 0;
        int j = 0;
        while (i != -1) {
            i = this.lexical.nextSetBit(i);
            j = other.lexical.nextSetBit(j);
            if (this.source[i] != other.source[j])
                return false;
        }
        i = 0;
        j = 0;
        while (i != -1) {
            i = this.targetLexical.nextSetBit(i);
            j = this.targetLexical.nextSetBit(j);
            if (this.target[i] != other.target[j])
                return false;
        }

        for (int k = 0; k < this.numNTs; k++) {
            if (this.nts[k] != other.nts[k])
                return false;
            if (this.ntAlignment[k] != other.ntAlignment[k])
                return false;
        }
        return true;
    }

    public IntPair lhsSpan()
    {
        return new IntPair(rhs.targetStart, rhs.targetEnd);
    }

    public IntPair ntSpan(int x)
    {
        if (numNTs <= x)
            return null;
        return new IntPair(targetNTs[x], targetNTEnds[x]);
    }

    public boolean hasAlignedTerminalsOutsidePhrase(PhrasePair pp, int limit)
    {
        int aligned = 0;
        int idx = lexical.nextSetBit(0);
        while (idx != -1) {
            if ((idx < pp.sourceStart || idx >= pp.sourceEnd)
                && alignment.sourceIsAligned(idx)) {
                aligned++;
                if (aligned >= limit) {
                    return true;
                }
            }
            idx = lexical.nextSetBit(idx + 1);

        }
        return false;
    }
}
