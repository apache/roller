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
package org.apache.roller.weblogger.ui.struts2.editor;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The categories page saves a new display order, reports entry counts, and offers the
 * media library for category images only when uploads are on.
 */
class CategoriesTest {

    private MockedStatic<WebloggerFactory> factory;
    private MockedStatic<CacheManager> cache;
    private Weblogger weblogger;
    private WeblogEntryManager entries;
    private WeblogManager weblogs;
    private Weblog weblog;
    private WeblogCategory java;
    private WeblogCategory roller;
    private WeblogCategory general;
    private Categories action;

    @BeforeEach
    void setUp() throws Exception {
        entries = mock(WeblogEntryManager.class);
        weblogger = mock(Weblogger.class);
        weblogs = mock(WeblogManager.class);
        when(weblogger.getWeblogEntryManager()).thenReturn(entries);
        when(weblogger.getWeblogManager()).thenReturn(weblogs);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        cache = mockStatic(CacheManager.class);

        weblog = mock(Weblog.class);
        java = category("c-java", "Java", 0);
        roller = category("c-roller", "Roller", 1);
        general = category("c-general", "General", 2);
        when(entries.getWeblogCategories(weblog)).thenReturn(Arrays.asList(java, roller, general));
        when(weblog.getWeblogCategories()).thenReturn(new ArrayList<>(List.of(java, roller, general)));
        when(entries.getWeblogCategoryEntryCounts(weblog)).thenReturn(Map.of("c-java", 17L));

        action = spy(new Categories());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        action.setActionWeblog(weblog);
    }

    @AfterEach
    void tearDown() {
        cache.close();
        factory.close();
    }

    private WeblogCategory category(String id, String name, int position) throws Exception {
        WeblogCategory category = new WeblogCategory();
        category.setId(id);
        category.setName(name);
        category.setPosition(position);
        when(entries.getWeblogCategory(weblog, id)).thenReturn(category);
        return category;
    }

    @Test
    void savesNewOrder() throws Exception {
        action.setCategoryIds(List.of("c-general", "c-java", "c-roller"));

        action.reorder();

        assertFalse(action.hasActionErrors());
        assertEquals(0, general.getPosition());
        assertEquals(1, java.getPosition());
        assertEquals(2, roller.getPosition());
        // one save of the weblog, not one per category
        verify(weblogs).saveWeblog(weblog);
        verify(entries, never()).saveWeblogCategory(any());
        verify(weblogger).flush();
    }

    @Test
    void putsTheWeblogsOwnCategoryListInTheNewOrder() throws Exception {
        action.setCategoryIds(List.of("c-general", "c-java", "c-roller"));

        action.reorder();

        assertEquals(List.of(general, java, roller), weblog.getWeblogCategories());
    }

    @Test
    void readsEachCategoryOnlyOnce() throws Exception {
        action.setCategoryIds(List.of("c-general", "c-java", "c-roller"));

        action.reorder();

        verify(entries, never()).getWeblogCategory(any(Weblog.class), anyString());
    }

    @Test
    void rejectsAnOrderThatLeavesOutACategory() throws Exception {
        action.setCategoryIds(List.of("c-general", "c-java"));

        action.reorder();

        assertTrue(action.hasActionErrors());
        assertEquals(2, general.getPosition());
        verify(weblogs, never()).saveWeblog(any());
        verify(weblogger, never()).flush();
    }

    @Test
    void rejectsAnOrderWithAnotherWeblogsCategory() throws Exception {
        action.setCategoryIds(List.of("c-general", "c-java", "c-elsewhere"));

        action.reorder();

        assertTrue(action.hasActionErrors());
        verify(weblogs, never()).saveWeblog(any());
    }

    @Test
    void choosesImagesOnlyFromTheMediaLibrary() {
        assertEquals("mediaFileView.image", action.getImageChooserType());
    }

    @Test
    void reportsEntryCountsAndUse() {
        when(weblog.getBloggerCategory()).thenReturn(general);

        action.execute();

        assertEquals(17, action.getEntryCount("c-java"));
        assertEquals(0, action.getEntryCount("c-roller"));
        assertTrue(action.isInUse("c-java"));
        assertFalse(action.isInUse("c-roller"));
        // blogging clients post to it, so removing it needs a replacement
        assertTrue(action.isInUse("c-general"));
    }

    @Test
    void offersTheMediaLibraryOnlyWhenUploadsAreOn() {
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")).thenReturn(true);
            assertTrue(action.isMediaLibraryEnabled());
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")).thenReturn(false);
            assertFalse(action.isMediaLibraryEnabled());
        }
    }
}
