package edu.jhu.thrax.extraction;

public class HieroLabeler implements SpanLabeler
{
    private final String label;

    public HieroLabeler(String s)
    {
        label = s;
    }

    public String getLabel(int start, int end)
    {
        return label;
    }
}

