package edu.jhu.thrax.hadoop.features;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.ThraxConfig;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Writable;

import java.util.Map;

public class PhrasePenaltyFeature extends SimpleFeature
{
    private static final Text LABEL = new Text("PhrasePenalty");
    private static final DoubleWritable VALUE = new DoubleWritable(ThraxConfig.PHRASE_PENALTY);

    public PhrasePenaltyFeature()
    {
        super("phrase-penalty");
    }

    public void score(RuleWritable r, Map<Text,Writable> map)
    {
        map.put(LABEL, VALUE);
    }
}

