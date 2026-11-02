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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.Map;

import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.MediaFileFilter.MediaFileOrder;
import org.apache.roller.weblogger.pojos.MediaFileFilter.SizeFilterType;
import org.apache.roller.weblogger.pojos.MediaFileType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaFileSearchBeanTest {

    @Test
    void browsingAFolderIsNewestFirstWithNoCriteria() {
        MediaFileSearchBean bean = new MediaFileSearchBean();
        bean.setType("mediaFileView.any");
        bean.setName("  ");
        assertFalse(bean.isSearch());
        assertTrue(bean.toParams().isEmpty());

        MediaFileFilter filter = new MediaFileFilter();
        bean.copyTo(filter);
        assertEquals(MediaFileOrder.DATE_UPLOADED_DESC, filter.getOrder());
        assertNull(filter.getName());
        assertNull(filter.getType());
        assertEquals(0, filter.getStartIndex());
        assertEquals(MediaFileSearchBean.PAGE_SIZE, filter.getLength());
    }

    @Test
    void criteriaSortAndScopeRoundTripAsParameters() {
        MediaFileSearchBean bean = new MediaFileSearchBean();
        bean.setName(" logo ");
        bean.setType("mediaFileView.image");
        bean.setSize(2);
        bean.setSizeFilterType("mediaFileView.gt");
        bean.setSizeUnit("mediaFileView.kb");
        bean.setTags("red  blue");
        bean.setScope("all");
        bean.setSortBy("name");
        bean.setPage(3);
        assertTrue(bean.isSearch());
        assertTrue(bean.isFiltersSet());

        Map<String, String> params = bean.toParams();
        assertEquals("logo", params.get("bean.name"));
        assertEquals("mediaFileView.image", params.get("bean.type"));
        assertEquals("2", params.get("bean.size"));
        assertEquals("red  blue", params.get("bean.tags"));
        assertEquals("all", params.get("bean.scope"));
        assertEquals("name", params.get("bean.sortBy"));
        assertFalse(params.containsKey("bean.page"), "the pager adds the page");

        MediaFileFilter filter = new MediaFileFilter();
        bean.copyTo(filter);
        assertEquals("logo", filter.getName());
        assertEquals(MediaFileType.IMAGE, filter.getType());
        assertEquals(2048, filter.getSize());
        assertEquals(SizeFilterType.GT, filter.getSizeFilterType());
        assertEquals(java.util.List.of("red", "blue"), filter.getTags());
        assertEquals(MediaFileOrder.NAME, filter.getOrder());
        assertEquals(3 * MediaFileSearchBean.PAGE_SIZE, filter.getStartIndex());
    }

    @Test
    void unknownValuesFallBackToDefaults() {
        MediaFileSearchBean bean = new MediaFileSearchBean();
        bean.setSortBy("dateUploaded; drop");
        bean.setScope("everything");
        bean.setPage(-4);
        assertEquals(MediaFileSearchBean.SORT_NEWEST, bean.getSortBy());
        assertEquals(MediaFileSearchBean.SCOPE_FOLDER, bean.getScope());
        assertEquals(0, bean.getPage());
    }

    @Test
    void hugePageStillGivesAPositiveStartIndex() {
        MediaFileSearchBean bean = new MediaFileSearchBean();
        bean.setPage(50_000_000);
        MediaFileFilter filter = new MediaFileFilter();
        bean.copyTo(filter);
        assertTrue(filter.getStartIndex() >= 0, "start index " + filter.getStartIndex());
    }

    @Test
    void allFoldersScopeOnlyMattersWhenSearching() {
        MediaFileSearchBean bean = new MediaFileSearchBean();
        bean.setScope("all");
        assertFalse(bean.toParams().containsKey("bean.scope"));
    }

    @Test
    void detailsPageKeepsOnlyAUrlSafeReturnQuery() {
        MediaFileEdit edit = new MediaFileEdit();
        edit.setReturnQuery("weblog=roller&directoryId=abc&bean.name=a%20b&bean.page=2");
        assertEquals("weblog=roller&directoryId=abc&bean.name=a%20b&bean.page=2", edit.getReturnQuery());

        edit.setReturnQuery("weblog=roller\"><script>");
        assertNull(edit.getReturnQuery());

        edit.setReturnQuery("x=1#//evil.example");
        assertNull(edit.getReturnQuery());
    }
}
