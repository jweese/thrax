package edu.jhu.thrax.extraction;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Scanner;

import java.io.IOException;

import edu.jhu.thrax.datatypes.*;
import edu.jhu.thrax.util.Vocabulary;
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
    public int NONLEX_SOURCE_LENGTH_LIMIT = 5;
    public int NONLEX_SOURCE_WORD_LIMIT = 5;
    public int NONLEX_TARGET_LENGTH_LIMIT = 5;
    public int NONLEX_TARGET_WORD_LIMIT = 5;
    public int NT_LIMIT = 2;
    public int LEXICAL_MINIMUM = 1;
    public boolean ALLOW_ADJACENT_NTS = false;
    public boolean ALLOW_LOOSE_BOUNDS = false;
    public int RULE_SPAN_LIMIT = 12;
    public int LEX_TARGET_LENGTH_LIMIT;
    public int LEX_SOURCE_LENGTH_LIMIT;

    public int X_ID;


    /**
     * Default constructor. The grammar parameters are initalized according
     * to how they are set in the thrax config file.
     */
    public HieroRuleExtractor()
    {
        INIT_LENGTH_LIMIT = ThraxConfig.INITIAL_PHRASE_LIMIT;
        SOURCE_LENGTH_LIMIT = ThraxConfig.SOURCE_LENGTH_LIMIT;
        NONLEX_SOURCE_LENGTH_LIMIT = ThraxConfig.NONLEX_SOURCE_LENGTH_LIMIT;
        NONLEX_SOURCE_WORD_LIMIT = ThraxConfig.NONLEX_SOURCE_WORD_LIMIT;
        NONLEX_TARGET_LENGTH_LIMIT = ThraxConfig.NONLEX_TARGET_LENGTH_LIMIT;
        NONLEX_TARGET_WORD_LIMIT = ThraxConfig.NONLEX_TARGET_WORD_LIMIT;
        NT_LIMIT = ThraxConfig.ARITY;
        LEXICAL_MINIMUM = ThraxConfig.LEXICALITY;
        ALLOW_ADJACENT_NTS = ThraxConfig.ADJACENT;
        ALLOW_LOOSE_BOUNDS = ThraxConfig.LOOSE;
        RULE_SPAN_LIMIT = ThraxConfig.RULE_SPAN_LIMIT;
        LEX_TARGET_LENGTH_LIMIT = ThraxConfig.LEX_TARGET_LENGTH_LIMIT;
        LEX_SOURCE_LENGTH_LIMIT = ThraxConfig.LEX_SOURCE_LENGTH_LIMIT;
        X_ID = Vocabulary.getId(ThraxConfig.DEFAULT_NT);
        if (HIERO_LABELS.isEmpty())
            HIERO_LABELS.add(X_ID);
    }

    public List<Rule> extract(String [] inputs)
    {
        if (inputs.length < 3) {
            return new ArrayList<Rule>();
        }
        for (String i : inputs) {
            if (i.trim().equals(""))
                return new ArrayList<Rule>();
        }

        int [] source = Vocabulary.getIds(inputs[0].split("\\s+"));
        int [] target = Vocabulary.getIds(inputs[1].split("\\s+"));
        Alignment alignment = new Alignment(inputs[2]);
        if (!alignment.consistent(source.length, target.length)) {
            System.err.println("WARNING: inconsistent alignment (skipping)");
            return new ArrayList<Rule>();
        }

        PhrasePair [][] phrasesByStart = initialPhrasePairs(source, target, alignment);
        HashMap<IntPair,Collection<Integer>> labelsBySpan = computeAllLabels(phrasesByStart, target.length);

        Queue<Rule> q = new LinkedList<Rule>();
        for (int i = 0; i < source.length; i++)
            q.offer(new Rule(source, target, alignment, i, NT_LIMIT));

        return processQueue(q, phrasesByStart, labelsBySpan);
    }

    protected List<Rule> processQueue(Queue<Rule> q, PhrasePair [][] phrasesByStart, HashMap<IntPair,Collection<Integer>> labelsBySpan)
    {
        List<Rule> rules = new ArrayList<Rule>();
        while (q.peek() != null) {
            Rule r = q.poll();

	    for (Rule t : getAlignmentVariants(r)) {
                if (isWellFormed(t)) {
			for (Rule s : getLabelVariants(t, labelsBySpan)) {
			    rules.add(s);
			}
		}
            }
            if (r.appendPoint > phrasesByStart.length - 1)
                continue;
            if (phrasesByStart[r.appendPoint] == null)
                continue;

//            if (r.numNTs + r.numTerminals < SOURCE_LENGTH_LIMIT &&
//                    r.appendPoint - r.rhs.sourceStart < RULE_SPAN_LIMIT) {
            if ((ThraxConfig.ALLOW_FULL_SENTENCE_RULES &&
                r.rhs.sourceStart == 0)
                ||
                (r.numNTs == 0 &&
                 r.appendPoint - r.rhs.sourceStart < LEX_SOURCE_LENGTH_LIMIT)
                ||
                (r.numNTs + r.numTerminals < NONLEX_SOURCE_LENGTH_LIMIT &&
                 r.appendPoint - r.rhs.sourceStart < RULE_SPAN_LIMIT)) {
                Rule s = r.copy();
                s.extendWithTerminal();
                q.offer(s);
	    }

            for (PhrasePair pp : phrasesByStart[r.appendPoint]) {
                if (pp.sourceEnd - r.rhs.sourceStart > RULE_SPAN_LIMIT
                    || 
                    (r.rhs.targetStart >= 0 &&
                     pp.targetEnd - r.rhs.targetStart > RULE_SPAN_LIMIT)) {
                    if (!ThraxConfig.ALLOW_FULL_SENTENCE_RULES ||
                        r.rhs.sourceStart != 0)
                        continue;
                }
                if (r.numNTs < NT_LIMIT &&
                        r.numNTs + r.numTerminals < LEX_SOURCE_LENGTH_LIMIT &&
                        (!r.sourceEndsWithNT || ALLOW_ADJACENT_NTS)) {
                    Rule s = r.copy();
                    s.extendWithNonterminal(pp);
                    q.offer(s);
                }
            }

        }
        return rules;
    }

    protected boolean isWellFormed(Rule r)
    {
        if (r.rhs.targetStart < 0)
            return false;
//        if (r.rhs.targetEnd - r.rhs.targetStart > RULE_SPAN_LIMIT ||
//            r.rhs.sourceEnd - r.rhs.sourceStart > RULE_SPAN_LIMIT) {
//            if (!ThraxConfig.ALLOW_FULL_SENTENCE_RULES ||
//                r.rhs.sourceStart != 0 ||
//                r.rhs.sourceEnd != r.source.length ||
//                r.rhs.targetStart != 0 ||
//                r.rhs.targetEnd != r.target.length) {
//                return false;
//            }
//        }
//        if (r.numNTs > 0) {
//            if (r.numTerminals > NONLEX_SOURCE_WORD_LIMIT)
//                return false;
//            if (r.numTerminals + r.numNTs > NONLEX_SOURCE_LENGTH_LIMIT)
//                return false;
//        }
        int targetTerminals = 0;
        for (int i = r.rhs.targetStart; i < r.rhs.targetEnd; i++) {
            if (r.targetLex[i] < 0) {
                if (r.alignment.targetIsAligned(i))
                    return false;
                else
                    r.targetLex[i] = 0;
            }
            if (r.targetLex[i] == 0)
                targetTerminals++;
            if (r.targetLex[i] == 0 && r.alignment.targetIsAligned(i)) {
                for (int k : r.alignment.e2f[i]) {
                    if (r.sourceLex[k] != 0)
                        return false;
                }
            }
        }
        if (r.numNTs > 0) {
            if (r.numTerminals > NONLEX_SOURCE_WORD_LIMIT)
                return false;
            if (r.numTerminals + r.numNTs > NONLEX_SOURCE_LENGTH_LIMIT)
                return false;
            if (targetTerminals > NONLEX_TARGET_WORD_LIMIT)
                return false;
            if (targetTerminals + r.numNTs > NONLEX_TARGET_LENGTH_LIMIT)
                return false;
        }
        else if (r.numNTs == 0) { 
            if (r.numTerminals > LEX_SOURCE_LENGTH_LIMIT ||
                targetTerminals > LEX_TARGET_LENGTH_LIMIT) {
//                if (!ThraxConfig.ALLOW_FULL_SENTENCE_RULES ||
//                    r.rhs.sourceStart != 0 ||
//                    r.rhs.sourceEnd != r.source.length ||
//                    r.rhs.targetStart != 0 ||
//                    r.rhs.targetEnd != r.target.length) {
                    return false;
//                }
            }
        }
        if (!ThraxConfig.ALLOW_ABSTRACT &&
            r.numTerminals == 0 &&
            targetTerminals == 0)
            return false;
        if (r.rhs.targetEnd - r.rhs.targetStart > RULE_SPAN_LIMIT ||
            r.rhs.sourceEnd - r.rhs.sourceStart > RULE_SPAN_LIMIT) {
            if (ThraxConfig.ALLOW_FULL_SENTENCE_RULES &&
                r.rhs.sourceStart == 0 &&
                r.rhs.sourceEnd == r.source.length &&
                r.rhs.targetStart == 0 &&
                r.rhs.targetEnd == r.target.length) {
                return true;
            }
            return false;
        }
        if (!ALLOW_LOOSE_BOUNDS &&
		(!r.alignment.sourceIsAligned(r.rhs.sourceEnd - 1) ||
                 !r.alignment.sourceIsAligned(r.rhs.sourceStart) ||
                 !r.alignment.targetIsAligned(r.rhs.targetEnd - 1) ||
                 !r.alignment.targetIsAligned(r.rhs.targetStart)))
            return false;
        return (r.alignedWords >= LEXICAL_MINIMUM);
    }

    private Collection<Rule> getAlignmentVariants(Rule r)
    {
	List<Rule> result = new ArrayList<Rule>();
	result.add(r);
	if (!ALLOW_LOOSE_BOUNDS)
	    return result;
	if (r.rhs.sourceStart < 0 || r.rhs.sourceEnd < 0 ||
	    r.rhs.targetStart < 0 || r.rhs.targetEnd < 0)
	    return result;
	int targetStart = r.rhs.targetStart;
	while (targetStart > 0 && !r.alignment.targetIsAligned(targetStart - 1)) {
	    targetStart--;
	}
	int targetEnd = r.rhs.targetEnd;
	while (targetEnd < r.target.length && !r.alignment.targetIsAligned(targetEnd)) {
	    targetEnd++;
	}
	for (int i = targetStart; i < r.rhs.targetStart; i++) {
	    Rule s = r.copy();
	    s.rhs.targetStart = i;
	    s.targetLex[i] = 0;
	    result.add(s);
	}
	if (targetEnd == r.rhs.targetEnd) {
	    return result;
	}
	List<Rule> otherResult = new ArrayList<Rule>();
        for (Rule x : result) {
	    for (int j = r.rhs.targetEnd + 1; j <= targetEnd; j++) {
		Rule s = x.copy();
		s.rhs.targetEnd = j;
		s.targetLex[j-1] = 0;
		otherResult.add(s);
	    }
	}
	result.addAll(otherResult);
	return result;
    }

    protected Collection<Rule> getLabelVariants(Rule r, HashMap<IntPair,Collection<Integer>> labelsBySpan)
    {
        Collection<Rule> result = new HashSet<Rule>();
        Queue<Rule> q = new LinkedList<Rule>();
        for (int i = 0; i < r.numNTs; i++)
            r.setNT(i, -1);
        Collection<Integer> lhsLabels = labelsBySpan.get(new IntPair(r.rhs.targetStart, r.rhs.targetEnd));
        if (lhsLabels == null || lhsLabels.isEmpty()) {
//            System.err.println("WARNING: no labels for left-hand side of rule. Span is " + new IntPair(r.rhs.targetStart, r.rhs.targetEnd));
            if (!ThraxConfig.ALLOW_X_NONLEX &&
                r.numNTs > 0)
                return result;
            lhsLabels = HIERO_LABELS;
        }
        for (int lhs : lhsLabels) {
            Rule s = r.copy();
            s.setLhs(lhs);
            q.offer(s);
        }
        for (int i = 0; i < r.numNTs; i++) {
            Collection<Integer> labels = labelsBySpan.get(r.ntSpan(i));
            if (labels == null || labels.isEmpty()) {
//                System.err.println("WARNING: no labels for target-side span of " + r.ntSpan(i));
                if (!ThraxConfig.ALLOW_X_NONLEX)
                    return result;
                labels = HIERO_LABELS;
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
        List<PhrasePair> list = new ArrayList<PhrasePair>();

        for (int i = 0; i < f.length; i++) {
            list.clear();
            int maxlen = f.length - i < INIT_LENGTH_LIMIT ? f.length - i : INIT_LENGTH_LIMIT;
            for (int len = 1; len <= maxlen; len++) {
                if (!ALLOW_LOOSE_BOUNDS && 
                        (!a.sourceIsAligned(i) || !a.sourceIsAligned(i+len-1)))
                    continue;
                for (PhrasePair pp : a.getAllPairsFromSource(i, i+len, ALLOW_LOOSE_BOUNDS, e.length)) {
                    if (pp.targetEnd - pp.targetStart <= INIT_LENGTH_LIMIT) {
                        list.add(pp);
		    }
                }
            }
            result[i] = new PhrasePair[list.size()];
            for (int j = 0; j < result[i].length; j++)
                result[i][j] = list.get(j);
        }
        return result;
    }

    static Collection<Integer> HIERO_LABELS = new ArrayList<Integer>();
    protected HashMap<IntPair,Collection<Integer>> computeAllLabels(PhrasePair [][] phrases, int targetLength)
    {
        HashMap<IntPair,Collection<Integer>> labelsBySpan = new HashMap<IntPair,Collection<Integer>>();
        for (PhrasePair [] plist : phrases) {
            for (PhrasePair pp : plist) {
                int from = pp.targetStart;
                int to = pp.targetEnd;
                IntPair span = new IntPair(from, to);
                labelsBySpan.put(span, HIERO_LABELS);
            }
        }
        return labelsBySpan;
    }

    public static void main(String [] argv) throws IOException
    {
        if (argv.length < 1) {
            System.err.println("usage: HieroRuleExtractor <conf file>");
            return;
        }
        ThraxConfig.configure(argv[0]);
        Scanner scanner = new Scanner(System.in);
        HieroRuleExtractor extractor = new HieroRuleExtractor();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String [] tokens = line.split(ThraxConfig.DELIMITER_REGEX);
            for (Rule r : extractor.extract(tokens))
                System.out.println(r);
        }
        return;
    }
}
