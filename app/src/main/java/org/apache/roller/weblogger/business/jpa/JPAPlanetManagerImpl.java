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
package org.apache.roller.weblogger.business.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.time.DateUtils;

import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.FeedManager;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.pojos.Weblog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages Planet Roller objects and entry aggregations in a database.
 */
public class JPAPlanetManagerImpl implements PlanetManager {

    private static Logger log = LoggerFactory.getLogger(JPAPlanetManagerImpl.class);

    private WeblogManager weblogManager;
    private URLStrategy urlStrategy;
    private FeedManager feedManager;
    private JPAPersistenceStrategy strategy;

    protected JPAPlanetManagerImpl() {}

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    public void setFeedManager(FeedManager feedManager) {
        this.feedManager = feedManager;
    }

    public void setStrategy(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void savePlanet(Planet group) {
        strategy.store(group);
    }

    @Override
    public void saveEntry(SubscriptionEntry entry) {
        strategy.store(entry);
    }

    @Override
    public void saveSubscription(Subscription sub) {
        Subscription existing = getSubscription(sub.getPlanet(), sub.getFeedURL());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            strategy.store(sub);
        } else {
            throw new IllegalStateException("ERROR: duplicate feed URLs not allowed");
        }
    }

    @Override
    public void deleteEntry(SubscriptionEntry entry) {
        strategy.remove(entry);
    }

    @Override
    public void deletePlanet(Planet group) {
        strategy.remove(group);
    }

    @Override
    public void deleteSubscription(Subscription sub) {
        strategy.remove(sub);
    }

