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
package org.apache.roller.tools;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.fetcher.impl.SyndFeedInfo;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
/*
 * Copyright 2005 Roller project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.pojos.PlanetConfigData;
import org.apache.roller.pojos.PlanetEntryData;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.PlanetSubscriptionData;
import org.apache.roller.util.Utilities;
import org.apache.roller.util.rome.DiskFeedInfoCache;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.texen.Generator;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;



/**
 * Utility that aggregates multiple newsfeeds using Rome Fetcher and calls
 * Velocity Texen control template generate files (HTML, RSS, OPML, etc.).
 * Does everything in memory; no database storage is used.
 *
 * @author David M Johnson
 */
public class PlanetTool {
    private static Log logger =
            LogFactory.getFactory().getInstance(PlanetTool.class);
    
    protected PlanetConfigData config = null;
    protected Map subsByURL = new HashMap(); // keys are URL strings
    protected Map groupsByHandle = new HashMap(); // keys are handle strings
    protected Map aggregationsByGroup = new HashMap(); // keys are GroupData objects
    
    /**
     * Construct by reading confuration JDOM Document.
     */
    public PlanetTool(Document doc) throws Exception {
        try {
            initFromXML(doc);
        } catch (JDOMException e) {
            throw new Exception("Extracting config from parsed XML", e);
        }
    }
    
    /**
     * Call Texen control template specified by configuration to generate files.
     */
    public void generatePlanet() throws Exception {
        try {
            VelocityEngine engine = new VelocityEngine();
            engine.setProperty("resource.loader","file");
            engine.setProperty("file.resource.loader.class",
                    "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            engine.setProperty("file.resource.loader.path",
                    getConfiguration().getTemplateDir());
            engine.init();
            
            VelocityContext context = new VelocityContext();
            context.put("date", new Date());
            context.put("utilities", new Utilities());
            context.put("planet", this);
            
            File outputDir = new File(getConfiguration().getOutputDir());
            if (!outputDir.exists()) outputDir.mkdirs();
            
            Generator generator = Generator.getInstance();
            generator.setVelocityEngine(engine);
            generator.setOutputEncoding("utf-8");
            generator.setInputEncoding("utf-8");
            generator.setOutputPath(getConfiguration().getOutputDir());
            generator.setTemplatePath(getConfiguration().getTemplateDir());
            generator.parse(config.getMainPage(), context);
            generator.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Writing planet files",e);
        }
    }
    
    public void saveConfiguration(PlanetConfigData config) throws Exception {
        this.config = config;
    }
    
    public void saveGroup(PlanetGroupData sub) throws Exception {
        groupsByHandle.put(sub.getHandle(), sub);
    }
    
    public void saveSubscription(PlanetSubscriptionData sub) throws Exception {
        subsByURL.put(sub.getFeedURL(), sub);
    }
    
    public void saveEntry(PlanetEntryData entry) throws Exception {
        // no-op
    }
    
    public PlanetSubscriptionData getSubscription(String feedUrl)
    throws Exception {
        return (PlanetSubscriptionData)subsByURL.get(feedUrl);
    }
    
    public PlanetConfigData getConfiguration() throws Exception {
        return config;
    }
    
    public List getGroupHandles() throws Exception {
        return new ArrayList(groupsByHandle.keySet());
    }
    
    public List getGroups() throws Exception {
        return new ArrayList(groupsByHandle.values());
    }
    
    public PlanetGroupData getGroup(String handle) throws Exception {
        return (PlanetGroupData)groupsByHandle.get(handle);
    }
    
    public List getAggregation(
            PlanetGroupData group, int maxEntries) throws Exception {
        long startTime = System.currentTimeMillis();
        List aggregation = null;
        try {
            // Get aggregation from cache
            aggregation = (List)aggregationsByGroup.get(group);
            if (aggregation == null) {
                // No aggregation found in cache, let's create a new one
                aggregation = new ArrayList();
                
                // Comparator to help us create reverse chrono sorted list of entries
                Comparator entryDateComparator = new EntryDateComparator();
                
                // Add all of group's subscription's entries to ordered collection
                Set sortedEntries = new TreeSet(entryDateComparator);
                Iterator subs = group.getSubscriptions().iterator();
                while (subs.hasNext()) {
                    PlanetSubscriptionData sub = (PlanetSubscriptionData)subs.next();
                    Iterator candidates = sub.getEntries().iterator();
                    while (candidates.hasNext()) {
                        PlanetEntryData candidate = (PlanetEntryData) candidates.next();
                        if (group.qualified(candidate)) {
                            sortedEntries.add(candidate);
                        }
                    }
                }
                
                // Throw away all but first maxEntris of our new entry list
                int count = 0;
                Iterator entries = sortedEntries.iterator();
                while (entries.hasNext() && count++ < maxEntries) {
                    aggregation.add(entries.next());
                }
                aggregationsByGroup.put(group, aggregation);
            }
        } catch (Exception e) {
            logger.error("ERROR: building aggregation for: "+group.getHandle(), e);
            throw new Exception(e);
        }
        long endTime = System.currentTimeMillis();
        logger.info("Generated aggregation in "
                +((endTime-startTime)/1000.0)+" seconds");
        return aggregation;
    }
    
