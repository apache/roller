/*
   Copyright 2015 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.apache.roller.weblogger;

import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.sql.Timestamp;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring-beans.xml")
abstract public class WebloggerTest {

    @Autowired
    private ApplicationContext appContext;

    @Resource
    protected WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Resource
    protected WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Resource
    protected UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Resource
    protected PlanetManager planetManager;

    public void setPlanetManager(PlanetManager manager) {
        this.planetManager = manager;
    }

    @Resource
    protected JPAPersistenceStrategy strategy;

    public void setJPAPersistenceStrategy(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    @Resource
    protected PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    @Before
    public void setUp() throws Exception {
        if (!WebloggerFactory.isBootstrapped()) {
            WebloggerFactory.bootstrap(appContext);
        }
    }

    /**
     * Convenience method that returns managed copy of given user.
     */
    protected User getManagedUser(User user) {
        return userManager.getUserByUserName(user.getUserName());
    }

    /**
     * Convenience method that returns managed copy of given WeblogEntry.
     */
    protected WeblogEntry getManagedWeblogEntry(WeblogEntry weblogEntry) {
        return weblogEntryManager.getWeblogEntry(weblogEntry.getId());
    }

    /**
     * Convenience method that returns managed copy of given website.
     */
    protected Weblog getManagedWeblog(Weblog weblog) {
        return weblogManager.getWeblog(weblog.getId());
    }

    protected void endSession(boolean flush) throws Exception {
        if (flush) {
            strategy.flush();
        }
        strategy.release();
    }

    protected User setupUser(String userName) throws Exception {
        User testUser = new User();
        testUser.setId(WebloggerCommon.generateUUID());
        testUser.setUserName(userName.toLowerCase());
        testUser.setPassword("password");
        testUser.setGlobalRole(GlobalRole.BLOGGER);
        testUser.setScreenName(userName);
        testUser.setEmailAddress("TestUser@dev.null");
        testUser.setLocale("en_US");
        testUser.setDateCreated(new java.util.Date());
        testUser.setEnabled(Boolean.TRUE);

        // store the user
        userManager.saveUser(testUser);

        // flush to db
        strategy.flush();

        // query for the user to make sure we return the persisted object
        User user = userManager.getUserByUserName(userName.toLowerCase());

        if (user == null) {
            throw new IllegalStateException("error inserting new user");
        }

        return user;
    }

    protected void teardownUser(String userName) throws Exception {
        User user = userManager.getUserByUserName(userName, null);
        userManager.removeUser(user);
        strategy.flush();
    }

    protected Weblog setupWeblog(String handle, User creator)
            throws Exception {

        Weblog testWeblog = new Weblog();
        testWeblog.setName("Test Weblog");
        testWeblog.setTagline("Test Weblog");
        testWeblog.setHandle(handle.toLowerCase());
        testWeblog.setEditorPage("editor-text.jsp");
        testWeblog.setBlacklist("");
        testWeblog.setTheme("basic");
        testWeblog.setLocale("en_US");
        testWeblog.setTimeZone("America/Los_Angeles");
        testWeblog.setDateCreated(new java.util.Date());
        testWeblog.setCreatorId(creator.getId());

        // add weblog
        weblogManager.addWeblog(testWeblog);

        // flush to db
        strategy.flush();

        // query for the new weblog and return it
        Weblog weblog = weblogManager.getWeblogByHandle(handle.toLowerCase());

        if (weblog == null) {
            throw new IllegalStateException("error setting up weblog");
        }

        return weblog;
    }

    protected void teardownWeblog(String id) throws Exception {
        Weblog weblog = weblogManager.getWeblog(id);
        weblogManager.removeWeblog(weblog);
        strategy.flush();
    }


    protected WeblogEntry setupWeblogEntry(String anchor, Weblog weblog, User user) throws Exception {
        return setupWeblogEntry(anchor, weblog.getWeblogCategories()
                        .iterator().next(), WeblogEntry.PubStatus.PUBLISHED, weblog, user);
    }

    protected WeblogEntry setupWeblogEntry(String anchor, WeblogCategory cat,
                                           WeblogEntry.PubStatus status, Weblog weblog, User user)
            throws Exception {

        WeblogEntry testEntry = new WeblogEntry();
        testEntry.setId(WebloggerCommon.generateUUID());
        testEntry.setTitle(anchor);
        testEntry.setText("blah blah entry");
        testEntry.setAnchor(anchor);
        testEntry.setPubTime(new java.sql.Timestamp(new java.util.Date()
                .getTime()));
        testEntry.setUpdateTime(new java.sql.Timestamp(new java.util.Date()
                .getTime()));
        testEntry.setStatus(status);
        testEntry.setWeblog(getManagedWeblog(weblog));
        testEntry.setCreatorId(user.getId());
        testEntry.setCategory(cat);

        // store entry
        weblogEntryManager.saveWeblogEntry(testEntry);

        // flush to db
        strategy.flush();

        // query for object
        WeblogEntry entry = weblogEntryManager.getWeblogEntry(testEntry.getId());

        if (entry == null) {
            throw new IllegalStateException("error setting up weblog entry");
        }

        return entry;
    }

    protected void teardownWeblogEntry(String id) throws Exception {
        WeblogEntry entry = weblogEntryManager.getWeblogEntry(id);
        weblogEntryManager.removeWeblogEntry(entry);
        strategy.flush();
    }

    protected WeblogEntryComment setupComment(String comment, WeblogEntry entry) throws Exception {

        WeblogEntryComment testComment = new WeblogEntryComment();
        testComment.setName("test");
        testComment.setEmail("test");
        testComment.setUrl("test");
        testComment.setRemoteHost("foofoo");
        testComment.setContent(comment);
        testComment.setPostTime(new java.sql.Timestamp(new java.util.Date()
                .getTime()));
        testComment.setWeblogEntry(getManagedWeblogEntry(entry));
        testComment.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);

        // store testComment
        weblogEntryManager.saveComment(testComment, true);

        // flush to db
        strategy.flush();

        // query for object
        WeblogEntryComment commentTest = weblogEntryManager.getComment(testComment.getId());

        if (commentTest == null) {
            throw new IllegalStateException("error setting up comment");
        }

        return commentTest;
    }

    protected void teardownComment(String id) throws Exception {
        WeblogEntryComment comment = weblogEntryManager.getComment(id);
        weblogEntryManager.removeComment(comment);
        strategy.flush();
    }

    protected Planet setupPlanet(String handle) throws Exception {
        Planet testPlanet = new Planet(handle, handle, handle);
        planetManager.savePlanet(testPlanet);
        strategy.flush();

        // query to make sure we return the persisted object
        Planet group = planetManager.getPlanetById(testPlanet.getId());
        if (group == null) {
            throw new IllegalStateException("error inserting new group");
        }
        return group;
    }

    protected void teardownPlanet(String handle) throws Exception {
        Planet planet = planetManager.getPlanet(handle);
        planetManager.deletePlanet(planet);
        strategy.flush();
    }

    protected Subscription setupSubscription(Planet planet, String feedUrl)
            throws Exception {
        // store
        Subscription testSub = new Subscription();
        testSub.setFeedURL(feedUrl);
        testSub.setTitle(feedUrl);
        testSub.setPlanet(planet);
        planetManager.saveSubscription(testSub);

        planet.getSubscriptions().add(testSub);

        // flush
        strategy.flush();

        // query to make sure we return the persisted object
        Subscription sub = planetManager.getSubscriptionById(testSub.getId());

        if (sub == null) {
            throw new IllegalStateException("error inserting new subscription");
        }
        return sub;
    }

    protected void teardownSubscription(String id) throws Exception {
        Subscription sub = planetManager.getSubscriptionById(id);
        planetManager.deleteSubscription(sub);
        strategy.flush();
    }

    protected SubscriptionEntry setupEntry(Subscription sub, String title)
            throws Exception {
        // make sure we are using a persistent object
        Subscription testSub = planetManager.getSubscriptionById(sub.getId());

        // store
        SubscriptionEntry testEntry = new SubscriptionEntry();
        testEntry.setPermalink(title);
        testEntry.setUri(title);
        testEntry.setTitle(title);
        Timestamp testTS = new java.sql.Timestamp(System.currentTimeMillis());
        testEntry.setPubTime(testTS);
        testEntry.setSubscription(testSub);
        testEntry.setUploaded(testTS);
        testSub.getEntries().add(testEntry);
        planetManager.saveEntry(testEntry);

        // flush
        strategy.flush();

        // query to make sure we return the persisted object
        SubscriptionEntry entry = planetManager.getEntryById(testEntry.getId());

        if (entry == null) {
            throw new IllegalStateException("error inserting new entry");
        }
        return entry;
    }
}
