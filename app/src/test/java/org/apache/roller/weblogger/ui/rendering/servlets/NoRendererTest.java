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

package org.apache.roller.weblogger.ui.rendering.servlets;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NoRendererTest {

    private HttpServletResponse response;
    private Template page;
    private Weblog weblog;
    private Log log;

    @BeforeEach
    void setUp() {
        response = mock(HttpServletResponse.class);
        page = mock(Template.class);
        when(page.getId()).thenReturn("t1");
        when(page.getName()).thenReturn("Weblog");
        weblog = mock(Weblog.class);
        when(weblog.getHandle()).thenReturn("blog");
        log = mock(Log.class);
    }

    @Test
    void logsOneWarningForATemplateWithNoContent() throws Exception {
        when(page.getTemplateRendition(RenditionType.STANDARD)).thenReturn(null);

        NoRenderer.sendNotFound(response, page, weblog, log);

        verify(log).warn(org.mockito.ArgumentMatchers.contains("it has no content"));
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void doesNotRepeatWhatTheRendererFactoryLogged() throws Exception {
        when(page.getTemplateRendition(RenditionType.STANDARD)).thenReturn(mock(TemplateRendition.class));

        NoRenderer.sendNotFound(response, page, weblog, log);

        verify(log, never()).warn(anyString());
        verify(log, never()).error(anyString());
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
