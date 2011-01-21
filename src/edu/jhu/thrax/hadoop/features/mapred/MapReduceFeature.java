package edu.jhu.thrax.hadoop.features;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;

import edu.jhu.thrax.hadoop.datatypes.RuleWritable;
import edu.jhu.thrax.hadoop.jobs.ThraxJob;

public abstract class MapReduceFeature extends ThraxJob
{

    public Class<? extends Reducer> combinerClass()
    {
        return IntSumReducer.class;
    }

    public abstract Class<? extends Mapper> mapperClass();

    public abstract Class<? extends WritableComparator> sortComparatorClass();

    public abstract Class<? extends Partitioner> partitionerClass();

    public abstract Class<? extends Reducer> reducerClass();
}

