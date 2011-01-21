package edu.jhu.thrax;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import edu.jhu.thrax.hadoop.jobs.*;

public class Thrax extends Configured implements Tool
{
    private Scheduler scheduler;
    private Configuration conf;

    public int run(String [] argv) throws Exception
    {
        // do some setup of configuration
        conf = getConf();
        scheduler = new Scheduler();
        // schedule all the jobs

        while (scheduler.notFinished()) {
            wait();
            for (Class<? extends ThraxJob> c : scheduler.getClassesByState(JobState.READY)) {
                scheduler.setState(c, JobState.RUNNING);
                (new Thread(new ThraxJobWorker(this, c, conf))).start();
            }
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

