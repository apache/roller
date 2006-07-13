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
 * WeblogEntryTest.java
 *
 * Created on April 9, 2006, 4:38 PM
 */

package org.apache.roller.business;

import java.util.List;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.TestUtils;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
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
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, null, null, null, 0, -1, null);
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
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, null, WeblogEntryData.PUBLISHED, null, 0, -1, null);
        assertNotNull(entries);
        assertEquals(2, entries.size());
        
        // get all entries in date range
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, entry2.getPubTime(), entry2.getPubTime(), null, null, null, 0, -1, null);
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry2, entries.get(0));
        
        // get all entries, limited to maxSize
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, null, null, null, 0, 2, null);
        assertNotNull(entries);
        assertEquals(2, entries.size());
        
        // get all entries in category
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, testWeblog.getDefaultCategory().getName(), null, null, 0, -1, null);
        assertNotNull(entries);
        assertEquals(3, entries.size());
        
        // get all entries, limited by offset/range
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, null, null, null, 1, 1, null);
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry2, entries.get(0));
        
        // get all entries, limited by locale
        entries = null;
        entries = mgr.getWeblogEntries(testWeblog, null, null, null, null, null, null, 0, -1, "en_US");
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry1, entries.get(0));
        
        // get pinned entries only
        entries = null;
        entries = mgr.getWeblogEntriesPinnedToMain(new Integer(5));
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry2, entries.get(0));
        
        // get next entries
        entries = null;
        entries = mgr.getNextEntries(entry1, null, 5);
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry2, entries.get(0));
        
        // get next entry
        entry = null;
        entry = mgr.getNextEntry(entry1, null);
        assertNotNull(entry);
        assertEquals(entry2, entry);
        
        // get previous entries
        entries = null;
        entries = mgr.getPreviousEntries(entry2, null, 5);
        assertNotNull(entries);
        assertEquals(1, entries.size());
        assertEquals(entry1, entries.get(0));
        
        // get previous entry
        entry = null;
        entry = mgr.getPreviousEntry(entry2, null);
        assertNotNull(entry);
        assertEquals(entry1, entry);
        
        // get object map
        entryMap = null;
        entryMap = mgr.getWeblogEntryObjectMap(testWeblog, null, null, null, null, 0, -1, null);
        assertNotNull(entryMap);
        assertTrue(entryMap.keySet().size() > 1);
        
        // get string map
        entryMap = null;
        entryMap = mgr.getWeblogEntryStringMap(testWeblog, null, null, null, null, 0, -1, null);
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
    
    
    /**
     * Test that we can add and remove entry attributes for an entry.
     */
    public void testEntryAttributeCRUD() throws Exception {
        
        // TODO: implement entry attribute test
    }
    
}
