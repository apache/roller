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
package org.apache.roller.business.datamapper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.PagePluginManager;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.PlanetConfigData;
import org.apache.roller.pojos.PlanetEntryData;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.PlanetGroupSubscriptionAssoc;
import org.apache.roller.pojos.PlanetSubscriptionData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;

import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;

/**
 * Manages Planet Roller objects and entry aggregations in a database.
 * 
 * @author Dave Johnson
 */
public class DatamapperPlanetManagerImpl implements PlanetManager {

    /** The strategy for this manager. */
    private DatamapperPersistenceStrategy strategy;

    protected Map lastUpdatedByGroup = new HashMap();
    protected static final String NO_GROUP = "zzz_nogroup_zzz";

    private static Log logger = LogFactory.getFactory().getInstance(
                DatamapperPlanetManagerImpl.class);

    private DatamapperWeblogManagerImpl weblogManager;

    public DatamapperPlanetManagerImpl 
            (DatamapperPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public void saveConfiguration(PlanetConfigData config)
            throws RollerException {
        strategy.store(config);
    }

    public void saveGroup(PlanetGroupData group) throws RollerException {
        Iterator assocs = group.getGroupSubscriptionAssocs().iterator();
        while (assocs.hasNext()) {
            PlanetGroupSubscriptionAssoc assoc = (PlanetGroupSubscriptionAssoc) assocs
                    .next();
            strategy.store(assoc);
        }
        strategy.store(group);
    }

    public void saveEntry(PlanetEntryData entry) throws RollerException {
        strategy.store(entry);
    }

    public void saveSubscription(PlanetSubscriptionData sub)
            throws RollerException {
        PlanetSubscriptionData existing = getSubscription(sub.getFeedUrl());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            strategy.store(sub);
        }
        else {
            throw new RollerException("ERROR: duplicate feed URLs not allowed");
        }
    }

    public PlanetConfigData getConfiguration() throws RollerException {
        return null;
    }

    public List getGroups() throws RollerException {
        return null;
    }

    public List getGroupHandles() throws RollerException {
        List handles = new ArrayList();
        Iterator list = getGroups().iterator();
        while (list.hasNext()) {
            PlanetGroupData group = (PlanetGroupData) list.next();
            handles.add(group.getHandle());
        }
        return handles;
    }

    public PlanetSubscriptionData getSubscription(String feedUrl)
            throws RollerException {
        return null;
    }

    public PlanetSubscriptionData getSubscriptionById(String id)
            throws RollerException {
        return (PlanetSubscriptionData) strategy.load(
                PlanetSubscriptionData.class, id);
    }

    public PlanetGroupData getGroup(String handle) throws RollerException {
        return null;
    }

    public PlanetGroupData getGroupById(String id) throws RollerException {
        return (PlanetGroupData) strategy.load(PlanetGroupData.class, id);
    }

    public synchronized List getAggregation(int maxEntries)
            throws RollerException {
        return getAggregation(null, maxEntries);
    }

    public synchronized List getAggregation(PlanetGroupData group,
            int maxEntries) throws RollerException {
        return null;
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

    public Iterator getAllSubscriptions() {
        return null;
    }

    public int getSubscriptionCount() throws RollerException {
        return -1;
    }

    public synchronized List getTopSubscriptions(int max)
            throws RollerException {
        return null;
    }

    public synchronized List getTopSubscriptions(PlanetGroupData group, int max)
            throws RollerException {
        return null;
    }

    public synchronized void clearCachedAggregations() {
        lastUpdatedByGroup.clear();
    }

    public Date getLastUpdated() {
        return (Date) lastUpdatedByGroup.get(NO_GROUP);
    }

    public Date getLastUpdated(PlanetGroupData group) {
        return (Date) lastUpdatedByGroup.get(group);
    }

    protected Set getNewEntriesLocal(PlanetSubscriptionData sub,
            FeedFetcher feedFetcher, FeedFetcherCache feedInfoCache)
            throws RollerException {

        Set newEntries = new TreeSet();
        try {
            // for local feeds, sub.author = website.handle
            if (sub.getAuthor() != null
                    && sub.getFeedUrl().endsWith(sub.getAuthor())) {

                logger.debug("Getting LOCAL feed " + sub.getFeedUrl());

                // get corresponding website object
                WebsiteData website = (WebsiteData)strategy.newQuery(
                        WebsiteData.class, "getByHandle&&Enabled")
                    .execute(sub.getAuthor());
                if (website == null)
                    return newEntries;

                // figure website last update time
                WeblogManager blogmgr = roller.getWeblogManager();

                Date siteUpdated = blogmgr.getWeblogLastPublishTime(website);
                if (siteUpdated == null) { // Site never updated, skip it
                    logger.warn("Last-publish time null, skipping local feed ["
                            + website.getHandle() + "]");
                    return newEntries;
                }

                // if website last update time > subsciption last update time
                List entries = new ArrayList();
                if (sub.getLastUpdated() == null
                        || siteUpdated.after(sub.getLastUpdated())) {
                    int entryCount = RollerRuntimeConfig
                            .getIntProperty("site.newsfeeds.defaultEntries");
                    entries = blogmgr.getWeblogEntries(website, null, // startDate
                            new Date(), // endDate
                            null, // catName
                            WeblogEntryData.PUBLISHED, // status
                            new Integer(entryCount)); // maxEntries

                    sub.setLastUpdated(siteUpdated);
                    saveSubscription(sub);

                }
                else {
                    if (logger.isDebugEnabled()) {
                        String msg = MessageFormat.format(
                                "   Skipping ({0} / {1})", new Object[] {
                                        siteUpdated, sub.getLastUpdated() });
                        logger.debug(msg);
                    }
                }
                // Populate subscription object with new entries
                PagePluginManager ppmgr = roller.getPagePluginManager();
                Map pagePlugins = ppmgr.createAndInitPagePlugins(website, null,
                        RollerRuntimeConfig.getProperty("site.absoluteurl"),
                        new VelocityContext());
                Iterator entryIter = entries.iterator();
                while (entryIter.hasNext()) {
                    try {
                        WeblogEntryData rollerEntry = (WeblogEntryData) entryIter
                                .next();
                        PlanetEntryData entry = new PlanetEntryData(
                                rollerEntry, sub, pagePlugins);
                        saveEntry(entry);
                        newEntries.add(entry);
                    }
                    catch (Exception e) {
                        logger.error("ERROR processing subscription entry", e);
                    }
                }
                return newEntries;
            }
        }
        catch (Exception e) {
            logger.warn("Problem reading local feed", e);
        }
        return getNewEntriesRemote(sub, feedFetcher, feedInfoCache);
    }

    public void refreshEntries() throws RollerException {
    }
}

