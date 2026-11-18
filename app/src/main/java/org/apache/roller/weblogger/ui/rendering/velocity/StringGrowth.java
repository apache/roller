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

import java.util.Set;

import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;

/**
 * Some string methods build a result much longer than their receiver and
 * arguments, so checking the result's length after the call is too late. This
 * works out, before a call from a template runs, the longest string it can
 * build, and names the methods whose result can't be bounded that way.
 *
 * <p>Methods are matched by name so that methods added in later Java versions
 * (String.indent, String.formatted, StringBuilder.repeat) are covered when
 * Roller runs on them.
 */
final class StringGrowth {

    /** String methods that are not offered to templates. */
    private static final Set<String> REFUSED_STRING_METHODS = Set.of(
            "repeat", "format", "formatted", "join", "indent");

    /** String methods whose result is never longer than the receiver or an argument. */
    private static final Set<String> NON_GROWING_STRING_METHODS = Set.of(
            "substring", "subSequence", "trim", "strip", "stripLeading",
            "stripTrailing", "stripIndent", "translateEscapes", "intern",
            "toString", "valueOf", "copyValueOf");

    /** StringBuilder and StringBuffer methods that are not offered to templates. */
    private static final Set<String> REFUSED_BUILDER_METHODS = Set.of("repeat");

    /** Characters a number, boolean or other value appended to a builder may take. */
    private static final long SCALAR_LENGTH = 32;

    /** Most characters toUpperCase or toLowerCase makes of one character. */
    private static final long CASE_GROWTH = 3;

    private StringGrowth() {
    }

    /**
     * @return true if the method must not be offered on this receiver: it can
     *         build a string whose length can't be worked out from the call
     */
    static boolean isRefused(Object receiver, String methodName, Class<?> returnType) {
        if (receiver instanceof String) {
            if (REFUSED_STRING_METHODS.contains(methodName)) {
                return true;
            }
            // a method this class doesn't know about that returns a string
            return returnType != null && CharSequence.class.isAssignableFrom(returnType)
                    && !NON_GROWING_STRING_METHODS.contains(methodName)
                    && !isBoundedStringMethod(methodName);
        }
        if (receiver instanceof StringBuilder || receiver instanceof StringBuffer) {
            return REFUSED_BUILDER_METHODS.contains(methodName);
        }
        return false;
    }

    private static boolean isBoundedStringMethod(String methodName) {
        switch (methodName) {
            case "replace":
            case "replaceAll":
            case "replaceFirst":
            case "concat":
            case "toUpperCase":
            case "toLowerCase":
                return true;
            default:
                return false;
        }
    }

    /**
     * @return the most characters the call can put in one string, or 0 when
     *         it doesn't build a string longer than its receiver or arguments
     */
    static long maxResultLength(Object receiver, String methodName, Object[] args) {
        Object[] a = args == null ? new Object[0] : args;
        if (receiver instanceof String) {
            return forString((String) receiver, methodName, a);
        }
        if (receiver instanceof StringBuilder || receiver instanceof StringBuffer) {
            return forBuilder((CharSequence) receiver, methodName, a);
        }
        if (receiver instanceof UtilitiesModel && "replace".equals(methodName)
                && a.length >= 3 && a[0] instanceof String) {
            long maxCount = a.length > 3 && a[3] instanceof Number
                    ? ((Number) a[3]).longValue() : -1;
            if (!(a[1] instanceof String) || ((String) a[1]).isEmpty()) {
                // StringUtils.replace leaves the text alone for an empty target
                return 0;
            }
            return literalReplace((String) a[0], (String) a[1], length(a[2]), maxCount);
        }
        return 0;
    }

    private static long forString(String s, String methodName, Object[] a) {
        long len = s.length();
        switch (methodName) {
            case "replace":
                if (a.length == 2 && a[0] instanceof CharSequence) {
                    return literalReplace(s, a[0].toString(), length(a[1]), -1);
                }
                return len;
            case "replaceAll":
            case "replaceFirst": {
                // each match adds the replacement, and each group reference
                // in it ($1, ${name}) can add up to the whole receiver; an
                // empty pattern matches len + 1 times
                long repl = length(a.length > 1 ? a[1] : null);
                long refs = count(a.length > 1 ? a[1] : null, '$');
                long perMatch = add(repl, multiply(refs, len));
                long matches = "replaceAll".equals(methodName) ? len + 1 : 1;
                return add(len, multiply(matches, perMatch));
            }
            case "concat":
                return add(len, length(a.length > 0 ? a[0] : null));
            case "toUpperCase":
            case "toLowerCase":
                return multiply(CASE_GROWTH, len);
            default:
                return 0;
        }
    }

    private static long forBuilder(CharSequence b, String methodName, Object[] a) {
        long len = b.length();
        switch (methodName) {
            case "setLength":
            case "ensureCapacity":
                return a.length == 1 && a[0] instanceof Number
                        ? ((Number) a[0]).longValue() : 0;
            case "append":
            case "appendCodePoint":
            case "insert":
            case "replace": {
                long total = len;
                for (Object arg : a) {
                    total = add(total, arg instanceof CharSequence || arg instanceof char[]
                            ? length(arg) : SCALAR_LENGTH);
                }
                return total;
            }
            default:
                return 0;
        }
    }

    /** Length of s with up to maxCount (or all, when negative) target occurrences replaced. */
    private static long literalReplace(String s, String target, long replacementLength,
            long maxCount) {
        long occurrences;
        if (target.isEmpty()) {
            occurrences = s.length() + 1L;
        } else {
            occurrences = 0;
            for (int i = s.indexOf(target); i >= 0; i = s.indexOf(target, i + target.length())) {
                occurrences++;
                if (occurrences == maxCount) {
                    break;
                }
            }
        }
        if (maxCount >= 0) {
            occurrences = Math.min(occurrences, maxCount);
        }
        long growth = Math.max(0, replacementLength - target.length());
        return add(s.length(), multiply(occurrences, growth));
    }

    private static long length(Object value) {
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length();
        }
        if (value instanceof char[]) {
            return ((char[]) value).length;
        }
        return value == null ? 0 : SCALAR_LENGTH;
    }

    private static long count(Object value, char c) {
        if (!(value instanceof CharSequence)) {
            return 0;
        }
        CharSequence cs = (CharSequence) value;
        long n = 0;
        for (int i = 0; i < cs.length(); i++) {
            if (cs.charAt(i) == c) {
                n++;
            }
        }
        return n;
    }

    private static long add(long a, long b) {
        long sum = a + b;
        return sum < 0 ? Long.MAX_VALUE : sum;
    }

    private static long multiply(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return a > Long.MAX_VALUE / b ? Long.MAX_VALUE : a * b;
    }
}
