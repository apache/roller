/*
 * Job.java
 *
 * Created on December 16, 2005, 6:14 PM
 */

package org.roller.business.runnable;

import java.util.Map;

/**
 * A job to be executed.
 *
 * @author Allen Gilliland
 */
public interface Job {
    
    /**
     * Execute the job.
     */
    public void execute();
    
    
    /**
     * Pass in input to be used for the job.
     */
    public void input(Map input);
    
    
    /**
     * Get any output from the job.
     */
    public Map output();
    
}
