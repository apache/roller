/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.model;

import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.PingQueueEntryData;


/**
 * PingQueueManager.  This interface describes the manager for the weblog update ping request queue. The queue is
 * processed by the <code>PingQueueProcesssor</code> and <code>PingQueueTask</code> components in the application
 * layer.
 */
public interface PingQueueManager {
    
    
    /**
     * Add a new persistent entry to the queue.  If the queue already contains an entry for the ping target and website
     * specified by this auto ping configuration, a new one will not be added.
     *
     * @param autoPing auto ping configuration for the ping request to be queued.
     */
    public void addQueueEntry(AutoPingData autoPing) throws RollerException;
    
    
    /**
     * Store the given queue entry.
     *
     * @param pingQueueEntry update the given queue entry
     * @throws RollerException
     */
    public void saveQueueEntry(PingQueueEntryData pingQueueEntry) throws RollerException;
    
    
    /**
     * Remove a queue entry.
     *
     * @param pingQueueEntry the entry to be removed.
     * @throws RollerException
     */
    public void removeQueueEntry(PingQueueEntryData pingQueueEntry) throws RollerException;
    
    
    /**
     * Retrieve an entry from the queue.
     *
     * @param id the unique id of the entry.
     * @return the queue entry with the specified id.
     * @throws RollerException
     */
    public PingQueueEntryData getQueueEntry(String id) throws RollerException;
    
    
    /**
     * Get all of the queue entries.
     *
     * @return the queue as a <code>List</code> of {@link PingQueueEntryData} objects.
     * @throws RollerException
     */
    public List getAllQueueEntries() throws RollerException;
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}
