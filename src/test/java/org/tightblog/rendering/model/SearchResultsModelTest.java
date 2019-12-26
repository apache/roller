/*
 * Copyright 2019 the original author or authors.
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
import org.tightblog.rendering.service.CalendarGenerator;
import org.tightblog.service.ThemeManager;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.rendering.service.WeblogEntryListGenerator;
import org.tightblog.dao.WeblogEntryDao;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SearchResultsModelTest {

    private SearchResultsModel searchResultsModel;
    private UserManager mockUserManager;
    private WeblogEntryManager mockWeblogEntryManager;
    private ThemeManager mockThemeManager;
    private WeblogEntryListGenerator mockWELG;
    private CalendarGenerator mockCalendarGenerator;
    private WeblogEntryDao mockWeblogEntryDao;
    private LuceneIndexer mockLuceneIndexer;

    @Before
    public void initialize() {
        mockUserManager = mock(UserManager.class);
        WeblogManager mockWeblogManager = mock(WeblogManager.class);
        mockWeblogEntryManager = mock(WeblogEntryManager.class);
        mockThemeManager = mock(ThemeManager.class);
        mockWELG = mock(WeblogEntryListGenerator.class);
        mockCalendarGenerator = mock(CalendarGenerator.class);
        mockWeblogEntryDao = mock(WeblogEntryDao.class);
        mockLuceneIndexer = mock(LuceneIndexer.class);

        searchResultsModel = new SearchResultsModel(
                mockUserManager, mockWeblogManager, mockWeblogEntryManager,
                mockThemeManager, mockWELG, mockCalendarGenerator,
                25, mockWeblogEntryDao, mockLuceneIndexer);
    }

    @Test
    public void testAccessors() {
        assertEquals(mockUserManager, searchResultsModel.getUserManager());
        assertEquals(mockWeblogEntryManager, searchResultsModel.getWeblogEntryManager());
        assertEquals(mockThemeManager, searchResultsModel.getThemeManager());
        assertEquals(mockWELG, searchResultsModel.getWeblogEntryListGenerator());
        assertEquals(mockCalendarGenerator, searchResultsModel.getCalendarGenerator());
        assertEquals(25, searchResultsModel.getMaxEntriesPerPage());
        assertEquals(mockWeblogEntryDao, searchResultsModel.getWeblogEntryDao());
        assertEquals(mockLuceneIndexer, searchResultsModel.getLuceneIndexer());
    }
}
