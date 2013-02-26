package edu.jhu.thrax.hadoop.jobs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

public abstract class ThraxJob {
	public Job getJob(Configuration conf) throws IOException {
		return new Job(conf);
	}

	public Set<Class<? extends ThraxJob>> getPrerequisites() {
		return new HashSet<Class<? extends ThraxJob>>();
	}

	public abstract String getName();

	public abstract String getOutputSuffix();
}
