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
