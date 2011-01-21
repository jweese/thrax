package edu.jhu.thrax;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.fs.Path;

import java.util.Map;
import java.util.Date;

import edu.jhu.thrax.util.ConfFileParser;
import edu.jhu.thrax.hadoop.jobs.*;
import edu.jhu.thrax.hadoop.features.mapred.MapReduceFeature;

public class Thrax extends Configured implements Tool
{
    private Scheduler scheduler;
    private Configuration conf;

    public synchronized int run(String [] argv) throws Exception
    {
        if (argv.length < 1) {
            System.err.println("usage: Thrax <conf file>");
            return 1;
        }
        // do some setup of configuration
        conf = getConf();
        Map<String,String> options = ConfFileParser.parse(argv[0]);
        for (String opt : options.keySet())
            conf.set("thrax." + opt, options.get(opt));
        String date = (new Date()).toString().replaceAll("\\s+", "_").replaceAll(":", "_");
        String workDir = "thrax_run_" + date + Path.SEPARATOR;
        conf.set("thrax.work-dir", workDir);
        scheduler = new Scheduler();
        // schedule all the jobs
        scheduler.schedule(ExtractionJob.class);
        for (String feature : conf.get("thrax.features", "").split("\\s+")) {
            MapReduceFeature f = FeatureJobFactory.get(feature);
            if (f instanceof MapReduceFeature) {
                scheduler.schedule(f.getClass());
                OutputJob.addPrerequisite(f.getClass());
            }
        }
        scheduler.schedule(OutputJob.class);

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
            System.err.println("hadoop fs -getmerge " + workDir + "final <destination>");
        }
        return 0;
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
                thrax.workerDone(theClass, false);
            }
            return;
        }
    }

}

