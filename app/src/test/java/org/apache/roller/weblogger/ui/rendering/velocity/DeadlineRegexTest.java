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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/** DeadlineRegex has to give the same results as the String methods it stands in for. */
class DeadlineRegexTest {

    private static final String[] TEXTS = {"", "a", "a,b,,c,,", "Hello, World", " x  y "};
    private static final String[] PATTERNS = {"", ",", "\\s+", "l+", "(?=(.*))", "(.)", "\\b", "$"};
    private static final String[] REPLACEMENTS = {"", "x", "$0", "[$1]", "\\$"};

    @Test
    void givesTheSameResultsAsString() {
        for (String s : TEXTS) {
            for (String p : PATTERNS) {
                assertEquals(s.matches(p), DeadlineRegex.invoke(s, "matches", new Object[] {p}));
                assertArrayEquals(s.split(p),
                        (String[]) DeadlineRegex.invoke(s, "split", new Object[] {p}), s + " / " + p);
                for (int limit : new int[] {-1, 0, 1, 2}) {
                    assertArrayEquals(s.split(p, limit),
                            (String[]) DeadlineRegex.invoke(s, "split", new Object[] {p, limit}));
                }
                for (String r : REPLACEMENTS) {
                    if (r.contains("$1") && !p.contains("(")) {
                        continue; // no such group: the call throws
                    }
                    assertEquals(s.replaceAll(p, r),
                            DeadlineRegex.invoke(s, "replaceAll", new Object[] {p, r}));
                    assertEquals(s.replaceFirst(p, r),
                            DeadlineRegex.invoke(s, "replaceFirst", new Object[] {p, r}));
                }
            }
        }
    }

    @Test
    void leavesOtherCallsToString() {
        assertSame(DeadlineRegex.NOT_HANDLED, DeadlineRegex.invoke("a", "replace", new Object[] {"a", "b"}));
        assertSame(DeadlineRegex.NOT_HANDLED, DeadlineRegex.invoke("a", "split", new Object[] {'a'}));
    }
}
