package edu.jhu.thrax.extraction;

import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.syntax.LatticeArray;
import edu.jhu.thrax.datatypes.*;
import edu.jhu.thrax.util.exceptions.*;
import edu.jhu.thrax.util.io.InputUtilities;
import edu.jhu.thrax.util.ConfFileParser;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

public class SAMTExtractor extends HieroRuleExtractor {

    public static String name = "samt";

    private static final String FULL_SENTENCE_SYMBOL = "_S";
    private static final int FULL_SENTENCE_ID = Vocabulary.getId(FULL_SENTENCE_SYMBOL);

    public boolean TARGET_IS_SAMT_SYNTAX = true;
    public boolean ALLOW_CONSTITUENT_LABEL = true;
    public boolean ALLOW_CCG_LABEL = true;
    public boolean ALLOW_CONCAT_LABEL = true;
    public boolean ALLOW_DOUBLE_CONCAT = true;
    public String UNARY_CATEGORY_HANDLER = "all";

    private LatticeArray lattice;

    public SAMTExtractor(Configuration conf)
    {
        super(conf);
        TARGET_IS_SAMT_SYNTAX = conf.getBoolean("thrax.target-is-samt-syntax", true);
        ALLOW_CONSTITUENT_LABEL = conf.getBoolean("thrax.allow-constituent-label", true);
        ALLOW_CCG_LABEL = conf.getBoolean("thrax.allow-ccg-label", true);
        ALLOW_CONCAT_LABEL = conf.getBoolean("thrax.allow-concat-label", true);
        ALLOW_DOUBLE_CONCAT = conf.getBoolean("thrax.allow-double-plus", true);
        UNARY_CATEGORY_HANDLER = conf.get("thrax.unary-category-handler", "all");
    }

    @Override
    public List<Rule> extract(String inp) throws MalformedInputException
    {
        String [] inputs = inp.split(ThraxConfig.DELIMITER_REGEX);
        if (inputs.length < 3) {
            throw new NotEnoughFieldsException();
        }
        String [] sourceWords = InputUtilities.getWords(inputs[0], SOURCE_IS_PARSED);
        String [] targetWords = InputUtilities.getWords(inputs[1], TARGET_IS_PARSED);
        if (sourceWords.length == 0 || targetWords.length == 0)
            throw new EmptySentenceException();
        int [] source = Vocabulary.getIds(sourceWords);
        int [] target = Vocabulary.getIds(targetWords);
        if (TARGET_IS_SAMT_SYNTAX)
            lattice = new LatticeArray(inputs[1].trim(), UNARY_CATEGORY_HANDLER);
        else
            lattice = new LatticeArray(inputs[0].trim(), UNARY_CATEGORY_HANDLER);
        if (REVERSE) {
            int [] tmp = source;
            source = target;
            target = tmp;
        }
        Alignment alignment = new Alignment(inputs[2], REVERSE);
        if (alignment.isEmpty())
            throw new EmptyAlignmentException();
        if (!alignment.consistent(source.length, target.length)) {
            throw new InconsistentAlignmentException(inputs[2]);
        }

        PhrasePair [][] phrasesByStart = initialPhrasePairs(source, target, alignment);
        HashMap<IntPair,Collection<Integer>> labelsBySpan = computeAllLabels(phrasesByStart, target.length);

        Queue<Rule> q = new LinkedList<Rule>();
        for (int i = 0; i < source.length; i++)
            q.offer(new Rule(source, target, alignment, i, NT_LIMIT));

        List<Rule> result = processQueue(q, phrasesByStart, labelsBySpan);
        return result;
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
        IntPair span = getSpanForSyntax(pp);
        int from = span.fst;
        int to = span.snd;
        Collection<Integer> c = new HashSet<Integer>();
        if (from == 0 && to == targetLength)
            c.add(FULL_SENTENCE_ID);
        int x;
        if (ALLOW_CONSTITUENT_LABEL) {
            x = lattice.getOneConstituent(from, to);
            if (x >= 0) {
                c.add(x);
                return c;
            }
        }
        if (ALLOW_CONCAT_LABEL) {
            x = lattice.getOneSingleConcatenation(from, to);
            if (x >= 0) {
                c.add(x);
                return c;
            }
        }
        if (ALLOW_CCG_LABEL) {
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
        }
        if (ALLOW_DOUBLE_CONCAT) {
            x = lattice.getOneDoubleConcatenation(from, to);
            if (x >= 0) {
                c.add(x);
                return c;
            }
        }
        //                c = HieroRuleExtractor.HIERO_LABELS;
        return c;
    }

    private IntPair getSpanForSyntax(PhrasePair pp)
    {
        int from;
        int to;
        if ((REVERSE && TARGET_IS_SAMT_SYNTAX)
            || (!REVERSE && !TARGET_IS_SAMT_SYNTAX)) {
            from = pp.sourceStart;
            to = pp.sourceEnd;
        }
        else {
            from = pp.targetStart;
            to = pp.targetEnd;
        }
        return new IntPair(from, to);
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
        Configuration conf = new Configuration();
        Map<String,String> options = ConfFileParser.parse(argv[0]);
        for (String opt : options.keySet())
            conf.set("thrax." + opt, options.get(opt));
        SAMTExtractor extractor = new SAMTExtractor(conf);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            for (Rule r : extractor.extract(line))
                System.out.println(r);
        }
        return;
    }

}
