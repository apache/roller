/*
 * Copyright 2005 David M Johnson
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
package org.roller.tools.planet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.texen.Generator;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.roller.RollerException;
import org.roller.business.PlanetManagerImpl;
import org.roller.pojos.PlanetConfigData;
import org.roller.pojos.PlanetEntryData;
import org.roller.pojos.PlanetGroupData;
import org.roller.pojos.PlanetSubscriptionData;
import org.roller.util.Utilities;


/**
 * Utility that aggregates multiple newsfeeds using Rome Fetcher and calls 
 * Velocity Texen control template generate files (HTML, RSS, OPML, etc.).
 * Does everything in memory; no database storage is used.
 * <p />
 * Written for Blogs, Wikis, and Feeds in Action and designed for use outside 
 * of Roller.
 *
 * @author David M Johnson
 */
public class PlanetTool extends PlanetManagerImpl
{
    private static Log logger = 
        LogFactory.getFactory().getInstance(PlanetTool.class);

    protected PlanetConfigData config = null;
    protected Map subsByURL = new HashMap(); // keys are URL strings
    protected Map groupsByHandle = new HashMap(); // keys are handle strings
    protected Map aggregationsByGroup = new HashMap(); // keys are GroupData objects
    
    /** 
     * Construct by reading confuration JDOM Document. 
     */
    public PlanetTool(Document doc) throws RollerException
    {
        try
        {
            initFromXML(doc);
        }
        catch (JDOMException e)
        {
            throw new RollerException("Extracting config from parsed XML", e);
        }
    }

    /**
     * Call Texen control template specified by configuration to generate files.
     */
    public void generatePlanet() throws RollerException
    {
        try
        {
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

            Generator generator = Generator.getInstance();
            generator.setVelocityEngine(engine);
            generator.setOutputEncoding("utf-8");
            generator.setInputEncoding("utf-8");
            generator.setOutputPath(getConfiguration().getOutputDir());
            generator.setTemplatePath(getConfiguration().getTemplateDir());
            generator.parse(config.getMainPage(), context);           
            generator.shutdown();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RollerException("Writing planet files",e);
        }
    }
    
    public void saveConfiguration(PlanetConfigData config) throws RollerException
    {
        this.config = config;
    }

    public void saveGroup(PlanetGroupData sub) throws RollerException
    {
        groupsByHandle.put(sub.getHandle(), sub);
    }

    public void saveSubscription(PlanetSubscriptionData sub) throws RollerException
    {
        subsByURL.put(sub.getFeedUrl(), sub);
    }
    
    public void saveEntry(PlanetEntryData entry) throws RollerException
    {
        // no-op
    }

    public PlanetSubscriptionData getSubscription(String feedUrl) 
        throws RollerException
    {
        return (PlanetSubscriptionData)subsByURL.get(feedUrl);
    }

    public PlanetConfigData getConfiguration() throws RollerException
    {
        return config;
    }

    public List getGroupHandles() throws RollerException
    {
        return new ArrayList(groupsByHandle.keySet());
    }

    public List getGroups() throws RollerException
    {
        return new ArrayList(groupsByHandle.values());
    }

    public PlanetGroupData getGroup(String handle) throws RollerException
    {
        return (PlanetGroupData)groupsByHandle.get(handle);
    }
    
    public List getAggregation(
            PlanetGroupData group, int maxEntries) throws RollerException
    {
        long startTime = System.currentTimeMillis();
        List aggregation = null;
        try
        {
            // Get aggregation from cache
            aggregation = (List)aggregationsByGroup.get(group);
            if (aggregation == null) 
            {
                // No aggregation found in cache, let's create a new one
                aggregation = new ArrayList();
                
                // Comparator to help us create reverse chrono sorted list of entries
                Comparator entryDateComparator = new EntryDateComparator(); 
                
                // Add all of group's subscription's entries to ordered collection
                Set sortedEntries = new TreeSet(entryDateComparator);
                Iterator subs = group.getSubscriptions().iterator();
                while (subs.hasNext())
                {
                    PlanetSubscriptionData sub = (PlanetSubscriptionData)subs.next();
                    Iterator candidates = sub.getEntries().iterator();
                    while (candidates.hasNext())
                    {
                        PlanetEntryData candidate = (PlanetEntryData) candidates.next();
                        if (group.qualified(candidate))
                        {
                            sortedEntries.add(candidate);                        
                        }
                    }
                }
                
                // Throw away all but first maxEntris of our new entry list
                int count = 0;
                Iterator entries = sortedEntries.iterator();
                while (entries.hasNext() && count++ < maxEntries)
                {
                    aggregation.add(entries.next());
                }
                aggregationsByGroup.put(group, aggregation);
            }
        }
        catch (Exception e)
        {
            logger.error("ERROR: building aggregation for: "+group.getHandle(), e);
            throw new RollerException(e);
        }
        long endTime = System.currentTimeMillis();
        logger.info("Generated aggregation in "
                +((endTime-startTime)/1000.0)+" seconds");
        return aggregation; 
    }
    
    public void deleteEntry(PlanetEntryData entry) throws RollerException
    {
        // no-op
    }

    public void deleteGroup(PlanetGroupData group) throws RollerException
    {
        // no-op
    }

    public void deleteSubscription(PlanetSubscriptionData group) throws RollerException
    {
        // no-op
    }
    
    public List getTopSubscriptions(int max) throws RollerException
    {
        throw new RuntimeException("Not implemented");
    }
    
