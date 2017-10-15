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

import java.util.Date;
import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.WebloggerException;


/**
 * Manages Planets, Groups, Subscriptions, and Entries.
 */
public interface PlanetManager extends Manager {
    
    
    void savePlanet(Planet planet) throws RollerException;
    
    
    void deletePlanet(Planet planet) throws RollerException;
    
    
    Planet getWeblogger(String handle) throws RollerException;
    
    
    Planet getWebloggerById(String id) throws RollerException;
    
    
    List<Planet> getWebloggers() throws RollerException;
    
    
    /**
     * Save new or update existing a group
     */
    void saveGroup(PlanetGroup sub) throws RollerException;
    
    
    /** 
     * Delete group and any subscriptions that are orphaned. 
     */
    void deleteGroup(PlanetGroup group) throws RollerException;
    
    
    PlanetGroup getGroup(Planet planet, String handle) throws RollerException;
    
    
    /**
     * Get group by ID rather than handle.
     */
    PlanetGroup getGroupById(String id) throws RollerException;
    
    
    /**
     * Save or update a subscription
     */
    void saveSubscription(Subscription sub) throws RollerException;
    
    
    /** 
     * Delete subscription, remove it from groups, cache, etc. 
     */
    void deleteSubscription(Subscription group) throws RollerException;
    
    
    /**
     * Get subscription by feedUrl.
     */
    Subscription getSubscription(String feedUrl) throws RollerException;
    
    
    /**
     * Get subscription by ID rather than feedUrl.
     */
    Subscription getSubscriptionById(String id) throws RollerException;
    
    
    /**
     * Get all subscriptions.
     */
    List<Subscription> getSubscriptions() throws RollerException;
    
    
    /**
     * Get total number of subscriptions.
     */
    int getSubscriptionCount() throws RollerException;
    
    
    /**
     * Get top X subscriptions.
     */
    List<Subscription> getTopSubscriptions(int offset, int len) throws RollerException;
    
    
    /**
     * Get top X subscriptions, restricted by group.
     */
    List<Subscription> getTopSubscriptions(PlanetGroup group, int offset, int len)
        throws RollerException;
    
    
    /**
     * Save new or update existing entry
     */
    void saveEntry(SubscriptionEntry entry) throws RollerException;
    
    
    /** 
     * Delete entry. 
     */
    void deleteEntry(SubscriptionEntry entry) throws RollerException;
    
    
    /**
     * Delete all entries for a subscription.
     *
     * @param sub The subscription to delete entries from.
     * @throws RollerException If there is a problem doing the delete.
     */
    void deleteEntries(Subscription sub) throws RollerException;
    
    
    /**
     * Lookup an entry by id.
     */
    SubscriptionEntry getEntryById(String id) throws RollerException;
    
    
    /**
     * Get entries in a single feed as list of SubscriptionEntry objects.
     */
    List<SubscriptionEntry> getEntries(Subscription sub, int offset, int len)
        throws RollerException;
    
    
    /**
     * Get Entries for a Group in reverse chronological order.
     *
     * @param group Restrict to entries from one group.
     * @param offset Offset into results (for paging)
     * @param len Maximum number of results to return (for paging)
     */
    List<SubscriptionEntry> getEntries(PlanetGroup group, int offset, int len)
        throws RollerException;
    
    
    /**
     * Get Entries for a Group in reverse chronological order, optionally
     * constrained to a certain timeframe.
     *
     * @param group Restrict to entries from one group.
     * @param startDate The oldest date for entries to include.
     * @param endDate The newest date for entries to include.
     * @param offset Offset into results (for paging)
     * @param len Maximum number of results to return (for paging)
     */
    List<SubscriptionEntry> getEntries(PlanetGroup group,
                           Date startDate, 
                           Date endDate,
                           int offset, 
                           int len) throws RollerException;

    /**
     * Add new PlanetGroup and add it to an existing planet.
     */
    void saveNewPlanetGroup(Planet planet, PlanetGroup planetGroup) throws WebloggerException;
}
