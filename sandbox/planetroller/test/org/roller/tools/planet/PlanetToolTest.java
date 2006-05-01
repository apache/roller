/*
 * Copyright 2005 Sun Microsystems, Inc.
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
package org.apache.roller.tools.planet;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.velocity.app.Velocity;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.pojos.PlanetConfigData;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.PlanetSubscriptionData;
import org.apache.roller.util.rome.ContentModule;

import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * Test command line version of PlanetManager (and misc. parsing tests)
 * @author David M Johnson
 */
public class PlanetToolTest extends TestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PlanetToolTest.class);
    }

    public void _testLoadConfig() throws Exception
    {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(
                new FileInputStream("./testdata/planet-test.xml"));
        
        Properties props = new Properties();
        Velocity.init(props);
        
        PlanetManager planetTool = new PlanetTool(doc);
        PlanetConfigData config = planetTool.getConfiguration();
 
        assertNotNull(config.getMainPage());
        assertEquals(3, planetTool.getGroupHandles().size());
        assertNotNull(planetTool.getGroup("all"));
        assertNotNull(planetTool.getGroup("roller"));
        assertNotNull(planetTool.getGroup("davejava"));
        
        PlanetGroupData allGroup = planetTool.getGroup("all");
        assertEquals(2, allGroup.getSubscriptions().size());
        
        PlanetGroupData lanceGroup = planetTool.getGroup("roller");
        assertEquals(2, lanceGroup.getSubscriptions().size());
        
        PlanetGroupData rollerGroup = planetTool.getGroup("davejava");
        assertEquals(1, rollerGroup.getSubscriptions().size());
        
        Iterator subs = allGroup.getSubscriptions().iterator();
        while (subs.hasNext())
        {
            PlanetSubscriptionData sub = (PlanetSubscriptionData) subs.next();
            assertNotNull(sub.getFeedUrl());
            assertNotNull(sub.getSiteUrl());
        }
    }
    public void _testAggregation() throws Exception
    {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(
                new FileInputStream("./testdata/planet-test.xml"));
         
        PlanetManager planetTool = new PlanetTool(doc);
        planetTool.refreshEntries();
        
        PlanetGroupData allGroup = planetTool.getGroup("all");
        List ag2 = planetTool.getAggregation(allGroup, 100);
        assertEquals(30, ag2.size());
        
        PlanetGroupData rollerGroup = planetTool.getGroup("roller");
        List ag3 = planetTool.getAggregation(rollerGroup, 100);
        assertEquals(15, ag3.size());

        PlanetGroupData daveGroup = planetTool.getGroup("davejava");
        List ag4 = planetTool.getAggregation(daveGroup, 15);
        assertEquals(15, ag4.size());        
    }
    /**
     * We had to extend Rome to handle Goslings's weird mix of RSS 0.91 + pubDates
     */
    public void testPubDateParsing() throws Exception
    { 
        _testPubDateParsing("http://weblogs.java.net/jag/blog.rss");
        _testPubDateParsing("http://lotusmedia.org/feed/");
        _testPubDateParsing("http://sciencepolitics.blogspot.com/atom.xml");
        _testPubDateParsing("http://mistersugar.com/?atom=1");
        _testPubDateParsing("http://alvinphillips.com/?atom=1");
        _testPubDateParsing("http://mistersugar.com/blogtogether/?rss=1");
        _testPubDateParsing("http://www.irelan.net/becoming/wp-rss.php");
    }
    public void _testPubDateParsing(String url) throws Exception
    { 
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        System.setProperty("httpclient.useragent", "Rome Fetcher");
        int code = client.executeMethod(method);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(method.getResponseBodyAsStream())); 
        assertNotNull(((SyndEntry)feed.getEntries().get(0)).getPublishedDate());
    }
    /**
     * Had to work around Rome bug that squashed &lt;dc:date&gt;
     */
    public void testDCDateParsing() throws Exception
    { 
        // Test funky RSS 2.0 with <dc:date> instead of <pubDate> 
        _testDCDateParsing("http://bitworking.org/index.rss");
        _testDCDateParsing("http://silflayhraka.com/index.xml");
        
        // Test RSS 1.0 with <dc:date>  
        _testDCDateParsing("http://www.isthatlegal.org/rss/isthatlegal.xml");
    }
    public void _testDCDateParsing(String url) throws Exception
    { 
        URL feedUrl = new URL(url);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl)); 
        SyndEntry entry = (SyndEntry)feed.getEntries().get(0);
        DCModule dcm = (DCModule)entry.getModule(DCModule.URI);
        assertNotNull(dcm.getDate());
    }
    /**
     * Feeds with content in &lt;content:encoded%gt; 
     */
    public void testContentEncodedParsing() throws Exception
    { 
        // Test feeds with content in <content:encoded> 
        _testContentEncodedParsing("http://www.intertwingly.net/blog/index.rdf");
        _testContentEncodedParsing("http://www.isthatlegal.org/rss/isthatlegal.xml");
    }
    public void _testContentEncodedParsing(String url) throws Exception
    { 
        URL feedUrl = new URL(url);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl)); 
        SyndEntry entry = (SyndEntry)feed.getEntries().get(0);
        ContentModule cm = (ContentModule)entry.getModule(ContentModule.URI);
        assertNotNull(cm.getEncoded());
    }
}







