package edu.jhu.thrax.lexprob;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

public class TableEntry
{
    public final int car;
    public final int cdr;
    public final double probability;

    public TableEntry(LongWritable pair, DoubleWritable d)
    {
        car = (int) (pair.get() >> 32);
        cdr = (int) pair.get();
        probability = d.get();
    }

    public String toString()
    {
        return String.format("(%s,%s):%.4f", car, cdr, probability);
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof TableEntry))
            return false;
        TableEntry te = (TableEntry) o;
        return car == te.car
            && cdr == te.cdr
            && probability == te.probability;
    }
}