    @Override
    public Subscription getSubscription(Planet planet, String feedUrl) {
        TypedQuery<Subscription> q = strategy.getNamedQuery("Subscription.getByPlanetAndFeedURL", Subscription.class);
        q.setParameter(1, planet);
        q.setParameter(2, feedUrl);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Subscription getSubscriptionById(String id) {
        return strategy.load(Subscription.class, id);
    }
    
    public Iterator getAllSubscriptions() {
        try {
            return (strategy.getNamedQuery(
                    "Subscription.getAll", Subscription.class).getResultList()).iterator();
        } catch (Exception e) {
            throw new RuntimeException(
                    "ERROR fetching subscription collection", e);
        }
    }

    @Override
    public int getSubscriptionCount() {
        Query q = strategy.getNamedQuery("Subscription.getAll", Subscription.class);
        return q.getResultList().size();
    }

    @Override
    public List<Planet> getPlanets() {
        TypedQuery<Planet> q = strategy.getNamedQuery("Planet.getAll", Planet.class);
        return q.getResultList();
    }

    @Override
    public Planet getPlanet(String handle) {
        TypedQuery<Planet> q = strategy.getNamedQuery("Planet.getByHandle", Planet.class);
        q.setParameter(1, handle);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Planet getPlanetById(String id) {
        return strategy.load(Planet.class, id);
    }

    @Override
    public void deleteEntries(Subscription sub) {
        for (Object entry : sub.getEntries()) {
            strategy.remove(entry);
        }
        // make sure and clear the other side of the association
        sub.getEntries().clear();
    }

    @Override
    public List<Subscription> getSubscriptions() {
        TypedQuery<Subscription> q = strategy.getNamedQuery("Subscription.getAllOrderByFeedURL", Subscription.class);
        return q.getResultList();
    }

    @Override
    public SubscriptionEntry getEntryById(String id) {
        return strategy.load(SubscriptionEntry.class, id);
    }

    @Override
    public List<SubscriptionEntry> getEntries(Subscription sub, int offset, int len) {
        if (sub == null) {
            throw new IllegalArgumentException("subscription cannot be null");
        }
        TypedQuery<SubscriptionEntry> q = strategy.getNamedQuery("SubscriptionEntry.getBySubscription", SubscriptionEntry.class);
        q.setParameter(1, sub);
        if (offset != 0) {
            q.setFirstResult(offset);
        }
        if (len != -1) {
            q.setMaxResults(len);
        }
        return q.getResultList();
    }

    @Override
    public List<SubscriptionEntry> getEntries(Planet group, int offset, int len) {
        return getEntries(group, null, null, offset, len);
    }

    @Override
    public List<SubscriptionEntry> getEntries(Planet group, Date startDate, Date endDate, int offset, int len) {

        if (group == null) {
            throw new IllegalArgumentException("group cannot be null or empty");
        }

        List<SubscriptionEntry> ret;

        long startTime = System.currentTimeMillis();

        StringBuilder sb = new StringBuilder();
        List<Object> params = new ArrayList<>();
        int size = 0;
        sb.append("SELECT e FROM SubscriptionEntry e ");

        params.add(size++, group.getHandle());
        sb.append("WHERE e.subscription.planet.handle = ?").append(size);

        if (startDate != null) {
            params.add(size++, new Timestamp(startDate.getTime()));
            sb.append(" AND e.pubTime > ?").append(size);
        }
        if (endDate != null) {
            params.add(size++, new Timestamp(endDate.getTime()));
            sb.append(" AND e.pubTime < :?").append(size);
        }
        sb.append(" ORDER BY e.pubTime DESC");

        TypedQuery<SubscriptionEntry> query = strategy.getDynamicQuery(sb.toString(), SubscriptionEntry.class);
        for (int i=0; i<params.size(); i++) {
            query.setParameter(i+1, params.get(i));
        }
        if (offset - 1 > 0) {
            query.setFirstResult((offset - 1) * len);
        }
        if (len != -1) {
            query.setMaxResults(len);
        }

        ret = query.getResultList();

        long endTime = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("Generated aggregation of {} in {} seconds", ret.size(),
                    (endTime - startTime) / DateUtils.MILLIS_PER_SECOND);
        }

        return ret;
    }

    @Override
    public void updateSubscriptions() {
        log.debug("--- BEGIN --- Updating all subscriptions");
        long startTime = System.currentTimeMillis();

        feedManager.updateSubscriptions(getSubscriptions());
        long endTime = System.currentTimeMillis();
        log.info("--- DONE --- Updated subscriptions in {} seconds",
                (endTime-startTime) / DateUtils.MILLIS_PER_SECOND);
    }

    @Override
    public void syncAllBlogsPlanet() {
        log.info("Syncing local weblogs with planet subscriptions list");

        // first, make sure there is an "all" pmgr group
        Planet planet = getPlanet("all");
        if (planet == null) {
            planet = new Planet();
            planet.setId(WebloggerCommon.generateUUID());
            planet.setHandle("all");
            planet.setTitle("All Blogs");
            savePlanet(planet);
            strategy.flush();
        }

        // walk through all enable weblogs and add/update subs as needed
        List<String> liveUserFeeds = new ArrayList<>();
        List<Weblog> weblogs = weblogManager.getWeblogs(Boolean.TRUE, null, null, 0, -1);
        for ( Weblog weblog : weblogs ) {

            log.debug("processing weblog - {}", weblog.getHandle());
            String feedUrl = "weblogger:" + weblog.getHandle();

            // add feed url to the "live" list
            liveUserFeeds.add(feedUrl);

            // if sub already exists then update it, otherwise add it
            Subscription sub = getSubscription(planet, feedUrl);
            if (sub == null) {
                log.info("ADDING feed: {}", feedUrl);

                sub = new Subscription();
                sub.setTitle(weblog.getName());
                sub.setFeedURL(feedUrl);
                sub.setSiteURL(urlStrategy.getWeblogURL(weblog, true));
                sub.setLastUpdated(new Date(0));
                sub.setPlanet(planet);
                saveSubscription(sub);

                planet.getSubscriptions().add(sub);
                savePlanet(planet);
            } else {
                log.debug("UPDATING feed: {}", feedUrl);
                sub.setTitle(weblog.getName());
                saveSubscription(sub);
            }

            // save as we go
            strategy.flush();
        }

        // new subs added, existing subs updated, now delete old subs
        Set<Subscription> deleteSubs = new HashSet<>();
        Set<Subscription> subs = planet.getSubscriptions();
        for (Subscription sub : subs) {
            // only delete subs from the group if ...
            // 1. they are local
            // 2. they are no longer listed as a weblog
            if (sub.getFeedURL().startsWith("weblogger:") &&
                    !liveUserFeeds.contains(sub.getFeedURL())) {
                deleteSubs.add(sub);
            }
        }

        // now go back through deleteSubs and do actual delete
        // this is required because deleting a sub in the loop above
        // causes a ConcurrentModificationException because we can't
        // modify a collection while we iterate over it
        for (Subscription deleteSub : deleteSubs) {
            log.info("DELETING feed: {}", deleteSub.getFeedURL());
            deleteSubscription(deleteSub);
            planet.getSubscriptions().remove(deleteSub);
        }

        // all done, lets save
        savePlanet(planet);
        strategy.flush();

    }
}
