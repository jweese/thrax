package edu.jhu.thrax.hadoop.output;

import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.Comparator;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class OutputReducer extends Reducer<RuleWritable, IntWritable, Text, NullWritable>
{
    private static final String DELIM = String.format(" %s ", ThraxConfig.DELIMITER);
    private boolean label;

    private RuleWritable currentRule;
    private TreeMap<Text,Writable> features;

    protected void setup(Context context) throws IOException, InterruptedException
    {
        Configuration conf = context.getConfiguration();
        Path [] localFiles = DistributedCache.getLocalCacheFiles(conf);
        if (localFiles != null) {
            // we are in distributed mode
            ThraxConfig.configure("thrax.config");
        }
        else {
            // distributed cache will not work in local mode
            String localWorkDir = conf.getRaw("thrax_work");
            String sep = localWorkDir.endsWith(Path.SEPARATOR) ? "" : Path.SEPARATOR;
            ThraxConfig.configure(localWorkDir + sep + "thrax.config");
        }
        label = ThraxConfig.LABEL_FEATURE_SCORES;
        currentRule = null;
        features = new TreeMap<Text,Writable>(new FeatureOrder(ThraxConfig.FEATURES.split("\\s+")));
    }

    protected void reduce(RuleWritable key, Iterable<IntWritable> values,
                          Context context) throws IOException, InterruptedException
    {
        if (currentRule == null || !key.sameYield(currentRule)) {
            if (currentRule == null)
                currentRule = new RuleWritable();
            else
                context.write(ruleToText(currentRule, features), NullWritable.get());
            currentRule.set(key);
            features.clear();
        }
        features.put(key.featureLabel, key.featureScore);
    }

    protected void cleanup(Context context) throws IOException, InterruptedException
    {
        context.write(ruleToText(currentRule, features), NullWritable.get());
    }

    private Text ruleToText(RuleWritable r)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(r.lhs);
        sb.append(DELIM);
        sb.append(r.source);
        sb.append(DELIM);
        sb.append(r.target);
        sb.append(DELIM);
        for (Writable key : r.features.keySet()) {
            if (label)
                sb.append(String.format("%s=%s ", key, r.features.get(key)));
            else
                sb.append(String.format("%s ", r.features.get(key)));
        }
        return new Text(sb.toString());
    }

    private Text ruleToText(RuleWritable r, TreeMap<Text,Writable> fs)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(r.lhs);
        sb.append(DELIM);
        sb.append(r.source);
        sb.append(DELIM);
        sb.append(r.target);
        sb.append(DELIM);
        for (Text t : fs.keySet()) {
            if (label)
                sb.append(String.format("%s=%s ", t, fs.get(t)));
            else
                sb.append(String.format("%s ", fs.get(t)));
        }
        return new Text(sb.toString());
    }

    private class FeatureOrder implements Comparator<Text>
    {
        private String [] features;

        public FeatureOrder(String [] fs)
        {
            features = fs;
        }

        public int compare(Text a, Text b)
        {
            int aIndex = Arrays.binarySearch(features, a.toString());
            int bIndex = Arrays.binarySearch(features, b.toString());

            return aIndex - bIndex;
        }
    }
}

