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
package org.tightblog.business;

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
import org.tightblog.pojos.CommentSearchCriteria;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.User;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntry.PubStatus;
import org.tightblog.pojos.WeblogEntrySearchCriteria;
import org.tightblog.pojos.WeblogEntryTag;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntryTagAggregate;
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
public class WeblogEntryTest extends WebloggerTest {

    private static Logger log = LoggerFactory.getLogger(WeblogEntryTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("entryTestUser");
        testWeblog = setupWeblog("entryTestWeblog", testUser);
        endSession(true);
    }
    
    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
        endSession(true);
    }

    @Test
    public void testWeblogEntryCRUD() throws Exception {
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

        WeblogCategory cat = weblogManager.getWeblogCategoryByName(testWeblog, "General");
        testEntry.setCategory(cat);
        
        // create a weblog entry
        weblogEntryManager.saveWeblogEntry(testEntry);
        String id = testEntry.getId();
        endSession(true);
        
        // make sure entry was created
        entry = weblogEntryManager.getWeblogEntry(id, false);
        assertNotNull(entry);
        assertEquals(testEntry, entry);
        
        // update a weblog entry
        entry.setTitle("testtest");
        weblogEntryManager.saveWeblogEntry(entry);
        endSession(true);
        
        // make sure entry was updated
        entry = weblogEntryManager.getWeblogEntry(id, false);
        assertNotNull(entry);
        assertEquals("testtest", entry.getTitle());
        
        // delete a weblog entry
        weblogEntryManager.removeWeblogEntry(entry);
        endSession(true);
        
        // make sure entry was deleted
        entry = weblogEntryManager.getWeblogEntry(id, false);
        assertNull(entry);
    }

    @Test
    public void testWeblogEntryLookups() throws Exception {
        WeblogEntry entry;
        List entries;
        Map entryMap;

        // setup some test entries to use
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
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
        entry2.setUpdateTime(Instant.now().plus(1, ChronoUnit.HOURS));
        entry2.setPubTime(entry2.getUpdateTime());
        weblogEntryManager.saveWeblogEntry(entry2);

        entry3.setStatus(PubStatus.DRAFT);
        entry3.setUpdateTime(Instant.now().plus(1, ChronoUnit.DAYS));
        entry3.setPubTime(entry3.getUpdateTime());
        weblogEntryManager.saveWeblogEntry(entry3);
        
        entry4.setPubTime(Instant.now().minus(1, ChronoUnit.DAYS));
        entry5.setPubTime(Instant.now().minus(1, ChronoUnit.HOURS));
        
        endSession(true);
        
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);

        entry1 = getManagedWeblogEntry(entry1);
        entry2 = getManagedWeblogEntry(entry2);
        entry3 = getManagedWeblogEntry(entry3);
        entry4 = getManagedWeblogEntry(entry4);
        entry5 = getManagedWeblogEntry(entry5);
        
        // get entry by id
        entry = weblogEntryManager.getWeblogEntry(entry1.getId(), false);
        assertNotNull(entry);
        assertEquals(entry1.getAnchor(), entry.getAnchor());
        assertEquals(entry1.getSearchDescription(), "sample search description");
        
        // get entry by anchor
        entry = weblogEntryManager.getWeblogEntryByAnchor(testWeblog, entry1.getAnchor());
        assertNotNull(entry);
        assertEquals(entry1.getTitle(), entry.getTitle());
        
        // get all entries for weblog
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(testWeblog);
        entries = weblogEntryManager.getWeblogEntries(wesc);
        assertNotNull(entries);
        assertEquals(5, entries.size());
        assertEquals(entry3, entries.get(0));
        
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
        wesc3.setStartDate(entry2.getPubTime());
        wesc3.setEndDate(entry2.getPubTime());
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
        entry = weblogEntryManager.getNextEntry(entry4, null);
        assertNotNull(entry);
        assertEquals(entry5, entry);
        
        // get previous entry
        entry = weblogEntryManager.getPreviousEntry(entry5, null);
        assertNotNull(entry);
        assertEquals(entry4, entry);
        
        // get object map
        WeblogEntrySearchCriteria wesc8 = new WeblogEntrySearchCriteria();
        wesc8.setWeblog(testWeblog);
        entryMap = weblogEntryManager.getDateToWeblogEntryMap(wesc8);
        assertNotNull(entryMap);
        assertTrue(entryMap.keySet().size() > 1);
        
        // teardown our test entries
        teardownWeblogEntry(entry1.getId());
        teardownWeblogEntry(entry2.getId());
        teardownWeblogEntry(entry3.getId());
        endSession(true);
    }

    @Test
    public void testCreateAnchor() throws Exception {
        // setup some test entries to use
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        WeblogEntry entry1 = setupWeblogEntry("entry1", testWeblog, testUser);
        endSession(true);
        
        // make sure createAnchor gives us a new anchor value
        entry1 = getManagedWeblogEntry(entry1);
        String anchor = weblogEntryManager.createAnchor(entry1);
        assertNotNull(anchor);
        assertNotSame("entry1", anchor);
        
        // make sure we can create a new entry with specified anchor
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        WeblogEntry entry2 = setupWeblogEntry(anchor, testWeblog, testUser);
        endSession(true);
        assertNotNull(entry2);
        
        // teardown our test entries
        teardownWeblogEntry(entry1.getId());
        teardownWeblogEntry(entry2.getId());
        endSession(true);
    }

    @Test
    public void testCreateAnEntryWithTagsShortcut() throws Exception {
        try {
            WeblogEntry entry;
            testWeblog = getManagedWeblog(testWeblog);
            testUser = getManagedUser(testUser);

            WeblogEntry testEntry = new WeblogEntry();
            testEntry.setTitle("entryTestEntry");
            testEntry.setText("blah blah entry");
            testEntry.setAnchor("testEntryAnchor");
            testEntry.setStatus(PubStatus.PUBLISHED);
            testEntry.setPubTime(Instant.now());
            testEntry.setUpdateTime(Instant.now());
            testEntry.setWeblog(testWeblog);
            testEntry.setCreator(testUser);
            testEntry.setCategory(weblogManager.getWeblogCategoryByName(testWeblog, "General"));

            // shortcut
            addTag(testEntry, "testTag");

            // create a weblog entry
            weblogEntryManager.saveWeblogEntry(testEntry);
            String id = testEntry.getId();
            endSession(true);

            // make sure entry was created
            entry = weblogEntryManager.getWeblogEntry(id, false);
            assertNotNull(entry);
            assertEquals(testEntry, entry);
            assertNotNull(entry.getTags());
            assertEquals(1, entry.getTags().size());
            assertEquals("testtag", (entry.getTags()
                    .iterator().next()).getName());
            endSession(true);

            // teardown our test entry
            teardownWeblogEntry(id);
            endSession(true);
        
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw); 
            t.printStackTrace(pw);
            log.info(sw.toString());
        }
    }

    @Test
    public void testAddMultipleTags() throws Exception {
        // setup some test entries to use
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
        addTag(entry, "testTag");
        addTag(entry, "whateverTag");
        String id = entry.getId();
        weblogEntryManager.saveWeblogEntry(entry);
        endSession(true);

        entry = weblogEntryManager.getWeblogEntry(id, false);
        addTag(entry, "testTag2");
        weblogEntryManager.saveWeblogEntry(entry);
        endSession(true);

        entry = weblogEntryManager.getWeblogEntry(id, false);
        assertEquals(3, entry.getTags().size());

        // teardown our test entry
        teardownWeblogEntry(id);
        endSession(true);
    }

    @Test
    public void testAddMultipleIdenticalTags() throws Exception {
        // setup some test entries to use
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
        addTag(entry, "testTag");
        String id = entry.getId();
        weblogEntryManager.saveWeblogEntry(entry);
        endSession(true);

        entry = weblogEntryManager.getWeblogEntry(id, false);
        addTag(entry, "testTag");
        weblogEntryManager.saveWeblogEntry(entry);
        endSession(true);

        entry = weblogEntryManager.getWeblogEntry(id, false);
        assertEquals(1, entry.getTags().size());

        // teardown our test entry
        teardownWeblogEntry(id);
        endSession(true);
    }

    @Test
    public void testRemoveTagsViaShortcut() throws Exception {
        try {
            // setup some test entries to use
            testWeblog = getManagedWeblog(testWeblog);
            testUser = getManagedUser(testUser);
            WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
            addTag(entry, "testTag");
            addTag(entry, "testTag2");
            String id = entry.getId();
            weblogEntryManager.saveWeblogEntry(entry);
            endSession(true);

            entry = weblogEntryManager.getWeblogEntry(id, false);
            assertEquals(2, entry.getTags().size());
            endSession(true);

            entry = weblogEntryManager.getWeblogEntry(id, false);
            entry.setTags(Collections.emptySet());
            weblogEntryManager.saveWeblogEntry(entry);
            endSession(true);

            entry = weblogEntryManager.getWeblogEntry(id, false);
            assertEquals(0, entry.getTags().size());
            endSession(true);

            // teardown our test entry
            teardownWeblogEntry(id);
            endSession(true);
            
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw); 
            t.printStackTrace(pw);
            log.info(sw.toString());
        }
    }

    @Test
    public void testTagsExist() throws Exception {
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        Weblog weblog = setupWeblog("tagsExistWeblog1", testUser);
        String wid = weblog.getId();
        
        // setup some test entries to use
        WeblogEntry entry = setupWeblogEntry("tagsExistEntry1", testWeblog, testUser);
        String id1 = entry.getId();
        addTag(entry, "blahTag");
        addTag(entry, "fooTag");
        weblogEntryManager.saveWeblogEntry(entry);

        WeblogEntry entry2 = setupWeblogEntry("tagsExistEntry2", weblog, testUser);
        String id2 = entry2.getId();
        addTag(entry2, "aaaTag");
        addTag(entry2, "bbbTag");
        weblogEntryManager.saveWeblogEntry(entry2);
        endSession(true);
        
        // we'll need these
        String tag1 = "nonExistTag";
        String tag2 = "blahtag";
        
        // test site-wide
        assertTrue(weblogManager.getTagExists(null, tag2));
        assertFalse(weblogManager.getTagExists(null, tag1));
        
        // test weblog specific
        testWeblog = getManagedWeblog(testWeblog);
        weblog = getManagedWeblog(weblog);
        assertTrue(weblogManager.getTagExists(testWeblog, tag2));
        assertFalse(weblogManager.getTagExists(testWeblog, tag1));
        assertFalse(weblogManager.getTagExists(weblog, tag2));
        
        // teardown our test data
        teardownWeblogEntry(id1);
        teardownWeblogEntry(id2);
        endSession(true);

        teardownWeblog(wid);
        endSession(true);
    }

    @Test
    public void testGetEntriesByTag() throws Exception {
        try {
            // setup some test entries to use
            testWeblog = getManagedWeblog(testWeblog);
            testUser = getManagedUser(testUser);
            WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
            String id = entry.getId();
            addTag(entry, "testTag");
            weblogEntryManager.saveWeblogEntry(entry);
            endSession(true);

            testWeblog = getManagedWeblog(testWeblog);

            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(testWeblog);
            // tags are always saved lowercase (testTag -> testtag)
            wesc.setTags(Collections.singleton("testtag"));
            List results = weblogEntryManager.getWeblogEntries(wesc);
            assertEquals(1, results.size());
            WeblogEntry testEntry = (WeblogEntry) results.iterator().next();
            assertEquals(entry, testEntry);
        
            // teardown our test entry
            teardownWeblogEntry(id);
            endSession(true);
            
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw); 
            t.printStackTrace(pw);
            log.info(sw.toString());
        }
    }

    @Test
    public void testRemoveEntryTagCascading() throws Exception {
        // setup some test entries to use
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
        addTag(entry, "testTag");
        String id = entry.getId();
        weblogEntryManager.saveWeblogEntry(entry);
        endSession(true);

        testWeblog = getManagedWeblog(testWeblog);
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(testWeblog);
        // tags are always saved lowercase (testTag -> testtag)
        wesc.setTags(Collections.singleton("testtag"));
        List results = weblogEntryManager.getWeblogEntries(wesc);
        assertEquals(1, results.size());
        WeblogEntry testEntry = (WeblogEntry) results.iterator().next();
        assertEquals(entry, testEntry);

        // teardown our test entry
        teardownWeblogEntry(id);
        endSession(true);

        testWeblog = getManagedWeblog(testWeblog);
        results = weblogEntryManager.getWeblogEntries(wesc);
        assertEquals(0, results.size());

        // terminate
        endSession(true);
    }

    @Test
    public void testUpdateTags() throws Exception {
        // setup some test entries to use
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        WeblogEntry entry = setupWeblogEntry("entry1", testWeblog, testUser);
        addTag(entry, "testWillStayTag");
        addTag(entry, "testTagWillBeRemoved");
        String id = entry.getId();
        weblogEntryManager.saveWeblogEntry(entry);
        endSession(true);

        entry = weblogEntryManager.getWeblogEntry(id, false);
        assertEquals(2, entry.getTags().size());

        entry.updateTags(new HashSet<>(Arrays.asList("testwillstaytag testnewtag testnewtag3".split("\\s+"))));
        weblogEntryManager.saveWeblogEntry(entry);
        endSession(true);

        entry = weblogEntryManager.getWeblogEntry(id, false);
        Set<String> tagNames = entry.getTags().stream()
                .map(WeblogEntryTag::getName)
                .collect(Collectors.toCollection(HashSet<String>::new));

        assertEquals(3, entry.getTags().size());
        assertEquals(3, tagNames.size());
        assertEquals(true, tagNames.contains("testwillstaytag"));
        assertEquals(true, tagNames.contains("testnewtag"));
        assertEquals(true, tagNames.contains("testnewtag3"));

        // teardown our test entry
        teardownWeblogEntry(id);
        endSession(true);
    }

    @Test
    public void testTagAggregates() throws Exception {
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        Weblog testWeblog2 = setupWeblog("entryTestWeblog2", testUser);

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

            endSession(true);

            testWeblog = getManagedWeblog(testWeblog);
            tags = weblogManager.getTags(testWeblog, null, null, 0, -1);
            assertEquals(3, tags.size());

            HashMap<String,Integer> expectedWeblogTags = new HashMap<>();
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
            testWeblog2 = getManagedWeblog(testWeblog2);
            testUser = getManagedUser(testUser);
            entry = setupWeblogEntry("entry3", testWeblog2, testUser);
            addTag(entry, "one");
            addTag(entry, "three");
            addTag(entry, "four");
            weblogEntryManager.saveWeblogEntry(entry);

            endSession(true);

            // let's fetch "site" tags now
            tags = weblogManager.getTags(null, null, null, 0, -1);
            assertEquals(4, tags.size());

            HashMap<String, Integer> expectedSiteTags = new HashMap<>();
            expectedSiteTags.put("one", 3);
            expectedSiteTags.put("two", 2);
            expectedSiteTags.put("three", 2);
            expectedSiteTags.put("four", 1);

            for (WeblogEntryTagAggregate stat : tags) {
                if (!expectedSiteTags.containsKey(stat.getName()))
                    fail("Unexpected tagName.");

                Integer expectedCount = expectedSiteTags.get(stat.getName());
                assertEquals(expectedCount.intValue(), stat.getTotal());
            }

            endSession(true);

            testWeblog = getManagedWeblog(testWeblog);
            entry = weblogEntryManager.getWeblogEntryByAnchor(testWeblog, "entry2");
            entry.updateTags(new HashSet<>(Arrays.asList("one three five".split("\\s+"))));
            weblogEntryManager.saveWeblogEntry(entry);

            endSession(true);

            testWeblog = getManagedWeblog(testWeblog);
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

            teardownWeblog(testWeblog2.getId());
            endSession(true);

        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw); 
            t.printStackTrace(pw);
            log.error(sw.toString());
            System.out.println(sw.toString());
        }
    }

    @Test
    public void testTagAggregatesCaseSensitivity() throws Exception {
        Weblog testWeblog2 = setupWeblog("entryTestWeblog2", testUser);
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);

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

        endSession(true);

        tags = weblogManager.getTags(testWeblog, null, null, 0, -1);
        assertEquals(2, tags.size());

        HashMap<String, Integer> expectedWeblogTags = new HashMap<>();
        expectedWeblogTags.put("one", 1);
        expectedWeblogTags.put("two", 1);

        for (WeblogEntryTagAggregate stat : tags) {
            if (!expectedWeblogTags.containsKey(stat.getName()))
                fail("Unexpected tagName.");

            Integer expectedCount = expectedWeblogTags.get(stat.getName());
            assertEquals(expectedCount.intValue(), stat.getTotal());
        }

        // now add another entry in another blog
        entry = setupWeblogEntry("entry3", testWeblog2, testUser);
        addTag(entry, "ONE");
        addTag(entry, "three");
        weblogEntryManager.saveWeblogEntry(entry);
        
        endSession(true);
        
        // let's fetch "site" tags now
        tags = weblogManager.getTags(null, null, null, 0, -1);
        assertEquals(3, tags.size());

        HashMap<String, Integer> expectedSiteTags = new HashMap<>();
        expectedSiteTags.put("one", 2);
        expectedSiteTags.put("two", 1);
        expectedSiteTags.put("three", 1);

        for (WeblogEntryTagAggregate stat : tags) {
            if (!expectedSiteTags.containsKey(stat.getName()))
                fail("Unexpected tagName.");

            Integer expectedCount = expectedSiteTags.get(stat.getName());
            assertEquals(expectedCount.intValue(), stat.getTotal());
        }

        endSession(true);

        // teardown our test blog 2
        teardownWeblog(testWeblog2.getId());
        endSession(true);
    }

    @Test
    public void testEnclosureCRUD() throws Exception {
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

        WeblogCategory cat = weblogManager.getWeblogCategoryByName(testWeblog, "General");
        testEntry.setCategory(cat);
        
        // create a weblog entry
        weblogEntryManager.saveWeblogEntry(testEntry);
        String id = testEntry.getId();
        endSession(true);

        testEntry = getManagedWeblogEntry(testEntry);
        testEntry.setEnclosureUrl("http://podcast-schmodcast.com");
        testEntry.setEnclosureType("application/drivel");
        testEntry.setEnclosureLength(2141592654L);
                    
        endSession(true);
        
        // make sure entry was created
        entry = weblogEntryManager.getWeblogEntry(id, false);
        assertNotNull(entry);
        assertEquals(testEntry, entry);
        assertEquals(entry.getEnclosureUrl(), "http://podcast-schmodcast.com");
        assertEquals(entry.getEnclosureType(), "application/drivel");
        assertEquals(entry.getEnclosureLength(), new Long(2141592654L));
        
        // update a weblog entry
        entry.setTitle("testtest");
        weblogEntryManager.saveWeblogEntry(entry);
        endSession(true);
        
        // make sure entry was updated
        entry = weblogEntryManager.getWeblogEntry(id, false);
        assertNotNull(entry);
        assertEquals("testtest", entry.getTitle());
        
        // delete a weblog entry
        weblogEntryManager.removeWeblogEntry(entry);
        endSession(true);
        
        // make sure entry was deleted
        entry = weblogEntryManager.getWeblogEntry(id, false);
        assertNull(entry);
    }

    @Test
    public void testWeblogStats() throws Exception {
        long existingUserCount = userManager.getUserCount() - 1;
        
        User user1 = setupUser("statuser1");
        Weblog blog1 = setupWeblog("statblog1", user1);
        Weblog blog2 = setupWeblog("statblog2", user1);

        Weblog blog3 = setupWeblog("statblog3", user1);
        blog3.setVisible(Boolean.FALSE);
        weblogManager.saveWeblog(blog3);

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
        endSession(true);

        try {
            blog1 = weblogManager.getWeblog(blog1.getId());
            blog2 = weblogManager.getWeblog(blog2.getId());

            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(blog1);
            assertEquals(2L, weblogEntryManager.getEntryCount(wesc));

            wesc.setWeblog(blog2);
            assertEquals(3L, weblogEntryManager.getEntryCount(wesc));

            wesc.setWeblog(null);
            assertEquals(5L, weblogEntryManager.getEntryCount(wesc));

            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
            csc.setWeblog(blog1);
            assertEquals(2L, weblogEntryManager.getCommentCount(csc));

            csc.setWeblog(blog2);
            assertEquals(3L, weblogEntryManager.getCommentCount(csc));

            csc.setWeblog(null);
            assertEquals(5L, weblogEntryManager.getCommentCount(csc));

            assertEquals(4L, weblogManager.getWeblogCount());
            assertEquals(existingUserCount + 2L, userManager.getUserCount());
            
        } finally {
            teardownComment(comment1.getId());
            teardownComment(comment2.getId());
            teardownComment(comment3.getId());
            teardownComment(comment4.getId());
            teardownComment(comment5.getId());

            teardownWeblogEntry(entry1.getId());
            teardownWeblogEntry(entry2.getId());
            teardownWeblogEntry(entry3.getId());
            teardownWeblogEntry(entry4.getId());
            teardownWeblogEntry(entry5.getId());

            teardownWeblog(blog1.getId());
            teardownWeblog(blog2.getId());
            teardownWeblog(blog3.getId());

            teardownUser(user1.getId());
            
            endSession(true);
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
