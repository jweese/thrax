package edu.jhu.thrax.extraction;

import org.apache.hadoop.conf.Configuration;

public class HieroLabeler extends ConfiguredSpanLabeler
{
    private String label;

    public HieroLabeler(Configuration conf)
    {
        super(conf);
        label = conf.get("thrax.default-nt", "X");
    }

    public String getLabel(int start, int end)
    {
        return label;
    }
}

