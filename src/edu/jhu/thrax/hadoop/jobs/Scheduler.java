package edu.jhu.thrax.hadoop.jobs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;

public class Scheduler
{
		private HashSet<String> faked;
    private HashMap<Class<? extends ThraxJob>,JobState> jobs;

    public Scheduler(Configuration config)
    {
    		jobs = new HashMap<Class<? extends ThraxJob>,JobState>();
    		faked = new HashSet<String>();
    		
    		String[] faked_jobs = config.get("thrax.fake").split("\\s+");
    		for (String faked_job : faked_jobs)
    			faked.add(faked_job);
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
        jobs.put(jobClass, JobState.WAITING);
        System.err.println("[SCHED] scheduled job for " + jobClass);
        return true;
    }

    public boolean setState(Class<? extends ThraxJob> jobClass, JobState state) throws SchedulerException
    {
        if (jobs.containsKey(jobClass)) {
            jobs.put(jobClass, state);
            System.err.println(String.format("[SCHED] %s in state %s", jobClass, state));
            updateAllStates();
            return true;
        }
        return false;
    }

    @SuppressWarnings("fallthrough")
    public void updateAllStates() throws SchedulerException
    {
        for (Class<? extends ThraxJob> c : jobs.keySet()) {
            JobState state = jobs.get(c);
            switch (state) {
            case WAITING:
                checkReady(c);
                // fall through
            case READY:
                checkFailedPrereq(c);
                // fall through
            default:
                // do nothing
            }
        }
    }

    public void checkReady(Class<? extends ThraxJob> c) throws SchedulerException
    {
        ThraxJob job;
        try {
            job = c.newInstance();
        }
        catch (Exception e) {
            throw new SchedulerException(e.getMessage());
        }
        // check all succeeded
        // if state changes, have to recall check all states
        for (Class<? extends ThraxJob> p : job.getPrerequisites()) {
            if (!jobs.get(p).equals(JobState.SUCCESS))
                return;
        }
        // All prereqs are in state SUCCESS.
        
        if (faked.contains(job.getName())) {
        	System.err.println(String.format("[SCHED] Faking %s into state SUCCESS.", c));
        	setState(c, JobState.SUCCESS);
        } else {
        	setState(c, JobState.READY);
        }
    }

    public void checkFailedPrereq(Class<? extends ThraxJob> c) throws SchedulerException
    {
        ThraxJob job;
        try {
            job = c.newInstance();
        }
        catch (Exception e) {
            throw new SchedulerException(e.getMessage());
        }
        // check all succeeded
        // if state changes, have to recall check all states
        for (Class<? extends ThraxJob> p : job.getPrerequisites()) {
            JobState state = jobs.get(p);
            if (state.equals(JobState.FAILED) ||
                state.equals(JobState.PREREQ_FAILED)) {
                setState(c, JobState.PREREQ_FAILED);
                return;
            }
        }
        return;
    }
    
    public JobState getState(Class<? extends ThraxJob> jobClass)
    {
        return jobs.get(jobClass);
    }

    public boolean isScheduled(Class<? extends ThraxJob> jobClass)
    {
        return jobs.containsKey(jobClass);
    }

    public Set<Class<? extends ThraxJob>> getClassesByState(JobState state)
    {
        Set<Class<? extends ThraxJob>> result = new HashSet<Class<? extends ThraxJob>>();
        for (Class<? extends ThraxJob> c : jobs.keySet()) {
            if (jobs.get(c).equals(state))
                result.add(c);
        }
        return result;
    }

    public int numJobs()
    {
        return jobs.size();
    }

    public boolean notFinished()
    {
        for (Class<? extends ThraxJob> c : jobs.keySet()) {
            JobState state = jobs.get(c);
            if (state.equals(JobState.READY) ||
                state.equals(JobState.WAITING) ||
                state.equals(JobState.RUNNING))
                return true;
        }
        return false;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Class<? extends ThraxJob> c : jobs.keySet()) {
            sb.append(c + "\t" + jobs.get(c));
            sb.append("\n");
        }
        return sb.toString();
    }
}

