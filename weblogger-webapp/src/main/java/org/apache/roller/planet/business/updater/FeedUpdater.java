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

package org.apache.roller.planet.business.updater;

import org.apache.roller.planet.business.fetcher.FetcherException;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;


/**
 * A FeedUpdater is responsible for processing the updates of all Subscriptions
 * and their entries.  It is intended to combine the use of the FeedFetcher for
 * pulling fresh feed data with the PlanetManager for updating and persisting 
 * the updated data.
 *
 * NOTE: it must be explicitly stated that the operations of the FeedUpdater are
 * *not* considered atomic and they are *not* guaranteed to happen synchronously.
 * So callers of these methods should bear that in mind when using this class.
 */
public interface FeedUpdater {
    
    /**
     * Update a single Subscription.
     *
     * This method takes in an existing Subscription and updates it with
     * the data from the subscriptions source after fetching an updated version 
     * of the subscription.
     *
     * @param subscription The PlanetSubscription to be updated.
     * @throws FetcherException If there is an error updating the subscription.
     */
    public void updateSubscription(Subscription sub) throws UpdaterException;
    
    
    /**
     * Update all Subscriptions in the system.
     *
     * @throws UpdaterException If there is an error during the update and the operation cannot continue.
     */
    public void updateSubscriptions() throws UpdaterException;
    
    
    /**
     * Update all Subscriptions that are part of the specified group.
     *
     * @throws UpdaterException If there is an error during the update and the operation cannot continue.
     */
    public void updateSubscriptions(PlanetGroup group) throws UpdaterException;
    
}
