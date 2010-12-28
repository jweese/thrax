package edu.jhu.thrax.hadoop.extraction;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;

import edu.jhu.thrax.ThraxConfig;
import edu.jhu.thrax.datatypes.Rule;
import edu.jhu.thrax.datatypes.IntPair;
import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.features.FeatureFactory;
import edu.jhu.thrax.hadoop.features.SimpleFeature;
import edu.jhu.thrax.extraction.RuleExtractor;
import edu.jhu.thrax.extraction.RuleExtractorFactory;
import edu.jhu.thrax.util.UnknownGrammarTypeException;

import java.io.IOException;
import java.util.List;

public class ExtractionMapper extends Mapper<LongWritable, Text,
                                             RuleWritable, IntWritable>
{
    private RuleExtractor extractor;
    private IntWritable one = new IntWritable(1);
    private List<SimpleFeature> features;

    private Path [] localFiles;

    protected void setup(Context context) throws IOException, InterruptedException
    {
        try {
            Configuration conf = context.getConfiguration();
            Path [] localFiles = DistributedCache.getLocalCacheFiles(conf);
            if (localFiles != null) {
                // we are in distributed mode
                ThraxConfig.configure("thrax.config");
            }
            else {
                // we are in local mode, DistributedCache doesn't work
                String localWorkDir = conf.getRaw("thrax_work");
                String sep = localWorkDir.endsWith(Path.SEPARATOR) ? "" : Path.SEPARATOR;
                String thraxConfigFile = localWorkDir + sep + "thrax.config";
                ThraxConfig.configure(thraxConfigFile);
            }
            FeatureFactory factory = new FeatureFactory(ThraxConfig.FEATURES);
            features = factory.getSimpleFeatures();
            extractor = RuleExtractorFactory.create(ThraxConfig.GRAMMAR);
        }
        catch (UnknownGrammarTypeException ex) {
            // do nothing? I don't know what to do here
        }
    }

    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
        String line = value.toString();
        String [] inputs = line.split(ThraxConfig.DELIMITER_REGEX);
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = inputs[i].trim();
        }
        if (inputs.length < 3) {
            String [] realInputs = new String[3];
            for (int j = 0; j < realInputs.length; j++) {
                if (j < inputs.length)
                    realInputs[j] = inputs[j];
                else
                    realInputs[j] = "";
            }
            inputs = realInputs;
        }
        for (Rule r : extractor.extract(inputs)) {
            RuleWritable rw = new RuleWritable(r);
            for (SimpleFeature f : features)
                f.score(rw);
            context.write(rw, one);
        }
    }
}

