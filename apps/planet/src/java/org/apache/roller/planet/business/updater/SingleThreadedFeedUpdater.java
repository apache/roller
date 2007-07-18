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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.planet.business.fetcher.FetcherException;
import org.apache.roller.planet.config.PlanetRuntimeConfig;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.planet.pojos.SubscriptionEntry;


/**
 * A single threaded implementation of a FeedUpdater.
 */
public class SingleThreadedFeedUpdater implements FeedUpdater {
    
    private static Log log = LogFactory.getLog(SingleThreadedFeedUpdater.class);
    
    
    public SingleThreadedFeedUpdater() {
        // no-op
    }
    
    
    /**
     * @inheritDoc
     */
    public void updateSubscription(Subscription sub) throws UpdaterException {
        
        if(sub == null) {
            throw new IllegalArgumentException("cannot update null subscription");
        }
        
        updateProxySettings();
        
        log.debug("updating feed: "+sub.getFeedURL());
        
        long subStartTime = System.currentTimeMillis();
        
        Subscription updatedSub;
        try {
            // fetch the latest version of the subscription
            FeedFetcher fetcher = PlanetFactory.getPlanet().getFeedFetcher();
            updatedSub = fetcher.fetchSubscription(sub.getFeedURL(), sub.getLastUpdated());
        } catch (FetcherException ex) {
            throw new UpdaterException("Error fetching updated subscription", ex);
        }
        
        // if sub was unchanged then we are done
        if(updatedSub == null) {
            return;
        }
        
        // if this subscription hasn't changed since last update then we're done
        if(sub.getLastUpdated() != null &&
                !updatedSub.getLastUpdated().after(sub.getLastUpdated())) {
            log.debug("Skipping update, feed hasn't changed - "+sub.getFeedURL());
        }
        
        // update subscription attributes
        sub.setSiteURL(updatedSub.getSiteURL());
        sub.setTitle(updatedSub.getTitle());
        sub.setAuthor(updatedSub.getAuthor());
        sub.setLastUpdated(updatedSub.getLastUpdated());
        
        // update subscription entries
        int entries = 0;
        Set<SubscriptionEntry> newEntries = updatedSub.getEntries();
        if(newEntries.size() > 0) try {
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            
            // clear out old entries
            pmgr.deleteEntries(sub);
            
            // add fresh entries
            sub.getEntries().clear();
            sub.addEntries(newEntries);
            
            // save and flush
            pmgr.saveSubscription(sub);
            PlanetFactory.getPlanet().flush();
            
        } catch(PlanetException ex) {
            throw new UpdaterException("Error persisting updated subscription", ex);
        }
        
        long subEndTime = System.currentTimeMillis();
        log.debug("updated feed -- "+sub.getFeedURL()+" -- in "+
                ((subEndTime-subStartTime)/1000.0)+" seconds.  "+entries+
                " entries updated.");
    }
    
    
    /**
     * @inheritDoc
     */
    public void updateSubscriptions() throws UpdaterException {
        
        updateProxySettings();
        
        log.debug("--- BEGIN --- Updating all subscriptions");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // update all subscriptions in the system
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            updateSubscriptions(pmgr.getSubscriptions());
        } catch (PlanetException ex) {
            throw new UpdaterException("Error getting subscriptions list", ex);
        }
        
        long endTime = System.currentTimeMillis();
        log.info("--- DONE --- Updated subscriptions in "
                + ((endTime-startTime)/1000.0) + " seconds");
    }
    
    
    /**
     * @inheritDoc
     */
    public void updateSubscriptions(PlanetGroup group) throws UpdaterException {
        
        if(group == null) {
            throw new IllegalArgumentException("cannot update null group");
        }
        
        updateProxySettings();
        
        log.debug("--- BEGIN --- Updating subscriptions in group = "+group.getHandle());
        
        long startTime = System.currentTimeMillis();
        
        updateSubscriptions(group.getSubscriptions());
        
        long endTime = System.currentTimeMillis();
        log.info("--- DONE --- Updated subscriptions in "
                + ((endTime-startTime)/1000.0) + " seconds");
    }
    
    
    // convenience method which handles updating any arbitrary collection of subs
    private void updateSubscriptions(Collection<Subscription> subscriptions) {
        
        PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
        
        Iterator subs = subscriptions.iterator();
        while (subs.hasNext()) {
            Subscription sub = (Subscription)subs.next();
            
            try {
                // reattach sub.  sub gets detached as we iterate
                sub = pmgr.getSubscriptionById(sub.getId());
            } catch (PlanetException ex) {
                log.warn("Subscription went missing while doing update: "+ex.getMessage());
            }
            
            // this updates and saves
            try {
                updateSubscription(sub);
            } catch(Exception ex) {
                log.warn("Error updating subscription - "+sub.getFeedURL(), ex);
            }
        }
    }
    
    
    // upate proxy settings for jvm based on planet configuration
    private void updateProxySettings() {
        String proxyHost = PlanetRuntimeConfig.getProperty("site.proxyhost");
        int proxyPort = PlanetRuntimeConfig.getIntProperty("site.proxyport");
        if (proxyHost != null && proxyPort > 0) {
            System.setProperty("proxySet", "true");
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", Integer.toString(proxyPort));
        }
        /** a hack to set 15 sec timeouts for java.net.HttpURLConnection */
        System.setProperty("sun.net.client.defaultConnectTimeout", "15000");
        System.setProperty("sun.net.client.defaultReadTimeout", "15000");
    }
    
}
