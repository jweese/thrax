package edu.jhu.thrax.util.concurrent;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;

import edu.jhu.thrax.ThraxConfig;

public class ExtractionThreadPoolExecutor extends ThreadPoolExecutor {

    public ExtractionThreadPoolExecutor()
    {
        super(ThraxConfig.THREADS, ThraxConfig.THREADS,
              15, TimeUnit.SECONDS,
              new ArrayBlockingQueue(ThraxConfig.QUEUE_SIZE),
              new ThreadPoolExecutor.CallerRunsPolicy());
    }

    protected void beforeExecute(Thread t, Runnable r)
    {
        super.beforeExecute(t, r);
        ExtractionTask et = (ExtractionTask) r;
        if (ThraxConfig.verbosity > 0) {
            System.err.println(String.format("[%tc] thread %s processing line %d", System.currentTimeMillis(), t.getName(), et.lineNumber));
        }
    }

    protected void afterExecture(Runnable r, Throwable t)
    {
        super.afterExecute(r, t);
        ExtractionTask et = (ExtractionTask) r;
        if (t != null) {
            System.err.println(String.format("[%tc] EXCEPTION at line %d", System.currentTimeMillis(), et.lineNumber));
            System.err.println(t.getMessage());
        }
        else if (ThraxConfig.verbosity > 0) {
            System.err.println(String.format("[%tc] line %d completed", System.currentTimeMillis(), et.lineNumber));
        }
    }
}
