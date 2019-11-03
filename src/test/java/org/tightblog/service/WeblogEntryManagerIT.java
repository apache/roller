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
package org.tightblog.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.tightblog.WebloggerTest;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.User;
import org.tightblog.domain.WeblogCategory;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntry.PubStatus;
import org.tightblog.domain.WeblogEntrySearchCriteria;
import org.tightblog.domain.WeblogEntryTag;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntryTagAggregate;
import org.tightblog.util.Utilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Test WeblogEntry related business operations.
 */
public class WeblogEntryManagerIT extends WebloggerTest {

    private static Logger log = LoggerFactory.getLogger(WeblogEntryManagerIT.class);
    
    User testUser;
    Weblog testWeblog;
    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("entryTestUser");
        testWeblog = setupWeblog("entry-test-weblog", testUser);
    }
    
    @After
    public void tearDown() {
        weblogManager.removeWeblog(testWeblog);
        userManager.removeUser(testUser);
    }

    @Test
    public void testWeblogEntryCRUD() {
        WeblogEntry entry;
        
        WeblogEntry testEntry = new WeblogEntry();
        testEntry.setTitle("entryTestEntry");
        testEntry.setText("blah blah entry");
        testEntry.setAnchor("testEntryAnchor");
        testEntry.setPubTime(Instant.now());
        testEntry.setUpdateTime(Instant.now());
        testEntry.setWeblog(testWeblog);
        testEntry.setCreator(testUser);
        testEntry.setStatus(PubStatus.DRAFT);

        WeblogCategory cat = weblogCategoryDao.findByWeblogAndName(testWeblog, "General");
        testEntry.setCategory(cat);
        
        // create a weblog entry
        weblogEntryManager.saveWeblogEntry(testEntry);
        String id = testEntry.getId();

        // make sure entry was created
        entry = weblogEntryDao.findByIdOrNull(id);
        assertNotNull(entry);
        assertEquals(testEntry, entry);
        
        // update a weblog entry
        entry.setTitle("testtest");
        weblogEntryManager.saveWeblogEntry(entry);

        // make sure entry was updated
        entry = weblogEntryDao.findByIdOrNull(id);
        assertNotNull(entry);
        assertEquals("testtest", entry.getTitle());
        
        // delete a weblog entry
        weblogEntryManager.removeWeblogEntry(entry);

        // make sure entry was deleted
        entry = weblogEntryDao.findByIdOrNull(id);
        assertNull(entry);
    }

    @Test
    public void testWeblogEntryLookups() {
        WeblogEntry entry;
        List entries;
        Map entryMap;

        // setup some test entries to use
        WeblogEntry entry1 = setupWeblogEntry("entry1", testWeblog, testUser);
        WeblogEntry entry2 = setupWeblogEntry("entry2", testWeblog, testUser);
        WeblogEntry entry3 = setupWeblogEntry("entry3", testWeblog, testUser);
        WeblogEntry entry4 = setupWeblogEntry("entry4", testWeblog, testUser);
        WeblogEntry entry5 = setupWeblogEntry("entry5", testWeblog, testUser);
        
        // make a couple changes
        entry1.setStatus(PubStatus.PUBLISHED);
        entry1.setSearchDescription("sample search description");
        weblogEntryManager.saveWeblogEntry(entry1);
        
        entry2.setStatus(PubStatus.PUBLISHED);
        entry2.setUpdateTime(Instant.now().plus(2, ChronoUnit.HOURS));
        entry2.setPubTime(entry2.getUpdateTime());
        weblogEntryManager.saveWeblogEntry(entry2);

        entry3.setStatus(PubStatus.DRAFT);
        entry3.setUpdateTime(Instant.now().plus(1, ChronoUnit.DAYS));
        entry3.setPubTime(entry3.getUpdateTime());
        weblogEntryManager.saveWeblogEntry(entry3);
        
        entry4.setPubTime(Instant.now().minus(1, ChronoUnit.DAYS));
        entry5.setPubTime(Instant.now().minus(2, ChronoUnit.HOURS));

        weblogEntryManager.saveWeblogEntry(entry4);
        weblogEntryManager.saveWeblogEntry(entry5);

        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());

        entry1 = weblogEntryDao.findByIdOrNull(entry1.getId());
        entry2 = weblogEntryDao.findByIdOrNull(entry2.getId());
        entry3 = weblogEntryDao.findByIdOrNull(entry3.getId());
        entry4 = weblogEntryDao.findByIdOrNull(entry4.getId());
        entry5 = weblogEntryDao.findByIdOrNull(entry5.getId());
        
        // get entry by id
        entry = weblogEntryDao.findByIdOrNull(entry1.getId());
        assertNotNull(entry);
        assertEquals(entry1.getAnchor(), entry.getAnchor());
        assertEquals(entry1.getSearchDescription(), "sample search description");
        
        // get entry by anchor
        entry = weblogEntryDao.findByWeblogAndAnchor(testWeblog, entry1.getAnchor());
        assertNotNull(entry);
        assertEquals(entry1.getTitle(), entry.getTitle());
        
        // get all entries for weblog
        entries = weblogEntryDao.findByWeblog(testWeblog);
        assertNotNull(entries);
        assertEquals(5, entries.size());

        // get all (non-future) PUBLISHED entries in category 
        WeblogEntrySearchCriteria wesc9 = new WeblogEntrySearchCriteria();
        wesc9.setWeblog(testWeblog);
        wesc9.setCategoryName("General");
        wesc9.setStatus(PubStatus.PUBLISHED);
        entries = weblogEntryManager.getWeblogEntries(wesc9);
        assertNotNull(entries);
        assertEquals(3, entries.size());
        
        // get all (non-future) PUBLISHED entries only 
        WeblogEntrySearchCriteria wesc2 = new WeblogEntrySearchCriteria();
        wesc2.setWeblog(testWeblog);
        wesc2.setStatus(PubStatus.PUBLISHED);
        entries = weblogEntryManager.getWeblogEntries(wesc2);
        assertNotNull(entries);
        assertEquals(3, entries.size());
        
        // get all entries in date range
        WeblogEntrySearchCriteria wesc3 = new WeblogEntrySearchCriteria();
        wesc3.setWeblog(testWeblog);
        wesc3.setStartDate(entry2.getPubTime().minus(5, ChronoUnit.MINUTES));
        wesc3.setEndDate(entry2.getPubTime().plus(5, ChronoUnit.MINUTES));
        entries = weblogEntryManager.getWeblogEntries(wesc3);
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry2, entries.get(0));
        
        // get all entries, limited to maxSize
        WeblogEntrySearchCriteria wesc4 = new WeblogEntrySearchCriteria();
        wesc4.setWeblog(testWeblog);
        wesc4.setMaxResults(2);
        entries = weblogEntryManager.getWeblogEntries(wesc4);
        assertNotNull(entries);
        assertEquals(2, entries.size());
        
        // get all entries in category
        WeblogEntrySearchCriteria wesc5 = new WeblogEntrySearchCriteria();
        wesc5.setWeblog(testWeblog);
        wesc5.setCategoryName("General");
        entries = weblogEntryManager.getWeblogEntries(wesc5);
        assertNotNull(entries);
        assertEquals(5, entries.size());
        
        // get all entries, limited by offset/range
        WeblogEntrySearchCriteria wesc6 = new WeblogEntrySearchCriteria();
        wesc6.setWeblog(testWeblog);
        wesc6.setOffset(1);
        wesc6.setMaxResults(1);
        entries = weblogEntryManager.getWeblogEntries(wesc6);
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry2, entries.get(0));

        // get next entry
        entry = weblogEntryManager.getNextPublishedEntry(entry4);
        assertNotNull(entry);
        assertEquals(entry5, entry);
        
        // get previous entry
        entry = weblogEntryManager.getPreviousPublishedEntry(entry5);
        assertNotNull(entry);
        assertEquals(entry4, entry);
        
        // get object map
        WeblogEntrySearchCriteria wesc8 = new WeblogEntrySearchCriteria();
        wesc8.setWeblog(testWeblog);
        entryMap = weblogEntryManager.getDateToWeblogEntryMap(wesc8);
        assertNotNull(entryMap);
        assertTrue(entryMap.keySet().size() > 1);

        weblogEntryManager.removeWeblogEntry(entry1);
        weblogEntryManager.removeWeblogEntry(entry2);
        weblogEntryManager.removeWeblogEntry(entry3);
    }

    @Test
    public void testCreateAnchor() {
        WeblogEntry entry1 = setupWeblogEntry("entry1", testWeblog, testUser);

        // make sure createAnchor gives us a new anchor value
        entry1 = weblogEntryDao.findByIdOrNull(entry1.getId());
        String anchor = weblogEntryManager.createAnchor(entry1);
        assertNotNull(anchor);
        assertNotSame("entry1", anchor);
        
        // make sure we can create a new entry with specified anchor
        WeblogEntry entry2 = setupWeblogEntry(anchor, testWeblog, testUser);
        assertNotNull(entry2);

        weblogEntryManager.removeWeblogEntry(entry1);
        weblogEntryManager.removeWeblogEntry(entry2);
    }

    @Test
    public void testCreateAnEntryWithTagsShortcut() {
        try {
            WeblogEntry entry;
            WeblogEntry testEntry = new WeblogEntry();
            testEntry.setTitle("entryTestEntry");
            testEntry.setText("blah blah entry");
            testEntry.setAnchor("testEntryAnchor");
            testEntry.setStatus(PubStatus.PUBLISHED);
            testEntry.setPubTime(Instant.now());
            testEntry.setUpdateTime(Instant.now());
            testEntry.setWeblog(testWeblog);
            testEntry.setCreator(testUser);
            testEntry.setCategory(weblogCategoryDao.findByWeblogAndName(testWeblog, "General"));

            // shortcut
            addTag(testEntry, "testTag");

            // create a weblog entry
            weblogEntryManager.saveWeblogEntry(testEntry);
            String id = testEntry.getId();

            // make sure entry was created
            entry = weblogEntryDao.findByIdOrNull(id);
            assertNotNull(entry);
            assertEquals(testEntry, entry);
            assertNotNull(entry.getTags());
            assertEquals(1, entry.getTags().size());
            assertEquals("testtag", (entry.getTags()
                    .iterator().next()).getName());

            weblogEntryManager.removeWeblogEntry(testEntry);
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw); 
            t.printStackTrace(pw);
            log.info(sw.toString());
        }
    }

    @Test
    public void testAddMultipleTags() {
        // setup some test entries to use
        WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
        addTag(entry, "testTag");
        addTag(entry, "whateverTag");
        String id = entry.getId();
        weblogEntryManager.saveWeblogEntry(entry);

        entry = weblogEntryDao.findByIdOrNull(id);
        addTag(entry, "testTag2");
        weblogEntryManager.saveWeblogEntry(entry);

        entry = weblogEntryDao.findByIdOrNull(id);
        assertEquals(3, entry.getTags().size());

        weblogEntryManager.removeWeblogEntry(entry);
    }

    @Test
    public void testAddMultipleIdenticalTags() {
        // setup some test entries to use
        WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
        addTag(entry, "testTag");
        String id = entry.getId();
        weblogEntryManager.saveWeblogEntry(entry);

        entry = weblogEntryDao.findByIdOrNull(id);
        addTag(entry, "testTag");
        weblogEntryManager.saveWeblogEntry(entry);

        entry = weblogEntryDao.findByIdOrNull(id);
        assertEquals(1, entry.getTags().size());

        weblogEntryManager.removeWeblogEntry(entry);
    }

    @Test
    public void testRemoveTagsViaShortcut() {
        try {
            // setup some test entries to use
            WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
            addTag(entry, "testTag");
            addTag(entry, "testTag2");
            String id = entry.getId();
            weblogEntryManager.saveWeblogEntry(entry);

            entry = weblogEntryDao.findByIdOrNull(id);
            assertEquals(2, entry.getTags().size());

            entry = weblogEntryDao.findByIdOrNull(id);
            entry.setTags(Collections.emptySet());
            weblogEntryManager.saveWeblogEntry(entry);

            entry = weblogEntryDao.findByIdOrNull(id);
            assertEquals(0, entry.getTags().size());
            weblogEntryManager.removeWeblogEntry(entry);
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw); 
            t.printStackTrace(pw);
            log.info(sw.toString());
        }
    }

    @Test
    public void testGetEntriesByTag() {
        try {
            // setup some test entries to use
            WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
            addTag(entry, "testTag");
            weblogEntryManager.saveWeblogEntry(entry);

            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(testWeblog);
            // tags are always saved lowercase (testTag -> testtag)
            wesc.setTag("testtag");
            List results = weblogEntryManager.getWeblogEntries(wesc);
            assertEquals(1, results.size());
            WeblogEntry testEntry = (WeblogEntry) results.iterator().next();
            assertEquals(entry, testEntry);
            weblogEntryManager.removeWeblogEntry(entry);
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw); 
            t.printStackTrace(pw);
            log.info(sw.toString());
        }
    }

    @Test
    public void testRemoveEntryTagCascading() {
        // setup some test entries to use
        WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
        addTag(entry, "testTag");
        weblogEntryManager.saveWeblogEntry(entry);

        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(testWeblog);
        // tags are always saved lowercase (testTag -> testtag)
        wesc.setTag("testtag");
        List results = weblogEntryManager.getWeblogEntries(wesc);
        assertEquals(1, results.size());
        WeblogEntry testEntry = (WeblogEntry) results.iterator().next();
        assertEquals(entry, testEntry);

        weblogEntryManager.removeWeblogEntry(entry);
        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        results = weblogEntryManager.getWeblogEntries(wesc);
        assertEquals(0, results.size());
    }

    @Test
    public void testUpdateTags() {
        // setup some test entries to use
        WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
        addTag(entry, "testWillStayTag");
        addTag(entry, "testTagWillBeRemoved");
        String id = entry.getId();
        weblogEntryManager.saveWeblogEntry(entry);

        entry = weblogEntryDao.findByIdOrNull(id);
        assertEquals(2, entry.getTags().size());

        entry.updateTags(new HashSet<>(Arrays.asList("testwillstaytag testnewtag testnewtag3".split("\\s+"))));
        weblogEntryManager.saveWeblogEntry(entry);

        entry = weblogEntryDao.findByIdOrNull(id);
        Set<String> tagNames = entry.getTags().stream()
                .map(WeblogEntryTag::getName)
                .collect(Collectors.toCollection(HashSet::new));

        assertEquals(3, entry.getTags().size());
        assertEquals(3, tagNames.size());
        assertTrue(tagNames.contains("testwillstaytag"));
        assertTrue(tagNames.contains("testnewtag"));
        assertTrue(tagNames.contains("testnewtag3"));
        weblogEntryManager.removeWeblogEntry(entry);
    }

    @Test
    public void testTagAggregates() throws Exception {
        Weblog testWeblog2 = setupWeblog("entry-test-weblog2", testUser);

        try {
            // let's make sure we are starting from scratch

            // site-wide
            List<WeblogEntryTagAggregate> tags = weblogManager.getTags(null, null, null, 0, -1);
            assertEquals(0, tags.size());

            // first weblog
            tags = weblogManager.getTags(testWeblog, null, null, 0, -1);
            assertEquals(0, tags.size());

            // second weblog
            tags = weblogManager.getTags(testWeblog2, null, null, 0, -1);
            assertEquals(0, tags.size());

            // setup some test entries to use
            WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
            addTag(entry, "one");
            addTag(entry, "two");
            weblogEntryManager.saveWeblogEntry(entry);

            entry = setupWeblogEntry("entry2", testWeblog, testUser);
            addTag(entry, "one");
            addTag(entry, "two");
            addTag(entry, "three");
            weblogEntryManager.saveWeblogEntry(entry);

            tags = weblogManager.getTags(testWeblog, null, null, 0, -1);
            assertEquals(3, tags.size());

            Map<String, Integer> expectedWeblogTags = new HashMap<>();
            expectedWeblogTags.put("one", 2);
            expectedWeblogTags.put("two", 2);
            expectedWeblogTags.put("three", 1);

            for (WeblogEntryTagAggregate stat : tags) {
                if (!expectedWeblogTags.containsKey(stat.getName())) {
                    fail("Unexpected tagName.");
                }
                Integer expectedCount = expectedWeblogTags.get(stat.getName());
                assertEquals(expectedCount.intValue(), stat.getTotal());
            }

            // now add another entry in another blog
            testWeblog2 = weblogDao.findByIdOrNull(testWeblog2.getId());
            entry = setupWeblogEntry("entry3", testWeblog2, testUser);
            addTag(entry, "one");
            addTag(entry, "three");
            addTag(entry, "four");
            weblogEntryManager.saveWeblogEntry(entry);

            // let's fetch "site" tags now
            tags = weblogManager.getTags(null, null, null, 0, -1);
            assertEquals(4, tags.size());

            Map<String, Integer> expectedSiteTags = new HashMap<>();
            expectedSiteTags.put("one", 3);
            expectedSiteTags.put("two", 2);
            expectedSiteTags.put("three", 2);
            expectedSiteTags.put("four", 1);

            for (WeblogEntryTagAggregate stat : tags) {
                if (!expectedSiteTags.containsKey(stat.getName())) {
                    fail("Unexpected tagName.");
                }
                Integer expectedCount = expectedSiteTags.get(stat.getName());
                assertEquals(expectedCount.intValue(), stat.getTotal());
            }

            testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
            entry = weblogEntryManager.getWeblogEntryByAnchor(testWeblog, "entry2");
            entry.updateTags(new HashSet<>(Arrays.asList("one three five".split("\\s+"))));
            weblogEntryManager.saveWeblogEntry(entry);

            tags = weblogManager.getTags(testWeblog, null, null, 0, -1);
            assertEquals(4, tags.size());

            expectedWeblogTags = new HashMap<>();
            expectedWeblogTags.put("one", 2);
            expectedWeblogTags.put("two", 1);
            expectedWeblogTags.put("three", 1);
            expectedWeblogTags.put("five", 1);

            for (WeblogEntryTagAggregate stat : tags) {
                if (!expectedWeblogTags.containsKey(stat.getName())) {
                    fail("Unexpected tagName.");
                }
                Integer expectedCount =
                        expectedWeblogTags.get(stat.getName());
                assertEquals(stat.getName(),
                        expectedCount.intValue(), stat.getTotal());
            }

            tags = weblogManager.getTags(null, null, null, 0, -1);
            assertEquals(5, tags.size());

            expectedSiteTags = new HashMap<>();
            expectedSiteTags.put("one", 3);
            expectedSiteTags.put("two", 1);
            expectedSiteTags.put("three", 2);
            expectedSiteTags.put("four", 1);
            expectedSiteTags.put("five", 1);

            for (WeblogEntryTagAggregate stat : tags) {
                if (!expectedSiteTags.containsKey(stat.getName())) {
                    fail("Unexpected tagName.");
                }
                Integer expectedCount = expectedSiteTags.get(stat.getName());
                assertEquals(stat.getName(), expectedCount.intValue(), stat.getTotal());
            }

            weblogManager.removeWeblog(testWeblog2);
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw); 
            t.printStackTrace(pw);
            log.error(sw.toString());
        }
    }

    @Test
    public void testTagAggregatesCaseSensitivity() throws Exception {
        Weblog testWeblog2 = setupWeblog("entry-test-weblog2", testUser);

        // let's make sure we are starting from scratch

        // site-wide
        List<WeblogEntryTagAggregate> tags = weblogManager.getTags(null, null, null, 0, -1);
        assertEquals(0, tags.size());

        // first weblog
        tags = weblogManager.getTags(testWeblog, null, null, 0, -1);
        assertEquals(0, tags.size());

        // second weblog
        tags = weblogManager.getTags(testWeblog2, null, null, 0, -1);
        assertEquals(0, tags.size());

        // setup some test entries to use
        WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
        addTag(entry, "one");
        addTag(entry, "two");
        addTag(entry, "ONE");
        weblogEntryManager.saveWeblogEntry(entry);

        tags = weblogManager.getTags(testWeblog, null, null, 0, -1);
        assertEquals(2, tags.size());

        Map<String, Integer> expectedWeblogTags = new HashMap<>();
        expectedWeblogTags.put("one", 1);
        expectedWeblogTags.put("two", 1);

        for (WeblogEntryTagAggregate stat : tags) {
            if (!expectedWeblogTags.containsKey(stat.getName())) {
                fail("Unexpected tagName.");
            }
            Integer expectedCount = expectedWeblogTags.get(stat.getName());
            assertEquals(expectedCount.intValue(), stat.getTotal());
        }

        // now add another entry in another blog
        entry = setupWeblogEntry("entry3", testWeblog2, testUser);
        addTag(entry, "ONE");
        addTag(entry, "three");
        weblogEntryManager.saveWeblogEntry(entry);
        
        // let's fetch "site" tags now
        tags = weblogManager.getTags(null, null, null, 0, -1);
        assertEquals(3, tags.size());

        Map<String, Integer> expectedSiteTags = new HashMap<>();
        expectedSiteTags.put("one", 2);
        expectedSiteTags.put("two", 1);
        expectedSiteTags.put("three", 1);

        for (WeblogEntryTagAggregate stat : tags) {
            if (!expectedSiteTags.containsKey(stat.getName())) {
                fail("Unexpected tagName.");
            }
            Integer expectedCount = expectedSiteTags.get(stat.getName());
            assertEquals(expectedCount.intValue(), stat.getTotal());
        }

        weblogManager.removeWeblog(testWeblog2);
    }

    @Test
    public void testEnclosureCRUD() {
        WeblogEntry entry;
        
        WeblogEntry testEntry = new WeblogEntry();
        testEntry.setTitle("entryTestEntry");
        testEntry.setText("blah blah entry");
        testEntry.setAnchor("testEntryAnchor");
        testEntry.setPubTime(Instant.now());
        testEntry.setUpdateTime(Instant.now());
        testEntry.setWeblog(testWeblog);
        testEntry.setStatus(PubStatus.DRAFT);
        testEntry.setCreator(testUser);

        WeblogCategory cat = weblogCategoryDao.findByWeblogAndName(testWeblog, "General");
        testEntry.setCategory(cat);
        
        // create a weblog entry
        weblogEntryManager.saveWeblogEntry(testEntry);
        String id = testEntry.getId();

        testEntry = weblogEntryDao.findByIdOrNull(testEntry.getId());
        testEntry.setEnclosureUrl("http://podcast-schmodcast.com");
        testEntry.setEnclosureType("application/drivel");
        testEntry.setEnclosureLength(2141592654L);
        weblogEntryManager.saveWeblogEntry(testEntry);

        // make sure entry was created
        entry = weblogEntryDao.findByIdOrNull(id);
        assertNotNull(entry);
        assertEquals(testEntry, entry);
        assertEquals(entry.getEnclosureUrl(), "http://podcast-schmodcast.com");
        assertEquals(entry.getEnclosureType(), "application/drivel");
        assertEquals(entry.getEnclosureLength(), Long.valueOf(2141592654L));
        
        // update a weblog entry
        entry.setTitle("testtest");
        weblogEntryManager.saveWeblogEntry(entry);

        // make sure entry was updated
        entry = weblogEntryDao.findByIdOrNull(id);
        assertNotNull(entry);
        assertEquals("testtest", entry.getTitle());
        
        // delete a weblog entry
        weblogEntryManager.removeWeblogEntry(entry);

        // make sure entry was deleted
        entry = weblogEntryDao.findByIdOrNull(id);
        assertNull(entry);
    }

    @Test
    public void testWeblogStats() {
        long existingUserCount = userDao.count() - 1;
        
        User user1 = setupUser("statuser1");
        Weblog blog1 = setupWeblog("statblog1", user1);
        Weblog blog2 = setupWeblog("statblog2", user1);

        Weblog blog3 = setupWeblog("statblog3", user1);
        blog3.setVisible(Boolean.FALSE);
        weblogManager.saveWeblog(blog3, true);

        WeblogEntry entry1 = setupWeblogEntry("entry1", blog1, user1);
        WeblogEntry entry2 = setupWeblogEntry("entry2", blog1, user1);
        
        WeblogEntry entry3 = setupWeblogEntry("entry3", blog2, user1);
        WeblogEntry entry4 = setupWeblogEntry("entry4", blog2, user1);
        WeblogEntry entry5 = setupWeblogEntry("entry5", blog2, user1);
               
        WeblogEntryComment comment1 = setupComment("comment1", entry1);
        WeblogEntryComment comment2 = setupComment("comment2", entry1);
        
        WeblogEntryComment comment3 = setupComment("comment3", entry3);
        WeblogEntryComment comment4 = setupComment("comment4", entry3);
        WeblogEntryComment comment5 = setupComment("comment5", entry3);

        try {
            blog1 = weblogDao.findById(blog1.getId()).orElse(null);
            blog2 = weblogDao.findById(blog2.getId()).orElse(null);

            assertEquals(2L, weblogEntryDao.countByWeblog(blog1));
            assertEquals(3, weblogEntryDao.countByWeblog(blog2));
            assertEquals(5, weblogEntryDao.count());
            assertEquals(2, weblogEntryCommentDao.countByWeblogEntryAndStatusApproved(entry1));
            assertEquals(3, weblogEntryCommentDao.countByWeblogEntryAndStatusApproved(entry3));
            assertEquals(4L, weblogDao.count());
            assertEquals(existingUserCount + 2L, userDao.count());
        } finally {
            weblogEntryManager.removeComment(comment1);
            weblogEntryManager.removeComment(comment2);
            weblogEntryManager.removeComment(comment3);
            weblogEntryManager.removeComment(comment4);
            weblogEntryManager.removeComment(comment5);

            weblogEntryManager.removeWeblogEntry(entry1);
            weblogEntryManager.removeWeblogEntry(entry2);
            weblogEntryManager.removeWeblogEntry(entry3);
            weblogEntryManager.removeWeblogEntry(entry4);
            weblogEntryManager.removeWeblogEntry(entry5);

            weblogManager.removeWeblog(blog1);
            weblogManager.removeWeblog(blog2);
            weblogManager.removeWeblog(blog3);

            userManager.removeUser(user1);
        }
    }

    private void addTag(WeblogEntry entry, String name) {
        Locale localeObject = entry.getWeblog().getLocaleInstance();
        name = Utilities.normalizeTag(name, localeObject);
        if (name.length() == 0) {
            return;
        }
        for (WeblogEntryTag tag : entry.getTags()) {
            if (tag.getName().equals(name)) {
                return;
            }
        }
        WeblogEntryTag tag = new WeblogEntryTag();
        tag.setName(name);
        tag.setWeblog(entry.getWeblog());
        tag.setWeblogEntry(entry);
        entry.getTags().add(tag);
    }

}
