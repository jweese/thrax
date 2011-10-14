package edu.jhu.thrax;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.jhu.thrax.hadoop.features.mapred.MapReduceFeature;
import edu.jhu.thrax.hadoop.jobs.ExtractionJob;
import edu.jhu.thrax.hadoop.jobs.FeatureJobFactory;
import edu.jhu.thrax.hadoop.jobs.JobState;
import edu.jhu.thrax.hadoop.jobs.OutputJob;
import edu.jhu.thrax.hadoop.jobs.ParaphraseAggregationJob;
import edu.jhu.thrax.hadoop.jobs.ParaphrasePivotingJob;
import edu.jhu.thrax.hadoop.jobs.Scheduler;
import edu.jhu.thrax.hadoop.jobs.SchedulerException;
import edu.jhu.thrax.hadoop.jobs.ThraxJob;
import edu.jhu.thrax.util.ConfFileParser;

public class Thrax extends Configured implements Tool
{
    private Scheduler scheduler;
    private Configuration conf;

    public synchronized int run(String [] argv) throws Exception
    {
        if (argv.length < 1) {
            System.err.println("usage: Thrax <conf file> [output path]");
            return 1;
        }
        // do some setup of configuration
        conf = getConf();
        Map<String,String> options = ConfFileParser.parse(argv[0]);
        for (String opt : options.keySet())
            conf.set("thrax." + opt, options.get(opt));
        String date = (new Date()).toString().replaceAll("\\s+", "_").replaceAll(":", "_");

        String workDir = "thrax_run_" + date + Path.SEPARATOR;

        if (argv.length > 1) {
			workDir = argv[1];
            if (!workDir.endsWith(Path.SEPARATOR))
                workDir += Path.SEPARATOR;
		}

        conf.set("thrax.work-dir", workDir);
		conf.set("thrax.outputPath", workDir + "final");

    	scheduleJobs();
        
        do {
            for (Class<? extends ThraxJob> c : scheduler.getClassesByState(JobState.READY)) {
                scheduler.setState(c, JobState.RUNNING);
                (new Thread(new ThraxJobWorker(this, c, conf))).start();
            }
            wait();
        } while (scheduler.notFinished());
        System.err.print(scheduler);
        if (scheduler.getClassesByState(JobState.SUCCESS).size() == scheduler.numJobs()) {
            System.err.println("Work directory was " + workDir);
            System.err.println("To retrieve grammar:");
            System.err.println("hadoop fs -getmerge " + conf.get("thrax.outputPath","") + " <destination>");
        }
        return 0;
    }

    private synchronized void scheduleJobs() throws SchedulerException {
    	scheduler = new Scheduler();
    	// schedule all the jobs
        scheduler.schedule(ExtractionJob.class);
    	
        if ("translation".equals(conf.get("thrax.type"))) {
	        for (String feature : conf.get("thrax.features", "").split("\\s+")) {
	            MapReduceFeature f = FeatureJobFactory.get(feature);
	            if (f != null) {
	                scheduler.schedule(f.getClass());
	                OutputJob.addPrerequisite(f.getClass());
	            }
	        }
		} else {
			// Process paraphrasing features and schedule required 
			// translation grammar features.
			Set<MapReduceFeature> translation_features = new HashSet<MapReduceFeature>();
			for (String feature : conf.get("thrax.features", "").split("\\s+")) {
				translation_features.addAll(FeatureJobFactory.getPrerequisiteFeatures(feature));
	        }
			for (MapReduceFeature f : translation_features) {
				if (f != null) {
	            	scheduler.schedule(f.getClass());
	            	ParaphrasePivotingJob.addPrerequisite(f.getClass());
	            }
			}
			scheduler.schedule(ParaphrasePivotingJob.class);
			ParaphraseAggregationJob.addPrerequisite(ParaphrasePivotingJob.class);
			
			scheduler.schedule(ParaphrasePivotingJob.class);
			
			OutputJob.addPrerequisite(f.getClass());
		}
    	scheduler.schedule(OutputJob.class);
    }
    
    public static void main(String [] argv) throws Exception
    {
        ToolRunner.run(null, new Thrax(), argv);
        return;
    }

    protected synchronized void workerDone(Class<? extends ThraxJob> theClass, boolean success)
    {
        try {
            scheduler.setState(theClass, success ? JobState.SUCCESS : JobState.FAILED);
        }
        catch (SchedulerException e) {
            System.err.println(e.getMessage());
        }
        notify();
        return;
    }

    public class ThraxJobWorker implements Runnable
    {
        private Configuration configuration;
        private Thrax thrax;
        private Class<? extends ThraxJob> theClass;

        public ThraxJobWorker(Thrax t, Class<? extends ThraxJob> c, Configuration conf)
        {
            thrax = t;
            configuration = conf;
            theClass = c;
        }

        public void run()
        {
            try {
                ThraxJob thraxJob = theClass.newInstance();
                Job job = thraxJob.getJob(conf);
                job.waitForCompletion(false);
                thrax.workerDone(theClass, job.isSuccessful());
            }
            catch (Exception e) {
                e.printStackTrace();
                thrax.workerDone(theClass, false);
            }
            return;
        }
    }
}
