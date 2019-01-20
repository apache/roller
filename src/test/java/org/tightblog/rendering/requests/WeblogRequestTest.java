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
package org.tightblog.rendering.requests;

import org.junit.Test;
import org.springframework.mobile.device.DeviceType;
import org.tightblog.TestUtils;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class WeblogRequestTest {

    @Test
    public void testParseRequest() {
        HttpServletRequest mockRequest = TestUtils.createMockServletRequestForWeblogFeedRequest();
        when(mockRequest.getParameter("page")).thenReturn("24");
        when(mockRequest.getQueryString()).thenReturn("a=2&b=3");
        when(mockRequest.getServletPath()).thenReturn(
                "/tb-ui/rendering/page/myblog/category/categoryname/tag/tagname");

        WeblogRequest wr = WeblogRequest.create(mockRequest);
        assertEquals("24", wr.getRequestParameter("page"));
        assertEquals("a=2&b=3", wr.getQueryString());
        assertEquals(DeviceType.NORMAL, wr.getDeviceType());
        assertEquals("bob", wr.getAuthenticatedUser());
        assertTrue(wr.isLoggedIn());
        assertEquals(TestUtils.BLOG_HANDLE, wr.getWeblogHandle());
        assertEquals("category/categoryname/tag/tagname", wr.getExtraPathInfo());
        assertEquals(24, wr.getPageNum());

        when(mockRequest.getUserPrincipal()).thenReturn(null);
        when(mockRequest.getParameter("page")).thenReturn("invalid");
        wr = WeblogRequest.create(mockRequest);
        assertFalse(wr.isLoggedIn());
        assertEquals(0, wr.getPageNum());
    }
}
