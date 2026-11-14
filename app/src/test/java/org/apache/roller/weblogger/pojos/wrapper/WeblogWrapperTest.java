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
package org.apache.roller.weblogger.pojos.wrapper;

import java.util.List;

import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeblogWrapperTest {

    /** Page menus in weblog.vm and the bundled themes loop over $weblog.pages. */
    @Test
    void pagesListsTheWeblogsTemplates() throws Exception {
        ThemeTemplate about = mock(ThemeTemplate.class);
        when(about.getName()).thenReturn("About");
        when(about.getLink()).thenReturn("about");
        when(about.isNavbar()).thenReturn(true);
        WeblogTheme theme = mock(WeblogTheme.class);
        doReturn(List.of(about)).when(theme).getTemplates();
        Weblog weblog = mock(Weblog.class);
        when(weblog.getTheme()).thenReturn(theme);

        List<ThemeTemplateWrapper> pages = WeblogWrapper.wrap(weblog, mock(URLStrategy.class)).getPages();

        assertEquals(1, pages.size());
        assertEquals("About", pages.get(0).getName());
        assertEquals("about", pages.get(0).getLink());
        assertEquals(true, pages.get(0).isNavbar());
    }
}
