package edu.jhu.thrax.hadoop.features;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class TargetWordCounterFeature extends SimpleFeature
{
    private static final Text LABEL = new Text("TargetWords");

    public TargetWordCounterFeature()
    {
        super("target-word-count");
    }

    public void score(RuleWritable r)
    {
        int words = 0;
        for (String tok : r.target.toString().split("\\s+")) {
            if (!tok.startsWith("[")) {
                words++;
            }
        }
        r.features.put(LABEL, new IntWritable(words));
        return;
    }
}

