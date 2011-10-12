package edu.jhu.thrax.extraction;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import edu.jhu.thrax.datatypes.IntPair;

public class HieroLabeler implements SpanLabeler
{
    public void setInput(String input)
    {
        // do nothing
    }

    public Collection<Integer> getLabels(IntPair span)
    {
        // just return an empty collection, so it will use the default
        return Collections.emptySet();
    }
}

