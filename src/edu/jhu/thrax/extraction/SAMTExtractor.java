package edu.jhu.thrax.extraction;

import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.syntax.LatticeArray;
import edu.jhu.thrax.datatypes.*;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

public class SAMTExtractor extends HieroRuleExtractor {

    public static String name = "samt";

    public String [] requiredInputs()
    {
        return new String [] { ThraxConfig.SOURCE,
                               ThraxConfig.PARSE,
                               ThraxConfig.ALIGNMENT };
    }

    private LatticeArray lattice;
    private HashMap<IntPair,Collection<Integer>> labelsBySpan;

    public SAMTExtractor()
    {
        super();
        labelsBySpan = new HashMap<IntPair,Collection<Integer>>();
    }

    public Set<Rule> extract(Object [] inputs)
    {
        if (inputs.length < 3) {
            return null;
        }

        int [] source = (int []) inputs[0];
        String parse = (String) inputs[1];
        Alignment alignment = (Alignment) inputs[2];

        lattice = new LatticeArray(parse);
        int [] target = yield(parse);

        PhrasePair [][] phrasesByStart = initialPhrasePairs(source, target, alignment);
        computeAllLabels(phrasesByStart);

        Queue<Rule> q = new LinkedList<Rule>();
        for (int i = 0; i < source.length; i++)
            q.offer(new Rule(source, target, alignment, i, NT_LIMIT));

        return processQueue(q, phrasesByStart);
    }

    protected Collection<Rule> getLabelVariants(Rule r)
    {
        Collection<Rule> result = new HashSet<Rule>();
        Queue<Rule> q = new LinkedList<Rule>();
        for (int lhs : labelsBySpan.get(new IntPair(r.rhs.targetStart, r.rhs.targetEnd))) {
            Rule s = r.copy();
            s.setLhs(lhs);
            q.offer(s);
        }
        for (int i = 0; i < r.numNTs; i++) {
            Collection<Integer> labels = labelsBySpan.get(r.ntSpan(i));
            for (Rule s = q.poll(); s.getNT(i) != -1; s = q.poll()) {
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
                
    private int [] yield(String parse)
    {
        String [] tokens = parse.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").trim().split("\\s+");

        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < tokens.length; i++) {
            String t = tokens[i];
            if ("(".equals(t))
                i++;
            else if (")".equals(t));
                // do nothing
            else
                result.add(Vocabulary.getId(t));
        }
        int [] ret = new int[result.size()];
        for (int j = 0; j < ret.length; j++)
            ret[j] = result.get(j);
        return ret;
    }

    private void computeAllLabels(PhrasePair [][] phrases)
    {
        for (PhrasePair [] plist : phrases) {
            for (PhrasePair pp : plist) {
                int from = pp.targetStart;
                int to = pp.targetEnd;
                IntPair span = new IntPair(from, to);
                Collection<Integer> c = new HashSet<Integer>();
                c.add(HieroRuleExtractor.X_ID);
                for (int l : lattice.getConstituentLabels(from, to))
                    c.add(l);
                for (int l : lattice.getConcatenatedLabels(from, to))
                    c.add(l);
                for (int l : lattice.getCcgLabels(from, to))
                    c.add(l);
                labelsBySpan.put(span, c);
            }
        }
    }


}
