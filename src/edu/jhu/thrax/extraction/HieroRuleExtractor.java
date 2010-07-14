package edu.jhu.thrax.extraction;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

import edu.jhu.thrax.datatypes.*;
import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.features.Feature;
import edu.jhu.thrax.ThraxConfig;

/**
 * This class extracts Hiero-style SCFG rules. The inputs that are needed
 * are "source" "target" and "alignment", which are the source and target
 * sides of a parallel corpus, and an alignment between each of the sentences.
 */
public class HieroRuleExtractor implements RuleExtractor {

    public static String name = "hiero";

    public String [] requiredInputs()
    {
        return new String [] { "source", "target", "alignment" };
    }

    public int INIT_LENGTH_LIMIT = 10;
    public int SOURCE_LENGTH_LIMIT = 5;
    public int NT_LIMIT = 2;
    public int LEXICAL_MINIMUM = 1;
    public boolean ALLOW_ADJACENT_NTS = false;
    public boolean ALLOW_LOOSE_BOUNDS = false;

    public int X_ID;

    private ArrayList<Feature> features;
    private int featureLength;

    HashMap<IntPair,Collection<Integer>> labelsBySpan;

    /**
     * Default constructor. All it does is to initialize the list of
     * features to an empty list.
     */
    public HieroRuleExtractor()
    {
        features = new ArrayList<Feature>();
        featureLength = 0;
        INIT_LENGTH_LIMIT = ThraxConfig.INITIAL_PHRASE_LIMIT;
        SOURCE_LENGTH_LIMIT = ThraxConfig.SOURCE_LENGTH_LIMIT;
        NT_LIMIT = ThraxConfig.ARITY;
        LEXICAL_MINIMUM = ThraxConfig.LEXICALITY;
        ALLOW_ADJACENT_NTS = ThraxConfig.ADJACENT;
        ALLOW_LOOSE_BOUNDS = ThraxConfig.LOOSE;
        labelsBySpan = new HashMap<IntPair,Collection<Integer>>();
        X_ID = Vocabulary.getId(ThraxConfig.DEFAULT_NT);
        if (HIERO_LABELS.isEmpty())
            HIERO_LABELS.add(X_ID);
    }

    public List<Rule> extract(String [] inputs)
    {
        if (inputs.length < 3) {
            return null;
        }

        int [] source = Vocabulary.getIds(inputs[0].split("\\s+"));
        int [] target = Vocabulary.getIds(inputs[1].split("\\s+"));
        Alignment alignment = new Alignment(inputs[2]);

        PhrasePair [][] phrasesByStart = initialPhrasePairs(source, target, alignment);
        computeAllLabels(phrasesByStart);

        Queue<Rule> q = new LinkedList<Rule>();
        for (int i = 0; i < source.length; i++)
            q.offer(new Rule(source, target, alignment, i, NT_LIMIT));

        return processQueue(q, phrasesByStart);
    }

    protected List<Rule> processQueue(Queue<Rule> q, PhrasePair [][] phrasesByStart)
    {
        int numPrototypes = 0;
        List<Rule> rules = new ArrayList<Rule>();
        while (q.peek() != null) {
            Rule r = q.poll();

            if (isWellFormed(r)) {
                numPrototypes++;
                for (Rule s : getLabelVariants(r)) {
                    rules.add(s);
                }
            }
            if (r.appendPoint > phrasesByStart.length - 1)
                continue;
            if (phrasesByStart[r.appendPoint] == null)
                continue;

            if (r.numNTs + r.numTerminals < SOURCE_LENGTH_LIMIT &&
                    r.appendPoint - r.rhs.sourceStart < INIT_LENGTH_LIMIT) {
                Rule s = r.copy();
                s.extendWithTerminal();
                q.offer(s);
                    }

            for (PhrasePair pp : phrasesByStart[r.appendPoint]) {
                if (pp.sourceEnd - r.rhs.sourceStart > INIT_LENGTH_LIMIT ||
                        (r.rhs.targetStart >= 0 &&
                         pp.targetEnd - r.rhs.targetStart > INIT_LENGTH_LIMIT))
                    continue;
                if (r.numNTs < NT_LIMIT &&
                        r.numNTs + r.numTerminals < SOURCE_LENGTH_LIMIT &&
                        (!r.sourceEndsWithNT || ALLOW_ADJACENT_NTS)) {
                    Rule s = r.copy();
                    s.extendWithNonterminal(pp);
                    q.offer(s);
                        }
            }

        }
        return rules;
    }

