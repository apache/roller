/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.rendering.servlets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPreviewRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The page preview servlet refuses users without a role in the weblog.
 */
class PreviewServletAccessTest {

    @Test
    void userWithoutWeblogRoleGetsNotFound() throws Exception {
        User user = mock(User.class);
        when(user.getEnabled()).thenReturn(Boolean.TRUE);
        Weblog weblog = mock(Weblog.class);
        when(weblog.hasUserPermission(user, WeblogPermission.EDIT_DRAFT)).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        try (MockedConstruction<WeblogPreviewRequest> parsed = mockConstruction(
                WeblogPreviewRequest.class, (mocked, ctx) -> {
                    when(mocked.getWeblog()).thenReturn(weblog);
                    when(mocked.getUser()).thenReturn(user);
                })) {
            new PreviewServlet().doGet(request, response);
        }

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        verify(response, never()).setContentType(anyString());
        verify(response, never()).getWriter();
        verify(response, never()).getOutputStream();
    }
}
