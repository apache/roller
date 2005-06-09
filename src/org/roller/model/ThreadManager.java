package org.roller.model;

import java.util.TimerTask;
import java.sql.Date;

/**
 * Index to thread management for executing scheduled and asynchronous tasks.
 */
public interface ThreadManager
{
    public static final long MIN_RATE_INTERVAL_MINS = 1;

    public void executeInBackground(Runnable runnable)
            throws InterruptedException;

    public void executeInForeground(Runnable runnable)
            throws InterruptedException;

    public void scheduleDailyTimerTask(TimerTask task);

    public void scheduleHourlyTimerTask(TimerTask task);

    public void scheduleFixedRateTimerTask(TimerTask task, long delayMins, long periodMins);

    public void shutdown();

    public void release();
}