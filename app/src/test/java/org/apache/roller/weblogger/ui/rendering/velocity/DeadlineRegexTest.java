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
import static org.junit.jupiter.api.Assertions.assertThrows;

/** DeadlineRegex has to give the same results as the String methods it stands in for. */
class DeadlineRegexTest {

    private static final String[] TEXTS = {"", "a", "a,b,,c,,", "Hello, World", " x  y "};
    private static final String[] PATTERNS = {"", ",", "\\s+", "l+", "(?=(.*))", "(.)", "\\b", "$"};
    private static final String[] REPLACEMENTS = {"", "x", "$0", "[$1]", "\\$"};

    @Test
    void convertsArgumentsAsVelocityWould() {
        assertArrayEquals("a,b,c".split(",", 2),
                (String[]) DeadlineRegex.invoke("a,b,c", "split", new Object[] {",", "2"}));
        assertArrayEquals("a,b,c".split(",", 2),
                (String[]) DeadlineRegex.invoke("a,b,c", "split", new Object[] {",", 2L}));
        assertEquals("a1b1c", DeadlineRegex.invoke("a,b,c", "replaceAll", new Object[] {",", 1}));
    }

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
        assertSame(DeadlineRegex.NOT_HANDLED, DeadlineRegex.invoke("a", "split", new Object[] {"a", true}));
        assertSame(DeadlineRegex.NOT_HANDLED, DeadlineRegex.invoke("a", "matches", new Object[] {null}));
    }

    /** Only the pieces split returns count: it drops trailing empty ones. */
    @Test
    void trailingEmptyPiecesDoNotCountTowardTheLimit() {
        int max = DeadlineRegex.MAX_SPLIT_PIECES;
        String commas = ",".repeat(max + 50_000);
        assertArrayEquals(commas.split(","),
                (String[]) DeadlineRegex.invoke(commas, "split", new Object[] {","}));

        String atTheLimit = "a,".repeat(max) + commas;
        assertEquals(max,
                ((String[]) DeadlineRegex.invoke(atTheLimit, "split", new Object[] {","})).length);
        // empty pieces before a piece that is kept are returned, so they count
        assertEquals(max,
                ((String[]) DeadlineRegex.invoke(",".repeat(max - 1) + "a,,", "split",
                        new Object[] {","})).length);

        assertThrows(RenderLimitException.class,
                () -> DeadlineRegex.invoke("a," + atTheLimit, "split", new Object[] {","}));
        assertThrows(RenderLimitException.class,
                () -> DeadlineRegex.invoke(",".repeat(max) + "a", "split", new Object[] {","}));
        // a negative limit keeps the trailing empty pieces
        assertThrows(RenderLimitException.class,
                () -> DeadlineRegex.invoke(commas, "split", new Object[] {",", -1}));
    }
}
