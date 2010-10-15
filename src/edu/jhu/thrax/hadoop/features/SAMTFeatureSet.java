package edu.jhu.thrax.hadoop.features;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

import java.io.IOException;

public class SAMTFeatureSet extends Feature
{
    public String name()
    {
        return "samt";
    }

    public Class<? extends Mapper> mapperClass()
    {
        return Map.class;
    }

    public Class<? extends WritableComparator> sortComparatorClass()
    {
        return RuleWritable.YieldComparator.class;
    }

    public Class<? extends Partitioner> partitionerClass()
    {
        return RuleWritable.YieldPartitioner.class;
    }

    public Class<? extends Reducer> reducerClass()
    {
        return IntSumReducer.class;
    }

    public static class Map extends Mapper<RuleWritable,IntWritable,RuleWritable,IntWritable>
    {
        private final Text LEXICAL = new Text("Lexical");
        private IntWritable lex = new IntWritable();
        private final Text ABSTRACT = new Text("Abstract");
        private IntWritable abs = new IntWritable();
        private final Text ADJACENT = new Text("Adjacent");
        private IntWritable adj = new IntWritable();
        private final Text X_RULE = new Text("ContainsX");
        private IntWritable xRule = new IntWritable();
        private final Text TERMINALS_REMOVED = new Text("SourceTerminalsButNoTarget");
        private IntWritable removed = new IntWritable();
        private final Text TERMINALS_ADDED = new Text("TargetTerminalsButNoSource");
        private IntWritable added = new IntWritable();
        private final Text MONOTONIC = new Text("Monotonic");
        private IntWritable mono = new IntWritable();

        protected void map(RuleWritable key, IntWritable value, Context context) throws IOException, InterruptedException
        {
            int sourceTerminals = 0;
            int targetTerminals = 0;
            int sourceNTs = 0;
            int targetNTs = 0;
            for (String tok : key.source.toString().split("\\s+")) {
                if (tok.startsWith("["))
                    sourceNTs++;
                else
                    sourceTerminals++;
            }
            for (String tok : key.target.toString().split("\\s+")) {
                if (tok.startsWith("["))
                    targetNTs++;
                else
                    targetTerminals++;
            }

            lex.set((sourceNTs == 0 && targetNTs == 0) ? 1 : 0);
            abs.set((sourceTerminals == 0 && targetTerminals == 0) ? 1 : 0);
            adj.set((key.source.toString().indexOf("] [") == -1) ? 0 : 1);
            xRule.set(key.source.toString().matches("\\[X") ? 1 : 0);
            removed.set((sourceTerminals > 0 && targetTerminals == 0) ? 1 : 0);
            added.set((sourceTerminals == 0 && targetTerminals > 0) ? 1 : 0);
            mono.set(key.target.toString().matches("2\\].*1\\]") ? 0 : 1);

            key.features.put(LEXICAL, lex);
            key.features.put(ABSTRACT, abs);
            key.features.put(ADJACENT, adj);
            key.features.put(X_RULE, xRule);
            key.features.put(TERMINALS_REMOVED, removed);
            key.features.put(TERMINALS_ADDED, added);
            key.features.put(MONOTONIC, mono);
            context.write(key, value);
        }

    }

}

