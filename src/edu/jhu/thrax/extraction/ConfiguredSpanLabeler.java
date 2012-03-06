package edu.jhu.thrax.extraction;

import org.apache.hadoop.conf.Configuration;

import java.util.Collection;
import edu.jhu.thrax.datatypes.IntPair;

public abstract class ConfiguredSpanLabeler implements SpanLabeler
{
    private Configuration conf;

    public ConfiguredSpanLabeler(Configuration c)
    {
        conf = c;
    }

    public abstract String getLabel(int start, int end);
}

