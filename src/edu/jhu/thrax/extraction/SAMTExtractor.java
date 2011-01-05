package edu.jhu.thrax.extraction;

import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.syntax.LatticeArray;
import edu.jhu.thrax.datatypes.*;

import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.IOException;

public class SAMTExtractor extends HieroRuleExtractor {

    public static String name = "samt";

    private static final String FULL_SENTENCE_SYMBOL = "_S";
    private static final int FULL_SENTENCE_ID = Vocabulary.getId(FULL_SENTENCE_SYMBOL);

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
            return new ArrayList<Rule>();
        }
        for (String i : inputs) {
            if (i.trim().equals("") || i.trim().equals("()"))
                return new ArrayList<Rule>();
        }

        int [] source = Vocabulary.getIds(inputs[0].split("\\s+"));
        String parse = inputs[1];
        Alignment alignment = new Alignment(inputs[2]);

        lattice = new LatticeArray(parse);
        int [] target = yield(parse);

        if (!alignment.consistent(source.length, target.length)) {
            System.err.println("WARNING: inconsistent alignment (skipping)");
            return new ArrayList<Rule>();
        }

        PhrasePair [][] phrasesByStart = initialPhrasePairs(source, target, alignment);
        HashMap<IntPair,Collection<Integer>> labelsBySpan = computeAllLabels(phrasesByStart, target.length);

        Queue<Rule> q = new LinkedList<Rule>();
        for (int i = 0; i < source.length; i++)
            q.offer(new Rule(source, target, alignment, i, NT_LIMIT));

        List<Rule> result = processQueue(q, phrasesByStart, labelsBySpan);
        if (source.length > INIT_LENGTH_LIMIT || target.length > INIT_LENGTH_LIMIT) {
            // handle full-sentence rules
        }
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
            else if ("``".equals(t))
                result.add(Vocabulary.getId("\""));
            else if ("''".equals(t))
                result.add(Vocabulary.getId("\""));
            else if ("-lrb-".equalsIgnoreCase(t))
                result.add(Vocabulary.getId("("));
            else if ("-rrb-".equalsIgnoreCase(t))
                result.add(Vocabulary.getId(")"));
            else 
                result.add(Vocabulary.getId(t.toLowerCase()));
        }
        int [] ret = new int[result.size()];
        for (int j = 0; j < ret.length; j++)
            ret[j] = result.get(j);
        return ret;
    }

    protected HashMap<IntPair,Collection<Integer>> computeAllLabels(PhrasePair [][] phrases, int targetLength)
    {
        HashMap<IntPair,Collection<Integer>> labelsBySpan = new HashMap<IntPair,Collection<Integer>>();
        for (PhrasePair [] plist : phrases) {
            for (PhrasePair pp : plist) {
                Collection<Integer> c = spanLabels(pp, targetLength);
                IntPair span = new IntPair(pp.targetStart, pp.targetEnd);
                labelsBySpan.put(span, c);
            }
        }
        IntPair sentence = new IntPair(0, targetLength);
        if (!labelsBySpan.containsKey(sentence)) {
            PhrasePair pp = new PhrasePair(0, 0, 0, targetLength);
            labelsBySpan.put(sentence, spanLabels(pp, targetLength));
        }
        return labelsBySpan;
    }

    private Collection<Integer> spanLabels(PhrasePair pp, int targetLength)
    {
        int from = pp.targetStart;
        int to = pp.targetEnd;
        IntPair span = new IntPair(from, to);
        Collection<Integer> c = new HashSet<Integer>();
        if (from == 0 && to == targetLength)
            c.add(FULL_SENTENCE_ID);
        int x = lattice.getOneConstituent(from, to);
        if (x >= 0) {
            c.add(x);
            return c;
        }
        x = lattice.getOneSingleConcatenation(from, to);
        if (x >= 0) {
            c.add(x);
            return c;
        }
        x = lattice.getOneRightSideCCG(from, to);
        if (x >= 0) {
            c.add(x);
            return c;
        }
        x = lattice.getOneLeftSideCCG(from, to);
        if (x >= 0) {
            c.add(x);
            return c;
        }
        if (ThraxConfig.ALLOW_DOUBLE_CONCAT) {
            x = lattice.getOneDoubleConcatenation(from, to);
            if (x >= 0) {
                c.add(x);
                return c;
            }
        }
        //                c = HieroRuleExtractor.HIERO_LABELS;
        return c;
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

    public static void main(String [] argv) throws IOException
    {
        if (argv.length < 1) {
            System.err.println("usage: SAMTExtractor <conf file>");
            return;
        }
        ThraxConfig.configure(argv[0]);
        SAMTExtractor extractor = new SAMTExtractor();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String [] tokens = line.split(ThraxConfig.DELIMITER_REGEX);
            for (Rule r : extractor.extract(tokens))
                System.out.println(r);
        }
        return;
    }

}
