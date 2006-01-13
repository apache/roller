/*
 * QueuedReferrerProcessingJob.java
 *
 * Created on December 20, 2005, 3:08 PM
 */

package org.roller.business.referrers;

import org.roller.model.RollerFactory;


/**
 * Same as the ReferrerProcessingJob, except that we add a little logic that
 * tries to lookup incoming referrers from the ReferrerQueueManager.
 *
 * @author Allen Gilliland
 */
public class QueuedReferrerProcessingJob extends ReferrerProcessingJob {
    
    public QueuedReferrerProcessingJob() {
        super();
    }
    
    
    public void execute() {
        
        ReferrerQueueManager refQueue =
                RollerFactory.getRoller().getReferrerQueueManager();
        
        // check the queue for any incoming referrers
        referrer = refQueue.dequeue();
        
        // work until the queue is empty
        while(referrer != null) {
            super.execute();
            
            // check if there are more referrers to process
            referrer = refQueue.dequeue();
        }
        
    }
    
}
