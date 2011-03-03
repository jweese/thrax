package edu.jhu.thrax.extraction;

import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.syntax.LatticeArray;
import edu.jhu.thrax.datatypes.*;
import edu.jhu.thrax.util.exceptions.*;
import edu.jhu.thrax.util.io.InputUtilities;

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

    private LatticeArray lattice;

    public SAMTExtractor()
    {
        super();
    }

    @Override
    public List<Rule> extract(String inp) throws MalformedInputException
    {
        String [] inputs = inp.split(ThraxConfig.DELIMITER_REGEX);
        if (inputs.length < 3) {
            throw new NotEnoughFieldsException();
        }
        String [] sourceWords = InputUtilities.getWords(inputs[0], ThraxConfig.SOURCE_IS_PARSED);
        String [] targetWords = InputUtilities.getWords(inputs[1], ThraxConfig.TARGET_IS_PARSED);
        if (sourceWords.length == 0 || targetWords.length == 0)
            throw new EmptySentenceException();
        int [] source = Vocabulary.getIds(sourceWords);
        int [] target = Vocabulary.getIds(targetWords);
        Alignment alignment = new Alignment(inputs[2]);
        if (alignment.isEmpty())
            throw new EmptyAlignmentException();
        if (!alignment.consistent(source.length, target.length)) {
            throw new InconsistentAlignmentException(inputs[2]);
        }

        if (ThraxConfig.TARGET_IS_SAMT_SYNTAX)
            lattice = new LatticeArray(inputs[1].trim());
        else
            lattice = new LatticeArray(inputs[0].trim());

        PhrasePair [][] phrasesByStart = initialPhrasePairs(source, target, alignment);
        HashMap<IntPair,Collection<Integer>> labelsBySpan = computeAllLabels(phrasesByStart, target.length);

        Queue<Rule> q = new LinkedList<Rule>();
        for (int i = 0; i < source.length; i++)
            q.offer(new Rule(source, target, alignment, i, NT_LIMIT));

        List<Rule> result = processQueue(q, phrasesByStart, labelsBySpan);
        return result;
    }

    static int [] yield(String parse) throws MalformedParseException
    {
        String [] tokens = parse.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").trim().split("\\s+");
        int level = 0;
        boolean expectNT = false;

        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < tokens.length; i++) {
            String t = tokens[i];
            if ("(".equals(t)) {
                level++;
                expectNT = true;
            }
            else if (")".equals(t)) {
                if (level == 0)
                    throw new MalformedParseException(parse);
                level--;
                expectNT = false;
            }
            else if (expectNT) {
                expectNT = false;
            }
            else 
                result.add(Vocabulary.getId(t.toLowerCase()));
        }
        if (level != 0)
            throw new MalformedParseException(parse);
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

    public static void main(String [] argv) throws IOException,MalformedInputException
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
            for (Rule r : extractor.extract(line))
                System.out.println(r);
        }
        return;
    }

}
