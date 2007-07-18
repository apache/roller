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
/*
 * TestUtils.java
 *
 * Created on April 6, 2006, 8:38 PM
 */

package org.apache.roller.weblogger;

import org.apache.roller.planet.business.GuicePlanetProvider;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetProvider;
import org.apache.roller.planet.business.startup.PlanetStartup;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogHitCount;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Utility class for unit test classes.
 */
public final class TestUtils {
    
    
    public static void setupWeblogger() throws Exception {
        
        if(!WebloggerFactory.isBootstrapped()) {
            // do core services preparation
            WebloggerStartup.prepare();
            
            // do application bootstrapping
            WebloggerFactory.bootstrap();
            
            // always initialize the properties manager and flush
            WebloggerFactory.getWeblogger().getPropertiesManager().initialize();
            WebloggerFactory.getWeblogger().flush();
        }
    }
    
    
    public static void shutdownWeblogger() throws Exception {
        
        // trigger shutdown
        WebloggerFactory.getWeblogger().shutdown();
    }
    
    
    public static void setupPlanet() throws Exception {
        
        if(!PlanetFactory.isBootstrapped()) {
            // do core services preparation
            PlanetStartup.prepare();
            
            // do application bootstrapping
            String guiceModule = WebloggerConfig.getProperty("planet.aggregator.guice.module");
            PlanetProvider provider = new GuicePlanetProvider(guiceModule);
            PlanetFactory.bootstrap(provider);
            
            // always initialize the properties manager and flush
            PlanetFactory.getPlanet().getPropertiesManager().initialize();
            PlanetFactory.getPlanet().flush();
        }
    }
    
    
    /**
     * Convenience method that simulates the end of a typical session.
     *
     * Normally this would be triggered by the end of the response in the webapp
     * but for unit tests we need to do this explicitly.
     *
     * @param flush true if you want to flush changes to db before releasing
     */
    public static void endSession(boolean flush) throws Exception {
        
        if(flush) {
            WebloggerFactory.getWeblogger().flush();
        }
        
        WebloggerFactory.getWeblogger().release();
    }
    
    
    /**
     * Convenience method that creates a user and stores it.
     */
    public static User setupUser(String username) throws Exception {
        
        User testUser = new User();
        testUser.setUserName(username);
        testUser.setPassword("password");
        testUser.setScreenName("Test User Screen Name");
        testUser.setFullName("Test User");
        testUser.setEmailAddress("TestUser@dev.null");
        testUser.setLocale("en_US");
        testUser.setTimeZone("America/Los_Angeles");
        testUser.setDateCreated(new java.util.Date());
        testUser.setEnabled(Boolean.TRUE);
        
        // store the user
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        mgr.addUser(testUser);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
        
        // query for the user to make sure we return the persisted object
        User user = mgr.getUserByUserName(username);
        
        if(user == null)
            throw new WebloggerException("error inserting new user");
        
        return user;
    }
    
    
    /**
     * Convenience method for removing a user.
     */
    public static void teardownUser(String id) throws Exception {
        
        // lookup the user
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        User user = mgr.getUser(id);
        
        // remove the user
        mgr.removeUser(user);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method that creates a weblog and stores it.
     */
    public static Weblog setupWeblog(String handle, User creator) throws Exception {
        
        Weblog testWeblog = new Weblog();
        testWeblog.setName("Test Weblog");
        testWeblog.setDescription("Test Weblog");
        testWeblog.setHandle(handle);
        testWeblog.setEmailAddress("testweblog@dev.null");
        testWeblog.setEditorPage("editor-text.jsp");
        testWeblog.setBlacklist("");
        testWeblog.setEmailFromAddress("");
        testWeblog.setEditorTheme("basic");
        testWeblog.setLocale("en_US");
        testWeblog.setTimeZone("America/Los_Angeles");
        testWeblog.setDateCreated(new java.util.Date());
        testWeblog.setCreator(creator);
        
        // add weblog
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        mgr.addWebsite(testWeblog);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
        
        // query for the new weblog and return it
        Weblog weblog = mgr.getWebsiteByHandle(handle);
        
        if(weblog == null)
            throw new WebloggerException("error setting up weblog");
        
        return weblog;
    }
    
    
    /**
     * Convenience method for removing a weblog.
     */
    public static void teardownWeblog(String id) throws Exception {
        
        // lookup the weblog
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        Weblog weblog = mgr.getWebsite(id);
        
        // remove the weblog
        mgr.removeWebsite(weblog);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method for removing a permission.
     */
    public static void teardownPermissions(String id) throws Exception {
        
        // lookup the permissions
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        WeblogPermission perm = mgr.getPermissions(id);
        
        // remove the permissions
        mgr.removePermissions(perm);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
    }
 
    
    /**
     * Convenience method for creating a weblog category.
     */
    public static WeblogCategory setupWeblogCategory(Weblog weblog,
                                                         String name,
                                                         WeblogCategory parent)
            throws Exception {
        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogCategory root = mgr.getRootWeblogCategory(weblog);
        
        WeblogCategory catParent = root;
        if(parent != null) {
            catParent = parent;
        }
        WeblogCategory testCat = new WeblogCategory(weblog, catParent, name, null, null);
        mgr.saveWeblogCategory(testCat);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
        
        // query for object
        WeblogCategory cat = mgr.getWeblogCategory(testCat.getId());
        
        if(cat == null)
            throw new WebloggerException("error setting up weblog category");
        
        return cat;
    }
    
    
    /**
     * Convenience method for removing a weblog category.
     */
    public static void teardownWeblogCategory(String id) throws Exception {
        
        // lookup the cat
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogCategory cat = mgr.getWeblogCategory(id);
        
        // remove the cat
        mgr.removeWeblogCategory(cat);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method for creating a weblog entry.
     */
    public static WeblogEntry setupWeblogEntry(String anchor,
                                                   WeblogCategory cat,
                                                   Weblog weblog,
                                                   User user)
            throws Exception {
        
        WeblogEntry testEntry = new WeblogEntry();
        testEntry.setTitle(anchor);
        testEntry.setLink("testEntryLink");
        testEntry.setText("blah blah entry");
        testEntry.setAnchor(anchor);
        testEntry.setPubTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        testEntry.setUpdateTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        testEntry.setStatus(WeblogEntry.PUBLISHED);
        testEntry.setWebsite(getManagedWebsite(weblog));
        testEntry.setCreator(getManagedUser(user));
        testEntry.setCategory(cat);
        
        // store entry
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        mgr.saveWeblogEntry(testEntry);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
        
        // query for object
        WeblogEntry entry = mgr.getWeblogEntry(testEntry.getId());
        
        if(entry == null)
            throw new WebloggerException("error setting up weblog entry");
        
        return entry;
    }
    
    
    /**
     * Convenience method for removing a weblog entry.
     */
    public static void teardownWeblogEntry(String id) throws Exception {
        
        // lookup the entry
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogEntry entry = mgr.getWeblogEntry(id);
        
        // remove the entry
        mgr.removeWeblogEntry(entry);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method for creating a comment.
     */
    public static WeblogEntryComment setupComment(String content, WeblogEntry entry)
            throws Exception {
        
        WeblogEntryComment testComment = new WeblogEntryComment();
        testComment.setName("test");
        testComment.setEmail("test");
        testComment.setUrl("test");
        testComment.setRemoteHost("foofoo");
        testComment.setContent("this is a test comment");
        testComment.setPostTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        testComment.setWeblogEntry(getManagedWeblogEntry(entry));
        testComment.setStatus(WeblogEntryComment.APPROVED);
        
        // store testComment
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        mgr.saveComment(testComment);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
        
        // query for object
        WeblogEntryComment comment = mgr.getComment(testComment.getId());
        
        if(comment == null)
            throw new WebloggerException("error setting up comment");
        
        return comment;
    }
    
    
    /**
     * Convenience method for removing a comment.
     */
    public static void teardownComment(String id) throws Exception {
        
        // lookup the comment
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogEntryComment comment = mgr.getComment(id);
        
        // remove the comment
        mgr.removeComment(comment);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method for creating a ping target.
     */
    public static PingTarget setupPingTarget(String name, String url) 
            throws Exception {
        
        PingTarget testPing = new PingTarget();
        testPing.setName("testCommonPing");
        testPing.setPingUrl("http://localhost/testCommonPing");
        
        // store ping
        PingTargetManager pingMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        pingMgr.savePingTarget(testPing);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
        
        // query for it
        PingTarget ping = pingMgr.getPingTarget(testPing.getId());
        
        if(ping == null)
            throw new WebloggerException("error setting up ping target");
        
        return ping;
    }
    
    
    /**
     * Convenience method for removing a ping target.
     */
    public static void teardownPingTarget(String id) throws Exception {
        
        // query for it
        PingTargetManager pingMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        PingTarget ping = pingMgr.getPingTarget(id);
        
        // remove the ping
        pingMgr.removePingTarget(ping);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method for creating an auto ping.
     */
    public static AutoPing setupAutoPing(PingTarget ping, Weblog weblog)
            throws Exception {
        
        AutoPingManager mgr = WebloggerFactory.getWeblogger().getAutopingManager();
        
        // store auto ping
        AutoPing autoPing = new AutoPing(null, ping, getManagedWebsite(weblog) );
        mgr.saveAutoPing(autoPing);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
        
        // query for it
        autoPing = mgr.getAutoPing(autoPing.getId());
        
        if(autoPing == null)
            throw new WebloggerException("error setting up auto ping");
        
        return autoPing;
    }
    
    
    /**
     * Convenience method for removing an auto ping.
     */
    public static void teardownAutoPing(String id) throws Exception {
        
        // query for it
        AutoPingManager mgr = WebloggerFactory.getWeblogger().getAutopingManager();
        AutoPing autoPing = mgr.getAutoPing(id);
        
        // remove the auto ping
        mgr.removeAutoPing(autoPing);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method for creating a hit count.
     */
    public static WeblogHitCount setupHitCount(Weblog weblog, int amount)
            throws Exception {
        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        
        // store
        WeblogHitCount testCount = new WeblogHitCount();
        testCount.setWeblog(getManagedWebsite(weblog));
        testCount.setDailyHits(amount);
        mgr.saveHitCount(testCount);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
        
        // query for it
        testCount = mgr.getHitCount(testCount.getId());
        
        if(testCount == null)
            throw new WebloggerException("error setting up hit count");
        
        return testCount;
    }
    
    
    /**
     * Convenience method for removing a hit count.
     */
    public static void teardownHitCount(String id) throws Exception {
        
        // query for it
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogHitCount testCount = mgr.getHitCount(id);
        
        // remove
        mgr.removeHitCount(testCount);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method for creating a weblog folder.
     */
    public static WeblogBookmarkFolder setupFolder(Weblog weblog,
                                         String name,
                                         WeblogBookmarkFolder parent)
            throws Exception {
        
        BookmarkManager mgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        WeblogBookmarkFolder root = mgr.getRootFolder(weblog);
        
        WeblogBookmarkFolder folderParent = root;
        if(parent != null) {
            folderParent = parent;
        }
        WeblogBookmarkFolder testFolder = new WeblogBookmarkFolder(folderParent, name, null, weblog);
        mgr.saveFolder(testFolder);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
        
        // query for object
        WeblogBookmarkFolder cat = mgr.getFolder(testFolder.getId());
        
        if(testFolder == null)
            throw new WebloggerException("error setting up weblog folder");
        
        return testFolder;
    }
    
    
    /**
     * Convenience method for removing a weblog folder.
     */
    public static void teardownFolder(String id) throws Exception {
        
        // lookup the folder
        BookmarkManager mgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        WeblogBookmarkFolder folder = mgr.getFolder(id);
        
        // remove the cat
        mgr.removeFolder(folder);
        
        // flush to db
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method that returns managed copy of given user.
     */
    public static User getManagedUser(User user) throws WebloggerException {
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        return mgr.getUser(user.getId());
    }
    
    /**
     * Convenience method that returns managed copy of given website.
     */
    public static Weblog getManagedWebsite(Weblog website) throws WebloggerException {
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        return mgr.getWebsite(website.getId());
    }
    
    /**
     * Convenience method that returns managed copy of given WeblogEntry.
     */
    public static WeblogEntry getManagedWeblogEntry(WeblogEntry weblogEntry) throws WebloggerException {
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        return mgr.getWeblogEntry(weblogEntry.getId());
    }
    
}
