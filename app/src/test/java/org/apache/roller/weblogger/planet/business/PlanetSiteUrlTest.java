/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.planet.business;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.planet.tasks.SyncWebsitesTask;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Planet tasks don't store links to local weblogs while site.absoluteurl is
 * unset, since they have no request to take a host name from.
 */
class PlanetSiteUrlTest {

    private MockedStatic<WebloggerFactory> factory;
    private Weblogger weblogger;

    @BeforeEach
    void setUp() throws Exception {
        PropertiesManager properties = mock(PropertiesManager.class);
        when(properties.getProperty("site.absoluteurl")).thenAnswer(
                invocation -> new RuntimeConfigProperty("site.absoluteurl", ""));
        weblogger = mock(Weblogger.class);
        when(weblogger.getPropertiesManager()).thenReturn(properties);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void localFeedsAreNotFetchedWhileTheSiteUrlIsUnset() throws Exception {
        assertNull(new WebloggerRomeFeedFetcher().fetchSubscription("weblogger:blog", null));
        verify(weblogger, never()).getWeblogManager();
    }

    @Test
    void weblogsAreNotSyncedWhileTheSiteUrlIsUnset() {
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(() -> WebloggerConfig.getBooleanProperty("planet.aggregator.enabled"))
                    .thenReturn(true);

            new SyncWebsitesTask().runTask();
        }
        verify(weblogger, never()).getPlanetManager();
        verify(weblogger, never()).getWeblogManager();
    }
}
