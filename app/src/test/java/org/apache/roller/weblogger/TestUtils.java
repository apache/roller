/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger;

import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.UserWeblogRole;

/**
 * Utility class for unit test classes.
 */
public final class TestUtils {

    // Username prefix we are using (simplifies local testing)
    public static final String JUNIT_PREFIX = "junit_";

    public static void setupWeblogger() throws Exception {

        if (!WebloggerFactory.isBootstrapped()) {

            // do core services preparation
            WebloggerStartup.prepare();

            // do application bootstrapping
            WebloggerFactory.bootstrap();
        }
    }

    /**
     * Convenience method that simulates the end of a typical session.
     * 
     * Normally this would be triggered by the end of the response in the webapp
     * but for unit tests we need to do this explicitly.
     * 
     * @param flush
     *            true if you want to flush changes to db before releasing
     */
    public static void endSession(boolean flush) throws Exception {
        if (flush) {
            WebloggerFactory.flush();
        }
        WebloggerFactory.release();
    }

    /**
     * Convenience method that creates a user and stores it.
     */
    public static User setupUser(String userName) throws Exception {

        // Set local name
        userName = JUNIT_PREFIX + userName;

        User testUser = new User();
        testUser.setUserName(userName);
        testUser.setPassword("password");
        testUser.setGlobalRole(GlobalRole.BLOGGER);
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
        WebloggerFactory.flush();

        // query for the user to make sure we return the persisted object
        User user = mgr.getUserByUserName(userName);

        if (user == null) {
            throw new WebloggerException("error inserting new user");
        }

        return user;
    }

    /**
     * Convenience method for removing a user.
     */
    public static void teardownUser(String userName) throws Exception {

        // lookup the user
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        User user = mgr.getUserByUserName(userName, null);

        // remove the user
        mgr.removeUser(user);

        // flush to db
        WebloggerFactory.flush();
    }

    /**
     * Convenience method that creates a weblog and stores it.
     */
    public static Weblog setupWeblog(String handle, User creator)
            throws Exception {

        Weblog testWeblog = new Weblog();
        testWeblog.setName("Test Weblog");
        testWeblog.setTagline("Test Weblog");
        testWeblog.setHandle(handle);
        testWeblog.setEmailAddress("testweblog@dev.null");
        testWeblog.setEditorPage("editor-text.jsp");
        testWeblog.setBlacklist("");
        testWeblog.setEditorTheme("basic");
        testWeblog.setLocale("en_US");
        testWeblog.setTimeZone("America/Los_Angeles");
        testWeblog.setDateCreated(new java.util.Date());
        testWeblog.setCreatorUserName(creator.getUserName());

        // add weblog
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        mgr.addWeblog(testWeblog);

        // flush to db
        WebloggerFactory.flush();

        // query for the new weblog and return it
        Weblog weblog = mgr.getWeblogByHandle(handle);

        if (weblog == null) {
            throw new WebloggerException("error setting up weblog");
        }

        return weblog;
    }

    /**
     * Convenience method for removing a weblog.
     */
    public static void teardownWeblog(String id) throws Exception {

        // lookup the weblog
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        Weblog weblog = mgr.getWeblog(id);

        // remove the weblog
        mgr.removeWeblog(weblog);

        // flush to db
        WebloggerFactory.flush();
    }

    /**
     * Convenience method for removing a permission.
     */
    public static void teardownPermissions(UserWeblogRole perm)
            throws Exception {

        // remove all permissions
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        mgr.revokeWeblogRole(perm.getUser(), perm.getWeblog());

        // flush to db
        WebloggerFactory.flush();
    }

    /**
     * Convenience method for creating a weblog category.
     */
    public static WeblogCategory setupWeblogCategory(Weblog weblog, String name)
            throws Exception {

        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();

        WeblogCategory testCat = new WeblogCategory(weblog, name);
        weblog.addCategory(testCat);
        mgr.saveWeblogCategory(testCat);

        // flush to db
        WebloggerFactory.flush();

        // query for object
        WeblogCategory cat = mgr.getWeblogCategory(testCat.getId());

        if (cat == null) {
            throw new WebloggerException("error setting up weblog category");
        }

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
        WebloggerFactory.flush();
    }

    /**
     * Convenience method for creating a published weblog entry.
     */
    public static WeblogEntry setupWeblogEntry(String anchor,
            WeblogCategory cat, Weblog weblog, User user) throws Exception {

        return TestUtils.setupWeblogEntry(anchor, cat, PubStatus.PUBLISHED,
                weblog, user);
    }

