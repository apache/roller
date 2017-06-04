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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.ui.filters;

import org.tightblog.WebloggerTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Kohei Nozaki
 */
public class InitFilterTest extends WebloggerTest {

    private static final String SERVER_NAME = "tightblog.example.com";

    @Test
    public void testGetAbsoluteUrlOnRootWithHttp() throws Exception {
        String contextPath = "";
        String requestURI = "/";
        String requestURL = "http://tightblog.example.com/";

        String absoluteUrl = InitFilter.getAbsoluteUrl(false, SERVER_NAME, contextPath, requestURI, requestURL);
        assertEquals("http://tightblog.example.com", absoluteUrl);
    }

    @Test
    public void testGetAbsoluteUrlOnRootWithHttps() throws Exception {
        String contextPath = "";
        String requestURI = "/";
        String requestURL = "https://tightblog.example.com/";

        String absoluteUrl = InitFilter.getAbsoluteUrl(true, SERVER_NAME, contextPath, requestURI, requestURL);
        assertEquals("http://tightblog.example.com", absoluteUrl);
    }

    @Test
    public void testGetAbsoluteUrlAgainstTop() throws Exception {
        String contextPath = "/tightblog";
        String requestURI = "/tightblog/";
        String requestURL = "http://tightblog.example.com/tightblog/";

        String absoluteUrl = InitFilter.getAbsoluteUrl(false, SERVER_NAME, contextPath, requestURI, requestURL);
        assertEquals("http://tightblog.example.com/tightblog", absoluteUrl);
    }

    @Test
    public void testGetAbsoluteUrlAgainstPermalink() throws Exception {
        String contextPath = "/tightblog";
        String requestURI = "/tightblog/handle/entry/title";
        String requestURL = "http://tightblog.example.com/tightblog/handle/entry/title";

        String absoluteUrl = InitFilter.getAbsoluteUrl(false, SERVER_NAME, contextPath, requestURI, requestURL);
        assertEquals("http://tightblog.example.com/tightblog", absoluteUrl);
    }

    @Test
    public void testRemoveTrailingSlash() throws Exception {
        assertEquals("http://www.example.com", InitFilter.removeTrailingSlash("http://www.example.com/"));
        assertEquals("http://www.example.com", InitFilter.removeTrailingSlash("http://www.example.com"));
    }

}
