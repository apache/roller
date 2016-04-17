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

package org.apache.roller.weblogger.business;

import java.util.Date;
import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.pojos.Subscription;

/**
 * Manages Planets, Subscriptions, and Entries.
 */
public interface PlanetManager {

    /**
     * Retrieve all defined planets
     */
    List<Planet> getPlanets() throws WebloggerException;

    /**
     * Save new or update an existing planet
     */
    void savePlanet(Planet sub) throws WebloggerException;
    
    
    /** 
     * Delete planet and any subscriptions that are orphaned.
     */
    void deletePlanet(Planet planet) throws WebloggerException;
    
    
    Planet getPlanet(String handle) throws WebloggerException;
    
    
    /**
     * Get planet by ID rather than handle.
     */
    Planet getPlanetById(String id) throws WebloggerException;
    
    
    /**
     * Save or update a subscription
     */
    void saveSubscription(Subscription sub) throws WebloggerException;
    
    
    /** 
     * Delete subscription, remove it from planets, cache, etc.
     */
    void deleteSubscription(Subscription subscription) throws WebloggerException;
    
    
    /**
     * Get subscription by planet and feedUrl.
     */
    Subscription getSubscription(Planet planet, String feedUrl) throws WebloggerException;
    
    
    /**
     * Get subscription by ID rather than feedUrl.
     */
    Subscription getSubscriptionById(String id) throws WebloggerException;
    
    
    /**
     * Get all subscriptions.
     */
    List<Subscription> getSubscriptions() throws WebloggerException;
    
    
    /**
     * Get total number of subscriptions.
     */
    int getSubscriptionCount() throws WebloggerException;
    
    
    /**
     * Save new or update existing entry
     */
    void saveEntry(SubscriptionEntry entry) throws WebloggerException;

    /**
     * Refresh subscription entries
     */
    void updateSubscriptions() throws WebloggerException;

    /**
     * Task that will update the weblogger "all" planet (creating it first if necessary) to
     * consist of all blogs hosted by this weblogger instance, adding new and deleting old as
     * necessary.
     */
    void syncAllBlogsPlanet() throws WebloggerException;

    /**
     * Delete entry. 
     */
    void deleteEntry(SubscriptionEntry entry) throws WebloggerException;
    
    
    /**
     * Delete all entries for a subscription.
     *
     * @param sub The subscription to delete entries from.
     * @throws WebloggerException If there is a problem doing the delete.
     */
    void deleteEntries(Subscription sub) throws WebloggerException;
    
    
    /**
     * Lookup an entry by id.
     */
    SubscriptionEntry getEntryById(String id) throws WebloggerException;
    
    
    /**
     * Get entries in a single feed as list of SubscriptionEntry objects.
     */
    List<SubscriptionEntry> getEntries(Subscription sub, int offset, int len)
        throws WebloggerException;
    
    
    /**
     * Get Entries for a planet in reverse chronological order.
     *
     * @param planet Planet to retrieve entries for.
     * @param offset Offset into results (for paging)
     * @param len Maximum number of results to return (for paging)
     */
    List<SubscriptionEntry> getEntries(Planet planet, int offset, int len)
        throws WebloggerException;
    
    
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
                           int len) throws WebloggerException;
    
}