    /**
     * Convenience method for creating a published weblog entry with the blog's
     * default category
     */
    public static WeblogEntry setupWeblogEntry(String anchor, Weblog weblog,
            User user) throws Exception {

        return TestUtils.setupWeblogEntry(anchor, weblog.getWeblogCategories()
                .iterator().next(), PubStatus.PUBLISHED, weblog, user);
    }

    /**
     * Convenience method for creating a weblog entry
     */
    public static WeblogEntry setupWeblogEntry(String anchor,
            WeblogCategory cat, PubStatus status, Weblog weblog, User user)
            throws Exception {

        WeblogEntry testEntry = new WeblogEntry();
        testEntry.setTitle(anchor);
        testEntry.setText("blah blah entry");
        testEntry.setAnchor(anchor);
        testEntry.setPubTime(new java.sql.Timestamp(new java.util.Date()
                .getTime()));
        testEntry.setUpdateTime(new java.sql.Timestamp(new java.util.Date()
                .getTime()));
        testEntry.setStatus(status);
        testEntry.setWeblog(getManagedWebsite(weblog));
        testEntry.setCreatorUserName(getManagedUser(user).getUserName());
        testEntry.setCategory(cat);

        // store entry
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger()
                .getWeblogEntryManager();
        mgr.saveWeblogEntry(testEntry);

        // flush to db
        WebloggerFactory.flush();

        // query for object
        WeblogEntry entry = mgr.getWeblogEntry(testEntry.getId());

        if (entry == null) {
            throw new WebloggerException("error setting up weblog entry");
        }

        return entry;
    }

    /**
     * Convenience method for removing a weblog entry.
     */
    public static void teardownWeblogEntry(String id) throws Exception {

        // lookup the entry
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        WeblogEntry entry = mgr.getWeblogEntry(id);

        // remove the entry
        mgr.removeWeblogEntry(entry);

        // flush to db
        WebloggerFactory.flush();
    }

    /**
     * Convenience method for creating a comment.
     */
    public static WeblogEntryComment setupComment(String content,
            WeblogEntry entry) throws Exception {

        WeblogEntryComment testComment = new WeblogEntryComment();
        testComment.setName("test");
        testComment.setEmail("test");
        testComment.setUrl("test");
        testComment.setRemoteHost("foofoo");
        testComment.setContent("this is a test comment");
        testComment.setPostTime(new java.sql.Timestamp(new java.util.Date()
                .getTime()));
        testComment.setWeblogEntry(getManagedWeblogEntry(entry));
        testComment.setStatus(ApprovalStatus.APPROVED);

        // store testComment
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger()
                .getWeblogEntryManager();
        mgr.saveComment(testComment);

        // flush to db
        WebloggerFactory.flush();

        // query for object
        WeblogEntryComment comment = mgr.getComment(testComment.getId());

        if (comment == null) {
            throw new WebloggerException("error setting up comment");
        }

        return comment;
    }

    /**
     * Convenience method for removing a comment.
     */
    public static void teardownComment(String id) throws Exception {

        // lookup the comment
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger()
                .getWeblogEntryManager();
        WeblogEntryComment comment = mgr.getComment(id);

        // remove the comment
        mgr.removeComment(comment);

        // flush to db
        WebloggerFactory.flush();
    }

    /**
     * Convenience method for creating a ping target.
     */
    public static PingTarget setupPingTarget(String name, String url)
            throws Exception {

        PingTarget testPing = new PingTarget();
        testPing.setId(WebloggerCommon.generateUUID());
        testPing.setName("testCommonPing");
        testPing.setPingUrl("http://localhost/testCommonPing");

        // store ping
        PingTargetManager pingMgr = WebloggerFactory.getWeblogger()
                .getPingTargetManager();
        pingMgr.savePingTarget(testPing);

        // flush to db
        WebloggerFactory.flush();

        // query for it
        PingTarget ping = pingMgr.getPingTarget(testPing.getId());

        if (ping == null) {
            throw new WebloggerException("error setting up ping target");
        }

        return ping;
    }

    /**
     * Convenience method for removing a ping target.
     */
    public static void teardownPingTarget(String id) throws Exception {

        // query for it
        PingTargetManager pingMgr = WebloggerFactory.getWeblogger()
                .getPingTargetManager();
        PingTarget ping = pingMgr.getPingTarget(id);

        // remove the ping
        pingMgr.removePingTarget(ping);

        // flush to db
        WebloggerFactory.flush();
    }

