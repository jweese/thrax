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

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;

public class OutputReducer extends Reducer<RuleWritable, IntWritable, Text, NullWritable>
{
    private static final String DELIM = String.format(" %s ", ThraxConfig.DELIMITER);
    private boolean label;

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
    }

    protected void reduce(RuleWritable key, Iterable<IntWritable> values,
                          Context context) throws IOException, InterruptedException
    {
        context.write(ruleToText(key), NullWritable.get());
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
}

