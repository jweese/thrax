package edu.jhu.thrax.hadoop.features;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import java.util.Map;

public class UnalignedWordCounterFeature extends SimpleFeature
{
    private static final Text SRC_LABEL = new Text("UnalignedSource");
    private static final Text TGT_LABEL = new Text("UnalignedTarget");

    public UnalignedWordCounterFeature()
    {
        super("unaligned-word-count");
    }

    public void score(RuleWritable r)
    {
        int srcCount = 0;
        int tgtCount = 0;
        for (Text [] ts : r.f2e.get()) {
            if (ts[1].equals(WordLexicalProbabilityCalculator.UNALIGNED))
                srcCount++;
        }
        for (Text [] ts : r.e2f.get()) {
            if (ts[1].equals(WordLexicalProbabilityCalculator.UNALIGNED))
                tgtCount++;
        }

        r.features.put(SRC_LABEL, new IntWritable(srcCount));
        r.features.put(TGT_LABEL, new IntWritable(tgtCount));
        return;
    }

    public void score(RuleWritable r, Map<Text,Writable> map)
    {
        int srcCount = 0;
        int tgtCount = 0;
        for (Text [] ts : r.f2e.get()) {
            if (ts[1].equals(WordLexicalProbabilityCalculator.UNALIGNED))
                srcCount++;
        }
        for (Text [] ts : r.e2f.get()) {
            if (ts[1].equals(WordLexicalProbabilityCalculator.UNALIGNED))
                tgtCount++;
        }

        map.put(SRC_LABEL, new IntWritable(srcCount));
        map.put(TGT_LABEL, new IntWritable(tgtCount));
        return;
    }
}

