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


package org.apache.roller.business.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.AutoPing;
import org.apache.roller.pojos.PingQueueEntry;
import java.sql.Timestamp;
import java.util.List;
import org.apache.roller.business.pings.PingQueueManager;


/**
 * Hibernate implementation of the PingQueueManager.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 */
public class HibernatePingQueueManagerImpl implements PingQueueManager {
    
    static final long serialVersionUID = -7660638707453106615L;
    
    private static Log log = LogFactory.getLog(HibernatePingQueueManagerImpl.class);
    
    private HibernatePersistenceStrategy strategy = null;
    
    
    public HibernatePingQueueManagerImpl(HibernatePersistenceStrategy strat) {
        this.strategy = strat;
    }
    
    
    public PingQueueEntry getQueueEntry(String id) throws RollerException {
        return (PingQueueEntry) strategy.load(id, PingQueueEntry.class);
    }
    
    
    public void saveQueueEntry(PingQueueEntry pingQueueEntry) throws RollerException {
        log.debug("Storing ping queue entry: " + pingQueueEntry);
        strategy.store(pingQueueEntry);
    }
    
    
    public void removeQueueEntry(PingQueueEntry pingQueueEntry) throws RollerException {
        log.debug("Removing ping queue entry: " + pingQueueEntry);
        strategy.remove(pingQueueEntry);
    }
    
    
    public void addQueueEntry(AutoPing autoPing) throws RollerException {
        log.debug("Creating new ping queue entry for auto ping configuration: " + autoPing);
        
        // First check if there is an existing ping queue entry for the same target and website
        if (isAlreadyQueued(autoPing)) {
            log.debug("A ping queue entry is already present for this ping target and website: " + autoPing);
            return;
        }
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PingQueueEntry pingQueueEntry =
                new PingQueueEntry(null, now, autoPing.getPingTarget(), autoPing.getWebsite(), 0);
        this.saveQueueEntry(pingQueueEntry);
    }
    
    
    public List getAllQueueEntries() throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(PingQueueEntry.class);
            criteria.addOrder(Order.asc("entryTime"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException("ERROR retrieving queue entries.", e);
        }
    }
    
    
    // private helper to determine if an has already been queued for the same website and ping target.
    private boolean isAlreadyQueued(AutoPing autoPing) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(PingQueueEntry.class);
            criteria.add(Expression.eq("pingTarget", autoPing.getPingTarget()));
            criteria.add(Expression.eq("website", autoPing.getWebsite()));
            return !criteria.list().isEmpty();
        } catch (HibernateException e) {
            throw new RollerException("ERROR determining if preexisting queue entry is present.",e);
        }
    }
    
    
    public void release() {}
    
}
