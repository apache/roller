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
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;


/**
 * Hibernate implementation of the PlanetManager.
 */
@com.google.inject.Singleton
public class HibernatePlanetManagerImpl extends AbstractManagerImpl implements PlanetManager {
    
    private static Log log = LogFactory.getLog(HibernatePlanetManagerImpl.class);
    
    private final HibernatePersistenceStrategy strategy;
    
    
    @com.google.inject.Inject 
    protected HibernatePlanetManagerImpl(HibernatePersistenceStrategy strat) {        
        this.strategy = strat;
    }
    
    
    // save a Planet
    public void savePlanet(Planet planet) throws PlanetException {
        strategy.store(planet);
    }
        
    // delete a Planet
    public void deletePlanet(Planet planet) throws PlanetException {
        strategy.remove(planet);
    }
    
    
    // lookup Planet by handle
    public Planet getPlanet(String handle) throws PlanetException {
        Planet planet = null;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(Planet.class);
            criteria.add(Expression.ilike("handle", handle));
            planet = (Planet) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
        return planet;
    }
    
    
    // lookup Planet by id
    public Planet getPlanetById(String id) throws PlanetException {
        return (Planet) strategy.load(id, Planet.class);
    }
    
    
    // lookup all Planets
    public List getPlanets() throws PlanetException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(Planet.class);
            criteria.addOrder(Order.asc("title"));
            return criteria.list();
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
    }
    
    
    // save a Group
    public void saveGroup(PlanetGroup group)  throws PlanetException {
        strategy.store(group);
    }
        
    
    // delete a Group
    public void deleteGroup(PlanetGroup group) throws PlanetException {
        strategy.remove(group);
    }
    
    
    // lookup a Group by Planet & handle
    public PlanetGroup getGroup(Planet planet, String handle) 
            throws PlanetException {
        
        if(planet == null) {
            throw new PlanetException("planet cannot be null");
        }
        
        try {
            Session session = strategy.getSession();
            Criteria criteria = session.createCriteria(PlanetGroup.class);
            criteria.add(Expression.eq("planet", planet));
            criteria.add(Expression.eq("handle", handle));
            return (PlanetGroup) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
    }
    
    
    // lookup a Planet by id
    public PlanetGroup getGroupById(String id) throws PlanetException {
        return (PlanetGroup) strategy.load(id, PlanetGroup.class);
    }
    
    
    // save a Subscription
    public void saveSubscription(Subscription sub) 
            throws PlanetException {
        Subscription existing = getSubscription(sub.getFeedURL());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            this.strategy.store(sub);
        } else {
            throw new PlanetException("ERROR: duplicate feed URLs not allowed");
        }
    }
    
    
    // delete a Subscription
    public void deleteSubscription(Subscription sub) 
            throws PlanetException {
        strategy.remove(sub);
    }
    
    
    // lookup a Subscription by url
    public Subscription getSubscription(String feedURL) 
            throws PlanetException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria =
                    session.createCriteria(Subscription.class);
            criteria.add(Expression.eq("feedURL", feedURL));
            return (Subscription) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
    }
    
    
    // lookup a Subscription by id
    public Subscription getSubscriptionById(String id) 
            throws PlanetException {
        return (Subscription) strategy.load(id, Subscription.class);
    }
    
    
    // lookup all Subscriptions
    // TODO: make pageable
    public List getSubscriptions() throws PlanetException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria =
                    session.createCriteria(Subscription.class);
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
                    "select count(*) from org.apache.roller.planet.pojos.Subscription").uniqueResult();
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
    public List getTopSubscriptions(PlanetGroup group, int offset, int length) 
            throws PlanetException {
        
        List ret = null;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Query query = null;
            if (group != null) {
                query = session.createQuery(
                    "select sub from org.apache.roller.planet.pojos.Subscription sub "
                    +"join sub.groups group "
                    +"where group=:group "
                    +"order by sub.inboundblogs desc");
                query.setSerializable("group", group);
            } else {
                query = session.createQuery(
                    "select sub from org.apache.roller.planet.pojos.Subscription sub "
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
    public void saveEntry(SubscriptionEntry entry) throws PlanetException {
        strategy.store(entry);
    }
    
    
    // delete an Entry
    public void deleteEntry(SubscriptionEntry entry) throws PlanetException {
        strategy.remove(entry);
    }
    
    
    // delete all Entries from a Subscription
    public void deleteEntries(Subscription sub) 
            throws PlanetException {
        Iterator entries = sub.getEntries().iterator();
        while(entries.hasNext()) {
            strategy.remove(entries.next());
        }
        
        // make sure and clear the other side of the assocation
        sub.getEntries().clear();
    }
    
    
    // lookup Entry by id
    public SubscriptionEntry getEntryById(String id) throws PlanetException {
        return (SubscriptionEntry)strategy.load(id, SubscriptionEntry.class);
    }
    
    
    // lookup Entries from a specific Subscription
    public List getEntries(Subscription sub, int offset, int length)
            throws PlanetException {
            
        if(sub == null) {
            throw new PlanetException("subscription cannot be null");
        }
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(SubscriptionEntry.class);
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
    public List getEntries(PlanetGroup group, int offset, int len) 
            throws PlanetException {
        return getEntries(group, null, null, offset, len);
    } 
    
    
    // Lookup Entries from a specific group
    public List getEntries(PlanetGroup group, Date startDate, Date endDate, 
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
            sb.append("select e from SubscriptionEntry e ");
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
