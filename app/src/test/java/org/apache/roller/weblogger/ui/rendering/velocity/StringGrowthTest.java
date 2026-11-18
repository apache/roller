/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.rendering.velocity;

import java.util.Locale;

import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bound worked out before a string call has to be at least the length the
 * call really produces. These run each call on small strings and compare.
 */
class StringGrowthTest {

    private static final String[] TEXTS = {"", "a", "ab", "aaaa", "Hello, World", "ßßß ﬀ İ"};
    private static final String[] REPLACEMENTS = {"", "x", "xyz", "$0", "$1$1", "${g}", "\\$"};

    private static void assertBounds(String what, String actual, long bound) {
        assertTrue(bound >= actual.length(),
                what + " produced " + actual.length() + " chars, bound was " + bound);
    }

    @Test
    void literalReplaceIsExact() {
        for (String s : TEXTS) {
            for (String target : REPLACEMENTS) {
                for (String r : REPLACEMENTS) {
                    String actual = s.replace(target, r);
                    long bound = StringGrowth.maxResultLength(s, "replace", new Object[] {target, r});
                    assertEquals(Math.max(s.length(), actual.length()), bound,
                            "'" + s + "'.replace('" + target + "', '" + r + "')");
                }
            }
        }
    }

    @Test
    void caseChangesAndConcatAreBounded() {
        for (String s : TEXTS) {
            for (Locale l : new Locale[] {Locale.ROOT, new Locale("tr"), Locale.GERMAN}) {
                assertBounds("toUpperCase", s.toUpperCase(l),
                        StringGrowth.maxResultLength(s, "toUpperCase", new Object[] {l}));
                assertBounds("toLowerCase", s.toLowerCase(l),
                        StringGrowth.maxResultLength(s, "toLowerCase", new Object[] {l}));
            }
            assertBounds("concat", s.concat(s),
                    StringGrowth.maxResultLength(s, "concat", new Object[] {s}));
        }
    }

    @Test
    void utilitiesReplaceIsExact() {
        UtilitiesModel utils = new UtilitiesModel();
        assertEquals(3 + 2 * 3, StringGrowth.maxResultLength(utils, "replace",
                new Object[] {"a.a", "a", "xyzw"}));
        assertEquals(3 + 3, StringGrowth.maxResultLength(utils, "replace",
                new Object[] {"a.a", "a", "xyzw", 1}));
        assertEquals(0, StringGrowth.maxResultLength(utils, "replace",
                new Object[] {"a.a", "", "xyzw"}));
    }

    @Test
    void builderGrowthIsBounded() {
        StringBuilder b = new StringBuilder("abc");
        assertEquals(3 + 5, StringGrowth.maxResultLength(b, "append", new Object[] {"hello"}));
        assertEquals(1_000_000_000L, StringGrowth.maxResultLength(b, "setLength",
                new Object[] {1_000_000_000}));
        assertTrue(StringGrowth.isRefused(b, "repeat", StringBuilder.class));
    }

    @Test
    void hugeBoundsDoNotOverflow() {
        String s = "0".repeat(100_000);
        String r = "x".repeat(50_000);
        long bound = StringGrowth.maxResultLength(s, "replace", new Object[] {"", r});
        assertTrue(bound > Integer.MAX_VALUE, "bound was " + bound);
    }

    @Test
    void unboundableStringMethodsAreRefused() {
        for (String name : new String[] {"repeat", "format", "formatted", "join", "indent"}) {
            assertTrue(StringGrowth.isRefused("x", name, String.class), name);
        }
        assertTrue(StringGrowth.isRefused("x", "someNewMethod", String.class));
        for (String name : new String[] {"replace", "substring", "trim", "toUpperCase", "valueOf"}) {
            assertFalse(StringGrowth.isRefused("x", name, String.class), name);
        }
        assertFalse(StringGrowth.isRefused("x", "length", int.class));
        assertFalse(StringGrowth.isRefused("x", "split", String[].class));
    }
}
