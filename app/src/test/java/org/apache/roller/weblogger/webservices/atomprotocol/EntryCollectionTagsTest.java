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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.  For additional
 * information regarding copyright in this work, please see the NOTICE
 * file in the top level directory of this distribution.
 */
package org.apache.roller.weblogger.webservices.atomprotocol;

import com.rometools.rome.feed.atom.Category;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AtomPub categories with no scheme become entry tags. They must be normalized
 * the same way as tags entered in the weblog editor.
 */
class EntryCollectionTagsTest {

    private static Category category(String term, String scheme) {
        Category cat = new Category();
        cat.setTerm(term);
        cat.setScheme(scheme);
        return cat;
    }

    @Test
    void plainTermsAreKept() {
        List<Category> cats = new ArrayList<>();
        cats.add(category("java", null));
        cats.add(category("roller", null));
        assertEquals(" java roller", EntryCollection.tagsFromCategories(cats));
    }

    @Test
    void categoriesWithSchemeAreNotTags() {
        List<Category> cats = new ArrayList<>();
        cats.add(category("General", "http://example.test/weblog/"));
        cats.add(category("java", null));
        assertEquals(" java", EntryCollection.tagsFromCategories(cats));
    }

    @Test
    void markupCharactersAreReplacedLikeTheEditor() {
        List<Category> cats = new ArrayList<>();
        cats.add(category("<b>x</b>", null));
        cats.add(category("a=\"1\"", null));
        String tags = EntryCollection.tagsFromCategories(cats);
        assertTrue(tags.matches("[A-Za-z0-9 ]*"), tags);
        assertEquals("b x b a 1", tags.trim().replaceAll(" +", " "));
    }

    @Test
    void nonAsciiLettersAreKept() {
        List<Category> cats = new ArrayList<>();
        cats.add(category("café", null));
        assertEquals(" café", EntryCollection.tagsFromCategories(cats));
    }

    @Test
    void noCategoriesGiveEmptyTags() {
        assertEquals("", EntryCollection.tagsFromCategories(null));
        assertEquals("", EntryCollection.tagsFromCategories(new ArrayList<>()));
    }
}
