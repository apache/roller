/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.util.cache;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.runnable.Job;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;


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
 */
public class FuturePostingsInvalidationJob implements Job {
    
    private static Log log = LogFactory.getLog(FuturePostingsInvalidationJob.class);
    
    // inputs from the user
    private Map inputs = null;
    
    // the set of entries we expire at the start of the next run
    private Set nextExpirations = null;
    
    // how far into the future we will look ahead, in minutes
    int peerTime = 5;
    
    public void execute() {
        
        log.debug("starting");
        
        try {
            WeblogManager wMgr = RollerFactory.getRoller().getWeblogManager();
            UserManager uMgr = RollerFactory.getRoller().getUserManager();
            
            Date now = new Date();
            
            if(nextExpirations != null) {
                String websiteid = null;
                WebsiteData weblog = null;
                
                Iterator weblogs = nextExpirations.iterator();
                while(weblogs.hasNext()) {
                    websiteid = (String) weblogs.next();
                    
                    try {
                        // lookup the actual entry
                        weblog = uMgr.getWebsite(websiteid);
                        
                        log.debug("expiring"+weblog.getHandle());
                        
                        // to expire weblog content we have to update the
                        // last modified time of the weblog and save it
                        weblog.setLastModified(now);
                        uMgr.saveWebsite(weblog);
                        
                    } catch (RollerException ex) {
                        log.warn("couldn't lookup entry "+websiteid);
                    }
                }
                
                // commit the changes
                RollerFactory.getRoller().flush();
            }
            
            // XX mins in the future
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            cal.add(Calendar.MINUTE, this.peerTime);
            Date end = cal.getTime();
            
            log.debug("looking up entries between "+now+" and "+end);
            
            // get all published entries between start and end date
            List expiringEntries = wMgr.getWeblogEntries(null, null, now, end, null, 
                    null, null, WeblogEntryData.PUBLISHED, null, 0, -1);
            
            // we only really want the weblog ids
            Set expiringWeblogs = new HashSet();
            Iterator it = expiringEntries.iterator();
            while(it.hasNext()) {
                expiringWeblogs.add(((WeblogEntryData) it.next()).getWebsite().getId());
            }
            
            this.nextExpirations = expiringWeblogs;
            
        } catch(Exception e) {
            log.error(e);
        }
        
        log.debug("finished");
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
        
        log.info("Peeking "+this.peerTime+" minutes into the future each pass");
    }
    
}
