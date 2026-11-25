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

package org.apache.roller.weblogger.ui.rendering;

import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class WeblogRequestMapperReservedTest {

    @Test
    void pathsRollerServesItselfAreReserved() {
        // the paths Spring Security serves or leaves out of security.access rules
        for (String name : new String[] {"roller-ui", "images", "theme", "webjars", "oauth2"}) {
            assertTrue(WeblogRequestMapper.isReservedHandle(name), name);
        }
        assertTrue(WeblogRequestMapper.isReservedHandle("WebJars"), "regardless of case");
        assertFalse(WeblogRequestMapper.isReservedHandle("family"));
        assertFalse(WeblogRequestMapper.isReservedHandle(null));
    }

    @Test
    void aWeblogWithAReservedHandleIsNotShownInAnyCase() throws Exception {
        Weblogger weblogger = mock(Weblogger.class);
        WeblogManager weblogs = mock(WeblogManager.class);
        when(weblogger.getWeblogManager()).thenReturn(weblogs);
        // as a database that ignores case finds the weblog whatever the URL's case
        when(weblogs.getWeblogByHandle(anyString())).thenReturn(mock(Weblog.class));

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            WeblogRequestMapper mapper = new WeblogRequestMapper();

            for (String path : new String[] {"/webjars/", "/WebJars/", "/OAuth2/", "/Roller-UI/"}) {
                assertFalse(mapper.handleRequest(new MockHttpServletRequest("GET", path),
                        new MockHttpServletResponse()), path);
            }
            MockHttpServletResponse response = new MockHttpServletResponse();
            assertTrue(mapper.handleRequest(new MockHttpServletRequest("GET", "/family/"), response));
            assertEquals("/roller-ui/rendering/page/family", response.getForwardedUrl());
        }
    }
}
