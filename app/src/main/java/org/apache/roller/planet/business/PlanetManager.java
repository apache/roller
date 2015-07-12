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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.planet.business;

import java.util.Date;
import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.Subscription;

/**
 * Manages Planets, Subscriptions, and Entries.
 */
public interface PlanetManager {

    /**
     * Retrieve all defined planets
     */
    List<Planet> getPlanets() throws RollerException;

    /**
     * Save new or update an existing planet
     */
    void savePlanet(Planet sub) throws RollerException;
    
    
    /** 
     * Delete planet and any subscriptions that are orphaned.
     */
    void deletePlanet(Planet planet) throws RollerException;
    
    
    Planet getPlanet(String handle) throws RollerException;
    
    
    /**
     * Get planet by ID rather than handle.
     */
    Planet getPlanetById(String id) throws RollerException;
    
    
    /**
     * Save or update a subscription
     */
    void saveSubscription(Subscription sub) throws RollerException;
    
    
    /** 
     * Delete subscription, remove it from planets, cache, etc.
     */
    void deleteSubscription(Subscription subscription) throws RollerException;
    
    
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
     * Get top X subscriptions, restricted by planet.
     */
    List<Subscription> getTopSubscriptions(Planet planet, int offset, int len)
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
     * Get Entries for a planet in reverse chronological order.
     *
     * @param planet Restrict to entries from one planet.
     * @param offset Offset into results (for paging)
     * @param len Maximum number of results to return (for paging)
     */
    List<SubscriptionEntry> getEntries(Planet planet, int offset, int len)
        throws RollerException;
    
    
    /**
     * Get Entries for a planet in reverse chronological order, optionally
     * constrained to a certain timeframe.
     *
     * @param planet Restrict to entries from one planet.
     * @param startDate The oldest date for entries to include.
     * @param endDate The newest date for entries to include.
     * @param offset Offset into results (for paging)
     * @param len Maximum number of results to return (for paging)
     */
    List<SubscriptionEntry> getEntries(Planet planet,
                           Date startDate, 
                           Date endDate,
                           int offset, 
                           int len) throws RollerException;
    
}
