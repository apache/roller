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
package org.tightblog;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.WebloggerContext;
import org.tightblog.pojos.GlobalRole;
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserStatus;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WebloggerProperties;
import org.junit.Before;
import org.tightblog.repository.BlogrollLinkRepository;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public abstract class WebloggerTest {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    protected BlogrollLinkRepository blogrollLinkRepository;

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
    protected JPAPersistenceStrategy strategy;

    public void setJPAPersistenceStrategy(JPAPersistenceStrategy jpaStrategy) {
        this.strategy = jpaStrategy;
    }

    @Resource
    protected URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    @Before
    public void setUp() throws Exception {
        if (!WebloggerContext.isBootstrapped()) {
            WebloggerContext.bootstrap(appContext);
        }
    }

    /**
     * Convenience method that returns managed copy of given user.
     */
    protected User getManagedUser(User user) {
        return userManager.getEnabledUserByUserName(user.getUserName());
    }

    /**
     * Convenience method that returns managed copy of given WeblogEntry.
     */
    protected WeblogEntry getManagedWeblogEntry(WeblogEntry weblogEntry) {
        return weblogEntryManager.getWeblogEntry(weblogEntry.getId(), false);
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
        testUser.setUserName(userName.toLowerCase());
        testUser.setGlobalRole(GlobalRole.BLOGGER);
        testUser.setScreenName(userName);
        testUser.setEmailAddress("TestUser@dev.null");
        testUser.setDateCreated(Instant.now());
        testUser.setStatus(UserStatus.ENABLED);
        userManager.saveUser(testUser);

        // query for the user to make sure we return the persisted object
        User user = userManager.getEnabledUserByUserName(userName.toLowerCase());

        if (user == null) {
            throw new IllegalStateException("error inserting new user");
        }

        return user;
    }

    protected void teardownUser(String userId) throws Exception {
        User user = userManager.getUser(userId);
        userManager.removeUser(user);
        strategy.flush();
    }

    protected Weblog setupWeblog(String handle, User creator)
            throws Exception {

        Instant now = Instant.now();
        Weblog testWeblog = new Weblog();
        testWeblog.setName("Test Weblog");
        testWeblog.setTagline("Test Weblog");
        testWeblog.setHandle(handle.toLowerCase());
        testWeblog.setEditFormat(Weblog.EditFormat.HTML);
        testWeblog.setBlacklist("");
        testWeblog.setTheme("basic");
        testWeblog.setLocale("en_US");
        testWeblog.setTimeZone("America/Los_Angeles");
        testWeblog.setDateCreated(now);
        testWeblog.setCreator(creator);
        testWeblog.setLastModified(now);

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
        WebloggerProperties props = strategy.getWebloggerProperties();
        if (props.getMainBlog() != null && id.equals(props.getMainBlog().getId())) {
            props.setMainBlog(null);
        }
        Weblog weblog = weblogManager.getWeblog(id);
        weblogManager.removeWeblog(weblog);
    }

    protected WeblogEntry setupWeblogEntry(String anchor, Weblog weblog, User user) throws Exception {
        return setupWeblogEntry(anchor, weblog.getWeblogCategories()
                        .iterator().next(), WeblogEntry.PubStatus.PUBLISHED, weblog, user);
    }

    protected WeblogEntry setupWeblogEntry(String anchor, WeblogCategory cat,
                                           WeblogEntry.PubStatus status, Weblog weblog, User user)
            throws Exception {

        WeblogEntry testEntry = new WeblogEntry();
        testEntry.setTitle(anchor);
        testEntry.setText("blah blah entry");
        testEntry.setEditFormat(Weblog.EditFormat.HTML);
        testEntry.setAnchor(anchor);
        testEntry.setPubTime(Instant.now());
        testEntry.setUpdateTime(Instant.now());
        testEntry.setStatus(status);
        testEntry.setWeblog(getManagedWeblog(weblog));
        testEntry.setCreator(user);
        testEntry.setCategory(cat);

        // store entry
        weblogEntryManager.saveWeblogEntry(testEntry);

        // flush to db
        strategy.flush();

        // query for object
        WeblogEntry entry = weblogEntryManager.getWeblogEntry(testEntry.getId(), false);

        if (entry == null) {
            throw new IllegalStateException("error setting up weblog entry");
        }

        return entry;
    }

    protected void teardownWeblogEntry(String id) throws Exception {
        WeblogEntry entry = weblogEntryManager.getWeblogEntry(id, false);
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
        testComment.setPostTime(Instant.now());
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

    public static WeblogEntry genWeblogEntry(String anchor, Instant pubTime,
                                             Weblog weblog) {
        WeblogEntry entry = new WeblogEntry();
        entry.setAnchor(anchor);
        entry.setWeblog(weblog == null ? new Weblog() : weblog);
        entry.setPubTime(pubTime == null ? Instant.now().minus(2, ChronoUnit.DAYS) : pubTime);
        return entry;
    }

    /**
     * Some tests are expected to log an exception while running, this notice
     * helps to separate expected exceptions vs. non-expected ones in the logging
     * @param log - logger to log to
     * @param exceptionType - name of exception (IllegalArgumentException, FileNotFoundException, etc.)
     */
    public static void logExpectedException(Logger log, String exceptionType) {
        log.info("Test is expected to log a/an {}:", exceptionType);
    }

}
