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

package org.apache.roller.weblogger.config;

import org.apache.roller.planet.business.MultiPlanetURLStrategy;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.planet.business.PlanetURLStrategy;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.ui.rendering.util.cache.PlanetCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.SiteWideCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogFeedCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogPageCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Links and cached content while site.absoluteurl is unset, and when it is
 * set while content is being rendered.
 */
class SiteUrlWhileUnsetTest {

    private static final String SITE_URL = "https://blog.example.org/roller";

    private MockedStatic<WebloggerFactory> factory;
    private PropertiesManager properties;
    private String key;

    @BeforeEach
    void setUp() throws Exception {
        properties = mock(PropertiesManager.class);
        // a fresh copy on each read, as a new persistence session would load
        when(properties.getProperty("site.absoluteurl")).thenAnswer(
                invocation -> new RuntimeConfigProperty("site.absoluteurl", ""));
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPropertiesManager()).thenReturn(properties);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        key = "SiteUrlWhileUnsetTest/" + System.nanoTime();
    }

    @AfterEach
    void tearDown() {
        WebloggerRuntimeConfig.clearRequestAbsoluteContextURL();
        WeblogPageCache.getInstance().clear();
        WeblogFeedCache.getInstance().clear();
        SiteWideCache.getInstance().clear();
        PlanetCache.getInstance().clear();
        factory.close();
    }

    private void siteUrlIsSet() throws Exception {
        when(properties.getProperty("site.absoluteurl")).thenAnswer(
                invocation -> new RuntimeConfigProperty("site.absoluteurl", SITE_URL));
    }

    private static void putEverywhere(String key, Object value) {
        WeblogPageCache.getInstance().put(key, value);
        WeblogFeedCache.getInstance().put(key, value);
        SiteWideCache.getInstance().put(key, value);
        PlanetCache.getInstance().put(key, value);
    }

    private static void assertNothingCached(String key) {
        assertNull(WeblogPageCache.getInstance().get(key, 0));
        assertNull(WeblogFeedCache.getInstance().get(key, 0));
        assertNull(SiteWideCache.getInstance().get(key));
        assertNull(PlanetCache.getInstance().get(key));
    }

    @Test
    void aRenderThatStartedWithoutTheSiteUrlIsNotServedToOtherHosts() throws Exception {
        // a request starts while the URL is unset and builds its links from
        // its own host name
        WebloggerRuntimeConfig.setRequestAbsoluteContextURL("http://other.example.com");
        assertEquals("http://other.example.com", WebloggerRuntimeConfig.getAbsoluteContextURL());

        // an administrator saves the site URL before that request is cached
        siteUrlIsSet();
        assertEquals("http://other.example.com", WebloggerRuntimeConfig.getAbsoluteContextURL(),
                "links in one rendering must all use the same URL");
        putEverywhere(key, "rendered for other.example.com");
        WebloggerRuntimeConfig.clearRequestAbsoluteContextURL();

        // a later request uses the shared cache key
        WebloggerRuntimeConfig.setRequestAbsoluteContextURL("https://blog.example.org/roller");
        assertEquals(SITE_URL, WebloggerRuntimeConfig.getAbsoluteContextURL());
        assertNothingCached(key);
    }

    @Test
    void backgroundRenderingIsNotCachedWhileTheSiteUrlIsUnset() throws Exception {
        // the cache warmup job renders feeds outside of any request
        WeblogFeedCache.getInstance().put(key, "warmed without a site URL");
        PlanetCache.getInstance().put(key, "warmed without a site URL");

        siteUrlIsSet();
        WebloggerRuntimeConfig.setRequestAbsoluteContextURL(SITE_URL);
        assertNull(WeblogFeedCache.getInstance().get(key, 0));
        assertNull(PlanetCache.getInstance().get(key));
    }

    @Test
    void backgroundRenderingIsCachedOnceTheSiteUrlIsSet() throws Exception {
        siteUrlIsSet();
        WeblogFeedCache.getInstance().put(key, "warmed");

        WebloggerRuntimeConfig.setRequestAbsoluteContextURL("http://other.example.com");
        assertEquals("warmed", WeblogFeedCache.getInstance().get(key, 0));
    }

    @Test
    void savingTheSiteUrlClearsContentCachedPerHost() throws Exception {
        WebloggerRuntimeConfig.setRequestAbsoluteContextURL("http://other.example.com");
        putEverywhere(key, "rendered for other.example.com");
        when(properties.compareAndSetProperty(eq("site.absoluteurl"), eq(""), any()))
                .thenReturn(true);

        assertTrue(WebloggerRuntimeConfig.saveAbsoluteContextURLIfUnset(SITE_URL));

        assertNothingCached(key);
    }

    @Test
    void planetLinksUseTheRequestUrlWhileTheSiteUrlIsUnset() {
        WebloggerRuntimeConfig.setRequestAbsoluteContextURL("http://other.example.com");

        assertEquals("http://other.example.com/ignored/",
                new MultiPlanetURLStrategy().getPlanetURL("ignored"));
        assertEquals("http://other.example.com/ignored/group/all/feed/rss",
                new MultiPlanetURLStrategy().getPlanetGroupFeedURL("ignored", "all", "rss"));
        assertEquals("http://other.example.com",
                new PlanetURLStrategy().getPlanetURL("ignored"));
    }

    @Test
    void planetLinksUseTheSiteUrlOnceSet() throws Exception {
        siteUrlIsSet();
        WebloggerRuntimeConfig.setRequestAbsoluteContextURL("http://other.example.com");

        assertEquals(SITE_URL + "/ignored/", new MultiPlanetURLStrategy().getPlanetURL("ignored"));
        assertEquals(SITE_URL, new PlanetURLStrategy().getPlanetURL("ignored"));
    }
}
