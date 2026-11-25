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

import java.util.Collections;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Templates list a category's entries and an entry's comments through the
 * wrappers. Whatever a template asks for, it gets only published entries and
 * approved comments.
 */
public class TemplateLookupsPublishedOnlyTest {

    @Test
    public void categoryEntriesArePublishedOnlyWhateverTheTemplateAsks() throws Exception {
        WeblogCategory category = mock(WeblogCategory.class);
        WeblogEntry published = new WeblogEntry();
        published.setAnchor("published");
        when(category.retrieveWeblogEntries(true)).thenReturn(Collections.singletonList(published));

        WeblogCategoryWrapper wrapper = WeblogCategoryWrapper.wrap(category, null);
        assertEquals(1, wrapper.retrieveWeblogEntries(false).size());
        assertEquals(1, wrapper.retrieveWeblogEntries(true).size());
        verify(category, never()).retrieveWeblogEntries(false);
    }

    @Test
    public void entryCommentsAreApprovedOnlyWhateverTheTemplateAsks() {
        WeblogEntry entry = mock(WeblogEntry.class);
        WeblogEntryComment approved = new WeblogEntryComment();
        when(entry.getComments(true, true)).thenReturn(Collections.singletonList(approved));

        WeblogEntryWrapper wrapper = WeblogEntryWrapper.wrap(entry, null);
        assertEquals(1, wrapper.getComments(false, false).size());
        assertEquals(1, wrapper.getComments(true, false).size());
        assertEquals(1, wrapper.getComments(false, true).size());
        verify(entry, never()).getComments(anyBoolean(), eq(false));
        verify(entry, never()).getComments(eq(false), anyBoolean());
    }
}
