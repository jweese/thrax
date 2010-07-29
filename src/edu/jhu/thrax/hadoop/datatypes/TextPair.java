package edu.jhu.thrax.hadoop.datatypes;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.Text;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TextPair implements Writable
{
    public Text fst;
    public Text snd;

    public TextPair()
    {
        fst = new Text();
        snd = new Text();
    }

    public TextPair(Text car, Text cdr)
    {
        fst = car;
        snd = cdr;
    }

    public void reverse()
    {
        Text tmp = fst;
        fst = snd;
        snd = tmp;
    }

    public void write(DataOutput out) throws IOException
    {
        fst.write(out);
        snd.write(out);
    }

    public void readFields(DataInput in) throws IOException
    {
        fst.readFields(in);
        snd.readFields(in);
    }

    public int hashCode()
    {
        return fst.hashCode() * 163 + snd.hashCode();
    }

    public boolean equals(Object o)
    {
        if (o instanceof TextPair) {
            TextPair tp = (TextPair) o;
            return fst.equals(tp.fst) && snd.equals(tp.snd);
        }
        return false;
    }

    public String toString()
    {
        return fst.toString() + "\t" + snd.toString();
    }

    public int compareTo(TextPair tp)
    {
        int cmp = fst.compareTo(tp.fst);
        if (cmp != 0) {
            return cmp;
        }
        return snd.compareTo(tp.snd);
    }

}

