package edu.jhu.thrax.hadoop.jobs;

import java.util.HashMap;

public class Scheduler
{
    private HashMap<Class<? extends ThraxJob>,JobState> jobs;

    public Scheduler()
    {
        jobs = new HashMap<Class<? extends ThraxJob>,JobState>();
    }

    public boolean schedule(Class<? extends ThraxJob> jobClass) throws SchedulerException
    {
        if (jobs.containsKey(jobClass))
            return false;
        ThraxJob job;
        try {
            job = jobClass.newInstance();
        }
        catch (Exception e) {
            throw new SchedulerException(e.getMessage());
        }
        for (Class<? extends ThraxJob> c : job.getPrerequisites()) {
            schedule(c);
        }
        if (job.getPrerequisites().size() == 0)
            jobs.put(jobClass, JobState.READY);
        else
            jobs.put(jobClass, JobState.WAITING);
        return true;
    }

    public boolean setState(Class<? extends ThraxJob> jobClass, JobState state)
    {
        if (jobs.containsKey(jobClass)) {
            jobs.put(jobClass, state);
            return true;
        }
        return false;
    }
    
    public JobState getState(Class<? extends ThraxJob> jobClass)
    {
        return jobs.get(jobClass);
    }

    public boolean isScheduled(Class<? extends ThraxJob> jobClass)
    {
        return jobs.containsKey(jobClass);
    }
}

