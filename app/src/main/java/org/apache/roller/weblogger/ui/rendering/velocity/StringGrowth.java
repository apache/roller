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
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.roller.util.RegexUtil;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;

/**
 * Some string methods build a result much longer than their receiver and
 * arguments, so checking the result's length after the call is too late. This
 * works out, before a call from a template runs, the longest string it can
 * build, and names the methods whose result can't be bounded that way. The
 * same goes for the $utils methods (UtilitiesModel) that escape, encode or
 * add to text.
 *
 * <p>Methods are matched by name so that methods added in later Java versions
 * (String.indent, String.formatted, StringBuilder.repeat) are covered when
 * Roller runs on them.
 */
final class StringGrowth {

    /**
     * String methods that are not offered to templates: those that build a
     * string whose length can't be worked out from the call, and those that
     * run a regular expression or return a piece for every line, which
     * DeadlineRegex doesn't run for them.
     */
    private static final Set<String> REFUSED_STRING_METHODS = Set.of(
            "repeat", "format", "formatted", "join", "indent", "splitWithDelimiters", "lines");

    /** String methods whose result is never longer than the receiver or an argument. */
    private static final Set<String> NON_GROWING_STRING_METHODS = Set.of(
            "substring", "subSequence", "trim", "strip", "stripLeading",
            "stripTrailing", "stripIndent", "translateEscapes", "intern",
            "toString", "valueOf", "copyValueOf");

    /** StringBuilder and StringBuffer methods that are not offered to templates. */
    private static final Set<String> REFUSED_BUILDER_METHODS = Set.of("repeat");

    /**
     * UtilitiesModel methods that return a string no longer than an argument,
     * or a short one of fixed length.
     */
    private static final Set<String> NON_GROWING_UTILITIES_METHODS = Set.of(
            "getModelName", "toString", "formatIso8601Date", "formatIso8601Day",
            "formatRfc822Date", "format8charsDate", "left", "unescapeHTML",
            "unescapeXML", "unescapeJavaScript", "removeHTML", "decode");

    /** Characters a number, boolean or other value appended to a builder may take. */
    private static final long SCALAR_LENGTH = 32;

    /**
     * The characters toUpperCase or toLowerCase turn into more than one, in
     * any locale. The Lithuanian lower case of I, J and I with ogonek gains a
     * dot before an accent, so those count as growing by one.
     */
    private static final Growth CASE_GROWTH = new Growth(c -> {
        String s = String.valueOf((char) c);
        int longest = 1;
        for (Locale l : new Locale[] {Locale.ROOT, Locale.forLanguageTag("tr"),
                Locale.forLanguageTag("az"), Locale.forLanguageTag("lt")}) {
            longest = Math.max(longest, Math.max(s.toUpperCase(l).length(),
                    s.toLowerCase(l).length()));
        }
        return c == 'I' || c == 'J' || c == '\u012e' ? Math.max(longest, 2) : longest;
    });

    /** Most characters addNofollow is taken to make of one character; see forUtilities. */
    private static final long NOFOLLOW_GROWTH = 9;

    /** Most characters a pattern letter of a date format writes, with room to spare. */
    private static final long DATE_LETTER_LENGTH = 64;

