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
package org.tightblog.service;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.tightblog.WebloggerTest;
import org.tightblog.domain.User;
import org.tightblog.domain.UserWeblogRole;
import org.tightblog.domain.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tightblog.domain.WeblogBookmark;
import org.tightblog.domain.WeblogRole;

import static org.junit.Assert.*;

/**
 * Test Weblog related business operations.
 */
public class WeblogManagerIT extends WebloggerTest {
    private User testUser;
    private Weblog testWeblog;

    /**
     * All tests in this suite require a user.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("weblogTestUser");
        testWeblog = setupWeblog("testweblogmanager", testUser);
    }

    @After
    public void tearDown() {
        weblogManager.removeWeblog(testWeblog);
        userManager.removeUser(testUser);
    }

    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testWeblogCRUD() {
        Weblog weblog;

        Weblog testWeblogA = new Weblog();
        testWeblogA.setName("Test Weblog");
        testWeblogA.setTagline("Test Weblog");
        testWeblogA.setHandle("testweblog");
        testWeblogA.setEditFormat(Weblog.EditFormat.HTML);
        testWeblogA.setBlacklist("");
        testWeblogA.setTheme("basic");
        testWeblogA.setLocale("en_US");
        testWeblogA.setTimeZone("America/Los_Angeles");
        testWeblogA.setDateCreated(Instant.now());
        testWeblogA.setCreator(testUser);

        // make sure test weblog does not exist
        weblog = weblogDao.findByHandleAndVisibleTrue(testWeblogA.getHandle());
        assertNull(weblog);

        // add test weblog
        weblogManager.addWeblog(testWeblogA);
        String id = testWeblogA.getId();

        // make sure test weblog exists
        weblog = weblogDao.findById(id).orElse(null);
        assertNotNull(weblog);
        assertEquals(testWeblogA, weblog);

        // modify weblog and save
        weblog.setName("testtesttest");
        weblogManager.saveWeblog(weblog, true);

        // make sure changes were saved
        weblog = weblogDao.findById(id).orElse(null);
        assertNotNull(weblog);
        assertEquals("testtesttest", weblog.getName());

        // remove test weblog
        weblogManager.removeWeblog(weblog);

        // make sure weblog no longer exists
        weblog = weblogDao.findById(id).orElse(null);
        assertNull(weblog);
    }
    
    /**
     * Test lookup mechanisms.
     */
    @Test
    public void testWeblogLookups() {
        Weblog testWeblog1 = null;
        Weblog testWeblog2 = null;
        try {
            Weblog weblog;

            // start with no permissions
            userWeblogRoleDao.deleteByUser(testUser);

            List<UserWeblogRole> userRoles = userWeblogRoleDao.findByUser(testUser);
            assertEquals(0, userRoles.size());

            // add test weblogs
            testWeblog1 = setupWeblog("test-weblog1", testUser);
            testWeblog2 = setupWeblog("test-weblog2", testUser);

            // lookup by id
            weblog = weblogDao.findById(testWeblog1.getId()).orElse(null);
            assertNotNull(weblog);
            assertEquals(testWeblog1.getHandle(), weblog.getHandle());
            
            // lookup by weblog handle
            weblog = weblogDao.findByHandleAndVisibleTrue(testWeblog1.getHandle());
            assertNotNull(weblog);
            assertEquals(testWeblog1.getHandle(), weblog.getHandle());
            
            // make sure disabled weblogs are not returned
            weblog.setVisible(Boolean.FALSE);
            weblogManager.saveWeblog(weblog, true);
            weblog = weblogDao.findByHandleAndVisibleTrue(testWeblog1.getHandle());
            assertNull(weblog);
            
            // restore visible state
            weblog = weblogDao.findByHandle(testWeblog1.getHandle());
            weblog.setVisible(Boolean.TRUE);
            weblogManager.saveWeblog(weblog, true);
            weblog = weblogDao.findByHandleAndVisibleTrue(testWeblog1.getHandle());
            assertNotNull(weblog);
            
            userManager.grantWeblogRole(testUser, testWeblog1, WeblogRole.EDIT_DRAFT);

            // get all weblogs for user
            userRoles = userWeblogRoleDao.findByUser(testUser);
            assertEquals(2, userRoles.size());

        } finally {
            if (testWeblog1 != null) {
                weblogManager.removeWeblog(testWeblog1);
            }
            if (testWeblog2 != null) {
                weblogManager.removeWeblog(testWeblog2);
            }
        }
    }

    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testHitCountCRUD() {
        Weblog aWeblog = weblogDao.findById(testWeblog.getId()).orElse(null);
        int oldHits = aWeblog.getHitsToday();
        aWeblog.setHitsToday(oldHits + 10);
        weblogManager.saveWeblog(aWeblog, true);

        // make sure it was stored
        aWeblog = weblogDao.findById(testWeblog.getId()).orElse(null);
        assertNotNull(aWeblog);
        assertEquals(aWeblog, testWeblog);
        assertEquals(oldHits + 10, aWeblog.getHitsToday());
    }