    private boolean isWellFormed(Rule r)
    {
        if (r.rhs.targetStart < 0)
            return false;
        if (r.rhs.targetEnd - r.rhs.targetStart > INIT_LENGTH_LIMIT)
            return false;
        if (!r.alignment.sourceIsAligned(r.rhs.sourceEnd - 1) ||
                !r.alignment.sourceIsAligned(r.rhs.sourceStart) ||
                !r.alignment.targetIsAligned(r.rhs.targetEnd - 1) ||
                !r.alignment.targetIsAligned(r.rhs.targetStart))
            return false;
        for (int i = r.rhs.targetStart; i < r.rhs.targetEnd; i++) {
            if (r.targetLex[i] < 0) {
                if (r.alignment.targetIsAligned(i))
                    return false;
                else
                    r.targetLex[i] = 0;
            }
            if (r.targetLex[i] == 0) {
                for (int k : r.alignment.e2f[i]) {
                    if (r.sourceLex[k] != 0)
                        return false;
                }
            }
        }
        return (r.alignedWords >= LEXICAL_MINIMUM);
    }

    protected Collection<Rule> getLabelVariants(Rule r)
    {
        Collection<Rule> result = new HashSet<Rule>();
        Queue<Rule> q = new LinkedList<Rule>();
        for (int i = 0; i < r.numNTs; i++)
            r.setNT(i, -1);
        Collection<Integer> lhsLabels = labelsBySpan.get(new IntPair(r.rhs.targetStart, r.rhs.targetEnd));
        if (lhsLabels == null || lhsLabels.isEmpty()) {
            System.err.println("WARNING: no labels for left-hand side of rule. Span is " + new IntPair(r.rhs.targetStart, r.rhs.targetEnd));
            return result;
        }
        for (int lhs : lhsLabels) {
            Rule s = r.copy();
            s.setLhs(lhs);
            q.offer(s);
        }
        for (int i = 0; i < r.numNTs; i++) {
            Collection<Integer> labels = labelsBySpan.get(r.ntSpan(i));
            if (labels == null || labels.isEmpty()) {
                System.err.println("WARNING: no labels for target-side span of " + r.ntSpan(i));
            }
            else if (ThraxConfig.verbosity > 1) {
                System.err.print("labels for span " + r.ntSpan(i));
                for (int l : labels)
                    System.err.print(String.format(" %s", Vocabulary.getWord(l)));
                System.err.println();
            }
            for (Rule s = q.peek(); s != null && s.getNT(i) == -1; s = q.peek()) {
                s = q.poll();
                for (int l : labels) {
                    Rule t = s.copy();
                    t.setNT(i, l);
                    q.offer(t);
                }
            }
        }
        result.addAll(q);
        return result;
    }

    protected PhrasePair [][] initialPhrasePairs(int [] f, int [] e, Alignment a)
    {

        PhrasePair [][] result = new PhrasePair[f.length][];

        ArrayList<PhrasePair> list = new ArrayList<PhrasePair>();
        for (int i = 0; i < f.length; i++) {
            list.clear();
            int maxlen = f.length - i < INIT_LENGTH_LIMIT ? f.length - i : INIT_LENGTH_LIMIT;
            for (int len = 1; len <= maxlen; len++) {
                if (!ALLOW_LOOSE_BOUNDS && 
                        (!a.sourceIsAligned(i) || !a.sourceIsAligned(i+len-1)))
                    continue;
                PhrasePair pp = a.getPairFromSource(i, i+len);
                if (pp != null && pp.targetEnd - pp.targetStart <= INIT_LENGTH_LIMIT) {
                    list.add(pp);
                }
            }
            result[i] = new PhrasePair[list.size()];
            list.toArray(result[i]);
        }
        return result;
    }

    static Collection<Integer> HIERO_LABELS = new ArrayList<Integer>();
    protected void computeAllLabels(PhrasePair [][] phrases)
    {
        for (PhrasePair [] plist : phrases) {
            for (PhrasePair pp : plist) {
                int from = pp.targetStart;
                int to = pp.targetEnd;
                IntPair span = new IntPair(from, to);
                labelsBySpan.put(span, HIERO_LABELS);
            }
        }
    }
}
