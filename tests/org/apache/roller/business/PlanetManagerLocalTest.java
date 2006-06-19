/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.business;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.roller.RollerTestBase;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.ui.admin.struts.actions.RefreshEntriesTask;
import org.apache.roller.ui.core.tasks.SyncWebsitesTask;

/**
 * Test database implementation of PlanetManager for local feeds.
 * @author Dave Johnson
 */
public class PlanetManagerLocalTest extends RollerTestBase {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PlanetManagerLocalTest.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        super.setUpTestWeblogs();
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        super.tearDownTestWeblogs();
    }
    public void testRefreshEntries() {
        try {      
            PlanetManager planet = getRoller().getPlanetManager();
            
            // run sync task to fill aggregator with websites created by super
            SyncWebsitesTask syncTask = new SyncWebsitesTask();
            syncTask.init(getRoller(), "dummy");
            syncTask.run();           
            
            RefreshEntriesTask refreshTask = new RefreshEntriesTask();
            refreshTask.init(getRoller(), "dummy");
            refreshTask.run();
            
            List agg = planet.getAggregation(null, null, 0, -1);
            int size = agg.size();
            assertEquals(mBlogCount * mExpectedPublishedEntryCount, size);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public static Test suite() {
        return new TestSuite(PlanetManagerLocalTest.class);
    }
    
    
}

