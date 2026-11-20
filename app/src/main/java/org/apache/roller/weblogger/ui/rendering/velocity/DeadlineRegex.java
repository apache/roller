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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
 *
 * <p>An expression can also backtrack without reading the text, when parts
 * of it can match an empty string in more than one way, and then the
 * deadline is never checked. checkPattern refuses the usual forms of that
 * before the expression runs.
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
        // the arguments already have the method's parameter types: Velocity
        // finds no method for a call whose arguments don't fit
        String regex = args[0].toString();
        Object second = args.length > 1 ? args[1] : null;
        CharSequence text = new DeadlineText(s);
        switch (methodName) {
            case "matches":
                return args.length == 1 ? compile(regex).matcher(text).matches()
                        : NOT_HANDLED;
            case "replaceAll":
            case "replaceFirst":
                return args.length == 2 ? replace(s, compile(regex).matcher(text),
                        second == null ? null : second.toString(),
                        "replaceAll".equals(methodName)) : NOT_HANDLED;
            case "split":
                Integer limit = args.length == 1 ? Integer.valueOf(0) : toInt(second);
                if (limit == null) {
                    return NOT_HANDLED;
                }
                return split(s, compile(regex).matcher(text), limit);
            default:
                return NOT_HANDLED;
        }
    }

    /** No more pieces than a #foreach can visit (directive.foreach.max_loops). */
    static final int MAX_SPLIT_PIECES = 100_000;

    /**
     * Pattern.split, throwing if it would return more than MAX_SPLIT_PIECES.
     * With a limit of 0 split drops trailing empty pieces, so those are
     * neither counted nor allocated: empty pieces are only added once a piece
     * that isn't empty follows them.
     */
    private static String[] split(String s, Matcher m, int limit) {
        List<String> pieces = new ArrayList<>();
        int heldBack = 0;
        int index = 0;
        boolean last = false;
        while (!last) {
            last = limit > 0 && pieces.size() + heldBack == limit - 1 || !m.find();
            if (!last && m.end() == 0) {
                // an empty match at the start doesn't make an empty first piece
                continue;
            }
            if (last && index == 0) {
                // a string with no match is returned whole, even when empty
                return new String[] {s};
            }
            int end = last ? s.length() : m.start();
            if (limit == 0 && end == index) {
                heldBack++;
            } else {
                if (pieces.size() + heldBack >= MAX_SPLIT_PIECES) {
                    throw new RenderLimitException("split produced more than "
                            + MAX_SPLIT_PIECES + " pieces");
                }
                for (; heldBack > 0; heldBack--) {
                    pieces.add("");
                }
                pieces.add(s.substring(index, end));
            }
            index = last ? end : m.end();
        }
        return pieces.toArray(new String[0]);
    }

    private static Integer toInt(Object value) {
        return value instanceof Number ? Integer.valueOf(((Number) value).intValue()) : null;
    }

    /**
     * Matcher.replaceAll and replaceFirst, checking before each replacement,
     * and before the text after the last one, that it can't take the result
     * past the output limit: a replacement adds its own length plus the
     * length of each group it refers to ($1, ${name}) in that match.
     */
    private static String replace(String s, Matcher m, String replacement, boolean all) {
        if (replacement == null) {
            // as Matcher does: replaceFirst refuses null at once, and
            // replaceAll only once there is something to replace
            if (!all || m.find()) {
                throw new NullPointerException("replacement");
            }
            return s;
        }
        List<Object> references = groupReferences(replacement, m.groupCount());
        StringBuilder out = new StringBuilder();
        int previousEnd = 0;
        while (m.find()) {
            long grown = replacement.length();
            for (Object reference : references) {
                grown += groupLength(m, reference);
            }
            RenderSession.checkLength((long) out.length() + (m.start() - previousEnd) + grown);
            m.appendReplacement(out, replacement);
            previousEnd = m.end();
            if (!all) {
                break;
            }
        }
        // the text after the last replacement is copied as it is
        RenderSession.checkLength((long) out.length() + (m.regionEnd() - previousEnd));
        m.appendTail(out);
        return out.toString();
    }

    /**
     * @return the groups a replacement refers to, as group numbers and names,
     *         read the way Matcher.appendReplacement reads them. A reference
     *         it would refuse ends the list; appendReplacement then throws.
     */
    private static List<Object> groupReferences(String replacement, int groupCount) {
        List<Object> references = new ArrayList<>();
        int i = 0;
        int n = replacement.length();
        while (i < n) {
            char c = replacement.charAt(i++);
            if (c == '\\') {
                i++;
            } else if (c == '$') {
                if (i >= n) {
                    break;
                }
                if (replacement.charAt(i) == '{') {
                    int close = replacement.indexOf('}', i);
                    if (close < 0) {
                        break;
                    }
                    references.add(replacement.substring(i + 1, close));
                    i = close + 1;
                } else {
                    int group = replacement.charAt(i) - '0';
                    if (group < 0 || group > 9) {
                        break;
                    }
                    // more digits are read while they name a group that exists
                    for (i++; i < n; i++) {
                        int digit = replacement.charAt(i) - '0';
                        if (digit < 0 || digit > 9 || group * 10 + digit > groupCount) {
                            break;
                        }
                        group = group * 10 + digit;
                    }
                    references.add(group);
                }
            }
        }
        return references;
    }

    private static long groupLength(Matcher m, Object reference) {
        try {
            int start = reference instanceof Integer ? m.start((Integer) reference)
                    : m.start((String) reference);
            int end = reference instanceof Integer ? m.end((Integer) reference)
                    : m.end((String) reference);
            return start < 0 ? 0 : end - start;
        } catch (IndexOutOfBoundsException | IllegalArgumentException ex) {
            // appendReplacement throws for a group that doesn't exist
            return 0;
        }
    }

    private static Pattern compile(String regex) {
        checkPattern(regex);
        return Pattern.compile(regex);
    }

    /**
     * Throws if part of the expression can match an empty string in more
     * than one way at the same place: a group or assertion that can match
     * empty followed by a quantifier, or a group with two alternatives that
     * can. Matching such an expression can try exponentially many ways
     * without reading a character, so DeadlineText never gets to check the
     * deadline. See canMatchEmpty for how a part is tried.
     */
    static void checkPattern(String regex) {
        // for each open group: where its body and its current alternative
        // start, how many of its alternatives can match empty, and whether
        // it is a lookaround, which matches empty whatever it contains
        Deque<int[]> groups = new ArrayDeque<>();
        int n = regex.length();
        int i = 0;
        while (i < n) {
            char c = regex.charAt(i);
            if (c == '\\') {
                i = skipEscape(regex, i);
            } else if (c == '[') {
                i = skipClass(regex, i);
            } else if (c == '^' || c == '$') {
                i++;
                refuseQuantifier(regex, i);
            } else if (c == '(') {
                int[] group = openGroup(regex, i);
                if (group == null) {
                    // flags such as (?i), which apply to the rest of the group
                    i = regex.indexOf(')', i) + 1;
                    if (i == 0) {
                        return;
                    }
                } else {
                    groups.push(group);
                    i = group[0];
                }
            } else if (c == '|' && !groups.isEmpty()) {
                int[] group = groups.peek();
                if (canMatchEmpty(regex.substring(group[1], i))) {
                    group[2]++;
                }
                group[1] = ++i;
            } else if (c == ')' && !groups.isEmpty()) {
                int[] group = groups.pop();
                boolean alternatives = group[1] != group[0];
                if (alternatives && canMatchEmpty(regex.substring(group[1], i))) {
                    group[2]++;
                }
                if (group[2] > 1) {
                    throw refused();
                }
                boolean empty = group[3] == 1 || canMatchEmpty(regex.substring(group[0], i));
                i++;
                if (empty) {
                    refuseQuantifier(regex, i);
                }
            } else {
                i++;
            }
        }
    }

    /**
     * @return true if part of an expression can match an empty string.
     *         Lookarounds, anchors and boundaries match empty somewhere
     *         whatever they test, so they are taken out first, and the part
     *         is tried with and without comments mode, which an inline (?x)
     *         may have turned on. A part that doesn't compile on its own
     *         counts as one that can.
     */
    private static boolean canMatchEmpty(String part) {
        String rest = withoutZeroWidth(part);
        try {
            return Pattern.compile(rest).matcher("").matches()
                    || Pattern.compile(rest, Pattern.COMMENTS).matcher("").matches();
        } catch (RuntimeException ex) {
            return true;
        }
    }

    /** @return the part with its lookarounds, anchors and boundaries taken out */
    private static String withoutZeroWidth(String part) {
        StringBuilder out = new StringBuilder();
        int n = part.length();
        int i = 0;
        while (i < n) {
            char c = part.charAt(i);
            if (c == '\\') {
                char e = i + 1 < n ? part.charAt(i + 1) : 0;
                int end = e == 'Q' ? skipEscape(part, i) : Math.min(i + 2, n);
                if ("bBAGzZ".indexOf(e) < 0 || e == 0) {
                    out.append(part, i, end);
                }
                i = end;
            } else if (c == '[') {
                int end = skipClass(part, i);
                out.append(part, i, end);
                i = end;
            } else if (c == '^' || c == '$') {
                i++;
            } else if (c == '(' && isLookaround(part, i)) {
                out.append("(?:)");
                i = closingParen(part, i) + 1;
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    private static boolean isLookaround(String regex, int i) {
        return regex.startsWith("(?=", i) || regex.startsWith("(?!", i)
                || regex.startsWith("(?<=", i) || regex.startsWith("(?<!", i);
    }

    /** @return the index of the ')' closing the group that opens at start, or the last index */
    private static int closingParen(String regex, int start) {
        int depth = 0;
        int i = start;
        while (i < regex.length()) {
            char c = regex.charAt(i);
            if (c == '\\') {
                i = regex.startsWith("\\Q", i) ? skipEscape(regex, i) : i + 2;
                continue;
            }
            if (c == '[') {
                i = skipClass(regex, i);
                continue;
            }
            if (c == '(') {
                depth++;
            } else if (c == ')' && --depth == 0) {
                return i;
            }
            i++;
        }
        return regex.length() - 1;
    }

    /**
     * @return for the group opening at start: where its body starts, where
     *         its first alternative starts, 0 alternatives that can match
     *         empty, and 1 for a lookaround; or null for flags with no body
     */
    private static int[] openGroup(String regex, int start) {
        int i = start + 1;
        if (i >= regex.length() || regex.charAt(i) != '?') {
            return new int[] {i, i, 0, 0};
        }
        i++;
        char c = i < regex.length() ? regex.charAt(i) : 0;
        char next = i + 1 < regex.length() ? regex.charAt(i + 1) : 0;
        if (c == '=' || c == '!') {
            return new int[] {i + 1, i + 1, 0, 1};
        }
        if (c == '<' && (next == '=' || next == '!')) {
            return new int[] {i + 2, i + 2, 0, 1};
        }
        if (c == '<') {
            int body = regex.indexOf('>', i) + 1;
            body = body == 0 ? regex.length() : body;
            return new int[] {body, body, 0, 0};
        }
        if (c == ':' || c == '>') {
            return new int[] {i + 1, i + 1, 0, 0};
        }
        while (i < regex.length() && (Character.isLetter(regex.charAt(i))
                || regex.charAt(i) == '-')) {
            i++;
        }
        if (i < regex.length() && regex.charAt(i) == ':') {
            return new int[] {i + 1, i + 1, 0, 0};
        }
        return null;
    }

    /** @return the index after the escape at i; a quantified backreference or boundary is refused */
    private static int skipEscape(String regex, int i) {
        char c = i + 1 < regex.length() ? regex.charAt(i + 1) : 0;
        if (c == 'Q') {
            int end = regex.indexOf("\\E", i + 2);
            return end < 0 ? regex.length() : end + 2;
        }
        int end = Math.min(i + 2, regex.length());
        if (c == 'k') {
            int close = regex.indexOf('>', i);
            end = close < 0 ? regex.length() : close + 1;
        } else if (c >= '1' && c <= '9') {
            while (end < regex.length() && Character.isDigit(regex.charAt(end))) {
                end++;
            }
        } else if ("bBAGzZ".indexOf(c) < 0 || c == 0) {
            return end;
        }
        // a backreference can match empty, and boundaries always do
        refuseQuantifier(regex, end);
        return end;
    }

    /** @return the index after the character class that opens at i */
    private static int skipClass(String regex, int i) {
        int depth = 0;
        int n = regex.length();
        while (i < n) {
            char c = regex.charAt(i);
            if (c == '\\') {
                i += 2;
                continue;
            }
            if (c == '[') {
                depth++;
                i++;
                // a ']' first in a class, after any '^', is a plain character
                if (i < n && regex.charAt(i) == '^') {
                    i++;
                }
                if (i < n && regex.charAt(i) == ']') {
                    i++;
                }
                continue;
            }
            i++;
            if (c == ']' && --depth == 0) {
                return i;
            }
        }
        return n;
    }

    private static void refuseQuantifier(String regex, int i) {
        if (i < regex.length() && "?*+{".indexOf(regex.charAt(i)) >= 0) {
            throw refused();
        }
    }

    private static RenderLimitException refused() {
        return new RenderLimitException("a regular expression repeats a part that"
                + " can match an empty string");
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
