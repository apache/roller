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
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
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
    
    
    public void saveGroup(PlanetGroupData group) throws PlanetException {
        strategy.store(group);
    }
    
    public void saveEntry(PlanetEntryData entry) throws PlanetException {
        strategy.store(entry);
    }
    
    public void saveSubscription(PlanetSubscriptionData sub)
    throws PlanetException {
        PlanetSubscriptionData existing = getSubscription(sub.getFeedURL());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            strategy.store(sub);
        } else {
            throw new PlanetException("ERROR: duplicate feed URLs not allowed");
        }
    }
    
    public void deleteEntry(PlanetEntryData entry) throws PlanetException {
        strategy.remove(entry);
    }
    
    public void deleteGroup(PlanetGroupData group) throws PlanetException {
        strategy.remove(group);
    }
    
    public void deleteSubscription(PlanetSubscriptionData sub)
    throws PlanetException {
        strategy.remove(sub);
    }
    
    public PlanetSubscriptionData getSubscription(String feedUrl)
    throws PlanetException {
        Query q = strategy.getNamedQuery("PlanetSubscriptionData.getByFeedURL");
        q.setParameter(1, feedUrl);
        try {
            return (PlanetSubscriptionData)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public PlanetSubscriptionData getSubscriptionById(String id)
    throws PlanetException {
        return (PlanetSubscriptionData) strategy.load(
                PlanetSubscriptionData.class, id);
    }
    
    public Iterator getAllSubscriptions() {
        try {
            return ((List)strategy.getNamedQuery(
                    "PlanetSubscriptionData.getAll").getResultList()).iterator();
        } catch (Throwable e) {
            throw new RuntimeException(
                    "ERROR fetching subscription collection", e);
        }
    }
    
    public int getSubscriptionCount() throws PlanetException {
        Query q = strategy.getNamedQuery("PlanetSubscriptionData.getAll");
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
            PlanetGroupData group, int offset, int len) throws PlanetException {
        List result = null;
        if (group != null) {
            Query q = strategy.getNamedQuery(
                    "PlanetSubscriptionData.getByGroupOrderByInboundBlogsDesc");
            q.setParameter(1, group);
            if (offset != 0) q.setFirstResult(offset);
            if (len != -1) q.setMaxResults(len);
            result = q.getResultList();
        } else {
            Query q = strategy.getNamedQuery(
                    "PlanetSubscriptionData.getAllOrderByInboundBlogsDesc");
            if (offset != 0) q.setFirstResult(offset);
            if (len != -1) q.setMaxResults(len);
            result = q.getResultList();
        }
        return result;
    }
    
    public PlanetGroupData getGroup(String handle) throws PlanetException {
        Query q = strategy.getNamedQuery("PlanetGroupData.getByHandle");
        q.setParameter(1, handle);
        try {
            return (PlanetGroupData)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public PlanetGroupData getGroupById(String id) throws PlanetException {
        return (PlanetGroupData) strategy.load(PlanetGroupData.class, id);
    }        
    
    public void release() {}
    
    
    public void savePlanet(PlanetData planet) throws PlanetException {
        strategy.store(planet);
    }
    
    public PlanetData getPlanet(String handle) throws PlanetException {
        Query q = strategy.getNamedQuery("PlanetData.getByHandle");
        q.setParameter(1, handle);
        try {
            return (PlanetData)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public PlanetData getPlanetById(String id) throws PlanetException {
        return (PlanetData)strategy.load(PlanetData.class, id);
    }
    
    public List getPlanets() throws PlanetException {
        return (List)strategy.getNamedQuery("PlanetData.getAll").getResultList();
    }
    
    public List getGroupHandles(PlanetData planet) throws PlanetException {
        List handles = new ArrayList();
        Iterator list = getGroups(planet).iterator();
        while (list.hasNext()) {
            PlanetGroupData group = (PlanetGroupData) list.next();
            handles.add(group.getHandle());
        }
        return handles;
    }
    
    public List getGroups(PlanetData planet) throws PlanetException {
        Query q = strategy.getNamedQuery("PlanetGroupData.getByPlanet");
        q.setParameter(1, planet.getHandle());
        return q.getResultList();
    }
    
    public PlanetGroupData getGroup(PlanetData planet, String handle) throws PlanetException {
        Query q = strategy.getNamedQuery("PlanetGroupData.getByPlanetAndHandle");
        q.setParameter(1, planet.getHandle());
        q.setParameter(2, handle);
        try {
            return (PlanetGroupData)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public void deletePlanet(PlanetData planet) throws PlanetException {
        strategy.remove(planet);
    }
    
    public void deleteEntries(PlanetSubscriptionData sub) 
        throws PlanetException {
        Iterator entries = sub.getEntries().iterator();
        while(entries.hasNext()) {
            strategy.remove(entries.next());
        }
        
        // make sure and clear the other side of the assocation
        sub.getEntries().clear();
    }
    
    public List getSubscriptions() throws PlanetException {
        Query q = strategy.getNamedQuery("PlanetSubscriptionData.getAllOrderByFeedURL");
        return q.getResultList();
    }

    public PlanetEntryData getEntryById(String id) throws PlanetException {
        return (PlanetEntryData) strategy.load(PlanetEntryData.class, id);
    }

    public List getEntries(PlanetSubscriptionData sub, int offset, int len) throws PlanetException {            
        if(sub == null) {
            throw new PlanetException("subscription cannot be null");
        }
        Query q = strategy.getNamedQuery("PlanetEntryData.getBySubscription");
        q.setParameter(1, sub);
        if (offset != 0) q.setFirstResult(offset);
        if (len != -1) q.setMaxResults(len);
        return q.getResultList();
    }

    public List getEntries(PlanetGroupData group, int offset, int len) throws PlanetException {
        return getEntries(group, null, null, offset, len);
    }

    public List getEntries(PlanetGroupData group, Date startDate, Date endDate, int offset, int len) throws PlanetException {
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
            sb.append("SELECT e FROM PlanetEntryData e ");
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





