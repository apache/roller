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

import java.util.HashSet;
import java.util.Set;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tag names reach templates through the entry and tag wrappers. They must not
 * carry HTML-significant characters, whichever way the tag was stored.
 */
public class WeblogEntryTagWrapperTest {

    private static WeblogEntryTag storedTag(String name) {
        WeblogEntryTag tag = new WeblogEntryTag();
        tag.setName(name);
        return tag;
    }

    private static boolean hasHtmlCharacters(String s) {
        return s.matches(".*[<>&\"'`].*");
    }

    @Test
    public void tagsSetOnAnEntryAreFiltered() throws Exception {
        WeblogEntry entry = new WeblogEntry();
        entry.setTagsAsString("java <script>alert(1)</script> a\"b");
        for (WeblogEntryTag tag : entry.getTags()) {
            assertFalse(hasHtmlCharacters(tag.getName()), tag.getName());
        }
        assertEquals(3, entry.getTags().size());
    }

    @Test
    public void wrapperFiltersTagsStoredBeforeTheRules() {
        WeblogEntryTagWrapper wrapper = WeblogEntryTagWrapper.wrap(
                storedTag("<script>alert(1)</script>"));
        assertEquals("scriptalert(1)/script", wrapper.getName());
    }

    @Test
    public void wrapperKeepsOrdinaryTags() {
        assertEquals("c++", WeblogEntryTagWrapper.wrap(storedTag("c++")).getName());
    }

    @Test
    public void entryTagsAsStringIsFiltered() throws Exception {
        WeblogEntry entry = new WeblogEntry();
        Set<WeblogEntryTag> tags = new HashSet<>();
        tags.add(storedTag("java"));
        tags.add(storedTag("<b>x</b>"));
        entry.setTags(tags);
        String tagsAsString = WeblogEntryWrapper.wrap(entry, null).getTagsAsString();
        assertFalse(hasHtmlCharacters(tagsAsString), tagsAsString);
        assertTrue(tagsAsString.contains("java"), tagsAsString);
    }
}
