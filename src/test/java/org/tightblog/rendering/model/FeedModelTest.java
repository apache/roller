/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.rendering.model;

import org.junit.Before;
import org.junit.Test;
import org.tightblog.config.DynamicProperties;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.generators.WeblogEntryListGenerator;
import org.tightblog.rendering.requests.WeblogFeedRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FeedModelTest {

    private WeblogEntryListGenerator mockWeblogEntryListGenerator;
    private WeblogFeedRequest feedRequest;
    private FeedModel feedModel;
    private Weblog weblog;
    private DynamicProperties dp;

    @Before
    public void initialize() {
        WeblogEntryManager mockWeblogEntryManager = mock(WeblogEntryManager.class);
        mockWeblogEntryListGenerator = mock(WeblogEntryListGenerator.class);
        feedRequest = new WeblogFeedRequest();
        weblog = new Weblog();
        feedRequest.setWeblog(weblog);
        feedRequest.setWeblogCategoryName("stamps");
        feedRequest.setTag("collectibles");
        feedRequest.setPageNum(16);
        feedRequest.setSiteWide(true);
        dp = new DynamicProperties();
        feedModel = new FeedModel(mockWeblogEntryListGenerator, mockWeblogEntryManager, dp, 20);
        Map<String, Object> initVals = new HashMap<>();
        initVals.put("parsedRequest", feedRequest);
        feedModel.init(initVals);
    }

    @Test
    public void getWeblogEntriesPager() {
        feedModel.getWeblogEntriesPager();
        verify(mockWeblogEntryListGenerator).getChronoPager(weblog, null, "stamps",
                "collectibles", 16, 20, true);
    }

    @Test
    public void getLastUpdated() {
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);
        weblog.setLastModified(twoDaysAgo);
        dp.setLastSitewideChange(threeDaysAgo);

        Instant test = feedModel.getLastUpdated();
        assertEquals(threeDaysAgo, test);

        feedRequest.setSiteWide(false);
        test = feedModel.getLastUpdated();
        assertEquals(twoDaysAgo, test);
    }
}
