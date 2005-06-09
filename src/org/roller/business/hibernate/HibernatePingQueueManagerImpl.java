/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.business.hibernate;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.PingQueueManagerImpl;
import org.roller.pojos.AutoPingData;
import org.roller.pojos.PingQueueEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.PingTargetData;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

public class HibernatePingQueueManagerImpl extends PingQueueManagerImpl
{
    static final long serialVersionUID = -7660638707453106615L;

    private static Log logger = LogFactory.getLog(HibernatePingQueueManagerImpl.class);

    public HibernatePingQueueManagerImpl(PersistenceStrategy persistenceStrategy)
    {
        super(persistenceStrategy);
    }

    public void addQueueEntry(AutoPingData autoPing) throws RollerException
    {
        if (logger.isDebugEnabled()) logger.debug("Creating new ping queue entry for auto ping configuration: " + autoPing);

        // First check if there is an existing ping queue entry for the same target and website
        if (isAlreadyQueued(autoPing))
        {
            if (logger.isDebugEnabled()) logger.debug("A ping queue entry is already present for this ping target and website: " + autoPing);
            return;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        PingQueueEntryData pingQueueEntry =
            new PingQueueEntryData(null, now, autoPing.getPingTarget(), autoPing.getWebsite(), 0);
        storeQueueEntry(pingQueueEntry);
    }

    public void dropQueue() throws RollerException
    {
        logger.info("NOTICE Dropping all ping queue entries.");
        List queueEntries = getAllQueueEntries();
        removeEntries(queueEntries);
    }

    public List getAllQueueEntries() throws RollerException
    {
        Session session = ((HibernateStrategy) persistenceStrategy).getSession();
        Criteria criteria = session.createCriteria(PingQueueEntryData.class);
        criteria.addOrder(Order.asc("entryTime"));
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException("ERROR retrieving queue entries.", e);
        }
    }

    public void removeQueueEntriesByPingTarget(PingTargetData pingTarget) throws RollerException {
        try
        {
            if (logger.isDebugEnabled()) logger.debug("Removing all ping queue entries for ping target " + pingTarget);
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(PingQueueEntryData.class);
            criteria.add(Expression.eq("pingTarget", pingTarget));
            List queueEntries = criteria.list();
            removeEntries(queueEntries);
        }
        catch (HibernateException e)
        {
            throw new RollerException("ERROR removing queue entries for ping target " + pingTarget, e);
        }
    }

    public void removeQueueEntriesByWebsite(WebsiteData website) throws RollerException {
        try
        {
            if (logger.isDebugEnabled()) logger.debug("Removing all ping queue entries for website " + website);
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(PingQueueEntryData.class);
            criteria.add(Expression.eq("website", website));
            List queueEntries = criteria.list();
            removeEntries(queueEntries);
        }
        catch (HibernateException e)
        {
            throw new RollerException("ERROR removing queue entries for website " + website, e);
        }
    }

    // private helper to determine if an has already been queued for the same website and ping target.
    private boolean isAlreadyQueued(AutoPingData autoPing) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(PingQueueEntryData.class);
            criteria.add(Expression.eq("pingTarget", autoPing.getPingTarget()));
            criteria.add(Expression.eq("website", autoPing.getWebsite()));
            return !criteria.list().isEmpty();
        }
        catch (HibernateException e)
        {
            throw new RollerException("ERROR determining if preexisting queue entry is present.",e);
        }
    }

    // Private helper to remove a collection of queue entries
    private void removeEntries(Collection queueEntries) throws RollerException {
        for (Iterator i = queueEntries.iterator(); i.hasNext();)
        {
            PingQueueEntryData pqe = (PingQueueEntryData) i.next();
            pqe.remove();
        }
    }
}
