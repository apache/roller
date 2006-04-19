/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.business.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.pojos.AutoPingData;
import org.roller.pojos.PingQueueEntryData;
import java.sql.Timestamp;
import java.util.List;
import org.roller.model.PingQueueManager;


/**
 * Hibernate implementation of the PingQueueManager.
 */
public class HibernatePingQueueManagerImpl implements PingQueueManager {
    
    static final long serialVersionUID = -7660638707453106615L;
    
    private static Log log = LogFactory.getLog(HibernatePingQueueManagerImpl.class);
    
    private HibernatePersistenceStrategy strategy = null;
    
    
    public HibernatePingQueueManagerImpl(HibernatePersistenceStrategy strat) {
        this.strategy = strat;
    }
    
    
    public PingQueueEntryData getQueueEntry(String id) throws RollerException {
        return (PingQueueEntryData) strategy.load(id, PingQueueEntryData.class);
    }
    
    
    public void saveQueueEntry(PingQueueEntryData pingQueueEntry) throws RollerException {
        log.debug("Storing ping queue entry: " + pingQueueEntry);
        strategy.store(pingQueueEntry);
    }
    
    
    public void removeQueueEntry(PingQueueEntryData pingQueueEntry) throws RollerException {
        log.debug("Removing ping queue entry: " + pingQueueEntry);
        strategy.remove(pingQueueEntry);
    }
    
    
    public void addQueueEntry(AutoPingData autoPing) throws RollerException {
        log.debug("Creating new ping queue entry for auto ping configuration: " + autoPing);
        
        // First check if there is an existing ping queue entry for the same target and website
        if (isAlreadyQueued(autoPing)) {
            log.debug("A ping queue entry is already present for this ping target and website: " + autoPing);
            return;
        }
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PingQueueEntryData pingQueueEntry =
                new PingQueueEntryData(null, now, autoPing.getPingTarget(), autoPing.getWebsite(), 0);
        this.saveQueueEntry(pingQueueEntry);
    }
    
    
    public List getAllQueueEntries() throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(PingQueueEntryData.class);
            criteria.addOrder(Order.asc("entryTime"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException("ERROR retrieving queue entries.", e);
        }
    }
    
    
    // private helper to determine if an has already been queued for the same website and ping target.
    private boolean isAlreadyQueued(AutoPingData autoPing) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(PingQueueEntryData.class);
            criteria.add(Expression.eq("pingTarget", autoPing.getPingTarget()));
            criteria.add(Expression.eq("website", autoPing.getWebsite()));
            return !criteria.list().isEmpty();
        } catch (HibernateException e) {
            throw new RollerException("ERROR determining if preexisting queue entry is present.",e);
        }
    }
    
    
    public void release() {}
    
}
