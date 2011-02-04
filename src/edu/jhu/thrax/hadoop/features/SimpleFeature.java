package edu.jhu.thrax.hadoop.features;

import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public abstract class SimpleFeature
{
    public abstract void score(RuleWritable r, Map<Text,Writable> map);

    public abstract void unaryGlueRuleScore(Text nt, Map<Text,Writable> map);

    public abstract void binaryGlueRuleScore(Text nt, Map<Text,Writable> map);
}

