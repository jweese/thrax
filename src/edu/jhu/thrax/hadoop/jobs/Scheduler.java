package edu.jhu.thrax.hadoop.jobs;

import java.util.HashMap;

public class Scheduler
{
    private HashMap<Class<? extends ThraxJob>,JobState> jobs;

    public Scheduler()
    {
        jobs = new HashMap<Class<? extends ThraxJob>,JobState>();
    }
}

