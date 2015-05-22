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

import junit.framework.TestCase;

/**
 * @author Kohei Nozaki
 */
public class InitFilterTest extends TestCase {

    private static final String SERVER_NAME = "roller.example.com";

    public void testGetAbsoluteUrlOnRootWithHttp() throws Exception {
        boolean secure = false;
        String contextPath = "";
        String requestURI = "/";
        String requestURL = "http://roller.example.com/";

        String absoluteUrl = InitFilter.getAbsoluteUrl(secure, SERVER_NAME, contextPath, requestURI, requestURL);
        assertEquals("http://roller.example.com", absoluteUrl);
    }

    public void testGetAbsoluteUrlOnRootWithHttps() throws Exception {
        boolean secure = true;
        String contextPath = "";
        String requestURI = "/";
        String requestURL = "https://roller.example.com/";

        String absoluteUrl = InitFilter.getAbsoluteUrl(secure, SERVER_NAME, contextPath, requestURI, requestURL);
        assertEquals("http://roller.example.com", absoluteUrl);
    }

    public void testGetAbsoluteUrlAgainstTop() throws Exception {
        boolean secure = false;
        String contextPath = "/roller";
        String requestURI = "/roller/";
        String requestURL = "http://roller.example.com/roller/";

        String absoluteUrl = InitFilter.getAbsoluteUrl(secure, SERVER_NAME, contextPath, requestURI, requestURL);
        assertEquals("http://roller.example.com/roller", absoluteUrl);
    }

    public void testGetAbsoluteUrlAgainstPermalink() throws Exception {
        boolean secure = false;
        String contextPath = "/roller";
        String requestURI = "/roller/handle/entry/title";
        String requestURL = "http://roller.example.com/roller/handle/entry/title";

        String absoluteUrl = InitFilter.getAbsoluteUrl(secure, SERVER_NAME, contextPath, requestURI, requestURL);
        assertEquals("http://roller.example.com/roller", absoluteUrl);
    }

    public void testRemoveTrailingSlash() throws Exception {
        assertEquals("http://www.example.com", InitFilter.removeTrailingSlash("http://www.example.com/"));
        assertEquals("http://www.example.com", InitFilter.removeTrailingSlash("http://www.example.com"));
    }

}