    /** What UtilitiesModel.escapeHTML and escapeXML make of each character. */
    private static final Growth HTML_ESCAPED = escaped(StringEscapeUtils::escapeHtml4);
    private static final Growth XML_ESCAPED = escaped(StringEscapeUtils::escapeXml11);

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
        if (receiver instanceof UtilitiesModel) {
            // a method added later is refused until it is bounded here
            return returnType != null && CharSequence.class.isAssignableFrom(returnType)
                    && !NON_GROWING_UTILITIES_METHODS.contains(methodName)
                    && !isBoundedUtilitiesMethod(methodName);
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

    private static boolean isBoundedUtilitiesMethod(String methodName) {
        switch (methodName) {
            case "replace":
            case "toUpperCase":
            case "escapeHTML":
            case "transformToHTMLSubset":
            case "escapeXML":
            case "escapeJavaScript":
            case "encode":
            case "hexEncode":
            case "autoformat":
            case "addNofollow":
            case "encodeEmail":
            case "truncate":
            case "truncateNicely":
            case "truncateText":
            case "toBase64":
            case "formatDate":
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
        if (receiver instanceof UtilitiesModel) {
            return forUtilities(methodName, a);
        }
        return 0;
    }

    private static long forUtilities(String methodName, Object[] a) {
        // the arguments already have the method's parameter types: Velocity
        // finds no method for a call whose arguments don't fit
        CharSequence s = a.length > 0 ? text(a[0]) : null;
        switch (methodName) {
            case "replace": {
                if (a.length < 3 || s == null) {
                    return 0;
                }
                long maxCount = a.length > 3 && a[3] instanceof Number
                        ? ((Number) a[3]).longValue() : -1;
                CharSequence target = text(a[1]);
                if (target == null || target.length() == 0) {
                    // StringUtils.replace leaves the text alone for an empty target
                    return 0;
                }
                return literalReplace(s.toString(), target.toString(), length(a[2]), maxCount);
            }
            case "toUpperCase":
                return caseChange(s);
            case "escapeHTML":
            case "transformToHTMLSubset":
                // transformToHTMLSubset only shrinks text apart from the link
                // addresses it escapes the same way
                return HTML_ESCAPED.length(s);
            case "escapeXML":
                return XML_ESCAPED.length(s);
            case "escapeJavaScript":
                // at most six characters (\u00e9)
                return grown(s, 6, c -> c < 0x20 || c > 0x7e || c == '\'' || c == '"'
                        || c == '\\' || c == '/');
            case "encode":
                // up to three UTF-8 bytes of %XX each; a space becomes '+'
                return grown(s, 9, c -> !(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'
                        || c >= '0' && c <= '9' || c == '.' || c == '-' || c == '*'
                        || c == '_' || c == ' '));
            case "hexEncode":
                // every UTF-8 byte becomes %XX
                return add(multiply(3, s == null ? 0 : s.length()),
                        multiply(6, count(s, c -> c >= 0x80)));
            case "autoformat":
                return grown(s, "<br />".length(), c -> c == '\n');
            case "addNofollow":
                // only a link that already has rel="nofollow" gets another
                return s == null ? 0 : add(s.length(), multiply(" rel=\"nofollow\"".length(),
                        occurrences(s.toString(), "rel=\"nofollow\"")));
            case "encodeEmail":
                return encodedEmail(s);
            case "truncate":
            case "truncateNicely":
            case "truncateText":
                return add(length(s), length(a.length > 3 ? a[3] : null));
            case "toBase64":
                return a.length > 0 && a[0] instanceof byte[]
                        ? multiply(4, (((byte[]) a[0]).length + 2) / 3) : 0;
            case "formatDate": {
                CharSequence fmt = a.length > 1 ? text(a[1]) : null;
                return grown(fmt, DATE_LETTER_LENGTH,
                        c -> c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
            }
            default:
                return 0;
        }
    }

    /**
     * RegexUtil.encodeEmail hex encodes each mailto address, tripling it, and
     * then turns the '@' and a '.' of each address into -AT- and -DOT-.
     */
    private static long encodedEmail(CharSequence s) {
        if (s == null) {
            return 0;
        }
        long mailtoChars = 0;
        Matcher m = RegexUtil.MAILTO_PATTERN.matcher(s);
        while (m.find()) {
            mailtoChars += m.end(1) - m.start(1);
        }
        long perAt = "-AT-".length() - 1 + "-DOT-".length() - 1;
        return add(add(s.length(), multiply(2, mailtoChars)),
                multiply(perAt, count(s, c -> c == '@')));
    }

    /**
     * How many characters each char becomes, found by trying each one, so
     * that only the characters that really grow are counted.
     */
    private static final class Growth {

        private final byte[] extra = new byte[Character.MAX_VALUE + 1];

        Growth(java.util.function.IntUnaryOperator lengthOf) {
            for (int c = 0; c <= Character.MAX_VALUE; c++) {
                extra[c] = (byte) Math.max(0, lengthOf.applyAsInt(c) - 1);
            }
        }

        long length(CharSequence s) {
            if (s == null) {
                return 0;
            }
            long n = s.length();
            for (int i = 0; i < s.length(); i++) {
                n += extra[s.charAt(i)];
            }
            return n;
        }
    }

    private static Growth escaped(UnaryOperator<String> escape) {
        return new Growth(c -> escape.apply(String.valueOf((char) c)).length());
    }

    /** Length of a case change of s. */
    private static long caseChange(CharSequence s) {
        return CASE_GROWTH.length(s);
    }

    /** Length of s when each character that matches can become up to perChar characters. */
    private static long grown(CharSequence s, long perChar, IntPredicate grows) {
        return s == null ? 0 : add(s.length(), multiply(perChar - 1, count(s, grows)));
    }

    private static long count(CharSequence s, IntPredicate matches) {
        long n = 0;
        for (int i = 0; s != null && i < s.length(); i++) {
            if (matches.test(s.charAt(i))) {
                n++;
            }
        }
        return n;
    }

    private static long occurrences(String s, String target) {
        long n = 0;
        for (int i = s.indexOf(target); i >= 0; i = s.indexOf(target, i + target.length())) {
            n++;
        }
        return n;
    }

    private static CharSequence text(Object value) {
        if (value == null || value instanceof CharSequence) {
            return (CharSequence) value;
        }
        return value instanceof char[] ? new String((char[]) value) : String.valueOf(value);
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
            case "replaceFirst":
                // DeadlineRegex runs these and checks the limit as it goes
                return 0;
            case "concat":
                return add(len, length(a.length > 0 ? a[0] : null));
            case "toUpperCase":
            case "toLowerCase":
                return caseChange(s);
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
