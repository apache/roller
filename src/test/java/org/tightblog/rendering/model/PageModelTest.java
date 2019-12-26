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
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntryTagAggregate;
import org.tightblog.rendering.service.CalendarGenerator;
import org.tightblog.service.ThemeManager;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.rendering.service.WeblogEntryListGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PageModelTest {

    private PageModel pageModel;
    private UserManager mockUserManager;
    private WeblogEntryManager mockWeblogEntryManager;
    private WeblogEntryListGenerator mockWELG;
    private ThemeManager mockThemeManager;
    private WeblogManager mockWeblogManager;
    private CalendarGenerator mockCalendarGenerator;

    @Before
    public void initialize() {
        mockUserManager = mock(UserManager.class);
        mockWeblogManager = mock(WeblogManager.class);
        mockWeblogEntryManager = mock(WeblogEntryManager.class);
        mockThemeManager = mock(ThemeManager.class);
        mockWELG = mock(WeblogEntryListGenerator.class);
        mockCalendarGenerator = mock(CalendarGenerator.class);
        pageModel = new PageModel(
                mockUserManager, mockWeblogManager, mockWeblogEntryManager,
                mockThemeManager, mockWELG, mockCalendarGenerator,
                30
        );
    }

    @Test
    public void testAccessors() {
        assertEquals(mockUserManager, pageModel.getUserManager());
        assertEquals(mockWeblogEntryManager, pageModel.getWeblogEntryManager());
        assertEquals(mockThemeManager, pageModel.getThemeManager());
        assertEquals(mockWELG, pageModel.getWeblogEntryListGenerator());
        assertEquals(mockCalendarGenerator, pageModel.getCalendarGenerator());
        assertEquals(30, pageModel.getMaxEntriesPerPage());
    }

    @Test
    public void testPassThroughMethods() {
        Weblog weblog = new Weblog();
        when(mockWeblogManager.getAnalyticsTrackingCode(weblog)).thenReturn("code123");
        assertEquals("", pageModel.getAnalyticsTrackingCode(weblog, true));
        assertEquals("code123", pageModel.getAnalyticsTrackingCode(weblog, false));

        List<WeblogEntryTagAggregate> testList = new ArrayList<>();
        when(mockWeblogManager.getPopularTags(weblog, 0, 15)).thenReturn(testList);
        assertEquals(testList, pageModel.getPopularTags(weblog, 15));
    }
}