    @Test
    public void testIncrementHitCount() {
        Weblog aWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        aWeblog.setHitsToday(10);
        weblogManager.saveWeblog(aWeblog, true);

        // make sure it was created
        aWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        assertNotNull(aWeblog);
        assertEquals(10, aWeblog.getHitsToday());

        // increment
        for (int i = 0; i < 5; i++) {
            weblogManager.incrementHitCount(aWeblog);
        }
        weblogManager.updateHitCounters();

        // make sure it was incremented properly
        aWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        assertEquals(15, aWeblog.getHitsToday());
    }

    @Test
    public void testResetHitCounts() {
        Weblog blog1 = setupWeblog("hit-cnt-test1", testUser);
        Weblog blog2 = setupWeblog("hit-cnt-test2", testUser);

        blog1.setHitsToday(10);
        blog2.setHitsToday(20);

        weblogManager.saveWeblog(blog1, true);
        weblogManager.saveWeblog(blog2, true);

        try {
            // make sure data was properly initialized
            assertEquals(10, blog1.getHitsToday());
            assertEquals(20, blog2.getHitsToday());

            // reset all counts
            weblogDao.resetDailyHitCounts();

            blog1 = weblogDao.findByIdOrNull(blog1.getId());
            blog2 = weblogDao.findByIdOrNull(blog2.getId());

            // make sure it reset all counts
            assertEquals(0, blog1.getHitsToday());
            assertEquals(0, blog2.getHitsToday());

        } finally {
            weblogManager.removeWeblog(blog1);
            weblogManager.removeWeblog(blog2);
        }
    }

    @Test
    public void testBookmarkCRUD() {
        // Add bookmark
        WeblogBookmark bookmark1 = new WeblogBookmark(
                testWeblog,
                "TestBookmark1",
                "http://www.example1.com", "created by testBookmarkCRUD()"
        );
        testWeblog.addBookmark(bookmark1);
        bookmark1.calculatePosition();

        // Add another bookmark
        WeblogBookmark bookmark2 = new WeblogBookmark(
                testWeblog,
                "TestBookmark2",
                "http://www.example2.com", "created by testBookmarkCRUD()"
        );
        testWeblog.addBookmark(bookmark2);
        bookmark2.calculatePosition();
        weblogManager.saveWeblog(testWeblog, true);

        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        WeblogBookmark bookmarka;
        WeblogBookmark bookmarkb;

        // See that two bookmarks were stored
        List<WeblogBookmark> bookmarks = testWeblog.getBookmarks();
        assertEquals(2, bookmarks.size());
        Iterator<WeblogBookmark> iter = bookmarks.iterator();
        bookmarka = iter.next();
        bookmarkb = iter.next();

        // Remove one bookmark directly
        testWeblog.getBookmarks().remove(bookmarka);
        weblogManager.saveWeblog(testWeblog, true);
        assertFalse(blogrollLinkDao.findById(bookmarka.getId()).isPresent());

        // Weblog should now contain one bookmark
        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        assertNotNull(testWeblog);
        assertEquals(1, testWeblog.getBookmarks().size());
        assertEquals(bookmarkb.getId(), testWeblog.getBookmarks().get(0).getId());

        // Remove other bookmark via removing from weblog
        testWeblog.getBookmarks().remove(bookmarkb);
        weblogManager.saveWeblog(testWeblog, true);

        // Last bookmark should be gone
        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        assertEquals(0, testWeblog.getBookmarks().size());
        assertFalse(blogrollLinkDao.findById(bookmarkb.getId()).isPresent());
    }

    /**
     * Test all bookmark lookup methods.
     */
    @Test
    public void testBookmarkLookups() {
        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());

        // add some bookmarks
        WeblogBookmark b1 = new WeblogBookmark(testWeblog, "b1", "http://example1.com", "testbookmark13");
        testWeblog.addBookmark(b1);
        WeblogBookmark b2 = new WeblogBookmark(testWeblog, "b2", "http://example2.com", "testbookmark14");
        testWeblog.addBookmark(b2);
        WeblogBookmark b3 = new WeblogBookmark(testWeblog, "b3", "http://example3.com", "testbookmark16");
        testWeblog.addBookmark(b3);

        weblogManager.saveWeblog(testWeblog, true);

        // test lookup by id
        Optional<WeblogBookmark> testBookmark = blogrollLinkDao.findById(b1.getId());
        assertTrue(testBookmark.isPresent());
        assertEquals("b1", testBookmark.get().getName());

        // test lookup of all bookmarks for a website
        Weblog testWeblog2 = weblogDao.findById(testWeblog.getId()).orElse(null);
        List<WeblogBookmark> allBookmarks = testWeblog2.getBookmarks();
        assertNotNull(allBookmarks);
        assertEquals(3, allBookmarks.size());
    }
}