    public List getTopSubscriptions(PlanetGroupData group, int max) throws RollerException
    {
        throw new RuntimeException("Not implemented");
    }

    public List getTopSubscriptions(PlanetGroupData group, int max) throws RollerException
    {
        throw new RuntimeException("Not implemented");
    }

    public PlanetSubscriptionData getSubscriptionById(String id) 
        throws RollerException
    {
        throw new RuntimeException("NOT SUPPORTED");
    }

    public PlanetGroupData getGroupById(String id) 
        throws RollerException
    {
        throw new RuntimeException("NOT SUPPORTED");
    }
    
    public List getAggregation(int maxEntries) throws RollerException
    {
        throw new RuntimeException("NOT SUPPORTED");
    }

    //--------------------------------------------------------------------- console
    
    public static void main(String[] args) 
    {       
        String success = "Planet complete!";
        String error = null;
        Exception traceWorthy = null;
        String fileName = "planet-config.xml"; 
        if (args.length == 1)
        {
            fileName = args[0];
        }
        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new FileInputStream(fileName));
            PlanetTool planet = new PlanetTool(doc);         
            planet.refreshEntries();
            planet.generatePlanet();
            System.out.println(success);
            System.exit(0);
        }
        catch (FileNotFoundException fnfe)
        {
            error = "Configuration file ["+fileName+"] not found";
        }
        catch (JDOMException jde)
        {
            error = "Error parsing configuration file ["+fileName+"]";
            traceWorthy = jde;
        }
        catch (IOException ioe)
        {
            error = "IO error. Using configuration file ["+fileName+"]";
            traceWorthy = ioe;
        }
        catch (RollerException re)
        {
            error = re.getMessage();
        }    
        if (error != null) 
        {
            System.out.println(error);
            if (traceWorthy != null) traceWorthy.printStackTrace();
            System.exit(-1);
        }
    }
    
    public Iterator getAllSubscriptions()
    {
        return subsByURL.values().iterator();
    }
    
    //-------------------------------------------------------------------- privates

    /**
     * Load config data from XML using XPath.
     */
    protected void initFromXML(Document doc) throws RollerException, JDOMException
    {
        Map subsByID = new HashMap();
        Element elem = doc.getRootElement();
        
        config = new PlanetConfigData();
        config.setCacheDir(   getString(elem,"/planet-config/cache-dir"));
        config.setMainPage(   getString(elem,"/planet-config/main-page"));
        config.setGroupPage(  getString(elem,"/planet-config/group-page"));
        config.setAdminName(  getString(elem,"/planet-config/admin-name"));
        config.setAdminEmail( getString(elem,"/planet-config/admin-email"));
        config.setSiteUrl(    getString(elem,"/planet-config/site-url"));
        config.setOutputDir(  getString(elem,"/planet-config/output-dir"));
        config.setTemplateDir(getString(elem,"/planet-config/template-dir"));
        config.setTitle(      getString(elem,"/planet-config/title"));
        config.setDescription(getString(elem,"/planet-config/description"));
        
        XPath subsPath = XPath.newInstance("/planet-config/subscription");
        Iterator subs = subsPath.selectNodes(doc).iterator();
        while (subs.hasNext()) 
        {
            Element subElem = (Element)subs.next();
            PlanetSubscriptionData sub = new PlanetSubscriptionData();
            String id = subElem.getAttributeValue("id");
            sub.setTitle(   getString(subElem, "title"));
            sub.setAuthor(  getString(subElem, "author"));
            sub.setFeedUrl( getString(subElem, "feed-url"));
            sub.setSiteUrl( getString(subElem, "site-url"));           
            subsByURL.put(sub.getFeedUrl(), sub);
            subsByID.put(id, sub);
        }
        logger.info("Found "+subsByID.size()+" subscriptions");
                
        XPath groupsPath = XPath.newInstance("/planet-config/group");
        Iterator groups = groupsPath.selectNodes(doc).iterator();
        while (groups.hasNext()) 
        {
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
            while (refs.hasNext())
            {
                Element refElem = (Element)refs.next();
                String includeAll = refElem.getAttributeValue("include-all");
                if (includeAll != null && includeAll.equals("true"))
                {
                    //group.getSubscriptions().addAll(subsByID.values());
                    group.addSubscriptions(subsByID.values());
                }
                else 
                {
                    String refid = refElem.getAttributeValue("refid");
                    PlanetSubscriptionData sub = (PlanetSubscriptionData)subsByID.get(refid);
                    if (sub == null) 
                    {
                        throw new RollerException("No such subscription ["+refid+"]");
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
    
    public class EntryDateComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            PlanetEntryData e1 = (PlanetEntryData)o1;
            PlanetEntryData e2 = (PlanetEntryData)o2;
            if (e1.getPublished() != null && e2.getPublished() != null)
            {
                return e2.getPublished().compareTo(e1.getPublished());
            }
            if (e1.getPublished() == null)
            {
                logger.warn("Entry missing pubDate in sub: " 
                        + e1.getSubscription().getFeedUrl());
            }
            if (e2.getPublished() == null)
            {
                logger.warn("Entry missing pubDate in sub: " 
                        + e2.getSubscription().getFeedUrl());
            }            
            return 0;
        }            
    }

    /**
     * Total number of subscriptions.
     */
    public int getSubscriptionCount() throws RollerException 
    {
        return this.subsByURL.size();
    };
}

