package edu.jhu.thrax.extraction;

import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.syntax.LatticeArray;
import edu.jhu.thrax.datatypes.*;

import java.util.List;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collection;
import java.util.ArrayList;

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

    public List<Rule> extract(String [] inputs)
    {
        if (inputs.length < 3) {
            return null;
        }

        int [] source = Vocabulary.getIds(inputs[0].split("\\s+"));
        String parse = inputs[1];
        Alignment alignment = new Alignment(inputs[2]);

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
                Collection<Integer> labels;
                labels = lattice.getConstituentLabels(from, to);
                addUpTo(ThraxConfig.MAX_CONSTITUENT_LABELS, labels, c);
                labels = lattice.getConcatenatedLabels(from, to);
                addUpTo(ThraxConfig.MAX_CAT_LABELS, labels, c);
                labels = lattice.getCcgLabels(from, to);
                addUpTo(ThraxConfig.MAX_CCG_LABELS, labels, c);
                if (c.isEmpty())
                    c = HieroRuleExtractor.HIERO_LABELS;
                labelsBySpan.put(span, c);
            }
        }
    }

    private static <T> void addUpTo(int limit, Collection<T> src,
                                               Collection<T> dest)
    {
        if (limit < 0 || src.size() < limit) {
            dest.addAll(src);
            return;
        }
        int numAdded = 0;
        for (T x : src) {
            if (numAdded >= limit)
                break;
            dest.add(x);
            numAdded++;
        }
    }


}
