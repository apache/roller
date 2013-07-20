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
 * ReferrerQueueManager.java
 *
 * Created on December 16, 2005, 5:37 PM
 */

package org.apache.roller.weblogger.business.referrers;

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
    void processReferrer(IncomingReferrer ref);
    
    
    /**
     * Add a referrer to the queue.
     *
     * It is almost always preferable to call processReferrer() instead.
     */
    void enqueue(IncomingReferrer ref);
    
    
    /**
     * Get the next item in the queue.
     *
     * Returns null if there is nothing in the queue.
     */
    IncomingReferrer dequeue();
    
    
    /**
     * Called when the system is being shutdown.
     */
    void shutdown();
    
}
