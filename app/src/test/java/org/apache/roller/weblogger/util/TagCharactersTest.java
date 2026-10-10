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

package org.apache.roller.weblogger.util;

import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tag names may not contain characters that are significant in HTML, so
 * templates can print them as they are.
 */
public class TagCharactersTest {

    @Test
    public void htmlCharactersAreRemoved() {
        assertEquals("scriptalert(1)/script",
                Utilities.stripInvalidTagCharacters("<script>alert(1)</script>"));
        assertEquals("aonmouseover=x",
                Utilities.stripInvalidTagCharacters("a\"'onmouseover=x"));
        assertEquals("ab", Utilities.stripInvalidTagCharacters("a&b"));
        assertEquals("ab", Utilities.stripInvalidTagCharacters("a`b"));
    }

    @Test
    public void commaAndWhitespaceAreRemoved() {
        assertEquals("ab", Utilities.stripInvalidTagCharacters("a,b"));
        assertEquals("ab", Utilities.stripInvalidTagCharacters("a b\t"));
    }

    @Test
    public void ordinaryTagsAreKept() {
        for (String tag : new String[] {"java", "c++", ".net", "c#", "foo-bar",
                "foo_bar", "v1.2", "café", "日本"}) {
            assertEquals(tag, Utilities.stripInvalidTagCharacters(tag));
        }
    }

    @Test
    public void normalizeTagFiltersAndLowercases() {
        assertEquals("bfoo/b", Utilities.normalizeTag("<B>Foo</B>", Locale.ENGLISH));
    }
}
