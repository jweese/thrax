package edu.jhu.thrax.hadoop.features;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public abstract class SimpleFeature extends Feature
{
    public SimpleFeature(String name)
    {
        super(name);
    }

    public abstract void score(RuleWritable r);
}

