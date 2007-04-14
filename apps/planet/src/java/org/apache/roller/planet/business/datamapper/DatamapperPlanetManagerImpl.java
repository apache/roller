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

package org.apache.roller.planet.business.datamapper;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.business.datamapper.DatamapperPersistenceStrategy;
import org.apache.roller.business.datamapper.DatamapperQuery;
import org.apache.roller.planet.business.AbstractManagerImpl;


/**
 * Manages Planet Roller objects and entry aggregations in a database.
 * 
 * @author Dave Johnson
 */
public abstract class DatamapperPlanetManagerImpl extends AbstractManagerImpl implements PlanetManager {

    private static Log log = LogFactory.getLog(
        DatamapperPlanetManagerImpl.class);

    /** The strategy for this manager. */
    protected DatamapperPersistenceStrategy strategy;

    protected Map lastUpdatedByGroup = new HashMap();
    protected static final String NO_GROUP = "zzz_nogroup_zzz";

    public DatamapperPlanetManagerImpl 
            (DatamapperPersistenceStrategy strategy) {
        log.debug("Instantiating Datamapper Planet Manager");

        this.strategy = strategy;
    }

    public void saveGroup(PlanetGroupData group) throws RollerException {
        strategy.store(group);
    }

    public void saveEntry(PlanetEntryData entry) throws RollerException {
        strategy.store(entry);
    }

    public void saveSubscription(PlanetSubscriptionData sub)
            throws RollerException {
        PlanetSubscriptionData existing = getSubscription(sub.getFeedURL());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            strategy.store(sub);
        }
        else {
            throw new RollerException("ERROR: duplicate feed URLs not allowed");
        }
    }

    public void deleteEntry(PlanetEntryData entry) throws RollerException {
        strategy.remove(entry);
    }

    public void deleteGroup(PlanetGroupData group) throws RollerException {
        strategy.remove(group);
    }

    public void deleteSubscription(PlanetSubscriptionData sub)
            throws RollerException {
        strategy.remove(sub);
    }

    public PlanetSubscriptionData getSubscription(String feedUrl)
            throws RollerException {
        List results = (List) strategy.newQuery(PlanetSubscriptionData.class, 
                "PlanetSubscriptionData.getByFeedURL").execute(feedUrl); 
        return results.size()!=0 ? 
            (PlanetSubscriptionData)results.get(0) : null;
    }

    public PlanetSubscriptionData getSubscriptionById(String id)
            throws RollerException {
        return (PlanetSubscriptionData) strategy.load(
                PlanetSubscriptionData.class, id);
    }

    public int getSubscriptionCount() throws RollerException {
        return ((List)strategy.newQuery(PlanetSubscriptionData.class, 
                "PlanetSubscriptionData.getAll").execute()).size(); 
    }

    public List getTopSubscriptions(int offset, int length) 
            throws RollerException {
        return getTopSubscriptions(null, offset, length);
    }
        
    /**
     * Get top X subscriptions, restricted by group.
     */
    public List getTopSubscriptions(
            PlanetGroupData group, int offset, int len) throws RollerException {
        List result = null;
        if (group != null) {
            result = (List) strategy.newQuery(PlanetSubscriptionData.class,
                "PlanetSubscriptionData.getByGroupOrderByInboundBlogsDesc").execute(group);
        } else {
            result = (List) strategy.newQuery(PlanetSubscriptionData.class,
                "PlanetSubscriptionData.getAllOrderByInboundBlogsDesc").execute();
        }
        // TODO handle offset and length
        return result;
    }
    
    public PlanetGroupData getGroup(String handle) throws RollerException {
        List results = (List) strategy.newQuery(PlanetGroupData.class, 
                "PlanetGroupData.getByHandle").execute(handle); 
        // TODO handle max result == 1
        PlanetGroupData group = results.size()!=0 ? 
            (PlanetGroupData)results.get(0) : null;
        return group;
    }

    public PlanetGroupData getGroupById(String id) throws RollerException {
        return (PlanetGroupData) strategy.load(PlanetGroupData.class, id);
    }

    public void savePlanet(PlanetData planet) throws RollerException {
        strategy.store(planet);
    }

    public PlanetData getPlanet(String handle) throws RollerException {
        List results = (List) strategy.newQuery(PlanetData.class, 
            "PlanetData.getByHandle").execute(handle); 
        // TODO handle max result == 1
        PlanetData planet = results.size()!=0 ? 
            (PlanetData)results.get(0) : null;
        return planet;
    }

    public PlanetData getPlanetById(String id) throws RollerException {
        return (PlanetData)strategy.load(PlanetData.class, id);
    }

    public List getPlanets() throws RollerException {
        return (List)strategy.newQuery(PlanetData.class, 
            "PlanetData.getAll").execute(); 
    }

    public PlanetGroupData getGroup(PlanetData planet, String handle) throws RollerException {
        List results = (List) strategy.newQuery(PlanetData.class, 
            "PlanetGroupData.getByPlanetAndHandle").execute(new Object[] {planet.getHandle(), handle}); 
        // TODO handle max result == 1
        PlanetGroupData group = results.size()!=0 ? 
            (PlanetGroupData)results.get(0) : null;
        return group;
    }

    public void deletePlanet(PlanetData planet) throws RollerException {
        strategy.remove(planet);
    }

    public void deleteEntries(PlanetSubscriptionData sub) 
        throws RollerException {
        Iterator entries = sub.getEntries().iterator();
        while(entries.hasNext()) {
            strategy.remove(entries.next());
        }
        
        // make sure and clear the other side of the assocation
        sub.getEntries().clear();
    }        

    public List getSubscriptions() throws RollerException {
        return (List)strategy.newQuery(PlanetSubscriptionData.class, 
            "PlanetSubscriptionData.getAllOrderByFeedURL").execute(); 
    }

    public PlanetEntryData getEntryById(String id) throws RollerException {
        return (PlanetEntryData) strategy.load(PlanetEntryData.class, id);
    }

    public List getEntries(PlanetSubscriptionData sub, int offset, int len) throws RollerException {            
        if(sub == null) {
            throw new RollerException("subscription cannot be null");
        }
        boolean setRange = offset != 0 || len != -1;
        if (len == -1) {
            len = Integer.MAX_VALUE - offset;
        }
        DatamapperQuery q = strategy.newQuery(PlanetData.class, "PlanetEntryData.getBySubscription");
        if (setRange) q.setRange(offset, offset + len);
        return (List)q.execute(new Object[] {sub});
    }

    public List getEntries(PlanetGroupData group, int offset, int len) throws RollerException {
        return getEntries(group, null, null, offset, len);
    }

}

