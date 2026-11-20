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

package org.apache.roller.weblogger.ui.core.filters;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.ui.rendering.util.cache.SiteWideCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogFeedCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogPageCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


/**
 * @author Kohei Nozaki
 */
public class InitFilterTest  {

    private static final String SERVER_NAME = "roller.example.com";

    private MockedStatic<WebloggerFactory> factory;

    /**
     * Run with an empty site.absoluteurl, so getAbsoluteContextURL() has to
     * fall back to whatever the request supplies.
     */
    @BeforeEach
    public void setUp() {
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPropertiesManager()).thenReturn(mock(PropertiesManager.class));
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
    }

    @AfterEach
    public void tearDown() {
        factory.close();
    }

    static HttpServletRequest request(String scheme, String host, int port,
            String contextPath, String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        boolean defaultPort = ("http".equals(scheme) && port == 80)
                || ("https".equals(scheme) && port == 443);
        String url = scheme + "://" + host + (defaultPort ? "" : ":" + port) + uri;
        when(request.getScheme()).thenReturn(scheme);
        when(request.isSecure()).thenReturn("https".equals(scheme));
        when(request.getServerName()).thenReturn(host);
        when(request.getServerPort()).thenReturn(port);
        when(request.getContextPath()).thenReturn(contextPath);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        return request;
    }

    /** The absolute URL getAbsoluteContextURL() returns while handling it. */
    private static String urlDuring(InitFilter filter, HttpServletRequest request)
            throws Exception {
        AtomicReference<String> seen = new AtomicReference<>();
        FilterChain chain = (req, res) ->
                seen.set(WebloggerRuntimeConfig.getAbsoluteContextURL());
        filter.doFilter(request, mock(HttpServletResponse.class), chain);
        return seen.get();
    }

    @Test
    public void testGetAbsoluteUrlOnRootWithHttp() throws Exception {
        assertEquals("http://roller.example.com", urlDuring(new InitFilter(),
                request("http", SERVER_NAME, 80, "", "/")));
    }

    @Test
    public void testGetAbsoluteUrlOnRootWithHttps() throws Exception {
        assertEquals("https://roller.example.com", urlDuring(new InitFilter(),
                request("https", SERVER_NAME, 443, "", "/")));
    }

    @Test
    public void testGetAbsoluteUrlWithHttpsOnOtherPort() throws Exception {
        assertEquals("https://roller.example.com:8443/roller", urlDuring(new InitFilter(),
                request("https", SERVER_NAME, 8443, "/roller", "/roller/handle/")));
    }

    @Test
    public void testGetAbsoluteUrlWithHttpOnOtherPort() throws Exception {
        assertEquals("http://roller.example.com:8080/roller", urlDuring(new InitFilter(),
                request("http", SERVER_NAME, 8080, "/roller", "/roller/")));
    }

    @Test
    public void testGetAbsoluteUrlAgainstTop() throws Exception {
        assertEquals("http://roller.example.com/roller", urlDuring(new InitFilter(),
                request("http", SERVER_NAME, 80, "/roller", "/roller/")));
    }

    @Test
    public void testGetAbsoluteUrlAgainstPermalink() throws Exception {
        assertEquals("http://roller.example.com/roller", urlDuring(new InitFilter(),
                request("http", SERVER_NAME, 80, "/roller", "/roller/handle/entry/title")));
    }

    /**
     * Each request sees its own host, and nothing is left behind once the
     * request has been handled.
     */
    @Test
    public void testRequestHostIsNotKeptAfterTheRequest() throws Exception {
        InitFilter filter = new InitFilter();
        assertEquals("http://first.example.com", urlDuring(filter,
                request("http", "first.example.com", 80, "", "/")));
        assertNull(WebloggerRuntimeConfig.getAbsoluteContextURL(),
                "a request's host must not outlive the request");
        assertEquals("http://second.example.com", urlDuring(filter,
                request("http", "second.example.com", 80, "", "/")));
        assertNull(WebloggerRuntimeConfig.getAbsoluteContextURL());
    }

    /**
     * Content rendered with a URL taken from the request must not be stored
     * in the shared caches, where other visitors would be served it.
     */
    @Test
    public void testContentBuiltFromRequestHostIsCachedPerHost() throws Exception {
        String key = "InitFilterTest/" + System.nanoTime();
        AtomicReference<Object> seen = new AtomicReference<>();
        FilterChain store = (req, res) -> {
            String url = WebloggerRuntimeConfig.getAbsoluteContextURL();
            WeblogPageCache.getInstance().put(key, url);
            WeblogFeedCache.getInstance().put(key, url);
            SiteWideCache.getInstance().put(key, url);
        };
        try {
            new InitFilter().doFilter(request("http", "other.example.com", 80, "", "/"),
                    mock(HttpServletResponse.class), store);

            // the same host gets the cached content back
            FilterChain sameHost = (req, res) -> seen.set(WeblogPageCache.getInstance().get(key, 0));
            new InitFilter().doFilter(request("http", "other.example.com", 80, "", "/"),
                    mock(HttpServletResponse.class), sameHost);
            assertEquals("http://other.example.com", seen.get());

            // another host doesn't
            FilterChain otherHost = (req, res) -> seen.set(new Object[] {
                    WeblogPageCache.getInstance().get(key, 0),
                    WeblogFeedCache.getInstance().get(key, 0),
                    SiteWideCache.getInstance().get(key)});
            new InitFilter().doFilter(request("http", SERVER_NAME, 80, "", "/"),
                    mock(HttpServletResponse.class), otherHost);
            Object[] others = (Object[]) seen.get();
            assertNull(others[0]);
            assertNull(others[1]);
            assertNull(others[2]);
        } finally {
            FilterChain cleanup = (req, res) -> {
                WeblogPageCache.getInstance().remove(key);
                WeblogFeedCache.getInstance().remove(key);
                SiteWideCache.getInstance().remove(key);
            };
            new InitFilter().doFilter(request("http", "other.example.com", 80, "", "/"),
                    mock(HttpServletResponse.class), cleanup);
        }
    }

    @Test
    public void testCacheKeysIgnoreTheHostOnceTheSiteUrlIsSet() throws Exception {
        PropertiesManager properties = WebloggerFactory.getWeblogger().getPropertiesManager();
        when(properties.getProperty("site.absoluteurl")).thenReturn(
                new RuntimeConfigProperty("site.absoluteurl", "https://configured.example.org"));
        AtomicReference<String> key = new AtomicReference<>();
        new InitFilter().doFilter(request("http", "other.example.com", 80, "", "/"),
                mock(HttpServletResponse.class), (req, res) -> key.set(WebloggerRuntimeConfig.cacheKey("k")));
        assertEquals("k", key.get());
    }

    /** Caching is unaffected for content that did not use the request host. */
    @Test
    public void testOtherContentIsStillCached() throws Exception {
        String key = "InitFilterTest/" + System.nanoTime();
        FilterChain chain = (req, res) -> WeblogPageCache.getInstance().put(key, "page");
        AtomicReference<Object> seen = new AtomicReference<>();
        FilterChain lookup = (req, res) -> seen.set(WeblogPageCache.getInstance().get(key, 0));
        try {
            new InitFilter().doFilter(request("http", SERVER_NAME, 80, "", "/"),
                    mock(HttpServletResponse.class), chain);
            new InitFilter().doFilter(request("http", SERVER_NAME, 80, "", "/"),
                    mock(HttpServletResponse.class), lookup);
            assertNotNull(seen.get());
        } finally {
            new InitFilter().doFilter(request("http", SERVER_NAME, 80, "", "/"),
                    mock(HttpServletResponse.class), (req, res) -> WeblogPageCache.getInstance().remove(key));
        }
    }

    @Test
    public void testRemoveTrailingSlash() throws Exception {
        assertEquals("http://www.example.com", InitFilter.removeTrailingSlash("http://www.example.com/"));
        assertEquals("http://www.example.com", InitFilter.removeTrailingSlash("http://www.example.com"));
    }

}
