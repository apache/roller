/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roller.business.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.PlanetManagerImpl;
import org.roller.config.RollerConfig;
import org.roller.model.Roller;
import org.roller.pojos.PlanetConfigData;
import org.roller.pojos.PlanetEntryData;
import org.roller.pojos.PlanetGroupData;
import org.roller.pojos.PlanetGroupSubscriptionAssoc;
import org.roller.pojos.PlanetSubscriptionData;


/**
 * Manages Planet Roller objects and entry aggregations in a database.
 * @author Dave Johnson
 */
public class HibernatePlanetManagerImpl extends PlanetManagerImpl
{
    protected Map lastUpdatedByGroup = new HashMap();
    protected static final String NO_GROUP = "zzz_nogroup_zzz";     
    private static Log logger = 
        LogFactory.getFactory().getInstance(HibernatePlanetManagerImpl.class);

    public HibernatePlanetManagerImpl(
        PersistenceStrategy strategy, Roller roller)
    {
        super(strategy, roller);
    }

    public void saveConfiguration(PlanetConfigData config) 
        throws RollerException
    {
        config.save();
    }
    
    public void saveGroup(PlanetGroupData group) throws RollerException
    {
        Iterator assocs = group.getGroupSubscriptionAssocs().iterator();
        while (assocs.hasNext())
        {
            PlanetGroupSubscriptionAssoc assoc = 
                    (PlanetGroupSubscriptionAssoc)assocs.next();
            assoc.save();
        }
        group.save();
    }
    
    public void saveEntry(PlanetEntryData entry) throws RollerException
    {
        entry.save();
    }
    
    public void saveSubscription(PlanetSubscriptionData sub) 
        throws RollerException
    {
        PlanetSubscriptionData existing = getSubscription(sub.getFeedUrl());
        if (existing == null || (existing.getId().equals(sub.getId()))) 
        {
            sub.save();
        }
        else 
        {
            throw new RollerException("ERROR: duplicate feed URLs not allowed");
        }
    }
    
