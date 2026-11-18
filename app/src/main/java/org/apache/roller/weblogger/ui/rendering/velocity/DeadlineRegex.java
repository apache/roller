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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs the String methods that take a regular expression (matches,
 * replaceAll, replaceFirst and split) over text that checks the render
 * deadline as the expression reads it, so a slow expression stops with the
 * render instead of holding the thread, and replaceAll and replaceFirst stop
 * at the output limit as they build the result. Each gives the same result
 * as the String method; see String's documentation for the Pattern call each
 * one is equivalent to.
 */
final class DeadlineRegex {

    /** Returned when the call isn't one of these methods. */
    static final Object NOT_HANDLED = new Object();

    private DeadlineRegex() {
    }

    /**
     * @return the result of the call, or NOT_HANDLED when it isn't one of
     *         these methods or its arguments don't fit them
     */
    static Object invoke(String s, String methodName, Object[] args) {
        if (args == null || args.length == 0 || args.length > 2 || args[0] == null) {
            return NOT_HANDLED;
        }
        // Velocity converts arguments to the method's parameter types after
        // this sees them, so convert them the same way here
        String regex = args[0].toString();
        Object second = args.length > 1 ? args[1] : null;
        CharSequence text = new DeadlineText(s);
        switch (methodName) {
            case "matches":
                return args.length == 1 ? Pattern.compile(regex).matcher(text).matches()
                        : NOT_HANDLED;
            case "replaceAll":
            case "replaceFirst":
                return second == null ? NOT_HANDLED : replace(Pattern.compile(regex).matcher(text),
                        second.toString(), "replaceAll".equals(methodName));
            case "split":
                if (args.length == 1) {
                    return Pattern.compile(regex).split(text, 0);
                }
                Integer limit = toInt(second);
                return limit == null ? NOT_HANDLED : Pattern.compile(regex).split(text, limit);
            default:
                return NOT_HANDLED;
        }
    }

    private static Integer toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf(((String) value).trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Matcher.replaceAll and replaceFirst, checking before each replacement
     * that it can't take the result past the output limit: a replacement adds
     * at most its own length plus, for each group reference ($1, ${name}) in
     * it, the longest group of that match.
     */
    private static String replace(Matcher m, String replacement, boolean all) {
        long references = replacement.chars().filter(c -> c == '$').count();
        StringBuilder out = new StringBuilder();
        int previousEnd = 0;
        while (m.find()) {
            long longestGroup = 0;
            for (int g = 0; g <= m.groupCount(); g++) {
                if (m.start(g) >= 0) {
                    longestGroup = Math.max(longestGroup, m.end(g) - m.start(g));
                }
            }
            RenderSession.checkLength((long) out.length() + (m.start() - previousEnd)
                    + replacement.length() + references * longestGroup);
            m.appendReplacement(out, replacement);
            previousEnd = m.end();
            if (!all) {
                break;
            }
        }
        m.appendTail(out);
        RenderSession.checkLength(out.length());
        return out.toString();
    }

    /** A string that checks the deadline every few thousand character reads. */
    private static final class DeadlineText implements CharSequence {

        private final String s;
        private int reads = 0;

        DeadlineText(String s) {
            this.s = s;
        }

        @Override
        public char charAt(int index) {
            if ((++reads & 0xfff) == 0) {
                RenderSession.checkDeadline();
            }
            return s.charAt(index);
        }

        @Override
        public int length() {
            return s.length();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return s.subSequence(start, end);
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
