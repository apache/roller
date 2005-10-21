package org.roller.model;

import java.util.TimerTask;
import java.sql.Date;

/**
 * Thread management for executing scheduled and asynchronous tasks.
 */
public interface ThreadManager
{
    public static final long MIN_RATE_INTERVAL_MINS = 1;

    /**
     * Execute runnable in background (asynchronously).
     * @param runnable 
     * @throws java.lang.InterruptedException 
     */
    public void executeInBackground(Runnable runnable)
            throws InterruptedException;

    /**
     * Execute runnable in foreground (synchronously).
     */
    public void executeInForeground(Runnable runnable)
            throws InterruptedException;

    /**
     * Schedule task to run once a day.
     */
    public void scheduleDailyTimerTask(TimerTask task);

    /**
     * Schedule task to run once per hour.
     */
    public void scheduleHourlyTimerTask(TimerTask task);

    /**
     * Schedule task to run at fixed rate.
     */
    public void scheduleFixedRateTimerTask(TimerTask task, long delayMins, long periodMins);

    /**
     * Shutdown
     */
    public void shutdown();

    /**
     * Release all resources associated with Roller session.
     */
    public void release();
}