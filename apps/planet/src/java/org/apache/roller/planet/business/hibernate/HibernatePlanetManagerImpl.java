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

package org.apache.roller.planet.business.hibernate;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.hibernate.HibernatePersistenceStrategy;
import org.apache.roller.planet.business.AbstractManagerImpl;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;


/**
 * Hibernate implementation of the PlanetManager.
 */
public class HibernatePlanetManagerImpl extends AbstractManagerImpl
        implements PlanetManager {
    
    private static Log log = LogFactory.getLog(HibernatePlanetManagerImpl.class);
    
    private HibernatePersistenceStrategy strategy = null;
    
    
    public HibernatePlanetManagerImpl(HibernatePersistenceStrategy strat) {        
        this.strategy = strat;
    }
    
    
    // save a Planet
    public void savePlanet(PlanetData planet) throws PlanetException {
        strategy.store(planet);
    }
        
    // delete a Planet
    public void deletePlanet(PlanetData planet) throws PlanetException {
        strategy.remove(planet);
    }
    
    
    // lookup Planet by handle
    public PlanetData getPlanet(String handle) throws PlanetException {
        PlanetData planet = null;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetData.class);
            criteria.add(Expression.ilike("handle", handle));
            planet = (PlanetData) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
        return planet;
    }
    
    
    // lookup Planet by id
    public PlanetData getPlanetById(String id) throws PlanetException {
        return (PlanetData) strategy.load(id, PlanetData.class);
    }
    
    
    // lookup all Planets
    public List getPlanets() throws PlanetException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetData.class);
            criteria.addOrder(Order.asc("title"));
            return criteria.list();
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
    }
    
    
    // save a Group
    public void saveGroup(PlanetGroupData group)  throws PlanetException {
        // TODO: move this check outside this method?
        if (group.getId() == null || getGroupById(group.getId()) == null) {
            // If new group, make sure hadnle is unique within Planet
            if (getGroup(group.getPlanet(), group.getHandle()) != null) {
                throw new PlanetException("ERROR group handle already exists in Planet");
            }
        }
        strategy.store(group);
    }
        
    
    // delete a Group
    public void deleteGroup(PlanetGroupData group) throws PlanetException {
        strategy.remove(group);
    }
    
    
    // lookup a Group by Planet & handle
    public PlanetGroupData getGroup(PlanetData planet, String handle) 
            throws PlanetException {
        
        if(planet == null) {
            throw new PlanetException("planet cannot be null");
        }
        
        try {
            Session session = strategy.getSession();
            Criteria criteria = session.createCriteria(PlanetGroupData.class);
            criteria.add(Expression.eq("planet", planet));
            criteria.add(Expression.eq("handle", handle));
            return (PlanetGroupData) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
    }
    
    
    // lookup a Planet by id
    public PlanetGroupData getGroupById(String id) throws PlanetException {
        return (PlanetGroupData) strategy.load(id, PlanetGroupData.class);
    }
    
    
    // save a Subscription
    public void saveSubscription(PlanetSubscriptionData sub) 
            throws PlanetException {
        PlanetSubscriptionData existing = getSubscription(sub.getFeedURL());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            this.strategy.store(sub);
        } else {
            throw new PlanetException("ERROR: duplicate feed URLs not allowed");
        }
    }
    
    
    // delete a Subscription
    public void deleteSubscription(PlanetSubscriptionData sub) 
            throws PlanetException {
        strategy.remove(sub);
    }
    
    
    // lookup a Subscription by url
    public PlanetSubscriptionData getSubscription(String feedURL) 
            throws PlanetException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria =
                    session.createCriteria(PlanetSubscriptionData.class);
            criteria.add(Expression.eq("feedURL", feedURL));
            return (PlanetSubscriptionData) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
    }
    
    
    // lookup a Subscription by id
    public PlanetSubscriptionData getSubscriptionById(String id) 
            throws PlanetException {
        return (PlanetSubscriptionData) strategy.load(id, PlanetSubscriptionData.class);
    }
    
    
    // lookup all Subscriptions
    // TODO: make pageable
    public List getSubscriptions() throws PlanetException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria =
                    session.createCriteria(PlanetSubscriptionData.class);
            criteria.addOrder(Order.asc("feedURL"));
            return criteria.list();
        } catch (Throwable e) {
            throw new PlanetException("ERROR fetching subscription collection", e);
        }
    }
    
    
    // get subscriptions count
    public int getSubscriptionCount() throws PlanetException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Integer count = (Integer)session.createQuery(
                    "select count(*) from org.apache.roller.planet.pojos.PlanetSubscriptionData").uniqueResult();
            return count.intValue();
        } catch (Throwable e) {
            throw new PlanetException("ERROR fetching subscription count", e);
        }
    }
    
    
    // get popular Subscriptions from all Planets & Groups
    // TODO: test this method
    public List getTopSubscriptions(int offset, int length) 
            throws PlanetException {
        
        return getTopSubscriptions(null, offset, length);
    }
    
    
    // get popular Subscriptions from a specific Group
    // TODO: test this method
    public List getTopSubscriptions(PlanetGroupData group, int offset, int length) 
            throws PlanetException {
        
        List ret = null;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Query query = null;
            if (group != null) {
                query = session.createQuery(
                    "select sub from org.apache.roller.planet.pojos.PlanetSubscriptionData sub "
                    +"join sub.groups group "
                    +"where group=:group "
                    +"order by sub.inboundblogs desc");
                query.setSerializable("group", group);
            } else {
                query = session.createQuery(
                    "select sub from org.apache.roller.planet.pojos.PlanetSubscriptionData sub "
                    +"order by sub.inboundblogs desc");
            }
            if (offset != 0) {
                query.setFirstResult(offset);
            }
            if (length != -1) {
                query.setMaxResults(length);
            }
            ret = query.list();
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
        return ret;
    }
        
    // save an Entry
    public void saveEntry(PlanetEntryData entry) throws PlanetException {
        strategy.store(entry);
    }
    
    
    // delete an Entry
    public void deleteEntry(PlanetEntryData entry) throws PlanetException {
        strategy.remove(entry);
    }
    
    
    // delete all Entries from a Subscription
    public void deleteEntries(PlanetSubscriptionData sub) 
            throws PlanetException {
        Iterator entries = sub.getEntries().iterator();
        while(entries.hasNext()) {
            strategy.remove(entries.next());
        }
        
        // make sure and clear the other side of the assocation
        sub.getEntries().clear();
    }
    
    
    // lookup Entry by id
    public PlanetEntryData getEntryById(String id) throws PlanetException {
        return (PlanetEntryData)strategy.load(id, PlanetEntryData.class);
    }
    
    
    // lookup Entries from a specific Subscription
    public List getEntries(PlanetSubscriptionData sub, int offset, int length)
            throws PlanetException {
            
        if(sub == null) {
            throw new PlanetException("subscription cannot be null");
        }
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetEntryData.class);
            criteria.add(Expression.eq("subscription", sub));
            criteria.addOrder(Order.desc("pubTime"));
            criteria.setFirstResult(offset);
            if (length != -1) criteria.setMaxResults(length);
            return criteria.list();
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
    }
    
    
    // lookup Entries from a specific Group
    public List getEntries(PlanetGroupData group, int offset, int len) 
            throws PlanetException {
        return getEntries(group, null, null, offset, len);
    } 
    
    
    // Lookup Entries from a specific group
    public List getEntries(PlanetGroupData group, Date startDate, Date endDate, 
                           int offset, int length) 
            throws PlanetException {
        
        if (group == null) {
            throw new PlanetException("group cannot be null");
        }
        
        List ret = null;
        try {
            long startTime = System.currentTimeMillis();
            
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            
            StringBuffer sb = new StringBuffer();
            sb.append("select e from PlanetEntryData e ");
            sb.append("join e.subscription.groups g ");
            sb.append("where g=:group ");
            
            if (startDate != null) {
                sb.append("and e.pubTime > :startDate ");
            }
            if (endDate != null) {
                sb.append("and e.pubTime < :endDate ");
            }
            sb.append("order by e.pubTime desc");
            
            Query query = session.createQuery(sb.toString());
            query.setParameter("group", group);
            if(offset > 0) {
                query.setFirstResult(offset);
            }
            if (length != -1) {
                query.setMaxResults(length);
            }
            if (startDate != null) {
                query.setParameter("startDate", startDate);
            }
            if(endDate != null) {
                query.setParameter("endDate", endDate);
            }
            
            ret = query.list();
            
            long endTime = System.currentTimeMillis();
            
            log.debug("Generated aggregation in "
                    +((endTime-startTime)/1000.0)+" seconds");
            
        } catch (Throwable e) {
            throw new PlanetException(e);
        }
        
        return ret;
    }


}
