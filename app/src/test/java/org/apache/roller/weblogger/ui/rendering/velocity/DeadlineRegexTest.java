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

import java.lang.management.ManagementFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** DeadlineRegex has to give the same results as the String methods it stands in for. */
class DeadlineRegexTest {

    private static final String[] TEXTS = {"", "a", "a,b,,c,,", "Hello, World", " x  y "};
    private static final String[] PATTERNS = {"", ",", "\\s+", "l+", "(?=(.*))", "(.)", "\\b", "$"};
    private static final String[] REPLACEMENTS = {"", "x", "$0", "[$1]", "\\$"};

    /**
     * Expressions in which part can match an empty string in more than one
     * way at the same place are refused; the rest, including ones with
     * optional parts, lookarounds and alternatives, still run.
     */
    @Test
    void refusesExpressionsThatRepeatAnEmptyMatch() {
        String[] refused = {
            "(?:a?|b?)c", "(|)z", "(?:)*", "x(?:(?:)?){3}", "\\b*", "(?=)+", "^?a",
            "()\\1*", "(?:(?:a|){2})", "(?<n>a*|b*)", "(?i:x?|y?)", "((a)|(b)?|c*)d",
            "(?:(?<=k)|(?<!k))w", "(?:\\b|\\B)v", "(?x)(?:\t|\t)u", "(?:$|^)t",
        };
        for (String regex : refused) {
            assertThrows(RenderLimitException.class, () -> DeadlineRegex.checkPattern(regex),
                    regex);
        }
        String[] allowed = {
            "", "a", "(\\d+)-(\\d+)", "(?i)foo|bar", "(ab)+", "^\\s+|\\s+$",
            "(https?://)?(www\\.)?", "foo(?=\\s|$)", "[()|?*]+", "\\Q(|)*\\E",
            "(?<year>\\d{4})-(\\d{2})", "a|", "(a|b|)c", "(?:\\.\\d+)?", "(\\w)\\1",
            "(.*a){12}b", "[^\\]()]*",
        };
        for (String regex : allowed) {
            DeadlineRegex.checkPattern(regex);
        }
        for (String p : PATTERNS) {
            DeadlineRegex.checkPattern(p);
        }
    }

    /** As String does: replaceFirst refuses null, and replaceAll does once it matches. */
    @Test
    void aNullReplacementIsRefusedAsByString() {
        assertThrows(NullPointerException.class,
                () -> DeadlineRegex.invoke("abc", "replaceAll", new Object[] {"b", null}));
        assertThrows(NullPointerException.class,
                () -> DeadlineRegex.invoke("abc", "replaceFirst", new Object[] {"x", null}));
        assertEquals("abc", DeadlineRegex.invoke("abc", "replaceAll", new Object[] {"x", null}));
    }

    /**
     * A replacement is counted by the groups it refers to, so moving text
     * around in a string near the limit still works.
     */
    @Test
    void replacementsCountOnlyTheGroupsTheyUse() {
        RenderSession session = RenderSession.begin(null);
        try {
            int length = (int) (session.getMaxOutputChars() * 3 / 5);
            String s = "abc" + "0".repeat(length - 3);
            String swapped = (String) DeadlineRegex.invoke(s, "replaceAll",
                    new Object[] {"(?s)^(.)(.)(.)(.*)$", "$3$2$1$4"});
            assertEquals(length, swapped.length());
            assertEquals("cba0", swapped.substring(0, 4));
            assertEquals("<b>", DeadlineRegex.invoke("b", "replaceAll",
                    new Object[] {"(?<x>b)", "<${x}>"}));
            assertEquals("b1", DeadlineRegex.invoke("b", "replaceAll",
                    new Object[] {"(b)", "$11"}));
            // a group used twice is counted twice
            assertThrows(RenderLimitException.class, () -> DeadlineRegex.invoke(s, "replaceAll",
                    new Object[] {"(?s)^(.*)$", "$1$1"}));
        } finally {
            session.end();
        }
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

    private static long allocatedBytes() {
        return ((com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean())
                .getThreadAllocatedBytes(Thread.currentThread().getId());
    }

    /**
     * A replacement that takes a string at the output limit one character
     * past it has to stop before the rest of the string is copied after it.
     */
    @Test
    void theTextAfterTheLastReplacementCountsTowardTheLimit() {
        RenderSession session = RenderSession.begin(null);
        try {
            String s = "0".repeat((int) session.getMaxOutputChars());
            for (String method : new String[] {"replaceFirst", "replaceAll"}) {
                assertThrows(RenderLimitException.class,
                        () -> DeadlineRegex.invoke(s, method, new Object[] {"^0", "xx"}));
                long before = allocatedBytes();
                assertThrows(RenderLimitException.class,
                        () -> DeadlineRegex.invoke(s, method, new Object[] {"^0", "xx"}));
                long allocated = allocatedBytes() - before;
                assertTrue(allocated < 1024 * 1024,
                        method + " allocated " + allocated + " bytes before it was stopped");
            }
        } finally {
            session.end();
        }
    }
}
