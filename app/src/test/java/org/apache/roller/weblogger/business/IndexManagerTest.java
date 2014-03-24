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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.business.search.operations.AddEntryOperation;
import org.apache.roller.weblogger.business.search.operations.SearchOperation;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test Search Manager business layer operations.
 */
public class IndexManagerTest extends TestCase {
    User testUser = null;
    Weblog testWeblog = null;
    public static Log log = LogFactory.getLog(IndexManagerTest.class);    

    public IndexManagerTest(String name) {
        super(name);
    }
        
    public static Test suite() {
        return new TestSuite(IndexManagerTest.class);
    }

    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("entryTestUser");
            testWeblog = TestUtils.setupWeblog("entryTestWeblog", testUser);
            TestUtils.endSession(true);

            //WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
            //assertEquals(1, wmgr.getWeblogCount());
 
        } catch (Exception ex) {
            log.error("ERROR in test setup", ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in test teardown", ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
        
    public void testSearch() throws Exception {
        WeblogEntryManager wem = WebloggerFactory.getWeblogger().getWeblogEntryManager();

        WeblogEntry wd1 = new WeblogEntry();            
        wd1.setTitle("The Tholian Web");
        wd1.setText(
         "When the Enterprise attempts to ascertain the fate of the  "
        +"U.S.S. Defiant which vanished 3 weeks ago, the warp engines  "
        +"begin to lose power, and Spock reports strange sensor readings.");
        wd1.setAnchor("dummy1");
        wd1.setCreatorUserName(testUser.getUserName());
        wd1.setStatus(WeblogEntry.PUBLISHED);
        wd1.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        wd1.setPubTime(new Timestamp(System.currentTimeMillis()));
        wd1.setWebsite(TestUtils.getManagedWebsite(testWeblog));

        WeblogCategory cat = wem.getWeblogCategory(testWeblog.getDefaultCategory().getId());
        wd1.setCategory(cat);

        wem.saveWeblogEntry(wd1);
        TestUtils.endSession(true);
        wd1 = TestUtils.getManagedWeblogEntry(wd1);

        IndexManager imgr = WebloggerFactory.getWeblogger().getIndexManager();
        imgr.executeIndexOperationNow(
            new AddEntryOperation(WebloggerFactory.getWeblogger(), (IndexManagerImpl)imgr, wd1));

        WeblogEntry wd2 = new WeblogEntry();
        wd2.setTitle("A Piece of the Action");
        wd2.setText(
          "The crew of the Enterprise attempts to make contact with "
          +"the inhabitants of planet Sigma Iotia II, and Uhura puts Kirk "
          +"in communication with Boss Oxmyx.");
        wd2.setAnchor("dummy2");
        wd2.setStatus(WeblogEntry.PUBLISHED);
        wd2.setCreatorUserName(testUser.getUserName());
        wd2.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        wd2.setPubTime(new Timestamp(System.currentTimeMillis()));
        wd2.setWebsite(TestUtils.getManagedWebsite(testWeblog));

        cat = wem.getWeblogCategory(testWeblog.getDefaultCategory().getId());
        wd2.setCategory(cat);

        wem.saveWeblogEntry(wd2);
        TestUtils.endSession(true);
        wd2 = TestUtils.getManagedWeblogEntry(wd2);

         imgr.executeIndexOperationNow(
             new AddEntryOperation(WebloggerFactory.getWeblogger(), (IndexManagerImpl)imgr, wd2));

        Thread.sleep(RollerConstants.SEC_IN_MS);

        SearchOperation search = new SearchOperation(imgr);
        search.setTerm("Enterprise");
        imgr.executeIndexOperationNow(search);
        assertEquals(2, search.getResultsCount());

        SearchOperation search2 = new SearchOperation(imgr);
        search2.setTerm("Tholian");
        imgr.executeIndexOperationNow(search2);
        assertEquals(1, search2.getResultsCount());

        // Clean up
        imgr.removeEntryIndexOperation(wd1);
        imgr.removeEntryIndexOperation(wd2);

        SearchOperation search3 = new SearchOperation(imgr);
        search3.setTerm("Enterprise");
        imgr.executeIndexOperationNow(search3);
        assertEquals(0, search3.getResultsCount());
    }    
}
