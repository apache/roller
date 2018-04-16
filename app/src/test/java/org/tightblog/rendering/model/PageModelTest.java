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
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.rendering.requests.WeblogPageRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PageModelTest {

    private UserManager mockUserManager;
    private WeblogManager mockWeblogManager;
    private PageModel pageModel;
    private WeblogPageRequest pageRequest;

    @Before
    public void initialize() {
        mockUserManager = mock(UserManager.class);
        mockWeblogManager = mock(WeblogManager.class);
        pageModel = new PageModel();
        pageModel.setUserManager(mockUserManager);
        pageModel.setWeblogManager(mockWeblogManager);
        Weblog weblog = new Weblog();
        weblog.setHandle("testblog");
        pageRequest = new WeblogPageRequest();
        pageRequest.setWeblog(weblog);
        pageRequest.setWeblogHandle(weblog.getHandle());
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", pageRequest);
        pageModel.init(initData);
    }

    @Test
    public void testCheckUserRights() {
        // if preview, always false
        pageModel.setPreview(true);
        assertFalse(pageModel.isUserBlogOwner());
        assertFalse(pageModel.isUserBlogPublisher());

        // authenticated user is null, so both should be false
        pageModel.setPreview(false);
        assertFalse(pageModel.isUserBlogOwner());
        assertFalse(pageModel.isUserBlogPublisher());

        // authenticated user has neither role
        pageRequest.setAuthenticatedUser("bob");
        when(mockUserManager.checkWeblogRole("bob", "testblog", WeblogRole.POST)).thenReturn(false);
        when(mockUserManager.checkWeblogRole("bob", "testblog", WeblogRole.OWNER)).thenReturn(false);
        assertFalse(pageModel.isUserBlogOwner());
        assertFalse(pageModel.isUserBlogPublisher());

        // authenticated user has lower role
        when(mockUserManager.checkWeblogRole("bob", "testblog", WeblogRole.POST)).thenReturn(true);
        assertFalse(pageModel.isUserBlogOwner());
        assertTrue(pageModel.isUserBlogPublisher());

        // authenticated user has both roles
        when(mockUserManager.checkWeblogRole("bob", "testblog", WeblogRole.OWNER)).thenReturn(true);
        assertTrue(pageModel.isUserBlogOwner());
        assertTrue(pageModel.isUserBlogPublisher());
    }
}
