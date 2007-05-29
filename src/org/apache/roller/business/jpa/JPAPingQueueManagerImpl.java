
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
package org.apache.roller.business.jpa;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.RollerException;

import org.apache.roller.business.pings.PingQueueManager;
import org.apache.roller.pojos.AutoPing;
import org.apache.roller.pojos.PingQueueEntry;

/*
 * JPAPingQueueManagerImpl.java
 *
 * Created on May 28, 2006, 4:11 PM
 *
 */
public class JPAPingQueueManagerImpl implements PingQueueManager {

    private static Log log = LogFactory.getLog(
        JPAPingQueueManagerImpl.class);

    /** The strategy for this manager. */
    private JPAPersistenceStrategy strategy;

    /**
     * Creates a new instance of JPAPingQueueManagerImpl
     */
    public JPAPingQueueManagerImpl(
            JPAPersistenceStrategy strategy) {
        this.strategy =  strategy;
    }

    public PingQueueEntry getQueueEntry(String id) 
            throws RollerException {
        return (PingQueueEntry)strategy.load(
            PingQueueEntry.class, id);
    }

    public void saveQueueEntry(PingQueueEntry pingQueueEntry) 
            throws RollerException {
        log.debug("Storing ping queue entry: " + pingQueueEntry);
        strategy.store(pingQueueEntry);
    }

    public void removeQueueEntry(PingQueueEntry pingQueueEntry) 
            throws RollerException {
        log.debug("Removing ping queue entry: " + pingQueueEntry);
        strategy.remove(pingQueueEntry);
    }

    
    public void addQueueEntry(AutoPing autoPing) throws RollerException {
        log.debug("Creating new ping queue entry for auto ping configuration: " 
            + autoPing);
        
        // First check if there is an existing ping queue entry 
        // for the same target and website
        if (isAlreadyQueued(autoPing)) {
            log.debug("A ping queue entry is already present" +
                " for this ping target and website: " + autoPing);
            return;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        PingQueueEntry pingQueueEntry =
                new PingQueueEntry(
                    null, now, autoPing.getPingTarget(), 
                    autoPing.getWebsite(), 0);
        this.saveQueueEntry(pingQueueEntry);
    }

    public List getAllQueueEntries() 
            throws RollerException {
        return (List)strategy.getNamedQuery(
                "PingQueueEntry.getAllOrderByEntryTime").getResultList();
    }

    // private helper to determine if an has already been queued 
    // for the same website and ping target.
    private boolean isAlreadyQueued(AutoPing autoPing) 
        throws RollerException {
        // first, determine if an entry already exists
        Query q = strategy.getNamedQuery("PingQueueEntry.getByPingTarget&Website");
        q.setParameter(1, autoPing.getPingTarget());
        q.setParameter(2, autoPing.getWebsite());
        return q.getResultList().size() > 0;
    }

    public void release() {}
    

}
