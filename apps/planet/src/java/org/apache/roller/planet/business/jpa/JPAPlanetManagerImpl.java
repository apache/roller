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

package org.apache.roller.planet.business.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.planet.business.AbstractManagerImpl;

/**
 * Manages Planet Roller objects and entry aggregations in a database.
 *
 * @author Dave Johnson
 */
@com.google.inject.Singleton
public class JPAPlanetManagerImpl extends AbstractManagerImpl implements PlanetManager {
    
    private static Log log = LogFactory.getLog(JPAPlanetManagerImpl.class);
    
    /** The strategy for this manager. */
    private final JPAPersistenceStrategy strategy;
    
    protected Map lastUpdatedByGroup = new HashMap();
    protected static final String NO_GROUP = "zzz_nogroup_zzz";
    
    
    @com.google.inject.Inject  
    protected JPAPlanetManagerImpl(JPAPersistenceStrategy strategy) {
        log.debug("Instantiating JPA Planet Manager");
        
        this.strategy = strategy;
    }
    
    
    public void saveGroup(PlanetGroup group) throws PlanetException {
        strategy.store(group);
    }
    
    public void saveEntry(SubscriptionEntry entry) throws PlanetException {
        strategy.store(entry);
    }
    
    public void saveSubscription(Subscription sub)
    throws PlanetException {
        Subscription existing = getSubscription(sub.getFeedURL());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            strategy.store(sub);
        } else {
            throw new PlanetException("ERROR: duplicate feed URLs not allowed");
        }
    }
    
    public void deleteEntry(SubscriptionEntry entry) throws PlanetException {
        strategy.remove(entry);
    }
    
    public void deleteGroup(PlanetGroup group) throws PlanetException {
        strategy.remove(group);
    }
    
    public void deleteSubscription(Subscription sub)
    throws PlanetException {
        strategy.remove(sub);
    }
    
    public Subscription getSubscription(String feedUrl)
    throws PlanetException {
        Query q = strategy.getNamedQuery("Subscription.getByFeedURL");
        q.setParameter(1, feedUrl);
        try {
            return (Subscription)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public Subscription getSubscriptionById(String id)
    throws PlanetException {
        return (Subscription) strategy.load(
                Subscription.class, id);
    }
    
    public Iterator getAllSubscriptions() {
        try {
            return ((List)strategy.getNamedQuery(
                    "Subscription.getAll").getResultList()).iterator();
        } catch (Throwable e) {
            throw new RuntimeException(
                    "ERROR fetching subscription collection", e);
        }
    }
    
    public int getSubscriptionCount() throws PlanetException {
        Query q = strategy.getNamedQuery("Subscription.getAll");
        return q.getResultList().size();
    }
    
    public List getTopSubscriptions(int offset, int length)
    throws PlanetException {
        return getTopSubscriptions(null, offset, length);
    }
    
    /**
     * Get top X subscriptions, restricted by group.
     */
    public List getTopSubscriptions(
            PlanetGroup group, int offset, int len) throws PlanetException {
        List result = null;
        if (group != null) {
            Query q = strategy.getNamedQuery(
                    "Subscription.getByGroupOrderByInboundBlogsDesc");
            q.setParameter(1, group);
            if (offset != 0) q.setFirstResult(offset);
            if (len != -1) q.setMaxResults(len);
            result = q.getResultList();
        } else {
            Query q = strategy.getNamedQuery(
                    "Subscription.getAllOrderByInboundBlogsDesc");
            if (offset != 0) q.setFirstResult(offset);
            if (len != -1) q.setMaxResults(len);
            result = q.getResultList();
        }
        return result;
    }
    
    public PlanetGroup getGroup(String handle) throws PlanetException {
        Query q = strategy.getNamedQuery("PlanetGroup.getByHandle");
        q.setParameter(1, handle);
        try {
            return (PlanetGroup)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public PlanetGroup getGroupById(String id) throws PlanetException {
        return (PlanetGroup) strategy.load(PlanetGroup.class, id);
    }        
    
    public void release() {}
    
    
    public void savePlanet(Planet planet) throws PlanetException {
        strategy.store(planet);
    }
    
    public Planet getPlanet(String handle) throws PlanetException {
        Query q = strategy.getNamedQuery("Planet.getByHandle");
        q.setParameter(1, handle);
        try {
            return (Planet)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public Planet getPlanetById(String id) throws PlanetException {
        return (Planet)strategy.load(Planet.class, id);
    }
    
    public List getPlanets() throws PlanetException {
        return (List)strategy.getNamedQuery("Planet.getAll").getResultList();
    }
    
    public List getGroupHandles(Planet planet) throws PlanetException {
        List handles = new ArrayList();
        Iterator list = getGroups(planet).iterator();
        while (list.hasNext()) {
            PlanetGroup group = (PlanetGroup) list.next();
            handles.add(group.getHandle());
        }
        return handles;
    }
    
    public List getGroups(Planet planet) throws PlanetException {
        Query q = strategy.getNamedQuery("PlanetGroup.getByPlanet");
        q.setParameter(1, planet.getHandle());
        return q.getResultList();
    }
    
    public PlanetGroup getGroup(Planet planet, String handle) throws PlanetException {
        Query q = strategy.getNamedQuery("PlanetGroup.getByPlanetAndHandle");
        q.setParameter(1, planet.getHandle());
        q.setParameter(2, handle);
        try {
            return (PlanetGroup)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public void deletePlanet(Planet planet) throws PlanetException {
        strategy.remove(planet);
    }
    
    public void deleteEntries(Subscription sub) 
        throws PlanetException {
        Iterator entries = sub.getEntries().iterator();
        while(entries.hasNext()) {
            strategy.remove(entries.next());
        }
        
        // make sure and clear the other side of the assocation
        sub.getEntries().clear();
    }
    
    public List getSubscriptions() throws PlanetException {
        Query q = strategy.getNamedQuery("Subscription.getAllOrderByFeedURL");
        return q.getResultList();
    }

    public SubscriptionEntry getEntryById(String id) throws PlanetException {
        return (SubscriptionEntry) strategy.load(SubscriptionEntry.class, id);
    }

    public List getEntries(Subscription sub, int offset, int len) throws PlanetException {            
        if(sub == null) {
            throw new PlanetException("subscription cannot be null");
        }
        Query q = strategy.getNamedQuery("SubscriptionEntry.getBySubscription");
        q.setParameter(1, sub);
        if (offset != 0) q.setFirstResult(offset);
        if (len != -1) q.setMaxResults(len);
        return q.getResultList();
    }

    public List getEntries(PlanetGroup group, int offset, int len) throws PlanetException {
        return getEntries(group, null, null, offset, len);
    }

    public List getEntries(PlanetGroup group, Date startDate, Date endDate, int offset, int len) throws PlanetException {
        StringBuffer queryString = new StringBuffer();
                
        if(group == null) {
            throw new PlanetException("group cannot be null or empty");
        }
        
        List ret = null;
        try {
            long startTime = System.currentTimeMillis();
            
            StringBuffer sb = new StringBuffer();
            List params = new ArrayList();
            int size = 0;
            sb.append("SELECT e FROM SubscriptionEntry e ");
            sb.append("JOIN e.subscription.groups g ");
                        
            params.add(size++, group.getHandle());
            sb.append("WHERE g.handle = ?").append(size);
            
            if (startDate != null) {
                params.add(size++, new Timestamp(startDate.getTime()));
                sb.append(" AND e.pubTime > ?").append(size);
            }
            if (endDate != null) {
                params.add(size++, new Timestamp(endDate.getTime()));
                sb.append(" AND e.pubTime < :?").append(size);
            }
            sb.append(" ORDER BY e.pubTime DESC");
            
            Query query = strategy.getDynamicQuery(sb.toString());
            for (int i=0; i<params.size(); i++) {
                query.setParameter(i+1, params.get(i));
            }
            if(offset > 0) {
                query.setFirstResult(offset);
            }
            if (len != -1) {
                query.setMaxResults(len);
            }
            
            ret = query.getResultList();
            
            long endTime = System.currentTimeMillis();
            
            log.debug("Generated aggregation in "
                    +((endTime-startTime)/1000.0)+" seconds");
            
        } catch (Throwable e) {
            throw new PlanetException(e);
        }
        
        return ret;
    }
}





