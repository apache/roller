/*
 * ReferrerQueueManager.java
 *
 * Created on December 16, 2005, 5:37 PM
 */

package org.roller.business.referrers;

/**
 * A queue for incoming referrers.
 *
 * @author Allen Gilliland
 */
public interface ReferrerQueueManager {
    
    /**
     * Process an incoming referrer.
     *
     * This method may contain additional logic on how to deal with referrers.
     * It may process them immediately or it may store them for later processing.
     */
    public void processReferrer(IncomingReferrer ref);
    
    
    /**
     * Add a referrer to the queue.
     *
     * It is almost always preferable to call processReferrer() instead.
     */
    public void enqueue(IncomingReferrer ref);
    
    
    /**
     * Get the next item in the queue.
     *
     * Returns null if there is nothing in the queue.
     */
    public IncomingReferrer dequeue();
    
    
    /**
     * Called when the system is being shutdown.
     */
    public void shutdown();
    
}
