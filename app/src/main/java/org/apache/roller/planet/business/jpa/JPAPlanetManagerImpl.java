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
import org.apache.roller.RollerException;

import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.planet.business.AbstractManagerImpl;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;

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
    
    
    public void saveGroup(PlanetGroup group) throws RollerException {
        strategy.store(group);
    }
    
    public void saveEntry(SubscriptionEntry entry) throws RollerException {
        strategy.store(entry);
    }
    
    public void saveSubscription(Subscription sub)
    throws RollerException {
        Subscription existing = getSubscription(sub.getFeedURL());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            strategy.store(sub);
        } else {
            throw new WebloggerException("ERROR: duplicate feed URLs not allowed");
        }
    }
    
    public void deleteEntry(SubscriptionEntry entry) throws RollerException {
        strategy.remove(entry);
    }
    
    public void deleteGroup(PlanetGroup group) throws RollerException {
        strategy.remove(group);
    }
    
    public void deleteSubscription(Subscription sub)
    throws RollerException {
        strategy.remove(sub);
    }
    
    public Subscription getSubscription(String feedUrl)
    throws RollerException {
        Query q = strategy.getNamedQuery("Subscription.getByFeedURL");
        q.setParameter(1, feedUrl);
        try {
            return (Subscription)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public Subscription getSubscriptionById(String id)
    throws RollerException {
        return (Subscription) strategy.load(
                Subscription.class, id);
    }
    
    public Iterator getAllSubscriptions() {
        try {
            return ((List)strategy.getNamedQuery(
                    "Subscription.getAll").getResultList()).iterator();
        } catch (Exception e) {
            throw new RuntimeException(
                    "ERROR fetching subscription collection", e);
        }
    }
    
    public int getSubscriptionCount() throws RollerException {
        Query q = strategy.getNamedQuery("Subscription.getAll");
        return q.getResultList().size();
    }
    
    public List getTopSubscriptions(int offset, int length)
    throws RollerException {
        return getTopSubscriptions(null, offset, length);
    }
    
    /**
     * Get top X subscriptions, restricted by group.
     */
    public List getTopSubscriptions(
            PlanetGroup group, int offset, int len) throws RollerException {
        List result = null;
        if (group != null) {
            Query q = strategy.getNamedQuery(
                    "Subscription.getByGroupOrderByInboundBlogsDesc");
            q.setParameter(1, group);
            if (offset != 0) {
                q.setFirstResult(offset);
            }
            if (len != -1) {
                q.setMaxResults(len);
            }
            result = q.getResultList();
        } else {
            Query q = strategy.getNamedQuery(
                    "Subscription.getAllOrderByInboundBlogsDesc");
            if (offset != 0) {
                q.setFirstResult(offset);
            }
            if (len != -1) {
                q.setMaxResults(len);
            }
            result = q.getResultList();
        }
        return result;
    }
    
    public PlanetGroup getGroup(String handle) throws RollerException {
        Query q = strategy.getNamedQuery("PlanetGroup.getByHandle");
        q.setParameter(1, handle);
        try {
            return (PlanetGroup)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public PlanetGroup getGroupById(String id) throws RollerException {
        return (PlanetGroup) strategy.load(PlanetGroup.class, id);
    }        
    
    public void release() {}
    
    
    public void savePlanet(Planet planet) throws RollerException {
        strategy.store(planet);
    }
    
    public Planet getWeblogger(String handle) throws RollerException {
        Query q = strategy.getNamedQuery("Planet.getByHandle");
        q.setParameter(1, handle);
        try {
            return (Planet)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public Planet getWebloggerById(String id) throws RollerException {
        return (Planet)strategy.load(Planet.class, id);
    }
    
    public List getWebloggers() throws RollerException {
        return (List)strategy.getNamedQuery("Planet.getAll").getResultList();
    }
    
    public List getGroupHandles(Planet planet) throws RollerException {
        List<String> handles = new ArrayList<String>();
        for (PlanetGroup group : getGroups(planet)) {
            handles.add(group.getHandle());
        }
        return handles;
    }
    
    public List<PlanetGroup> getGroups(Planet planet) throws RollerException {
        Query q = strategy.getNamedQuery("PlanetGroup.getByPlanet");
        q.setParameter(1, planet.getHandle());
        return q.getResultList();
    }
    
    public PlanetGroup getGroup(Planet planet, String handle) throws RollerException {
        Query q = strategy.getNamedQuery("PlanetGroup.getByPlanetAndHandle");
        q.setParameter(1, planet.getHandle());
        q.setParameter(2, handle);
        try {
            return (PlanetGroup)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public void deletePlanet(Planet planet) throws RollerException {
        strategy.remove(planet);
    }
    
    public void deleteEntries(Subscription sub) 
        throws RollerException {
        for (Object entry : sub.getEntries()) {
            strategy.remove(entry);
        }
        // make sure and clear the other side of the association
        sub.getEntries().clear();
    }
    
    public List getSubscriptions() throws RollerException {
        Query q = strategy.getNamedQuery("Subscription.getAllOrderByFeedURL");
        return q.getResultList();
    }

    public SubscriptionEntry getEntryById(String id) throws RollerException {
        return (SubscriptionEntry) strategy.load(SubscriptionEntry.class, id);
    }

    public List getEntries(Subscription sub, int offset, int len) throws RollerException {            
        if (sub == null) {
            throw new WebloggerException("subscription cannot be null");
        }
        Query q = strategy.getNamedQuery("SubscriptionEntry.getBySubscription");
        q.setParameter(1, sub);
        if (offset != 0) {
            q.setFirstResult(offset);
        }
        if (len != -1) {
            q.setMaxResults(len);
        }
        return q.getResultList();
    }

    public List getEntries(PlanetGroup group, int offset, int len) throws RollerException {
        return getEntries(group, null, null, offset, len);
    }

    public List getEntries(PlanetGroup group, Date startDate, Date endDate, int offset, int len) throws RollerException {

        if (group == null) {
            throw new WebloggerException("group cannot be null or empty");
        }
        
        List ret = null;
        try {
            long startTime = System.currentTimeMillis();
            
            StringBuilder sb = new StringBuilder();
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
            if (offset > 0) {
                query.setFirstResult(offset);
            }
            if (len != -1) {
                query.setMaxResults(len);
            }
            
            ret = query.getResultList();
            
            long endTime = System.currentTimeMillis();
            
            log.debug("Generated aggregation of " + ret.size() + " in " + ((endTime-startTime)/1000.0) + " seconds");
            
        } catch (Exception e) {
            throw new WebloggerException(e);
        }
        
        return ret;
    }
}





