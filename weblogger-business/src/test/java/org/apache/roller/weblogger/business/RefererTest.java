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
package org.apache.roller.weblogger.business;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.referrers.RefererManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.util.DateUtil;


/**
 * Test Roller Referer Management.
 */
public class RefererTest extends TestCase {
    
    public static Log log = LogFactory.getLog(RefererTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    Weblog testWeblog2 = null;
    
    int count = 20;
    String testDay;
    String origSpamWords;
    
    
    public RefererTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(RefererTest.class);
    }
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("referTestUser");
            testWeblog = TestUtils.setupWeblog("referTestWeblog1", testUser);
            testWeblog2 = TestUtils.setupWeblog("referTestWeblog2", testUser);
            
            // add "spamtest" to refererSpamWords
            PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            RuntimeConfigProperty spamprop = pmgr.getProperty("spam.blacklist");
            this.origSpamWords = spamprop.getValue();
            spamprop.setValue(spamprop.getValue() + ", spamtest");
            pmgr.saveProperty(spamprop);
            
            // add a number of referers to play with
            RefererManager rmgr = WebloggerFactory.getWeblogger().getRefererManager();
            Calendar lCalendar = Calendar.getInstance();
            lCalendar.setTime(new Date());
            for (int i = 0; i < count; i++) {
                lCalendar.add(Calendar.DATE, -1);
                Timestamp day = new Timestamp(lCalendar.getTime().getTime());
                testDay = DateUtil.format8chars(day);
                
                rmgr.processReferrer("http://test"+i, "http://test"+i,
                    testWeblog.getHandle(), null, testDay);
                if (i % 2 == 0) { // half the referrers from weblog 2
                    rmgr.processReferrer("http://test"+i, "http://test"+i,
                        testWeblog2.getHandle(), null, testDay);
                }
            }
            
            TestUtils.endSession(true);
            
        } catch (Exception ex){
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    public void tearDown() throws Exception {
        
        try {
            // reset refererSpamWords to original value
            PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            RuntimeConfigProperty spamprop = pmgr.getProperty("spam.blacklist");
            spamprop.setValue(this.origSpamWords);
            pmgr.saveProperty(spamprop);
            
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownWeblog(testWeblog2.getId());
            TestUtils.teardownUser(testUser.getUserName());
            
            TestUtils.endSession(true);
            
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    public void testRefererCRUD() throws Exception {
        
        RefererManager mgr = WebloggerFactory.getWeblogger().getRefererManager();
        WeblogReferrer referer = null;
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogReferrer testReferer = new WeblogReferrer();
        testReferer.setWebsite(testWeblog);
        testReferer.setDateString("20060420");
        testReferer.setRefererUrl("blah");
        testReferer.setRefererPermalink("blah");
        testReferer.setRequestUrl("foo");
        testReferer.setTitle("lksdjf");
        testReferer.setTotalHits(new Integer(3));
        testReferer.setDayHits(new Integer(2));
        testReferer.setVisible(Boolean.TRUE);
        testReferer.setDuplicate(Boolean.FALSE);
        testReferer.setExcerpt("");
        
        // save referer
        mgr.saveReferer(testReferer);
        String id = testReferer.getId();
        TestUtils.endSession(true);
        
        // check that create was successful
        referer = null;
        referer = mgr.getReferer(id);
        assertNotNull(referer);
        assertEquals(testReferer, referer);
        
        // update referer
        referer.setTitle("testtesttest");
        mgr.saveReferer(referer);
        TestUtils.endSession(true);
        
        // check that update was successful
        referer = null;
        referer = mgr.getReferer(id);
        assertNotNull(referer);
        assertEquals("testtesttest", referer.getTitle());
        
        // delete referer
        mgr.removeReferer(referer);
        TestUtils.endSession(true);
        
        // check that delete was successful
        referer = null;
        referer = mgr.getReferer(id);
        assertNull(referer);
    }
    
    
    public void testGetReferersToDate() throws Exception {
        
        RefererManager rmgr = WebloggerFactory.getWeblogger().getRefererManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List referers = rmgr.getReferersToDate(testWeblog, testDay);
        assertEquals("Should be one Referer.", referers.size(), 1);
    }
    
    
    public void testRefererProcessing() throws WebloggerException {
        
        RefererManager rmgr = WebloggerFactory.getWeblogger().getRefererManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List refs = rmgr.getReferers(testWeblog);
        assertEquals("number of referers should equal count", count, refs.size());
        
        int hits = rmgr.getDayHits(testWeblog);
        assertEquals("There should be one fewer hits than referers", count, hits);
    }
    
    
    public void testApplyRefererFilters() throws Exception {
        
        log.info("Test apply referers (global)");
        
        RefererManager rmgr = WebloggerFactory.getWeblogger().getRefererManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List refs = rmgr.getReferers(testWeblog);
        assertEquals(count, refs.size());
        
        PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        RuntimeConfigProperty spamprop = pmgr.getProperty("spam.blacklist");
        String origWords = spamprop.getValue();
        spamprop.setValue(spamprop.getValue() + ", test");
        pmgr.saveProperty(spamprop);
        TestUtils.endSession(true);
        
        rmgr.applyRefererFilters();
        TestUtils.endSession(true);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        refs = rmgr.getReferers(testWeblog);
        assertEquals(0, refs.size());
        
        spamprop = pmgr.getProperty("spam.blacklist");
        spamprop.setValue(origWords);
        pmgr.saveProperty(spamprop);
        TestUtils.endSession(true);
    }
    
    
    public void testApplyRefererFiltersWebsite() throws Exception {
        
        log.info("Test apply referers (weblog)");
        
        RefererManager rmgr = WebloggerFactory.getWeblogger().getRefererManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List refs = rmgr.getReferers(testWeblog);
        assertEquals(count, refs.size());
        String origWords = null;
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        origWords = testWeblog.getBlacklist();
        testWeblog.setBlacklist("test");
        WebloggerFactory.getWeblogger().getWeblogManager().saveWeblog(testWeblog);
        TestUtils.endSession(true);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        rmgr.applyRefererFilters(testWeblog);
        TestUtils.endSession(true);
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        refs = rmgr.getReferers(testWeblog);
        assertEquals(0, refs.size());
    }
    
}

