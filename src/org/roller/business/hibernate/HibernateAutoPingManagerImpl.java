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
import org.roller.RollerException;
import org.roller.pojos.AutoPingData;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.config.PingConfig;
import org.roller.model.AutoPingManager;
import org.roller.model.PingQueueManager;
import org.roller.model.RollerFactory;


/**
 * Hibernate implementation of the AutoPingManager.
 */
public class HibernateAutoPingManagerImpl implements AutoPingManager {
    
    static final long serialVersionUID = 5420615676256979199L;
    
    private static Log log = LogFactory.getLog(HibernateAutoPingManagerImpl.class);
    
    private HibernatePersistenceStrategy strategy = null;
    
    
    public HibernateAutoPingManagerImpl(HibernatePersistenceStrategy strat) {
        this.strategy = strat;
    }
    
    
    public AutoPingData getAutoPing(String id) throws RollerException {
        return (AutoPingData) strategy.load(id, AutoPingData.class);
    }
    
    
    public void saveAutoPing(AutoPingData autoPing) throws RollerException {
        strategy.store(autoPing);
    }
    
    
    public void removeAutoPing(AutoPingData autoPing)  throws RollerException {
        //TODO: first remove all related category restrictions (category restrictions are not yet implemented)
        strategy.remove(autoPing);
    }
    
    
    public void removeAutoPing(PingTargetData pingTarget, WebsiteData website) throws RollerException {
        try {
            Session session = strategy.getSession();
            Criteria criteria = session.createCriteria(AutoPingData.class);
            
            // Currently category restrictions are not yet implemented, so we 
            // return all auto ping configs for the website.
            criteria.add(Expression.eq("pingTarget", pingTarget));
            criteria.add(Expression.eq("website", website));
            List matches = criteria.list();
            
            // This should have at most one element, but we remove them all regardless.
            this.removeAutoPings(matches);
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    public void removeAutoPings(Collection autopings) throws RollerException {
        
        // just go through the list and remove each auto ping
        Iterator pings = autopings.iterator();
        while (pings.hasNext()) {
            this.strategy.remove((AutoPingData) pings.next());
        }
    }
    
    
    public void removeAllAutoPings() throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(AutoPingData.class);
            List allAutoPings = criteria.list();
            this.removeAutoPings(allAutoPings);
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    public void queueApplicableAutoPings(WeblogEntryData changedWeblogEntry) throws RollerException {
        if (PingConfig.getSuspendPingProcessing()) {
            if (log.isDebugEnabled()) log.debug("Ping processing is suspended.  No auto pings will be queued.");
            return;
        }
        
        // TODO: new manager method for addQueueEntries(list)?
        PingQueueManager pingQueueMgr = RollerFactory.getRoller().getPingQueueManager();
        List applicableAutopings = getApplicableAutoPings(changedWeblogEntry);
        for (Iterator i = applicableAutopings.iterator(); i.hasNext(); ) {
            AutoPingData autoPing = (AutoPingData) i.next();
            pingQueueMgr.addQueueEntry(autoPing);
        }
    }
    
    
    public List getAutoPingsByWebsite(WebsiteData website) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(AutoPingData.class);
            // Currently category restrictions are not yet implemented, so we return all auto ping configs for the
            // website.
            criteria.add(Expression.eq("website", website));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    public List getAutoPingsByTarget(PingTargetData pingTarget) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(AutoPingData.class);
            // Currently category restrictions are not yet implemented, so we return all auto ping configs for the
            // website.
            criteria.add(Expression.eq("pingTarget", pingTarget));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    public List getApplicableAutoPings(WeblogEntryData changedWeblogEntry) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(AutoPingData.class);
            // Currently category restrictions are not yet implemented, so we return all auto ping configs for the
            // website.
            criteria.add(Expression.eq("website", changedWeblogEntry.getWebsite()));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    public List getCategoryRestrictions(AutoPingData autoPing) throws RollerException {
        return Collections.EMPTY_LIST;
    }
    
    
    public void setCategoryRestrictions(AutoPingData autoPing, Collection newCategories) {
        // NOT YET IMPLEMENTED
        return;
    }
    
    
    public void release() {}
    
}
