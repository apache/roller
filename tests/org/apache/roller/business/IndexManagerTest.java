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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.roller.business.search.IndexManagerImpl;
import org.apache.roller.business.search.operations.AddEntryOperation;
import org.apache.roller.business.search.operations.RemoveEntryOperation;
import org.apache.roller.business.search.operations.SearchOperation;
import org.apache.roller.business.search.IndexManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;


/**
 * Test Search Manager business layer operations.
 */
public class IndexManagerTest extends TestCase {
        
    public IndexManagerTest(String name) {
        super(name);
    }
        
    public static Test suite() {
        return new TestSuite(IndexManagerTest.class);
    }
        
    public void testSearch() throws Exception {
        IndexManager imgr = RollerFactory.getRoller().getIndexManager();

        WebsiteData website = new WebsiteData();
        website.setHandle("trekker");

        UserData user = new UserData();
        user.setUserName("nimoy");

        WeblogEntryData wd1 = new WeblogEntryData();            
        wd1.setId("dummy1");
        wd1.setAnchor("dummy1");
        wd1.setCreator(user);
        wd1.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        wd1.setPubTime(new Timestamp(System.currentTimeMillis()));
        wd1.setTitle("The Tholian Web");
        wd1.setWebsite(website);
        wd1.setText(
         "When the Enterprise attempts to ascertain the fate of the  "
        +"U.S.S. Defiant which vanished 3 weeks ago, the warp engines  "
        +"begin to lose power, and Spock reports strange sensor readings.");
        imgr.executeIndexOperationNow(
            new AddEntryOperation((IndexManagerImpl) imgr, wd1));

        WeblogEntryData wd2 = new WeblogEntryData();
        wd2.setId("dummy2");
        wd2.setAnchor("dummy2");
        wd2.setCreator(user);
        wd2.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        wd2.setPubTime(new Timestamp(System.currentTimeMillis()));
        wd2.setTitle("A Piece of the Action");
        wd2.setWebsite(website);
        wd2.setText(
          "The crew of the Enterprise attempts to make contact with "
          +"the inhabitants of planet Sigma Iotia II, and Uhura puts Kirk "
          +"in communication with Boss Oxmyx.");
         imgr.executeIndexOperationNow(
             new AddEntryOperation((IndexManagerImpl) imgr, wd2));

        Thread.sleep(1000);

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
