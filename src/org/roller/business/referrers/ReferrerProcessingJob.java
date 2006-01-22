/*
 * ReferrerProcessingJob.java
 *
 * Created on December 16, 2005, 6:26 PM
 */

package org.roller.business.referrers;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.runnable.Job;
import org.roller.model.RefererManager;
import org.roller.model.RollerFactory;


/**
 * A simple Job which processes an IncomingReferrer.
 *
 * @author Allen Gilliland
 */
public class ReferrerProcessingJob implements Job {
    
    private static Log mLogger = LogFactory.getLog(ReferrerProcessingJob.class);
    
    Map inputs = null;
    IncomingReferrer referrer = null;
    
    public ReferrerProcessingJob() {}
    
    
    /**
     * Execute job.
     *
     * We simply pass the referrer into the RefererManager to handle the details.
     */
    public void execute() {
        
        if(this.referrer == null)
            return;
        
        mLogger.debug("PROCESSING: "+referrer.getRequestUrl());
        
        // process a referrer
        try {
            RefererManager refMgr = RollerFactory.getRoller().getRefererManager();
            refMgr.processReferrer(referrer.getRequestUrl(),
                                   referrer.getQueryString(),
                                   referrer.getReferrerUrl(),
                                   referrer.getWeblogHandle(),
                                   referrer.getWeblogAnchor(),
                                   referrer.getWeblogDateString());
        } catch(RollerException re) {
            // trouble
            mLogger.warn("Trouble processing referrer", re);
        }
    }
    
    
    /**
     * Set input.
     */
    public void input(Map input) {
        this.inputs = input;
        
        // we are looking for the "referrer" key
        Object ref = input.get("referrer");
        
        if(ref instanceof IncomingReferrer) {
            this.referrer = (IncomingReferrer) ref;
        }
    }
    
    
    /**
     * Get output.
     */
    public Map output() {
        
        return null;
    }
    
}
