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

package org.apache.roller.business;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.TestUtils;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.TagStat;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogEntryTagData;
import org.apache.roller.pojos.WebsiteData;


/**
 * Test WeblogEntry related business operations.
 */
public class WeblogEntryTest extends TestCase {
    
    public static Log log = LogFactory.getLog(WeblogEntryTest.class);
    
    UserData testUser = null;
    WebsiteData testWeblog = null;
    
    
    public WeblogEntryTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(WeblogEntryTest.class);
    }
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        try {
            testUser = TestUtils.setupUser("entryTestUser");
            testWeblog = TestUtils.setupWeblog("entryTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testWeblogEntryCRUD() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        WeblogEntryData entry = null;
        
        WeblogEntryData testEntry = new WeblogEntryData();
        testEntry.setTitle("entryTestEntry");
        testEntry.setLink("testEntryLink");
        testEntry.setText("blah blah entry");
        testEntry.setAnchor("testEntryAnchor");
        testEntry.setPubTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        testEntry.setUpdateTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        testEntry.setWebsite(testWeblog);
        testEntry.setCreator(testUser);
        testEntry.setCategory(testWeblog.getDefaultCategory());
        
        // create a weblog entry
        mgr.saveWeblogEntry(testEntry);
        String id = testEntry.getId();
        TestUtils.endSession(true);
        
        // make sure entry was created
        entry = mgr.getWeblogEntry(id);
        assertNotNull(entry);
        assertEquals(testEntry, entry);
        
        // update a weblog entry
        entry.setTitle("testtest");
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);
        
        // make sure entry was updated
        entry = null;
        entry = mgr.getWeblogEntry(id);
        assertNotNull(entry);
        assertEquals("testtest", entry.getTitle());
        
        // delete a weblog entry
        mgr.removeWeblogEntry(entry);
        TestUtils.endSession(true);
        
        // make sure entry was deleted
        entry = null;
        entry = mgr.getWeblogEntry(id);
        assertNull(entry);
    }
    
    
    /**
     * Test lookup mechanisms ... 
     */
    public void testWeblogEntryLookups() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        WeblogEntryData entry = null;
        List entries = null;
        Map entryMap = null;
        
        // setup some test entries to use
        WeblogEntryData entry1 = TestUtils.setupWeblogEntry("entry1", testWeblog.getDefaultCategory(), testWeblog, testUser);
        WeblogEntryData entry2 = TestUtils.setupWeblogEntry("entry2", testWeblog.getDefaultCategory(), testWeblog, testUser);
        WeblogEntryData entry3 = TestUtils.setupWeblogEntry("entry3", testWeblog.getDefaultCategory(), testWeblog, testUser);
        
        // make a couple changes
        entry1.setLocale("en_US");
        mgr.saveWeblogEntry(entry1);
        entry2.setLocale("ja_JP");
        entry2.setPinnedToMain(Boolean.TRUE);
        entry2.setUpdateTime(new java.sql.Timestamp(entry2.getUpdateTime().getTime()+8822384));
        entry2.setPubTime(entry2.getUpdateTime());
        mgr.saveWeblogEntry(entry2);
        entry3.setStatus(WeblogEntryData.DRAFT);
        entry3.setUpdateTime(new java.sql.Timestamp(entry3.getUpdateTime().getTime()+348829384));
        entry3.setPubTime(entry3.getUpdateTime());
        mgr.saveWeblogEntry(entry3);
        
        TestUtils.endSession(true);
        
        log.debug("entry1 = "+entry1.getUpdateTime());
        log.debug("entry2 = "+entry2.getUpdateTime());
        log.debug("entry3 = "+entry3.getUpdateTime());
        
        // get entry by id
        entry = null;
        entry = mgr.getWeblogEntry(entry1.getId());
        assertNotNull(entry);
        assertEquals(entry1.getAnchor(), entry.getAnchor());
        
        // get entry by anchor
        entry = null;
        entry = mgr.getWeblogEntryByAnchor(testWeblog, entry1.getAnchor());
        assertNotNull(entry);
        assertEquals(entry1.getTitle(), entry.getTitle());
        
        // get all entries for weblog
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, null, null, null, null, null, null, 0, -1);
        assertNotNull(entries);
        assertEquals(3, entries.size());
        assertEquals(entry3, entries.get(0));
        
        // get all entries in category
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog.getDefaultCategory(), false);
        assertNotNull(entries);
        assertEquals(3, entries.size());
        
        // get all published entries only 
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, null, null, WeblogEntryData.PUBLISHED, null, null, null, 0, -1);
        assertNotNull(entries);
        assertEquals(2, entries.size());
        
        // get all entries in date range
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, entry2.getPubTime(), entry2.getPubTime(), null, null, null, null, null, null, 0, -1);
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry2, entries.get(0));
        
        // get all entries, limited to maxSize
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, null, null, null, null, null, null, 0, 2);
        assertNotNull(entries);
        assertEquals(2, entries.size());
        
        // get all entries in category
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, testWeblog.getDefaultCategory().getName(), null, null, null, null, null, 0, -1);
        assertNotNull(entries);
        assertEquals(3, entries.size());
        
        // get all entries, limited by offset/range
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, null, null, null, null, null, null, 1, 1);
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry2, entries.get(0));
        
        // get all entries, limited by locale
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, null, null, null, null, null, "en_US", 0, -1);
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry1, entries.get(0));
        
        // get pinned entries only
        entries = null;
        entries = mgr.getWeblogEntriesPinnedToMain(new Integer(5));
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry2, entries.get(0));
        
        // get next entry
        entry = null;
        entry = mgr.getNextEntry(entry1, null, null);
        assertNotNull(entry);
        assertEquals(entry2, entry);
        
        // get previous entry
        entry = null;
        entry = mgr.getPreviousEntry(entry2, null, null);
        assertNotNull(entry);
        assertEquals(entry1, entry);
        
        // get object map
        entryMap = null;
        entryMap = mgr.getWeblogEntryObjectMap(testWeblog, null, null, null, null, null, null, 0, -1);
        assertNotNull(entryMap);
        assertTrue(entryMap.keySet().size() > 1);
        
        // get string map
        entryMap = null;
        entryMap = mgr.getWeblogEntryStringMap(testWeblog, null, null, null, null, null, null, 0, -1);
        assertNotNull(entryMap);
        assertTrue(entryMap.keySet().size() > 1);
                
        // teardown our test entries
        TestUtils.teardownWeblogEntry(entry1.getId());
        TestUtils.teardownWeblogEntry(entry2.getId());
        TestUtils.teardownWeblogEntry(entry3.getId());
        TestUtils.endSession(true);
    }
    
    
    /**
     * Test that the createAnchor() method actually ensures unique anchors.
     */
    public void testCreateAnchor() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        WeblogEntryData entry = null;
        List entries = null;
        
        // setup some test entries to use
        WeblogEntryData entry1 = TestUtils.setupWeblogEntry("entry1", testWeblog.getDefaultCategory(), testWeblog, testUser);
        TestUtils.endSession(true);
        
        // make sure createAnchor gives us a new anchor value
        String anchor = mgr.createAnchor(entry1);
        assertNotNull(anchor);
        assertNotSame("entry1", anchor);
        
        // make sure we can create a new entry with specified anchor
        WeblogEntryData entry2 = TestUtils.setupWeblogEntry(anchor, testWeblog.getDefaultCategory(), testWeblog, testUser);
        TestUtils.endSession(true);
        assertNotNull(entry2);
        
        // teardown our test entries
        TestUtils.teardownWeblogEntry(entry1.getId());
        TestUtils.teardownWeblogEntry(entry2.getId());
        TestUtils.endSession(true);
    }
    
    public void testCreateAnEntryWithTagsShortcut() throws Exception {

        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        WeblogEntryData entry = null;

        WeblogEntryData testEntry = new WeblogEntryData();
        testEntry.setTitle("entryTestEntry");
        testEntry.setLink("testEntryLink");
        testEntry.setText("blah blah entry");
        testEntry.setAnchor("testEntryAnchor");
        testEntry.setPubTime(new java.sql.Timestamp(new java.util.Date()
                .getTime()));
        testEntry.setUpdateTime(new java.sql.Timestamp(new java.util.Date()
                .getTime()));
        testEntry.setWebsite(testWeblog);
        testEntry.setCreator(testUser);
        testEntry.setCategory(testWeblog.getDefaultCategory());

        // shortcut
        testEntry.addTag("testTag");

        // create a weblog entry
        mgr.saveWeblogEntry(testEntry);
        String id = testEntry.getId();
        TestUtils.endSession(true);

        // make sure entry was created
        entry = mgr.getWeblogEntry(id);
        assertNotNull(entry);
        assertEquals(testEntry, entry);
        assertNotNull(entry.getTags());
        assertEquals(1, entry.getTags().size());
        assertEquals("testtag", ((WeblogEntryTagData) entry.getTags()
                .iterator().next()).getName());

        // teardown our test entry
        TestUtils.teardownWeblogEntry(id);
        TestUtils.endSession(true);
    }
        
    public void testAddMultipleTags() throws Exception {

        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();

        // setup some test entries to use
        WeblogEntryData entry = TestUtils.setupWeblogEntry("entry1", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        entry.addTag("testTag");
        entry.addTag("whateverTag");
        String id = entry.getId();
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        entry.addTag("testTag2");
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        assertEquals(3, entry.getTags().size());

        // teardown our test entry
        TestUtils.teardownWeblogEntry(id);
        TestUtils.endSession(true);
    }
    
    public void testAddMultipleIdenticalTags() throws Exception {

        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();

        // setup some test entries to use
        WeblogEntryData entry = TestUtils.setupWeblogEntry("entry1", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        entry.addTag("testTag");
        String id = entry.getId();
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        entry.addTag("testTag");
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        assertEquals(1, entry.getTags().size());

        // teardown our test entry
        TestUtils.teardownWeblogEntry(id);
        TestUtils.endSession(true);
    }    
    
    public void testRemoveTagsViaShortcut() throws Exception {

        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();

        // setup some test entries to use
        WeblogEntryData entry = TestUtils.setupWeblogEntry("entry1", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        entry.addTag("testTag");
        entry.addTag("testTag2");
        String id = entry.getId();
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        assertEquals(2, entry.getTags().size());
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        mgr.removeWeblogEntryTag("testtag", entry);
        mgr.removeWeblogEntryTag("testtag2", entry);
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        assertEquals(0, entry.getTags().size());
        TestUtils.endSession(true);

        // teardown our test entry
        TestUtils.teardownWeblogEntry(id);
        TestUtils.endSession(true);
    }
    
    public void testTagsExist() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();

        WebsiteData weblog = TestUtils.setupWeblog("tagsExistWeblog1", testUser);
        String wid = weblog.getId();
        
        // setup some test entries to use
        WeblogEntryData entry = TestUtils.setupWeblogEntry("tagsExistEntry1", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        String id1 = entry.getId();
        entry.addTag("blahTag");
        entry.addTag("fooTag");
        mgr.saveWeblogEntry(entry);

        WeblogEntryData entry2 = TestUtils.setupWeblogEntry("tagsExistEntry2", weblog
                .getDefaultCategory(), weblog, testUser);
        String id2 = entry2.getId();
        entry2.addTag("aaaTag");
        entry2.addTag("bbbTag");
        mgr.saveWeblogEntry(entry2);
        TestUtils.endSession(true);
        
        // we'll need these
        List tags1 = new ArrayList();
        tags1.add("nonExistTag");
        
        List tags2 = new ArrayList();
        tags2.add("blahtag");
        
        // test site-wide
        this.assertTrue(mgr.getTagComboExists(tags2, null));
        this.assertFalse(mgr.getTagComboExists(tags1, null));
        
        // test weblog specific
        this.assertTrue(mgr.getTagComboExists(tags2, testWeblog));
        this.assertFalse(mgr.getTagComboExists(tags1, testWeblog));
        this.assertFalse(mgr.getTagComboExists(tags2, weblog));
        
        // teardown our test data
        TestUtils.teardownWeblogEntry(id1);
        TestUtils.teardownWeblogEntry(id2);
        TestUtils.teardownWeblog(wid);
        TestUtils.endSession(true);
    }
    
    public void testGetEntriesByTag() throws Exception {

        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();

        // setup some test entries to use
        WeblogEntryData entry = TestUtils.setupWeblogEntry("entry1", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        entry.addTag("testTag");
        String id = entry.getId();
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        List results = mgr.getWeblogEntries(testWeblog, null, null, null, null,
                Arrays.asList(new String[] { "testtag" }), null, null, null, null, 0,
                -1);
        assertEquals(1, results.size());
        WeblogEntryData testEntry = (WeblogEntryData) results.iterator().next();
        assertEquals(entry, testEntry);

        // teardown our test entry
        TestUtils.teardownWeblogEntry(id);
        TestUtils.endSession(true);
    }  
    
    public void testRemoveEntryTagCascading() throws Exception {

        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();

        // setup some test entries to use
        WeblogEntryData entry = TestUtils.setupWeblogEntry("entry1", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        entry.addTag("testTag");
        String id = entry.getId();
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        List results = mgr.getWeblogEntries(testWeblog, null, null, null, null,
                Arrays.asList(new String[] { "testtag" }), null, null, null, null, 0,
                -1);
        assertEquals(1, results.size());
        WeblogEntryData testEntry = (WeblogEntryData) results.iterator().next();
        assertEquals(entry, testEntry);

        // teardown our test entry
        TestUtils.teardownWeblogEntry(id);
        TestUtils.endSession(true);

        results = mgr.getWeblogEntries(testWeblog, null, null, null, null,
                Arrays.asList(new String[] { "testtag" }), null, null, null, null, 0,
                -1);
        assertEquals(0, results.size());

        // terminate
        TestUtils.endSession(true);
    }
    
    public void testUpdateTags() throws Exception {

        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();

        // setup some test entries to use
        WeblogEntryData entry = TestUtils.setupWeblogEntry("entry1", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        entry.addTag("testWillStayTag");
        entry.addTag("testTagWillBeRemoved");
        String id = entry.getId();
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        assertEquals(2, entry.getTags().size());

        List updateTags = new ArrayList();
        updateTags.add("testwillstaytag");
        updateTags.add("testnewtag");
        updateTags.add("testnewtag3");
        entry.updateTags(updateTags);
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        HashSet tagNames = new HashSet();
        for (Iterator it = entry.getTags().iterator(); it.hasNext();) {
            WeblogEntryTagData tagData = (WeblogEntryTagData) it.next();
            tagNames.add(tagData.getName());
        }

        assertEquals(3, entry.getTags().size());
        assertEquals(3, tagNames.size());
        assertEquals(true, tagNames.contains("testwillstaytag"));
        assertEquals(true, tagNames.contains("testnewtag"));
        assertEquals(true, tagNames.contains("testnewtag3"));

        // teardown our test entry
        TestUtils.teardownWeblogEntry(id);
        TestUtils.endSession(true);
    }

    /**
     * We want to make sure that the first time placed on the tag remains
     * through consequent updates.
     * 
     * @throws Exception
     */
    public void testUpdateTagTime() throws Exception {

        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();

        // setup some test entries to use
        WeblogEntryData entry = TestUtils.setupWeblogEntry("entry1", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        String id = entry.getId();

        entry.addTag("testWillStayTag");
        entry.addTag("testTagWillBeRemoved");
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        assertEquals(2, entry.getTags().size());

        Timestamp original = null;

        for (Iterator it = entry.getTags().iterator(); it.hasNext();) {
            WeblogEntryTagData tagData = (WeblogEntryTagData) it.next();
            if (tagData.getName().equals("testwillstaytag"))
                original = tagData.getTime();
        }

        List updateTags = new ArrayList();
        updateTags.add("testwillstaytag");
        updateTags.add("testnewtag");
        updateTags.add("testnewtag3");
        entry.updateTags(updateTags);
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);

        entry = mgr.getWeblogEntry(id);
        HashSet tagNames = new HashSet();
        for (Iterator it = entry.getTags().iterator(); it.hasNext();) {
            WeblogEntryTagData tagData = (WeblogEntryTagData) it.next();
            tagNames.add(tagData.getName());
            if (tagData.getName().equals("testwillstaytag"))
                assertEquals(original, tagData.getTime());
        }

        assertEquals(3, entry.getTags().size());
        assertEquals(3, tagNames.size());
        assertEquals(true, tagNames.contains("testwillstaytag"));
        assertEquals(true, tagNames.contains("testnewtag"));
        assertEquals(true, tagNames.contains("testnewtag3"));

        // teardown our test entry
        TestUtils.teardownWeblogEntry(id);
        TestUtils.endSession(true);
    }

    public void testTagAggregates() throws Exception {

        WebsiteData testWeblog2 = TestUtils.setupWeblog("entryTestWeblog2",
                testUser);

        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();

        // let's make sure we are starting from scratch

        // site-wide
        List tags = mgr.getTags(null, null, null, -1);
        assertEquals(0, tags.size());

        // first weblog
        tags = mgr.getTags(testWeblog, null, null, -1);
        assertEquals(0, tags.size());

        // second weblog
        tags = mgr.getTags(testWeblog2, null, null, -1);
        assertEquals(0, tags.size());

        // setup some test entries to use
        WeblogEntryData entry = TestUtils.setupWeblogEntry("entry1", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        entry.addTag("one");
        entry.addTag("two");
        mgr.saveWeblogEntry(entry);

        entry = TestUtils.setupWeblogEntry("entry2", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        entry.addTag("one");
        entry.addTag("two");
        entry.addTag("three");
        mgr.saveWeblogEntry(entry);

        TestUtils.endSession(true);

        tags = mgr.getTags(testWeblog, null, null, -1);
        assertEquals(3, tags.size());

        HashMap expectedWeblogTags = new HashMap();
        expectedWeblogTags.put("one", new Integer(2));
        expectedWeblogTags.put("two", new Integer(2));
        expectedWeblogTags.put("three", new Integer(1));

        for (Iterator it = tags.iterator(); it.hasNext();) {
            TagStat stat = (TagStat) it.next();
            if (!expectedWeblogTags.containsKey(stat.getName()))
                fail("Unexpected tagName.");

            Integer expectedCount = (Integer) expectedWeblogTags.get(stat
                    .getName());
            assertEquals(expectedCount.intValue(), stat.getCount());
        }

        // now add another entry in another blog
        entry = TestUtils.setupWeblogEntry("entry3", testWeblog2
                .getDefaultCategory(), testWeblog2, testUser);
        entry.addTag("one");
        entry.addTag("three");
        entry.addTag("four");
        mgr.saveWeblogEntry(entry);
        
        TestUtils.endSession(true);
        
        // let's fetch "site" tags now
        tags = mgr.getTags(null, null, null, -1);
        assertEquals(4, tags.size());

        HashMap expectedSiteTags = new HashMap();
        expectedSiteTags.put("one", new Integer(3));
        expectedSiteTags.put("two", new Integer(2));
        expectedSiteTags.put("three", new Integer(2));
        expectedSiteTags.put("four", new Integer(1));

        for (Iterator it = tags.iterator(); it.hasNext();) {
            TagStat stat = (TagStat) it.next();
            if (!expectedSiteTags.containsKey(stat.getName()))
                fail("Unexpected tagName.");

            Integer expectedCount = (Integer) expectedSiteTags.get(stat
                    .getName());
            assertEquals(expectedCount.intValue(), stat.getCount());
        }

        TestUtils.endSession(true);

        entry = mgr.getWeblogEntryByAnchor(testWeblog, "entry2");
        List updateTags = new ArrayList();
        updateTags.add("one");
        updateTags.add("three");
        updateTags.add("five");
        entry.updateTags(updateTags);
        mgr.saveWeblogEntry(entry);

        TestUtils.endSession(true);

        tags = mgr.getTags(testWeblog, null, null, -1);
        assertEquals(4, tags.size());

        expectedWeblogTags = new HashMap();
        expectedWeblogTags.put("one", new Integer(2));
        expectedWeblogTags.put("two", new Integer(1));
        expectedWeblogTags.put("three", new Integer(1));
        expectedWeblogTags.put("five", new Integer(1));

        for (Iterator it = tags.iterator(); it.hasNext();) {
            TagStat stat = (TagStat) it.next();
            if (!expectedWeblogTags.containsKey(stat.getName()))
                fail("Unexpected tagName.");

            Integer expectedCount = (Integer) expectedWeblogTags.get(stat
                    .getName());
            assertEquals(stat.getName(), expectedCount.intValue(), stat
                    .getCount());
        }

        tags = mgr.getTags(null, null, null, -1);
        assertEquals(5, tags.size());

        expectedSiteTags = new HashMap();
        expectedSiteTags.put("one", new Integer(3));
        expectedSiteTags.put("two", new Integer(1));
        expectedSiteTags.put("three", new Integer(2));
        expectedSiteTags.put("four", new Integer(1));
        expectedSiteTags.put("five", new Integer(1));

        for (Iterator it = tags.iterator(); it.hasNext();) {
            TagStat stat = (TagStat) it.next();
            if (!expectedSiteTags.containsKey(stat.getName()))
                fail("Unexpected tagName.");

            Integer expectedCount = (Integer) expectedSiteTags.get(stat
                    .getName());
            assertEquals(stat.getName(), expectedCount.intValue(), stat
                    .getCount());
        }

        // teardown our test blog 2
        TestUtils.teardownWeblog(testWeblog2.getId());
        TestUtils.endSession(true);
    }

    public void testTagAggregatesCaseSensitivity() throws Exception {

        WebsiteData testWeblog2 = TestUtils.setupWeblog("entryTestWeblog2",
                testUser);

        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();

        // let's make sure we are starting from scratch

        // site-wide
        List tags = mgr.getTags(null, null, null, -1);
        assertEquals(0, tags.size());

        // first weblog
        tags = mgr.getTags(testWeblog, null, null, -1);
        assertEquals(0, tags.size());

        // second weblog
        tags = mgr.getTags(testWeblog2, null, null, -1);
        assertEquals(0, tags.size());

        // setup some test entries to use
        WeblogEntryData entry = TestUtils.setupWeblogEntry("entry1", testWeblog
                .getDefaultCategory(), testWeblog, testUser);
        entry.addTag("one");
        entry.addTag("two");
        entry.addTag("ONE");
        mgr.saveWeblogEntry(entry);

        TestUtils.endSession(true);

        tags = mgr.getTags(testWeblog, null, null, -1);
        assertEquals(2, tags.size());

        HashMap expectedWeblogTags = new HashMap();
        expectedWeblogTags.put("one", new Integer(1));
        expectedWeblogTags.put("two", new Integer(1));

        for (Iterator it = tags.iterator(); it.hasNext();) {
            TagStat stat = (TagStat) it.next();
            if (!expectedWeblogTags.containsKey(stat.getName()))
                fail("Unexpected tagName.");

            Integer expectedCount = (Integer) expectedWeblogTags.get(stat.getName());
            assertEquals(expectedCount.intValue(), stat.getCount());
        }

        // now add another entry in another blog
        entry = TestUtils.setupWeblogEntry("entry3", testWeblog2
                .getDefaultCategory(), testWeblog2, testUser);
        entry.addTag("ONE");
        entry.addTag("three");
        mgr.saveWeblogEntry(entry);
        
        TestUtils.endSession(true);
        
        // let's fetch "site" tags now
        tags = mgr.getTags(null, null, null, -1);
        assertEquals(3, tags.size());

        HashMap expectedSiteTags = new HashMap();
        expectedSiteTags.put("one", new Integer(2));
        expectedSiteTags.put("two", new Integer(1));
        expectedSiteTags.put("three", new Integer(1));

        for (Iterator it = tags.iterator(); it.hasNext();) {
            TagStat stat = (TagStat) it.next();
            if (!expectedSiteTags.containsKey(stat.getName()))
                fail("Unexpected tagName.");

            Integer expectedCount = (Integer) expectedSiteTags.get(stat.getName());
            assertEquals(expectedCount.intValue(), stat.getCount());
        }

        TestUtils.endSession(true);

        // teardown our test blog 2
        TestUtils.teardownWeblog(testWeblog2.getId());
        TestUtils.endSession(true);
    }
           
  
    
    /**
     * Test that we can add and remove entry attributes for an entry.
     */
    public void testEntryAttributeCRUD() throws Exception {
        
        // TODO: implement entry attribute test
    }
    
    
    public void testWeblogStats() throws Exception {
        
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        UserManager umgr = RollerFactory.getRoller().getUserManager();
        
        UserData user1 = TestUtils.setupUser("statuser1");
        WebsiteData blog1 = TestUtils.setupWeblog("statblog1", user1);
        WebsiteData blog2 = TestUtils.setupWeblog("statblog2", user1);

        WebsiteData blog3 = TestUtils.setupWeblog("statblog3", user1);
        blog3.setEnabled(Boolean. FALSE);
        umgr.saveWebsite(blog3);

        WeblogEntryData entry1 = TestUtils.setupWeblogEntry("entry1", 
                testWeblog.getDefaultCategory(), blog1, user1);
        WeblogEntryData entry2 = TestUtils.setupWeblogEntry("entry2", 
                testWeblog.getDefaultCategory(), blog1, user1);
        
        WeblogEntryData entry3 = TestUtils.setupWeblogEntry("entry3", 
                testWeblog.getDefaultCategory(), blog2, user1);
        WeblogEntryData entry4 = TestUtils.setupWeblogEntry("entry4", 
                testWeblog.getDefaultCategory(), blog2, user1);
        WeblogEntryData entry5 = TestUtils.setupWeblogEntry("entry5", 
                testWeblog.getDefaultCategory(), blog2, user1);
               
        CommentData comment1 = TestUtils.setupComment("comment1", entry1);
        CommentData comment2 = TestUtils.setupComment("comment2", entry1);
        
        CommentData comment3 = TestUtils.setupComment("comment3", entry3);
        CommentData comment4 = TestUtils.setupComment("comment4", entry3);
        CommentData comment5 = TestUtils.setupComment("comment5", entry3);

        try {
            assertEquals(2L, blog1.getEntryCount());
            assertEquals(3L, blog2.getEntryCount());
            assertEquals(5L, wmgr.getEntryCount());

            assertEquals(2L, blog1.getCommentCount());
            assertEquals(3L, blog2.getCommentCount());
            assertEquals(5L, wmgr.getCommentCount());

            assertEquals(4L, umgr.getWeblogCount());
            assertEquals(2L, umgr.getUserCount());
            
        } finally {
            
            TestUtils.teardownComment(comment1.getId());
            TestUtils.teardownComment(comment2.getId());
            TestUtils.teardownComment(comment3.getId());
            TestUtils.teardownComment(comment4.getId());
            TestUtils.teardownComment(comment5.getId());

            TestUtils.teardownWeblogEntry(entry1.getId());
            TestUtils.teardownWeblogEntry(entry2.getId());
            TestUtils.teardownWeblogEntry(entry3.getId());
            TestUtils.teardownWeblogEntry(entry4.getId());
            TestUtils.teardownWeblogEntry(entry5.getId());

            TestUtils.teardownWeblog(blog1.getId());
            TestUtils.teardownWeblog(blog2.getId());
            TestUtils.teardownWeblog(blog3.getId());

            TestUtils.teardownUser(user1.getId());            
        }
    }
}



