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
/*
 * ReferrerProcessingJob.java
 *
 * Created on December 16, 2005, 6:26 PM
 */

package org.apache.roller.business.referrers;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.runnable.Job;
import org.apache.roller.business.referrers.RefererManager;
import org.apache.roller.business.RollerFactory;


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
                                   referrer.getReferrerUrl(),
                                   referrer.getWeblogHandle(),
                                   referrer.getWeblogAnchor(),
                                   referrer.getWeblogDateString());
            
            RollerFactory.getRoller().flush();
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
