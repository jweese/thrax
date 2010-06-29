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
        return new String [] { "source", "parse", "alignment" };
    }

    private LatticeArray lattice;

    public SAMTExtractor()
    {
        super();
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
                result.add(Vocabulary.getId(t.toLowerCase()));
        }
        int [] ret = new int[result.size()];
        for (int j = 0; j < ret.length; j++)
            ret[j] = result.get(j);
        return ret;
    }

    protected void computeAllLabels(PhrasePair [][] phrases)
    {
        for (PhrasePair [] plist : phrases) {
            for (PhrasePair pp : plist) {
                int from = pp.targetStart;
                int to = pp.targetEnd;
                IntPair span = new IntPair(from, to);
                Collection<Integer> c = new HashSet<Integer>();
                for (int l : lattice.getConstituentLabels(from, to))
                    c.add(l);
                for (int l : lattice.getConcatenatedLabels(from, to))
                    c.add(l);
                for (int l : lattice.getCcgLabels(from, to))
                    c.add(l);
                if (c.isEmpty())
                    c = HieroRuleExtractor.HIERO_LABELS;
                labelsBySpan.put(span, c);
            }
        }
    }


}