    public PlanetConfigData getConfiguration() throws RollerException
    {
        PlanetConfigData config = null;
        try
        {
            Session session = ((HibernateStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetConfigData.class);
            criteria.setMaxResults(1);
            List list = criteria.list();
            config = list.size()!=0 ? (PlanetConfigData)list.get(0) : null;
            
            // We inject the cache dir into the config object here to maintain
            // compatibility with the standaline version of the aggregator.
            if (config != null) 
            {
                config.setCacheDir(
                    RollerConfig.getProperty("planet.aggregator.cache.dir"));
            }                
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
        return config;
    }
    
    public List getGroups() throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetGroupData.class);
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }    
    }

    public List getGroupHandles() throws RollerException
    {
        List handles = new ArrayList();
        Iterator list = getGroups().iterator();
        while (list.hasNext()) 
        {
            PlanetGroupData group = (PlanetGroupData)list.next();
            handles.add(group.getHandle());
        }
        return handles;
    }
    
    public PlanetSubscriptionData getSubscription(String feedUrl) 
        throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)strategy).getSession();
            Criteria criteria = 
                    session.createCriteria(PlanetSubscriptionData.class);
            criteria.setMaxResults(1);
            criteria.add(Expression.eq("feedUrl", feedUrl));
            List list = criteria.list();
            return list.size()!=0 ? (PlanetSubscriptionData)list.get(0) : null;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    public PlanetSubscriptionData getSubscriptionById(String id) 
        throws RollerException
    {
        return (PlanetSubscriptionData)
            strategy.load(id, PlanetSubscriptionData.class);
    }

    public PlanetGroupData getGroup(String handle) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetGroupData.class);
            criteria.setMaxResults(1);
            criteria.add(Expression.eq("handle", handle));
            List list = criteria.list();
            return list.size()!=0 ? (PlanetGroupData)list.get(0) : null;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    public PlanetGroupData getGroupById(String id) throws RollerException
    {
        return (PlanetGroupData)
            strategy.load(id, PlanetGroupData.class);
    }

    public synchronized List getAggregation(int maxEntries) throws RollerException
    {
        return getAggregation(null, maxEntries);
    }
    
    public synchronized List getAggregation(PlanetGroupData group, int maxEntries) 
        throws RollerException
    {
        List ret = null;
        try
        {
            String groupHandle = (group == null) ? NO_GROUP : group.getHandle();
            ret = (List)aggregationsByGroup.get(groupHandle);
            if (ret == null) 
            {
                long startTime = System.currentTimeMillis();
                Session session = 
                    ((HibernateStrategy)strategy).getSession();
                if (group != null)
                {
                    Query query = session.createQuery(
                        "select entry from org.roller.pojos.PlanetEntryData entry "
                        +"join entry.subscription.groupSubscriptionAssocs assoc "
                        +"where assoc.group=:group order by entry.published desc");
                    query.setEntity("group", group);
                    query.setMaxResults(maxEntries);
                    ret = query.list();
                }
                else
                {
                    Query query = session.createQuery(
                       "select entry from org.roller.pojos.PlanetEntryData entry "
                       +"join entry.subscription.groupSubscriptionAssocs assoc "
                       +"where "
                       +"assoc.group.handle='external' or assoc.group.handle='all'"
                       +" order by entry.published desc");
                    query.setMaxResults(maxEntries);
                    ret = query.list();
                }
                Date retLastUpdated = null;
                if (ret.size() > 0)
                {
                    PlanetEntryData entry = (PlanetEntryData)ret.get(0);
                    retLastUpdated = entry.getPublished();
                }
                else 
                {
                    retLastUpdated = new Date();
                }
                aggregationsByGroup.put(groupHandle, ret);
                lastUpdatedByGroup.put(groupHandle, retLastUpdated);

                long endTime = System.currentTimeMillis();
                logger.info("Generated aggregation in "
                                    +((endTime-startTime)/1000.0)+" seconds");
            }
        }
        catch (Throwable e)
        {
            logger.error("ERROR: building aggregation for: "+group, e);
            throw new RollerException(e);
        }
        return ret; 
    }

    public void deleteEntry(PlanetEntryData entry) throws RollerException
    {
        entry.remove();
    }

    public void deleteGroup(PlanetGroupData group) throws RollerException
    {
        group.remove();
    }

    public void deleteSubscription(PlanetSubscriptionData sub) 
        throws RollerException
    {
        sub.remove();
    }
    
    public Iterator getAllSubscriptions()
    {
        try
        {
            Session session = ((HibernateStrategy)strategy).getSession();
            Criteria criteria = 
                    session.createCriteria(PlanetSubscriptionData.class);
            criteria.addOrder(Order.asc("feedUrl"));
            List list = criteria.list();
            return list.iterator();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(
                    "ERROR fetching subscription collection", e);
        }
    }
    
    public int getSubscriptionCount() throws RollerException 
    {
        try
        {
            Session session = ((HibernateStrategy)strategy).getSession();
            Integer count = (Integer)session.createQuery(
                "select count(*) from org.roller.pojos.PlanetSubscriptionData").uniqueResult();
            return count.intValue();
        }
        catch (Throwable e)
        {
            throw new RuntimeException(
                    "ERROR fetching subscription count", e);
        }
    }

    public synchronized List getTopSubscriptions(int max) throws RollerException
    {
        String groupHandle = NO_GROUP;
        List ret = (List)topSubscriptionsByGroup.get(groupHandle);
        if (ret == null)
        {
            try
            {
                Session session = ((HibernateStrategy)strategy).getSession();
                Criteria criteria = 
                        session.createCriteria(PlanetSubscriptionData.class);
                criteria.setMaxResults(max);
                criteria.addOrder(Order.desc("inboundblogs"));
                ret = criteria.list();
            }
            catch (HibernateException e)
            {
                throw new RollerException(e);
            }
            topSubscriptionsByGroup.put(groupHandle, ret);
        }
        return ret;
    }

    public synchronized List getTopSubscriptions(
            PlanetGroupData group, int max) throws RollerException
    {
        String groupHandle = (group == null) ? NO_GROUP : group.getHandle();
        List ret = (List)topSubscriptionsByGroup.get(groupHandle);
        if (ret == null)
        {
            try
            {
                Session session = ((HibernateStrategy)strategy).getSession();
                Query query = session.createQuery(
                 "select sub from org.roller.pojos.PlanetSubscriptionData sub "
                   +"join sub.groupSubscriptionAssocs assoc "
                   +"where "
                   +"assoc.group.handle=:groupHandle "
                   +"order by sub.inboundblogs desc");
                query.setString("groupHandle", group.getHandle());
                query.setMaxResults(max);
                ret = query.list();
            }
            catch (HibernateException e)
            {
                throw new RollerException(e);
            }
            topSubscriptionsByGroup.put(groupHandle, ret);
        }
        return ret;
    }

    public synchronized void clearCachedAggregations() 
    {
        aggregationsByGroup.purge();
        topSubscriptionsByGroup.purge();
        lastUpdatedByGroup.clear();
    }
    
    public Date getLastUpdated()
    {
        return (Date)lastUpdatedByGroup.get(NO_GROUP);
    }
    
    public Date getLastUpdated(PlanetGroupData group)
    {
        return (Date)lastUpdatedByGroup.get(group);
    }
}

