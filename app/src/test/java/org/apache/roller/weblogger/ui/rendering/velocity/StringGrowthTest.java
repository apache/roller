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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

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

    private static final String[] TEXTS = {"", "a", "ab", "aaaa", "Hello, World", "ßßß ﬀ İ",
        "I\u0300 J\u0301 \u00CC Ii"};
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
            for (Locale l : new Locale[] {Locale.ROOT, new Locale("tr"), Locale.GERMAN,
                    new Locale("lt")}) {
                assertBounds("toUpperCase", s.toUpperCase(l),
                        StringGrowth.maxResultLength(s, "toUpperCase", new Object[] {l}));
                assertBounds("toLowerCase", s.toLowerCase(l),
                        StringGrowth.maxResultLength(s, "toLowerCase", new Object[] {l}));
            }
            assertBounds("concat", s.concat(s),
                    StringGrowth.maxResultLength(s, "concat", new Object[] {s}));
        }
    }

    /** Each character's case change, in the locales whose rules differ, fits its bound. */
    @Test
    void caseChangesOfEveryCharAreBounded() {
        List<String> texts = everyChar();
        texts.add("I\u0300J\u0301\u012e\u0303");
        for (String s : texts) {
            long bound = StringGrowth.maxResultLength(s, "toUpperCase", new Object[0]);
            for (Locale l : new Locale[] {Locale.ROOT, new Locale("tr"), new Locale("az"),
                    new Locale("lt"), Locale.GERMAN}) {
                assertBounds("toUpperCase", s.toUpperCase(l), bound);
                assertBounds("toLowerCase", s.toLowerCase(l), bound);
            }
        }
    }

    /** Most letters outside ASCII keep their length, so large text in them still fits. */
    @Test
    void caseChangesOfTextOutsideAsciiAreNotOverestimated() {
        String text = "\u041f\u0440\u0438\u0432\u0435\u0442 \u4f60\u597d \u3053\u3093";
        assertEquals(text.length(), StringGrowth.maxResultLength(text, "toUpperCase",
                new Object[0]));
        assertEquals(text.length(), StringGrowth.maxResultLength(new UtilitiesModel(),
                "toUpperCase", new Object[] {text}));
    }

    /** Escaping counts what each character becomes, so the bound is the result's length. */
    @Test
    void escapingIsExact() {
        UtilitiesModel utils = new UtilitiesModel();
        String html = "<p>a&amp;b \"c\" d'e \u00e9</p>";
        assertEquals(utils.escapeHTML(html).length(),
                StringGrowth.maxResultLength(utils, "escapeHTML", new Object[] {html}));
        assertEquals(utils.escapeXML(html).length(),
                StringGrowth.maxResultLength(utils, "escapeXML", new Object[] {html}));
    }

    /** A case change only adds characters for characters outside ASCII. */
    @Test
    void caseChangesOfAsciiAreNotOverestimated() {
        String s = "Hello, World 0123456789";
        assertEquals(s.length(), StringGrowth.maxResultLength(s, "toUpperCase", new Object[0]));
        assertEquals(s.length(), StringGrowth.maxResultLength(s, "toLowerCase",
                new Object[] {Locale.ROOT}));
    }

    /** Every char on its own, a surrogate pair, and all of them in one string. */
    private static List<String> everyChar() {
        List<String> texts = new ArrayList<>();
        StringBuilder all = new StringBuilder();
        for (int c = 0; c <= 0xFFFF; c++) {
            texts.add(String.valueOf((char) c));
            all.append((char) c);
        }
        texts.add("\uD83D\uDE00");
        texts.add("x\uD83Dy");
        texts.add(all.toString());
        return texts;
    }

    /** UtilitiesModel methods that take one string, keyed by name. */
    private static Map<String, UnaryOperator<String>> oneStringCalls(UtilitiesModel utils) {
        Map<String, UnaryOperator<String>> calls = new LinkedHashMap<>();
        calls.put("toUpperCase", utils::toUpperCase);
        calls.put("escapeHTML", utils::escapeHTML);
        calls.put("unescapeHTML", utils::unescapeHTML);
        calls.put("escapeXML", utils::escapeXML);
        calls.put("unescapeXML", utils::unescapeXML);
        calls.put("escapeJavaScript", utils::escapeJavaScript);
        calls.put("unescapeJavaScript", utils::unescapeJavaScript);
        calls.put("removeHTML", utils::removeHTML);
        calls.put("autoformat", utils::autoformat);
        calls.put("hexEncode", utils::hexEncode);
        calls.put("encodeEmail", utils::encodeEmail);
        calls.put("encode", utils::encode);
        calls.put("decode", utils::decode);
        calls.put("transformToHTMLSubset", utils::transformToHTMLSubset);
        return calls;
    }

    private static void assertUtilityBounds(UtilitiesModel utils, String name, String actual,
            Object... args) {
        long longestArg = 0;
        for (Object arg : args) {
            longestArg = Math.max(longestArg, arg instanceof CharSequence
                    ? ((CharSequence) arg).length() : 0);
        }
        long bound = StringGrowth.maxResultLength(utils, name, args);
        assertTrue(actual == null || Math.max(bound, longestArg) >= actual.length(),
                name + " produced " + (actual == null ? 0 : actual.length())
                        + " chars, bound was " + bound);
    }

    @Test
    void utilitiesStringCallsAreBounded() {
        UtilitiesModel utils = new UtilitiesModel();
        List<String> texts = new ArrayList<>(everyChar());
        texts.add("a&lt;b&gt;b&lt;/b&gt;&lt;a href='http://x/\u00e9&quot;&amp;'&gt;y&lt;/a&gt;"
                + "&lt;a href=\"mailto:\u03d1&#233;&\"&gt;&lt;br /&gt;&quot;");
        texts.add("<a href=\"x\" rel=\"nofollow\"> <A HREF=y rel=\"nofollow\">");
        texts.add("mailto:a.b-c@d.example.org, mailto:a.b-c@d.example.org and x@y.z w@v.u.");
        texts.add("\\u0041 \\x41 \\101 %41 %E2%82%AC &#x1F600; &thetasym; &#1;");
        for (Map.Entry<String, UnaryOperator<String>> call : oneStringCalls(utils).entrySet()) {
            for (String s : texts) {
                String actual;
                try {
                    actual = call.getValue().apply(s);
                } catch (IllegalArgumentException ex) {
                    continue; // a malformed escape: the call throws
                }
                assertUtilityBounds(utils, call.getKey(), actual, s);
            }
        }
        for (String s : TEXTS) {
            assertUtilityBounds(utils, "truncateNicely", utils.truncateNicely(s, 1, 2, "..."),
                    s, 1, 2, "...");
            assertUtilityBounds(utils, "truncateText", utils.truncateText(s, 1, 2, "..."),
                    s, 1, 2, "...");
            assertUtilityBounds(utils, "truncate", utils.truncate(s, 1, 2, "..."),
                    s, 1, 2, "...");
            assertUtilityBounds(utils, "left", utils.left(s, 2), s, 2);
            assertUtilityBounds(utils, "removeHTML", utils.removeHTML(s, false), s, false);
        }
        for (int n = 0; n < 10; n++) {
            byte[] bytes = new byte[n];
            assertUtilityBounds(utils, "toBase64", utils.toBase64(bytes), bytes, null);
        }
    }

    @Test
    void dateFormatsAreBounded() {
        Date date = new Date(1_700_000_000_000L);
        // every pattern letter SimpleDateFormat knows, each at its longest form
        StringBuilder all = new StringBuilder();
        for (char c : "GyYMLwWDdFEuaHkKhmsSzZX".toCharArray()) {
            all.append(String.valueOf(c).repeat(c == 'X' ? 3 : 4)).append(' ');
        }
        int checked = 0;
        for (Locale l : Locale.getAvailableLocales()) {
            String actual = new java.text.SimpleDateFormat(all.toString(), l).format(date);
            long bound = StringGrowth.maxResultLength(new UtilitiesModel(), "formatDate",
                    new Object[] {date, all.toString(), null});
            assertTrue(bound >= actual.length(), l + ": " + actual.length() + " > " + bound);
            checked++;
        }
        assertTrue(checked > 100, "only " + checked + " locales were checked");
    }

    /** Plain text gives a bound equal to its length, so large plain text still fits. */
    @Test
    void utilitiesCallsOnPlainTextAreNotOverestimated() {
        UtilitiesModel utils = new UtilitiesModel();
        String plain = "Hello World 0123456789 abc";
        for (String name : oneStringCalls(utils).keySet()) {
            if ("hexEncode".equals(name)) {
                continue; // every character becomes at least three
            }
            assertTrue(StringGrowth.maxResultLength(utils, name, new Object[] {plain})
                    <= plain.length(), name);
        }
        assertEquals(3 * plain.length(), StringGrowth.maxResultLength(utils, "hexEncode",
                new Object[] {plain}));
        // escapeHTML and escapeXML leave most letters outside ASCII alone
        String text = "\u041f\u0440\u0438\u0432\u0435\u0442 \u4f60\u597d \u3053\u3093\u306b\u3061\u306f";
        for (String name : new String[] {"escapeHTML", "escapeXML", "transformToHTMLSubset"}) {
            assertEquals(text.length(), StringGrowth.maxResultLength(utils, name,
                    new Object[] {text}), name);
        }
        // a value that isn't a string is counted by its text
        assertTrue(StringGrowth.maxResultLength(utils, "escapeHTML", new Object[] {12345}) <= 5);
        assertTrue(StringGrowth.maxResultLength(utils, "escapeHTML",
                new Object[] {new StringBuilder("<<<ab")}) >= "&lt;&lt;&lt;ab".length());
    }

    /** A String-returning method added to UtilitiesModel is refused until it is bounded here. */
    @Test
    void everyUtilitiesStringMethodIsKnown() {
        UtilitiesModel utils = new UtilitiesModel();
        for (Method m : UtilitiesModel.class.getMethods()) {
            if (Modifier.isPublic(m.getModifiers())
                    && CharSequence.class.isAssignableFrom(m.getReturnType())) {
                assertFalse(StringGrowth.isRefused(utils, m.getName(), m.getReturnType()),
                        m.getName());
            }
        }
        assertTrue(StringGrowth.isRefused(utils, "someNewMethod", String.class));
        assertFalse(StringGrowth.isRefused(utils, "someNewMethod", boolean.class));
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

    /** A list appended as an Object is printed in full, so it counts as its text. */
    @Test
    void appendingAListCountsItsText() {
        java.util.List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }
        StringBuilder b = new StringBuilder("x");
        long bound = StringGrowth.maxResultLength(b, "append", new Object[] {list});
        assertTrue(bound >= 1 + list.toString().length(), "bound was " + bound);
        assertTrue(StringGrowth.maxResultLength(b, "append", new Object[] {null}) >= 5);
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