    public void deleteEntry(PlanetEntryData entry) throws Exception {
        // no-op
    }
    
    public void deleteGroup(PlanetGroupData group) throws Exception {
        // no-op
    }
    
    public void deleteSubscription(PlanetSubscriptionData group) throws Exception {
        // no-op
    }
    
    public void clearCachedAggregations() {
        // no-op
    }
    
    public List getTopSubscriptions(int max) throws Exception {
        throw new RuntimeException("NOT SUPPORTED");
    }
    
    public List getTopSubscriptions(PlanetGroupData group, int max) throws Exception {
        throw new RuntimeException("NOT SUPPORTED");
    }
    
    public PlanetSubscriptionData getSubscriptionById(String id) throws Exception {
        throw new RuntimeException("NOT SUPPORTED");
    }
    
    public PlanetGroupData getGroupById(String id)
    throws Exception {
        throw new RuntimeException("NOT SUPPORTED");
    }
    
    public List getAggregation(int maxEntries) throws Exception {
        throw new RuntimeException("NOT SUPPORTED");
    }
    
    public Date getLastUpdated() {
        throw new RuntimeException("NOT SUPPORTED");
    }
    
    public Date getLastUpdated(PlanetGroupData group) {
        throw new RuntimeException("NOT SUPPORTED");
    }
    //--------------------------------------------------------------------- console
    
