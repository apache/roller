/*
 * FuturePostingsInvalidationJob.java
 *
 * Created on February 14, 2006, 5:48 PM
 */

package org.roller.presentation.cache;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.business.referrers.ReferrerProcessingJob;
import org.roller.business.runnable.*;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogEntryData;


/**
 * Trigger cache invalidations for entries published into the future.
 *
 * This job is meant to be run at timed intervals, it will not do any
 * good to run this job only a single time.
 *
 * We do things in a somewhat counterintuitive manner, but it makes things
 * easier on us from an operational point of view.  We start by looking up
 * all entries published between now and some time XX mins in the future.  We
 * then save that list and when XX mins has passed we invalidate the list and
 * query for entries published in the next XX mins.
 *
 * Basically we are building a short term list of future published entries
 * and expiring them once our wait period is over.  This prevents us from
 * having to somehow determine which entries published in the last XX mins
 * had previously been published into the future.
 *
 * @author Allen Gilliland
 */
public class FuturePostingsInvalidationJob implements Job {
    
    private static Log mLogger = LogFactory.getLog(FuturePostingsInvalidationJob.class);
    
    // inputs from the user
    private Map inputs = null;
    
    // the list of entries we expire at the start of the next run
    private List nextExpirations = null;
    
    // how far into the future we will look ahead, in minutes
    int peerTime = 5;
    
    public void execute() {
        
        mLogger.debug("starting");
        
        // notify the cache manager of an invalidation
        if(nextExpirations != null) {
            WeblogEntryData entry = null;
            Iterator entries = nextExpirations.iterator();
            while(entries.hasNext()) {
                entry = (WeblogEntryData) entries.next();
                
                mLogger.debug("expiring "+entry.getAnchor());
                
                CacheManager.invalidate(entry);
            }
        }
        
        // look for postings from current time to current time plus XX mins
        List expiringEntries = null;
        try {
            WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
            
            // current time
            Date start = new Date();
            
            // XX mins in the future
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            cal.add(Calendar.MINUTE, this.peerTime);
            Date end = cal.getTime();
            
            mLogger.debug("looking up entries between "+start+" and "+end);
            
            // get all published entries between start and end date
            expiringEntries = mgr.getWeblogEntries(null, start, end, null, 
                    null, WeblogEntryData.PUBLISHED, null);
            
            this.nextExpirations = expiringEntries;
            
        } catch(Exception e) {
            mLogger.error(e);
        }
        
        mLogger.debug("finished");
    }
    
    
    public Map output() {
       return null; 
    }
    
    
    public void input(Map input) {
        this.inputs = input;
        
        // extract peer time if possible
        Integer pTime = (Integer) this.inputs.get("peerTime");
        if(pTime != null) {
            this.peerTime = pTime.intValue();
        }
        
        mLogger.info("Peeking "+this.peerTime+" minutes into the future each pass");
    }
    
}
