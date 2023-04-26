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

/* Created on March 8, 2023 */

package org.apache.roller.weblogger.ui.rendering.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.search.IndexManagerTest;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.roller.weblogger.business.search.IndexManagerTest.createWeblogEntries;
import static org.apache.roller.weblogger.ui.rendering.util.WeblogSearchRequest.SEARCH_SERVLET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchResultsFeedModelTest {
    User testUser = null;
    Weblog testWeblog = null;
    public static Log log = LogFactory.getLog(IndexManagerTest.class);

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        try {
            testUser = TestUtils.setupUser("entrytestuser");
            testWeblog = TestUtils.setupWeblog("entrytestweblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in test setup", ex);
            throw new Exception("Test setup failed", ex);
        }
    }

    @AfterEach
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

    @Test
    void testBasicOperation() throws Exception {

        IndexManager indexManager =
            WebloggerFactory.getWeblogger().getIndexManager();
        WeblogEntryManager entryManager = WebloggerFactory.getWeblogger().getWeblogEntryManager();

        List<WeblogEntry> entries = createWeblogEntries(testWeblog, indexManager, entryManager);

        try {
            SearchResultsFeedModel model = executeSearch("Enterprise");
            assertEquals(2, model.getResults().size());

            model = executeSearch("Tholian");
            assertEquals(1, model.getResults().size());

        } finally {
            for (WeblogEntry entry : entries) {
                indexManager.removeEntryIndexOperation(TestUtils.getManagedWeblogEntry(entry));
            }
            indexManager.removeWeblogIndex(testWeblog);
        }
    }

    SearchResultsFeedModel executeSearch(String term) throws WebloggerException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn(SEARCH_SERVLET);
        when(request.getRequestURL()).thenReturn(
            new StringBuffer(String.format("http://localhost/%s", SEARCH_SERVLET)));
        when(request.getPathInfo()).thenReturn(null);

//        WeblogSearchRequest searchRequest = new WeblogSearchRequest(request);
//        searchRequest.setWeblogHandle(testWeblog.getHandle());
//        searchRequest.setQuery(term);

        WeblogFeedRequest feedRequest = new WeblogFeedRequest();
        feedRequest.setWeblog(testWeblog);
        feedRequest.setTerm(term);

        Map<String, Object> initData = new HashMap<>();
        //initData.put("searchRequest", searchRequest);
        initData.put("parsedRequest", feedRequest);

        SearchResultsFeedModel model = new SearchResultsFeedModel();
        model.init(initData);
        return model;
    }


}