    public static void main(String[] args) {
        String success = "Planet complete!";
        String error = null;
        Exception traceWorthy = null;
        String fileName = "planet-config.xml";
        if (args.length == 1) {
            fileName = args[0];
        }
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new FileInputStream(fileName));
            PlanetTool planet = new PlanetTool(doc);
            planet.refreshEntries();
            planet.generatePlanet();
            System.out.println(success);
            System.exit(0);
        } catch (FileNotFoundException fnfe) {
            error = "Configuration file ["+fileName+"] not found";
        } catch (JDOMException jde) {
            error = "Error parsing configuration file ["+fileName+"]";
            traceWorthy = jde;
        } catch (IOException ioe) {
            error = "IO error. Using configuration file ["+fileName+"]";
            traceWorthy = ioe;
        } catch (Exception re) {
            error = re.getMessage();
        }
        if (error != null) {
            System.out.println(error);
            if (traceWorthy != null) traceWorthy.printStackTrace();
            System.exit(-1);
        }
    }
    
    public Iterator getAllSubscriptions() {
        return subsByURL.values().iterator();
    }
    
    //-------------------------------------------------------------------- privates
    
    /**
     * Load config data from XML using XPath.
     */
    protected void initFromXML(Document doc) throws Exception, JDOMException {
        Map subsByID = new HashMap();
        Element elem = doc.getRootElement();
        
        config = new PlanetConfigData();
        config.setCacheDir(   getString(elem,"/planet-config/cache-dir"));
        config.setMainPage(   getString(elem,"/planet-config/main-page"));
        config.setGroupPage(  getString(elem,"/planet-config/group-page"));
        config.setAdminName(  getString(elem,"/planet-config/admin-name"));
        config.setAdminEmail( getString(elem,"/planet-config/admin-email"));
        config.setSiteURL(    getString(elem,"/planet-config/site-url"));
        config.setOutputDir(  getString(elem,"/planet-config/output-dir"));
        config.setTemplateDir(getString(elem,"/planet-config/template-dir"));
        config.setTitle(      getString(elem,"/planet-config/title"));
        config.setDescription(getString(elem,"/planet-config/description"));
        
        XPath subsPath = XPath.newInstance("/planet-config/subscription");
        Iterator subs = subsPath.selectNodes(doc).iterator();
        while (subs.hasNext()) {
            Element subElem = (Element)subs.next();
            PlanetSubscriptionData sub = new PlanetSubscriptionData();
            String id = subElem.getAttributeValue("id");
            sub.setTitle(   getString(subElem, "title"));
            sub.setAuthor(  getString(subElem, "author"));
            sub.setFeedURL( getString(subElem, "feed-url"));
            sub.setSiteURL( getString(subElem, "site-url"));
            subsByURL.put(sub.getFeedURL(), sub);
            subsByID.put(id, sub);
        }
        logger.info("Found "+subsByID.size()+" subscriptions");
        
        XPath groupsPath = XPath.newInstance("/planet-config/group");
        Iterator groups = groupsPath.selectNodes(doc).iterator();
        while (groups.hasNext()) {
            Element groupElem = (Element)groups.next();
            PlanetGroupData group = new PlanetGroupData();
            group.setHandle(groupElem.getAttributeValue("handle"));
            group.setTitle(          getString(groupElem, "title"));
            group.setDescription(    getString(groupElem, "description"));
            group.setMaxFeedEntries( getInt(   groupElem, "max-feed-entries"));
            group.setMaxPageEntries( getInt(   groupElem, "max-page-entries"));
            group.setCategoryRestriction(
                    getString(groupElem, "category-restriction"));
            
            XPath refsPath = XPath.newInstance("subscription-ref");
            Iterator refs = refsPath.selectNodes(groupElem).iterator();
            while (refs.hasNext()) {
                Element refElem = (Element)refs.next();
                String includeAll = refElem.getAttributeValue("include-all");
                if (includeAll != null && includeAll.equals("true")) {
                    //group.getSubscriptions().addAll(subsByID.values());
                    group.addSubscriptions(subsByID.values());
                } else {
                    String refid = refElem.getAttributeValue("refid");
                    PlanetSubscriptionData sub = (PlanetSubscriptionData)subsByID.get(refid);
                    if (sub == null) {
                        throw new Exception("No such subscription ["+refid+"]");
                    }
                    //group.getSubscriptions().add(sub);
                    group.addSubscription(sub);
                }
            }
            groupsByHandle.put(group.getHandle(), group);
        }
        logger.info("Found "+groupsByHandle.size()+" groups");
    }
    
    //--------------------------------------------------------------- utilities
    
    protected String getString(Element elem, String path) throws JDOMException {
        XPath xpath = XPath.newInstance(path);
        Element e = (Element)xpath.selectSingleNode(elem);
        return e!=null ? e.getText() : null;
    }
    
    protected int getInt(Element elem, String path) throws JDOMException {
        XPath xpath = XPath.newInstance(path);
        Element e = (Element)xpath.selectSingleNode(elem);
        return e!=null ? Integer.parseInt(e.getText()) : 0;
    }
    
    public class EntryDateComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            PlanetEntryData e1 = (PlanetEntryData)o1;
            PlanetEntryData e2 = (PlanetEntryData)o2;
            if (e1.getPubTime() != null && e2.getPubTime() != null) {
                return e2.getPubTime().compareTo(e1.getPubTime());
            }
            if (e1.getPubTime() == null) {
                logger.warn("Entry missing pubDate in sub: "
                        + e1.getSubscription().getFeedURL());
            }
            if (e2.getPubTime() == null) {
                logger.warn("Entry missing pubDate in sub: "
                        + e2.getSubscription().getFeedURL());
            }
            return 0;
        }
    }
    
    /**
     * Total number of subscriptions.
     */
    public int getSubscriptionCount() throws Exception {
        return this.subsByURL.size();
    }
        
    public void refreshEntries() throws Exception {
        Date now = new Date();
        long startTime = System.currentTimeMillis();
        PlanetConfigData config = getConfiguration();
        if (config == null || config.getCacheDir() == null) {
            logger.warn("Planet cache directory not set, aborting refresh");
            return;
        }
        FeedFetcherCache feedInfoCache =
                new DiskFeedInfoCache(config.getCacheDir());
        
        if (config.getProxyHost()!=null && config.getProxyPort() > 0) {
            System.setProperty("proxySet", "true");
            System.setProperty("http.proxyHost", config.getProxyHost());
            System.setProperty("http.proxyPort",
                    Integer.toString(config.getProxyPort()));
        }
        /** a hack to set 15 sec timeouts for java.net.HttpURLConnection */
        System.setProperty("sun.net.client.defaultConnectTimeout", "15000");
        System.setProperty("sun.net.client.defaultReadTimeout", "15000");
        
        FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
        //FeedFetcher feedFetcher = new HttpClientFeedFetcher(feedInfoCache);
        feedFetcher.setUsingDeltaEncoding(false);
        feedFetcher.setUserAgent("RollerPlanetAggregator");
                
        // Loop through all subscriptions in the system
        Iterator subs = getAllSubscriptions();
        while (subs.hasNext()) {
            long subStartTime = System.currentTimeMillis();
            
            // Fetch latest entries for each subscription
            Set newEntries = null;
            int count = 0;
            PlanetSubscriptionData sub = (PlanetSubscriptionData)subs.next();
            newEntries = getNewEntriesRemote(sub, feedFetcher, feedInfoCache);
            count = newEntries.size();
            
            logger.debug("   Entry count: " + count);
            if (count > 0) {
                Iterator entryIter = sub.getEntries().iterator();
                while (entryIter.hasNext()) {
                    deleteEntry((PlanetEntryData)entryIter.next());
                }
                sub.purgeEntries();
                sub.addEntries(newEntries);
            }
            long subEndTime = System.currentTimeMillis();
            logger.info("   " + count + " - "
                    + ((subEndTime-subStartTime)/1000.0)
                    + " seconds to process (" + count + ") entries of "
                    + sub.getFeedURL());
        }
        // Clear the aggregation cache
        clearCachedAggregations();
        
        long endTime = System.currentTimeMillis();
        logger.info("--- DONE --- Refreshed entries in "
                + ((endTime-startTime)/1000.0) + " seconds");
    }
    
    /**
     * Override this if you have local feeds (i.e. feeds that you don't
     * have to fetch via HTTP and parse with ROME).
     */
    protected Set getNewEntriesLocal(PlanetSubscriptionData sub,
            FeedFetcher feedFetcher, FeedFetcherCache feedInfoCache)
            throws Exception {
        
        // If you don't override, local feeds will be treated as remote feeds
        return getNewEntriesRemote(sub, feedFetcher, feedInfoCache);
    }
    
    protected Set getNewEntriesRemote(PlanetSubscriptionData sub,
            FeedFetcher feedFetcher, FeedFetcherCache feedInfoCache)
            throws Exception {
        
        Set newEntries = new TreeSet();
        SyndFeed feed = null;
        URL feedUrl = null;
        Date lastUpdated = new Date();
        try {
            feedUrl = new URL(sub.getFeedURL());
            logger.debug("Get feed from cache "+sub.getFeedURL());
            feed = feedFetcher.retrieveFeed(feedUrl);
            SyndFeedInfo feedInfo = feedInfoCache.getFeedInfo(feedUrl);
            if (feedInfo.getLastModified() != null) {
                long lastUpdatedLong =
                        ((Long)feedInfo.getLastModified()).longValue();
                if (lastUpdatedLong != 0) {
                    lastUpdated = new Date(lastUpdatedLong);
                }
            }
            Thread.sleep(100); // be nice
        } catch (Exception e) {
            logger.warn("ERROR parsing " + sub.getFeedURL()
            + " : " + e.getClass().getName() + " : " + e.getMessage());
            logger.debug(e);
            return newEntries; // bail out
        }
        if (lastUpdated!=null && sub.getLastUpdated()!=null) {
            Calendar feedCal = Calendar.getInstance();
            feedCal.setTime(lastUpdated);
            
            Calendar subCal = Calendar.getInstance();
            subCal.setTime(sub.getLastUpdated());
            
            if (!feedCal.after(subCal)) {
                if (logger.isDebugEnabled()) {
                    String msg = MessageFormat.format(
                            "   Skipping ({0} / {1})",
                            new Object[] {
                        lastUpdated, sub.getLastUpdated()});
                        logger.debug(msg);
                }
                return newEntries; // bail out
            }
        }
        if (feed.getPublishedDate() != null) {
            sub.setLastUpdated(feed.getPublishedDate());
            saveSubscription(sub);
        }
        
        // Kludge for Feeds without entry dates: most recent entry is given
        // feed's last publish date (or yesterday if none exists) and earler
        // entries are placed at once day intervals before that.
        Calendar cal = Calendar.getInstance();
        if (sub.getLastUpdated() != null) {
            cal.setTime(sub.getLastUpdated());
        } else {
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1);
        }
        
        // Populate subscription object with new entries
        Iterator entries = feed.getEntries().iterator();
        while (entries.hasNext()) {
            try {
                SyndEntry romeEntry = (SyndEntry) entries.next();
                PlanetEntryData entry =
                        new PlanetEntryData(feed, romeEntry, sub);
                if (entry.getPubTime() == null) {
                    logger.debug(
                            "No published date, assigning fake date for "+feedUrl);
                    entry.setPubTime(new Timestamp(cal.getTime().getTime()));
                }
                if (entry.getPermalink() == null) {
                    logger.warn("No permalink, rejecting entry from "+feedUrl);
                } else {
                    saveEntry(entry);
                    newEntries.add(entry);
                }
                cal.add(Calendar.DATE, -1);
            } catch (Exception e) {
                logger.error("ERROR processing subscription entry", e);
            }
        }
        return newEntries;
    }
}

