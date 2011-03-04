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
import edu.jhu.thrax.extraction.RuleExtractor;
import edu.jhu.thrax.extraction.RuleExtractorFactory;
import edu.jhu.thrax.util.exceptions.*;
import edu.jhu.thrax.util.MalformedInput;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExtractionMapper extends Mapper<LongWritable, Text,
                                             RuleWritable, IntWritable>
{
    private RuleExtractor extractor;
    private IntWritable one = new IntWritable(1);

    private Path [] localFiles;

    protected void setup(Context context) throws IOException, InterruptedException
    {
        Configuration conf = context.getConfiguration();
//        Path [] localFiles = DistributedCache.getLocalCacheFiles(conf);
//        if (localFiles != null) {
//            // we are in distributed mode
//            ThraxConfig.configure("thrax.config");
//        }
//        else {
            // we are in local mode, DistributedCache doesn't work
//            String localWorkDir = conf.getRaw("thrax_work");
//            String sep = localWorkDir.endsWith(Path.SEPARATOR) ? "" : Path.SEPARATOR;
//            String thraxConfigFile = localWorkDir + sep + "thrax.config";
//            ThraxConfig.configure(thraxConfigFile);
//        }
//            FeatureFactory factory = new FeatureFactory(ThraxConfig.FEATURES);
//            features = factory.getSimpleFeatures();
        try {
            extractor = RuleExtractorFactory.create(conf);
        }
        catch (ConfigurationException ex) {
            System.err.println(ex.getMessage());
        }
    }

    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
    {
        if (extractor == null)
            return;
        String line = value.toString();
        try {
            for (Rule r : extractor.extract(line)) {
                RuleWritable rw = new RuleWritable(r);
//            for (SimpleFeature f : features)
//                f.score(rw);
                context.write(rw, one);
            }
        }
        catch (NotEnoughFieldsException e) {
            context.getCounter(MalformedInput.NOT_ENOUGH_FIELDS).increment(1);
        }
        catch (EmptySentenceException e) {
            context.getCounter(MalformedInput.EMPTY_SENTENCE).increment(1);
        }
        catch (MalformedParseException e) {
            context.getCounter(MalformedInput.MALFORMED_PARSE).increment(1);
        }
        catch (EmptyAlignmentException e) {
            context.getCounter(MalformedInput.EMPTY_ALIGNMENT).increment(1);
        }
        catch (InconsistentAlignmentException e) {
            context.getCounter(MalformedInput.INCONSISTENT_ALIGNMENT).increment(1);
        }
        catch (MalformedInputException e) {
            context.getCounter(MalformedInput.UNKNOWN).increment(1);
        }
    }
}

