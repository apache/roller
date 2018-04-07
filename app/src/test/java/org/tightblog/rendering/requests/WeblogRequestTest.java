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

import org.junit.Before;
import org.junit.Test;
import org.springframework.mobile.device.DeviceType;

import javax.servlet.http.HttpServletRequest;

import java.security.Principal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeblogRequestTest {

    private HttpServletRequest mockRequest;

    @Before
    public void initializeMocks() {
        mockRequest = mock(HttpServletRequest.class);
    }

    @Test
    public void testParseRequest() {
        when(mockRequest.getParameter("page")).thenReturn("24");
        when(mockRequest.getQueryString()).thenReturn("a=2&b=3");
        Principal mockPrincipal = mock(Principal.class);
        when(mockRequest.getUserPrincipal()).thenReturn(mockPrincipal);
        when(mockPrincipal.getName()).thenReturn("bob");
        when(mockRequest.getPathInfo()).thenReturn("/myblog/category/categoryname/tag/tagname");

        WeblogRequest.Creator creator = new WeblogRequest.Creator();
        WeblogRequest wr = creator.create(mockRequest);
        assertEquals("24", wr.getRequestParameter("page"));
        assertEquals("a=2&b=3", wr.getQueryString());
        assertEquals(DeviceType.NORMAL, wr.getDeviceType());
        assertEquals("bob", wr.getAuthenticatedUser());
        assertTrue(wr.isLoggedIn());
        assertEquals("myblog", wr.getWeblogHandle());
        assertEquals("category/categoryname/tag/tagname", wr.getExtraPathInfo());
        assertEquals(24, wr.getPageNum());
    }

    @Test
    public void testIllegalArgumentExceptionWithNoWeblog() {
        when(mockRequest.getRequestURL()).thenReturn(
                new StringBuffer("http://mytesturl/abc"));
        when(mockRequest.getPathInfo()).thenReturn("//category/categoryname/tag/tagname");
        WeblogRequest.Creator creator = new WeblogRequest.Creator();
        try {
            WeblogRequest wr = creator.create(mockRequest);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "Not a weblog request, http://mytesturl/abc");
        }
    }
}
