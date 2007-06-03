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

package org.apache.roller.planet.business;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;


/**
 * Manages Planets, Groups, Subscriptions, and Entries.
 */
public interface PlanetManager extends Manager {
    
    
    public void savePlanet(PlanetData planet) throws PlanetException;
    
    
    public void deletePlanet(PlanetData planet) throws PlanetException;
    
    
    public PlanetData getPlanet(String handle) throws PlanetException;
    
    
    public PlanetData getPlanetById(String id) throws PlanetException;
    
    
    public List getPlanets() throws PlanetException;
    
    
    /**
     * Save new or update existing a group
     */
    public void saveGroup(PlanetGroupData sub) throws PlanetException;
    
    
    /** 
     * Delete group and any subscriptions that are orphaned. 
     */
    public void deleteGroup(PlanetGroupData group) throws PlanetException;
    
    
    public PlanetGroupData getGroup(PlanetData planet, String handle) throws PlanetException;
    
    
    /**
     * Get group by ID rather than handle.
     */
    public PlanetGroupData getGroupById(String id) throws PlanetException;
    
    
    /**
     * Save or update a subscription
     */
    public void saveSubscription(PlanetSubscriptionData sub) throws PlanetException;
    
    
    /** 
     * Delete subscription, remove it from groups, cache, etc. 
     */
    public void deleteSubscription(PlanetSubscriptionData group) throws PlanetException;
    
    
    /**
     * Get subscription by feedUrl.
     */
    public PlanetSubscriptionData getSubscription(String feedUrl) throws PlanetException;
    
    
    /**
     * Get subscription by ID rather than feedUrl.
     */
    public PlanetSubscriptionData getSubscriptionById(String id) throws PlanetException;
    
    
    /**
     * Get all subscriptions.
     */
    public List getSubscriptions() throws PlanetException;
    
    
    /**
     * Get total number of subscriptions.
     */
    public int getSubscriptionCount() throws PlanetException;
    
    
    /**
     * Get top X subscriptions.
     */
    public List getTopSubscriptions(int offset, int len) throws PlanetException;
    
    
    /**
     * Get top X subscriptions, restricted by group.
     */
    public List getTopSubscriptions(PlanetGroupData group, int offset, int len) 
        throws PlanetException;
    
    
    /**
     * Save new or update existing entry
     */
    public void saveEntry(PlanetEntryData entry) throws PlanetException;
    
    
    /** 
     * Delete entry. 
     */
    public void deleteEntry(PlanetEntryData entry) throws PlanetException;
    
    
    /**
     * Delete all entries for a subscription.
     *
     * @param subscription The subscription to delete entries from.
     * @throws PlanetException If there is a problem doing the delete.
     */
    public void deleteEntries(PlanetSubscriptionData sub) throws PlanetException;
    
    
    /**
     * Lookup an entry by id.
     */
    public PlanetEntryData getEntryById(String id) throws PlanetException;
    
    
    /**
     * Get entries in a single feed as list of PlanetEntryData objects.
     */
    public List getEntries(PlanetSubscriptionData sub, int offset, int len) 
        throws PlanetException;
    
    
    /**
     * Get Entries for a Group in reverse chonological order.
     *
     * @param group Restrict to entries from one group.
     * @param offset Offset into results (for paging)
     * @param len Maximum number of results to return (for paging)
     */
    public List getEntries(PlanetGroupData group, int offset, int len) 
        throws PlanetException;
    
    
    /**
     * Get Entries for a Group in reverse chonological order, optionally 
     * constrained to a certain timeframe.
     *
     * @param group Restrict to entries from one group.
     * @param startDate The oldest date for entries to include.
     * @param endDate The newest date for entries to include.
     * @param offset Offset into results (for paging)
     * @param len Maximum number of results to return (for paging)
     */
    public List getEntries(PlanetGroupData group, 
                           Date startDate, 
                           Date endDate,
                           int offset, 
                           int len) throws PlanetException;
    
}
