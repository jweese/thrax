package edu.jhu.thrax.datatypes;

import edu.jhu.thrax.util.Vocabulary;

import java.util.Arrays;
import java.util.ArrayList;

public class Rule {

    int lhs;
    public PhrasePair rhs;

    // backing data, from sentence. shared among all rules extracted from
    // this sentence.
    public int [] source;
    public int [] target;
    public Alignment alignment;

    int [] nts;
    public byte numNTs;

    public boolean sourceEndsWithNT;

    public double [] scores;

    public int appendPoint;
    public byte [] sourceLex;
    public byte [] targetLex; 

    public int alignedWords;
    public int numTerminals;

    ArrayList<Integer> yield;
    boolean yieldChanged;

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

        yield = new ArrayList<Integer>();
        yieldChanged = true;
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

        ret.yield = (ArrayList<Integer>) this.yield.clone();
        ret.yieldChanged = this.yieldChanged;
        return ret;
    }

    public int getLhs()
    {
        return lhs;
    }

    public void setLhs(int label)
    {
        yieldChanged = (lhs == label);
        lhs = label;
    }

    public int getNT(int index)
    {
        return nts[index];
    }

    public void setNT(int index, int label)
    {
        yieldChanged = (nts[index] == label);
        nts[index] = label;
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
        yieldChanged = true;
    }

    public void extendWithTerminal()
    {
        sourceLex[appendPoint] = 0;
        numTerminals++;
        sourceEndsWithNT = false;
        if (!alignment.sourceIsAligned(appendPoint)) {
            appendPoint++;
            rhs.sourceEnd = appendPoint;
            return;
        }
        for (int j : alignment.f2e[appendPoint]) {
            targetLex[j] = 0;
            if (rhs.targetEnd < 0 || j + 1 > rhs.targetEnd)
                rhs.targetEnd = j + 1;
            if (rhs.targetStart < 0 || j < rhs.targetStart)
                rhs.targetStart = j;
        }
        alignedWords++;
        appendPoint++;
        rhs.sourceEnd = appendPoint;
        yieldChanged = true;
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
        if (o == this)
            return true;
        if (!(o instanceof Rule))
            return false;
        Rule other = (Rule) o;
        if (this.lhs != other.lhs)
            return false;
        if (!this.yield().equals(other.yield()))
            return false;
        return true;
    }

    public ArrayList<Integer> yield()
    {
        if (!yieldChanged)
            return yield;
        yield.clear();
        int last = -1;
        for (int i = rhs.sourceStart; i < rhs.sourceEnd; i++) {
            int x = sourceLex[i];
            if (x == 0)
                yield.add(source[i]);
            if (x > 0 && x != last) {
                last = x;
                yield.add(nts[last-1]);
            }
        }
        last = -1;
        for (int j = rhs.targetStart; j < rhs.targetEnd; j++) {
            int y = targetLex[j];
            if (y < 0)
                yield.add(y);
            if (y == 0)
                yield.add(target[j]);
            if (y > 0 && y != last) {
                last = y;
                yield.add(nts[last-1]);
            }
        }
        yieldChanged = false;
        return yield;
    }

    public int hashCode()
    {
        int result = 17;
        int last = -1;
        result = result * 37 + lhs;
        for (int i = 0; i < sourceLex.length; i++) {
            int x = sourceLex[i];
            if (x == 0)
                result = result * 37 + source[i];
            else if (x != last && x > 0) {
                last = x;
                result = result * 37 + nts[last-1];
            }
        }
        last = -1;
        for (int j = 0; j < targetLex.length; j++) {
            int x = targetLex[j];
            if (x == 0)
                result = result * 37 + target[j];
            else if (x != last && x > 0) {
                last = x;
                result = result * 37 + nts[last-1];
            }
        }
        return result;
    }

    public IntPair ntSpan(int index)
    {
        if (index < 0 || index > numNTs - 1)
            return null;
        int start = -1;
        
        for (int i = rhs.targetStart; i < rhs.targetEnd; i++) {
            int x = targetLex[i];
            if (x == index + 1 && start == -1)
                start = x;
            if (start != -1 && x != index + 1)
                return new IntPair(start, x);
        }
        return new IntPair(start, rhs.targetEnd);
    }
}
