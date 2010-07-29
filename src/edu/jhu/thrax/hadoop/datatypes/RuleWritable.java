package edu.jhu.thrax.hadoop.datatypes;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.TwoDArrayWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.util.Vocabulary;
import edu.jhu.thrax.datatypes.Rule;
import edu.jhu.thrax.hadoop.features.LexicalProbability;

public class RuleWritable implements Writable
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

}

