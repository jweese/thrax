package edu.jhu.thrax.hadoop.datatypes;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.TwoDArrayWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.datatypes.Rule;
import edu.jhu.thrax.hadoop.features.LexicalProbability;

public class RuleWritable implements WritableComparable<RuleWritable>
{
    public Text lhs;
    public Text source;
    public Text target;
    public TwoDArrayWritable f2e;
    public TwoDArrayWritable e2f;
    public MapWritable features;

    public RuleWritable()
    {
        lhs = new Text();
        source = new Text();
        target = new Text();
        f2e = new TwoDArrayWritable(Text.class);
        e2f = new TwoDArrayWritable(Text.class);
        features = new MapWritable();
    }

    public RuleWritable(Rule r)
    {
        String [] parts = r.toString().split(ThraxConfig.DELIMITER);
        lhs = new Text(parts[0].trim());
        source = new Text(parts[1].trim());
        target = new Text(parts[2].trim());
        f2e = new TwoDArrayWritable(Text.class, sourceAlignmentArray(r));
        e2f = new TwoDArrayWritable(Text.class, targetAlignmentArray(r));
        features = new MapWritable();
    }

    public void write(DataOutput out) throws IOException
    {
        lhs.write(out);
        source.write(out);
        target.write(out);
        f2e.write(out);
        e2f.write(out);
        features.write(out);
    }

    public void readFields(DataInput in) throws IOException
    {
        lhs.readFields(in);
        source.readFields(in);
        target.readFields(in);
        f2e.readFields(in);
        e2f.readFields(in);
        features.readFields(in);
    }

    private static Text [][] sourceAlignmentArray(Rule r)
    {
        int numPairs = 0;
        for (int i = r.rhs.sourceStart; i < r.rhs.sourceEnd; i++) {
            if (r.sourceLex[i] == 0) {
                if (r.alignment.sourceIsAligned(i))
                    numPairs += r.alignment.f2e[i].length;
                else
                    numPairs += 1;
            }
        }
        Text [][] result = new Text[numPairs][];
        int idx = 0;
        for (int i = r.rhs.sourceStart; i < r.rhs.sourceEnd; i++) {
            if (r.sourceLex[i] == 0) {
                Text src = new Text(Vocabulary.getWord(r.source[i]));
                if (r.alignment.sourceIsAligned(i)) {
                    for (int x : r.alignment.f2e[i]) {
                        result[idx] = new Text[2];
                        result[idx][0] = src;
                        result[idx][1] = new Text(Vocabulary.getWord(r.target[x]));
                        idx++;
                    }
                }
                else {
                    result[idx] = new Text[2];
                    result[idx][0] = src;
                    result[idx][1] = LexicalProbability.UNALIGNED;
                    idx++;
                }
            }
        }
        return result;
    }

    private static Text [][] targetAlignmentArray(Rule r)
    {
        int numPairs = 0;
        for (int i = r.rhs.targetStart; i < r.rhs.targetEnd; i++) {
            if (r.targetLex[i] == 0) {
                if (r.alignment.targetIsAligned(i))
                    numPairs += r.alignment.e2f[i].length;
                else
                    numPairs += 1;
            }
        }
        Text [][] result = new Text[numPairs][];
        int idx = 0;
        for (int i = r.rhs.targetStart; i < r.rhs.targetEnd; i++) {
            if (r.targetLex[i] == 0) {
                Text tgt = new Text(Vocabulary.getWord(r.target[i]));
                if (r.alignment.targetIsAligned(i)) {
                    for (int x : r.alignment.e2f[i]) {
                        result[idx] = new Text[2];
                        result[idx][0] = tgt;
                        result[idx][1] = new Text(Vocabulary.getWord(r.source[x]));
                        idx++;
                    }
                }
                else {
                    result[idx] = new Text[2];
                    result[idx][0] = tgt;
                    result[idx][1] = LexicalProbability.UNALIGNED;
                }
            }
        }
        return result;
    }

    public boolean equals(Object o)
    {
        if (o instanceof RuleWritable) {
            RuleWritable that = (RuleWritable) o;
            return lhs.equals(that.lhs) &&
                   source.equals(that.source) &&
                   target.equals(that.target) &&
                   f2e.equals(that.f2e) &&
                   e2f.equals(that.e2f);
        }
        return false;
    }

    public int hashCode()
    {
        int result = 163;
        result = 37 * result + lhs.hashCode();
        result = 37 * result + source.hashCode();
        result = 37 * result + target.hashCode();
        result = 37 * result + f2e.hashCode();
        result = 37 * result + e2f.hashCode();
        return result;
    }

    public int compareTo(RuleWritable that)
    {
        int cmp = lhs.compareTo(that.lhs);
        if (cmp != 0)
            return cmp;
        cmp = source.compareTo(that.source);
        if (cmp != 0)
            return cmp;
        return target.compareTo(that.target);
    }

    public static class YieldComparator extends WritableComparator
    {
        private static final Text.Comparator TEXT_COMPARATOR = new Text.Comparator();
        public YieldComparator()
        {
            super(RuleWritable.class);
        }

        public int compare(byte [] b1, int s1, int l1,
                           byte [] b2, int s2, int l2)
        {
            try {
                int cmp;
                int vis1 = WritableUtils.decodeVIntSize(b1[s1]);
                int vi1 = readVInt(b1, s1);
                int len1 = vis1 + vi1;
                int vis2 = WritableUtils.decodeVIntSize(b2[s2]);
                int vi2 = readVInt(b2, s2);
                int len2 = vis2 + vi2;

                cmp = TEXT_COMPARATOR.compare(b1, s1, len1, b2, s2, len2);
                if (cmp != 0) {
                    return cmp;
                }
                vis1 = WritableUtils.decodeVIntSize(b1[s1 + len1]);
                vi1 = readVInt(b1, s1 + len1);
                int start1 = s1 + len1;
                len1 = vis1 + vi1;
                vis2 = WritableUtils.decodeVIntSize(b2[s2 + len2]);
                vi2 = readVInt(b2, s2 + len2);
                int start2 = s2 + len2;
                len2 = vis2 + vi2;
                cmp = TEXT_COMPARATOR.compare(b1, start1, len1, b2, start2, len2);
                if (cmp != 0) {
                    return cmp;
                }
                return TEXT_COMPARATOR.compare(b1, start1 + len1, l1 - start1 - len1, b2, start2 + len2, l2 - start2 - len2);
            }
            catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    static {
        WritableComparator.define(RuleWritable.class, new YieldComparator());
    }
}