    /**
     * Convenience method for creating an auto ping.
     */
    public static AutoPing setupAutoPing(PingTarget ping, Weblog weblog)
            throws Exception {

        PingTargetManager mgr = WebloggerFactory.getWeblogger()
                .getPingTargetManager();

        // store auto ping
        AutoPing autoPing = new AutoPing(ping, getManagedWebsite(weblog));
        mgr.saveAutoPing(autoPing);

        // flush to db
        WebloggerFactory.flush();

        // query for it
        autoPing = mgr.getAutoPing(autoPing.getId());

        if (autoPing == null) {
            throw new WebloggerException("error setting up auto ping");
        }

        return autoPing;
    }

    /**
     * Convenience method that returns managed copy of given user.
     */
    public static User getManagedUser(User user) throws WebloggerException {
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        return mgr.getUserByUserName(user.getUserName());
    }

    /**
     * Convenience method that returns managed copy of given website.
     */
    public static Weblog getManagedWebsite(Weblog website)
            throws WebloggerException {
        return WebloggerFactory.getWeblogger().getWeblogManager()
                .getWeblog(website.getId());
    }

    /**
     * Convenience method that returns managed copy of given WeblogEntry.
     */
    public static WeblogEntry getManagedWeblogEntry(WeblogEntry weblogEntry)
            throws WebloggerException {
        return WebloggerFactory.getWeblogger().getWeblogEntryManager()
                .getWeblogEntry(weblogEntry.getId());
    }

    /**
     * Convenience method that returns managed copy of given WeblogEntry.
     */
    public static WeblogCategory getManagedWeblogCategory(WeblogCategory cat)
            throws WebloggerException {
        return WebloggerFactory.getWeblogger().getWeblogManager()
                .getWeblogCategory(cat.getId());
    }

    /**
     * Convenience method that creates a planet and stores it.
     */
    public static Planet setupPlanet(String handle)
            throws Exception {

        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();

        // store
        Planet testPlanet = new Planet(handle, handle, handle);
        mgr.savePlanet(testPlanet);

        // flush
        WebloggerFactory.flush();

        // query to make sure we return the persisted object
        Planet group = mgr.getPlanetById(testPlanet.getId());

        if (group == null) {
            throw new WebloggerException("error inserting new group");
        }

        return group;
    }

    /**
     * Convenience method for removing a planet.
     */
    public static void teardownPlanet(String handle) throws Exception {

        // lookup
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Planet planet = mgr.getPlanet(handle);

        // remove
        mgr.deletePlanet(planet);

        // flush
        WebloggerFactory.flush();
    }

    /**
     * Convenience method that creates a sub and stores it.
     */
    public static Subscription setupSubscription(Planet planet, String feedUrl)
            throws Exception {

        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();

        // store
        Subscription testSub = new Subscription();
        testSub.setFeedURL(feedUrl);
        testSub.setTitle(feedUrl);
        testSub.setPlanet(planet);
        mgr.saveSubscription(testSub);

        planet.getSubscriptions().add(testSub);

        // flush
        WebloggerFactory.flush();

        // query to make sure we return the persisted object
        Subscription sub = mgr.getSubscriptionById(testSub.getId());

        if (sub == null) {
            throw new WebloggerException("error inserting new subscription");
        }

        return sub;
    }

    /**
     * Convenience method for removing a sub.
     */
    public static void teardownSubscription(String id) throws Exception {

        // lookup
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Subscription sub = mgr.getSubscriptionById(id);

        // remove
        mgr.deleteSubscription(sub);

        // flush
        WebloggerFactory.flush();
    }

    /**
     * Convenience method that creates an entry and stores it.
     */
    public static SubscriptionEntry setupEntry(Subscription sub, String title)
            throws Exception {

        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();

        // make sure we are using a persistent object
        Subscription testSub = mgr.getSubscriptionById(sub.getId());

        // store
        SubscriptionEntry testEntry = new SubscriptionEntry();
        testEntry.setPermalink(title);
        testEntry.setTitle(title);
        testEntry
                .setPubTime(new java.sql.Timestamp(System.currentTimeMillis()));
        testEntry.setSubscription(testSub);
        testSub.getEntries().add(testEntry);
        mgr.saveEntry(testEntry);

        // flush
        WebloggerFactory.flush();

        // query to make sure we return the persisted object
        SubscriptionEntry entry = mgr.getEntryById(testEntry.getId());

        if (entry == null) {
            throw new WebloggerException("error inserting new entry");
        }

        return entry;
    }

    /**
     * Convenience method for removing an entry.
     */
    public static void teardownEntry(String id) throws Exception {

        // lookup
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        SubscriptionEntry entry = mgr.getEntryById(id);

        // remove
        mgr.deleteEntry(entry);
        entry.getSubscription().getEntries().remove(entry);

        // flush
        WebloggerFactory.flush();
    }
}
