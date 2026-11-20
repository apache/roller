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
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.roller.RollerException;
import org.apache.roller.weblogger.ui.rendering.RenderingException;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository.DeviceType;
import org.apache.roller.weblogger.ui.rendering.velocity.RollerVelocityTestSupport.CeilingWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A weblog template can loop, build strings and write output, so rendering
 * one has to stop at a size and time limit instead of running until the
 * server runs out of memory or threads. These render through the shipped
 * engine configuration and VelocityRenderer, as the page servlets do.
 */
public class RenderLimitsTest {

    /** Roller's default output limit, plus room for the last write. */
    private static final long OUTPUT_LIMIT = 10L * 1024 * 1024 + 64 * 1024;

    private static final Duration WITHIN = Duration.ofSeconds(30);

    @BeforeAll
    public static void setUpEngine() throws Exception {
        RollerVelocityTestSupport.install();
    }

    private static final class Outcome {
        CeilingWriter out = new CeilingWriter();
        RenderingException failure;
    }

    private static Outcome render(String name, String source) throws Exception {
        return render(name, source, new HashMap<>());
    }

    private static Outcome render(String name, String source, Map<String, Object> context)
            throws Exception {
        RollerVelocityTestSupport.addTemplate(name, source);
        Outcome outcome = new Outcome();
        VelocityRenderer renderer = new VelocityRenderer(
                RollerVelocityTestSupport.page(name), DeviceType.standard);
        try {
            renderer.render(context, outcome.out);
        } catch (RenderingException ex) {
            outcome.failure = ex;
        }
        return outcome;
    }

    /** RenderingException keeps its cause as the root cause, not in getCause(). */
    private static Throwable causeOf(Throwable t) {
        return t instanceof RollerException ? ((RollerException) t).getRootCause() : t.getCause();
    }

    private static boolean reachedTestCeiling(Throwable t) {
        for (; t != null; t = causeOf(t)) {
            if (t instanceof IllegalStateException
                    && "test ceiling reached".equals(t.getMessage())) {
                return true;
            }
        }
        return false;
    }

    /** Asserts the render failed with this limit, rather than after doing the work. */
    private static void assertLimit(Outcome outcome, String message) {
        assertNotNull(outcome.failure, "rendering should have failed with \"" + message
                + "\", but wrote " + outcome.out.getHead());
        Throwable t = outcome.failure;
        while (t != null && !(t instanceof RenderLimitException)) {
            t = causeOf(t);
        }
        assertNotNull(t, "rendering failed with something other than a limit: "
                + outcome.failure);
        assertTrue(t.getMessage().startsWith(message), "stopped by: " + t.getMessage());
    }

    /** Renders with a smaller output limit, so a test needn't build tens of megabytes. */
    private static Outcome renderWithLimit(String name, String source, int maxOutputChars)
            throws Exception {
        try (AutoCloseable limit = RollerVelocityTestSupport.property(
                RenderSession.MAX_OUTPUT_CHARS, String.valueOf(maxOutputChars))) {
            return render(name, source);
        }
    }

    private static void assertStoppedByRoller(Outcome outcome) {
        assertFalse(outcome.out.getCount() > OUTPUT_LIMIT,
                "rendering wrote " + outcome.out.getCount() + " chars before stopping");
        assertNotNull(outcome.failure, "rendering should have failed");
        assertFalse(reachedTestCeiling(outcome.failure),
                "only the test's own ceiling stopped rendering");
    }

    /**
     * A loop longer than the limit fails the render rather than stopping
     * quietly, which would serve and cache the page as if it were whole.
     */
    @Test
    public void aHugeForeachRangeFailsAtTheLoopLimit() {
        assertTimeoutPreemptively(WITHIN, () -> {
            Outcome outcome = render("limits-foreach.vm",
                    "#foreach($i in [1..2000000000])0123456789#end");
            assertLimit(outcome, "#foreach made more than 100000 passes");
            assertTrue(outcome.out.getCount() <= 100000 * 10,
                    "#foreach ran past 100000 passes: wrote " + outcome.out.getCount());
        });
    }

    @Test
    public void aForeachOfExactlyTheLoopLimitCompletes() throws Exception {
        Outcome whole = render("limits-foreach-whole.vm",
                "#set($n = 0)#foreach($i in [1..100000])#set($n = $i)#end$n");
        assertEquals(null, whole.failure);
        assertEquals("100000", whole.out.getHead());

        Outcome over = render("limits-foreach-over.vm",
                "#set($n = 0)#foreach($i in [1..100001])#set($n = $i)#end$n");
        assertLimit(over, "#foreach made more than 100000 passes");
    }

    @Test
    public void nestedLoopsStopAtTheOutputLimit() {
        assertTimeoutPreemptively(WITHIN, () -> assertStoppedByRoller(render(
                "limits-nested.vm",
                "#foreach($i in [1..100000])#foreach($j in [1..100000])"
                        + "0123456789#end#end")));
    }

    @Test
    public void limitsApplyInsideMacrosAndParsedTemplates() {
        RollerVelocityTestSupport.addTemplate("limits-parsed.vm", "#spin()");
        assertTimeoutPreemptively(WITHIN, () -> assertStoppedByRoller(render(
                "limits-macro.vm",
                "#macro(spin)#foreach($i in [1..100000])#foreach($j in [1..100000])"
                        + "0123456789#end#end#end#parse('limits-parsed.vm')")));
    }

    @Test
    public void aLoopThatWritesNothingStopsAtTheDeadline() {
        assertTimeoutPreemptively(WITHIN, () -> {
            Outcome outcome = render("limits-deadline.vm",
                    "#foreach($i in [1..100000])#foreach($j in [1..100000])"
                            + "#set($k = $j)#end#end");
            assertNotNull(outcome.failure, "rendering should have failed");
        });
    }

    @Test
    public void aStringDoubledInPlaceStopsAtTheSizeLimit() {
        assertTimeoutPreemptively(WITHIN, () -> {
            Outcome outcome = render("limits-doubling.vm",
                    "#set($s = '0123456789')"
                            + "#foreach($i in [1..22])#set($s = \"$s$s\")#end$s.length()");
            assertNotNull(outcome.failure,
                    "rendering should have failed, but wrote " + outcome.out.getHead());
        });
    }

    @Test
    public void stringRepeatIsNotAvailableToTemplates() {
        assertTimeoutPreemptively(WITHIN, () -> {
            Outcome outcome = render("limits-repeat.vm",
                    "#set($s = '0123456789')"
                            + "$s.repeat(3000000)$s.repeat(3000000)$s.repeat(3000000)");
            assertFalse(reachedTestCeiling(outcome.failure),
                    "String.repeat filled the output");
            assertTrue(outcome.out.getCount() < 1024 * 1024,
                    "String.repeat wrote " + outcome.out.getCount() + " chars");
            assertFalse(outcome.out.getHead().contains("01234567890123456789"),
                    "String.repeat ran: " + outcome.out.getHead());
        });
    }

    /** The limits are chained after SecureUberspector, which still applies. */
    @Test
    public void theIntrospectionSandboxStillApplies() throws Exception {
        Outcome outcome = render("limits-sandbox.vm",
                "#set($s = 'x')[$s.class.classLoader]"
                        + "[$s.getClass().forName('java.lang.Runtime')]");
        assertEquals("[$s.class.classLoader][$s.getClass().forName('java.lang.Runtime')]",
                outcome.out.getHead());
    }

    /** A 10,240-character string of digits, 1,024 of them zeros. */
    private static final String TEN_THOUSAND_DIGITS = "#set($s = '0123456789')"
            + "#foreach($i in [1..10])#set($s = \"$s$s\")#end";

    private static long allocatedBytes() {
        return ((com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean())
                .getThreadAllocatedBytes(Thread.currentThread().getId());
    }

    /**
     * Renders on this thread and returns how many bytes the render allocated,
     * after a first render that loads the engine and compiles the template.
     */
    private static long allocatedByRender(String name, String source,
            Map<String, Object> context) throws Exception {
        Outcome warmUp = render(name, source, context);
        assertNotNull(warmUp.failure, "rendering should have failed");
        long before = allocatedBytes();
        Outcome outcome = render(name, source, context);
        long allocated = allocatedBytes() - before;
        assertNotNull(outcome.failure, "rendering should have failed");
        assertFalse(reachedTestCeiling(outcome.failure),
                "only the test's own ceiling stopped rendering");
        return allocated;
    }

    /**
     * Each of these would build a string of about 42 million characters, four
     * times the output limit. The call has to be refused before it runs, not
     * after the result is built.
     */
    @Test
    public void growingStringCallsAreRefusedBeforeTheyRun() throws Exception {
        String[] calls = {
            "$s.replace('0', \"$s$s$s$s\")",
            "$s.replaceAll('0', \"$s$s$s$s\")",
            "$utils.replace($s, '0', \"$s$s$s$s\")",
        };
        Map<String, Object> context = new HashMap<>();
        context.put("utils", new UtilitiesModel());
        for (int i = 0; i < calls.length; i++) {
            long allocated = allocatedByRender("limits-growing-" + i + ".vm",
                    TEN_THOUSAND_DIGITS + calls[i], context);
            // replaceAll builds part of the result before it stops; without
            // the checks each of these allocates over 100 MB
            assertTrue(allocated < 64L * 1024 * 1024,
                    calls[i] + " allocated " + allocated + " bytes before it was stopped");
        }
    }

    /**
     * $utils methods that escape or encode text can make each character
     * several, so they too have to be refused before they run. Each of these
     * on 6,291,456 characters would build at least twice the output limit.
     */
    @Test
    public void expandingUtilityCallsAreRefusedBeforeTheyRun() throws Exception {
        String[] calls = {
            "$utils.escapeHTML($s)",
            "$utils.escapeXML($s)",
            "$utils.escapeJavaScript($s)",
            "$utils.encode($s)",
            "$utils.hexEncode($s)",
            "$utils.autoformat($s)",
        };
        Map<String, Object> context = new HashMap<>();
        context.put("utils", new UtilitiesModel());
        String setup = "#set($s = \"<\u00e9\n\")#foreach($i in [1..21])#set($s = \"$s$s\")#end";
        // building the string alone, without the call
        String baselineName = "limits-expanding-baseline.vm";
        render(baselineName, setup + "$s.length()", context);
        long before = allocatedBytes();
        Outcome baseline = render(baselineName, setup + "$s.length()", context);
        long building = allocatedBytes() - before;
        assertEquals(null, baseline.failure);
        for (int i = 0; i < calls.length; i++) {
            long allocated = allocatedByRender("limits-expanding-" + i + ".vm",
                    setup + calls[i], context) - building;
            // without the checks each call allocates at least 25 MB more
            assertTrue(allocated < 16L * 1024 * 1024,
                    calls[i] + " allocated " + allocated + " bytes before it was stopped");
        }
    }

    /** A case change of a large ASCII string fits whenever the string does. */
    @Test
    public void caseChangesOfALargeAsciiStringStillWork() throws Exception {
        Map<String, Object> context = new HashMap<>();
        context.put("utils", new UtilitiesModel());
        Outcome outcome = render("limits-case.vm", FIVE_MILLION_DIGITS
                + "$s.toUpperCase().length() $s.toLowerCase().length()"
                + " $utils.toUpperCase($s).length() $utils.escapeHTML($s).length()",
                context);
        assertEquals(null, outcome.failure);
        assertEquals("5242880 5242880 5242880 5242880", outcome.out.getHead());
    }

    /**
     * A string just under the output limit split into one-character pieces
     * would allocate millions of strings before anything checks their size,
     * so a split may return no more pieces than a #foreach can visit.
     */
    @Test
    public void splittingIntoMorePiecesThanALoopCanVisitIsRefused() throws Exception {
        // 10,485,760 characters, just under the output limit
        String nearTheLimit = TEN_THOUSAND_DIGITS
                + "#foreach($i in [1..10])#set($s = \"$s$s\")#end";
        long allocated = allocatedByRender("limits-split.vm",
                nearTheLimit + "$s.split('').size()", new HashMap<>());
        // the string itself takes about 40 MB to build; the pieces would add
        // over 500 MB
        assertTrue(allocated < 160L * 1024 * 1024,
                "split allocated " + allocated + " bytes before it was stopped");

        Outcome ordinary = render("limits-split-ordinary.vm",
                TEN_THOUSAND_DIGITS + "$s.split('0').size() $s.split('', 3).size()");
        assertEquals(null, ordinary.failure);
        assertEquals("1025 3", ordinary.out.getHead());
    }

    /** String.format can pad to any width, so it is not offered. */
    @Test
    public void stringFormatIsNotAvailableToTemplates() throws Exception {
        Outcome outcome = render("limits-format.vm",
                "#set($s = 'x')$s.format('%05000000d', 1)");
        assertFalse(outcome.out.getHead().contains("00000000"),
                "String.format ran");
        assertTrue(outcome.out.getCount() < 1024, "String.format wrote "
                + outcome.out.getCount() + " chars");
    }

    @Test
    public void ordinaryStringCallsStillWork() throws Exception {
        Map<String, Object> context = new HashMap<>();
        context.put("utils", new UtilitiesModel());
        Outcome outcome = render("limits-ordinary.vm",
                "#set($s = 'Hello, World')"
                        + "[$s.replace('o', '0')][$s.replace('', '-')][$s.replaceAll('[A-Z]', '_')]"
                        + "[$s.replaceFirst('l+', 'L')][$s.concat('!')][$s.toUpperCase()]"
                        + "[$s.toLowerCase()][$s.substring(7)][$s.trim()]"
                        + "[$utils.replace($s, ' ', '_')]",
                context);
        assertEquals(null, outcome.failure);
        assertEquals("[Hell0, W0rld][-H-e-l-l-o-,- -W-o-r-l-d-][_ello, _orld]"
                + "[HeLo, World][Hello, World!][HELLO, WORLD]"
                + "[hello, world][World][Hello, World]"
                + "[Hello,_World]", outcome.out.getHead());
    }

    @Test
    public void ordinaryLiteralsSumsBlocksAndExpressionsStillWork() throws Exception {
        Outcome outcome = render("limits-ordinary-literals.vm",
                "#set($a = 'x')#set($b = \"[$a$a]\")#set($c = $a + $b + 1)"
                        + "#define($d)<#if($a)$a#end>#end#set($e = $d + '')"
                        + "#macro(wrap)($bodyContent)#end"
                        + "$b|$c|$d|$e|#@wrap()$a#end|$a.matches('x')|$c.split('\\[').size()"
                        + "|$c.replaceAll('[x]', 'y')");
        assertEquals(null, outcome.failure);
        assertEquals("[xx]|x[xx]1|<x>|<x>|(x)|true|2|y[yy]1", outcome.out.getHead());
    }

    /** A string of 5,242,880 digits, half the output limit. */
    private static final String FIVE_MILLION_DIGITS = "#set($s = '0123456789')"
            + "#foreach($i in [1..19])#set($s = \"$s$s\")#end";

    /** About 30 million characters of text, from loops that insert nothing. */
    private static final String THIRTY_MILLION_CHARS =
            "#foreach($i in [1..100000])#foreach($j in [1..30])0123456789#end#end";

    private static void assertStoppedInMemory(String name, String source) throws Exception {
        Outcome outcome = render(name, source);
        assertNotNull(outcome.failure, name + " should have failed, but wrote "
                + outcome.out.getCount() + " chars");
        assertFalse(reachedTestCeiling(outcome.failure),
                "only the test's own ceiling stopped rendering");
        assertTrue(outcome.out.getCount() < 1024, name + " wrote " + outcome.out.getCount());
    }

    @Test
    public void aStringDoubledWithPlusStopsAtTheSizeLimit() throws Exception {
        assertStoppedInMemory("limits-plus-doubling.vm", "#set($s = '0123456789')"
                + "#foreach($i in [1..22])#set($s = $s + $s)#end$s.length()");
    }

    @Test
    public void oneLongSumStopsAtTheSizeLimit() throws Exception {
        assertStoppedInMemory("limits-plus-sum.vm", FIVE_MILLION_DIGITS
                + "#set($t = $s + $s + $s + $s)$t.length()");
    }

    @Test
    public void aStringLiteralWithManyReferencesStopsAtTheSizeLimit() throws Exception {
        assertStoppedInMemory("limits-literal-refs.vm", FIVE_MILLION_DIGITS
                + "#set($t = \"$s$s$s$s\")$t.length()");
    }

    @Test
    public void loopsInsideAStringLiteralStopAtTheSizeLimit() throws Exception {
        assertStoppedInMemory("limits-literal-loops.vm",
                "#set($t = \"" + THIRTY_MILLION_CHARS + "\")$t.length()");
    }

    @Test
    public void aDefinedBlockTurnedIntoAStringStopsAtTheSizeLimit() throws Exception {
        assertStoppedInMemory("limits-define.vm",
                "#define($b)" + THIRTY_MILLION_CHARS + "#end#set($t = $b + '')$t.length()");
    }

    @Test
    public void macroRecursionThatWritesNothingStopsAtTheDeadline() throws Exception {
        long start = System.nanoTime();
        // four calls a level, 13 levels deep: about 90 million calls
        String call = "#set($e = $d - 1)#spin($e)";
        Outcome outcome = render("limits-macro-recursion.vm",
                "#macro(spin $d)#if($d > 0)" + call + call + call + call + "#end#end"
                        + "#spin(13)done");
        long seconds = (System.nanoTime() - start) / 1_000_000_000L;
        assertNotNull(outcome.failure, "rendering should have failed after "
                + seconds + " seconds, but wrote " + outcome.out.getHead());
        assertTrue(seconds < 10, "rendering took " + seconds + " seconds");
    }

    @Test
    public void aSlowRegularExpressionStopsAtTheDeadline() throws Exception {
        String[] calls = {"$s.matches('(.*a){12}b')", "$s.split('(.*a){12}b', 2)"};
        for (int i = 0; i < calls.length; i++) {
            long start = System.nanoTime();
            Outcome outcome = render("limits-regex-" + i + ".vm",
                    "#set($s = '" + "a".repeat(36) + "')[" + calls[i] + "]");
            long seconds = (System.nanoTime() - start) / 1_000_000_000L;
            assertNotNull(outcome.failure, calls[i] + " should have failed, but wrote "
                    + outcome.out.getHead() + " in " + seconds + " seconds");
            assertTrue(seconds < 10, calls[i] + " took " + seconds + " seconds");
        }
    }

    @Test
    public void aRegexReplaceWithGroupsOnALongStringStillWorks() throws Exception {
        Outcome outcome = render("limits-regex-groups.vm", TEN_THOUSAND_DIGITS
                + "#set($r = $s.replaceAll('(\\d)', '<$1>'))$r.length() $r.substring(0, 9)");
        assertEquals(null, outcome.failure);
        assertEquals("30720 <0><1><2>", outcome.out.getHead());
    }

    /**
     * Velocity asks a node for its template's name to say where something
     * went wrong, so the nodes the parser swaps in have to know it too.
     */
    @Test
    public void arithmeticOnAnUndefinedReferenceStillRenders() throws Exception {
        Outcome outcome = render("limits-undefined-sum.vm",
                "#set($x = $undefined + 1)ok");
        assertEquals(null, outcome.failure);
        assertEquals("ok", outcome.out.getHead());
    }

    @Test
    public void endlessMacroRecursionStopsAtTheDepthLimit() throws Exception {
        Outcome outcome = render("limits-macro-depth.vm", "#macro(r)#r()#end#r()");
        // Velocity's own error for a macro that calls itself too deeply gets
        // the renderer's error page
        assertEquals(null, outcome.failure);
        assertTrue(outcome.out.getHead().contains("<!-- error-parse -->"),
                "rendering wrote " + outcome.out.getHead());
    }

    /** 81,920 characters, for tests with a limit of 100,000. */
    private static final String EIGHTY_THOUSAND_DIGITS = "#set($s = '0123456789')"
            + "#foreach($i in [1..13])#set($s = \"$s$s\")#end";

    /** A list or map is checked as text before Velocity turns it into one string. */
    @Test
    public void aLargeListIsRefusedBeforeItIsWrittenAsText() throws Exception {
        String[] uses = {
            "$r",
            "$r.toString()",
            "#set($t = '' + $r)",
            "#set($s = 'x')$s.valueOf($r)",
            "#if($r == 'x')yes#end",
            "#set($m = {'k': $r})$m",
            "#set($m = {'k': $r})$m.entrySet()",
            "#set($m = {'k': $r})#foreach($e in $m.entrySet())$e#end",
        };
        for (int i = 0; i < uses.length; i++) {
            Outcome outcome = renderWithLimit("limits-list-text-" + i + ".vm",
                    "#set($r = [1..2000000])" + uses[i], 100_000);
            assertLimit(outcome, "a string would be longer than 100000 characters");
        }
    }

    @Test
    public void aDeeplyNestedListIsRefusedBeforeItIsWrittenAsText() throws Exception {
        Outcome outcome = renderWithLimit("limits-nested-list.vm",
                "#set($l = [1])#foreach($i in [1..20])#set($l = [$l, $l])#end$l", 100_000);
        assertLimit(outcome, "a string would be longer than 100000 characters");
    }

    @Test
    public void smallListsAndMapsStillPrint() throws Exception {
        Outcome outcome = render("limits-list-ordinary.vm",
                "#set($l = [1, 'two', [3]])#set($m = {'a': 1})"
                        + "#set($x = $l.add($l))$l $m ${l.size()} #if($m != 'x')ne#end");
        assertEquals(null, outcome.failure);
        assertEquals("[1, two, [3], (this Collection)] {a=1} 4 ne", outcome.out.getHead());
    }

    @Test
    public void aTemplateThatBuildsAHugeListStopsAtTheLimit() throws Exception {
        Outcome doubled = render("limits-list-doubling.vm",
                "#set($l = [1])#foreach($i in [1..21])#set($x = $l.addAll($l))#end$l.size()");
        assertLimit(doubled, "rendering added more than 1000000 items");

        // the range is refused before any of it is copied
        Outcome copied = render("limits-list-copy.vm",
                "#set($l = [])#set($x = $l.addAll([1..1000001]))$l.size()");
        assertLimit(copied, "rendering added more than 1000000 items");

        Outcome array = render("limits-list-array.vm",
                "#set($r = [1..1000001])#set($a = $r.toArray())$a.size()");
        assertLimit(array, "rendering added more than 1000000 items");

        // adding a large list as one item is one item
        Outcome one = render("limits-list-one.vm",
                "#set($l = [])#set($x = $l.add([1..2000000]))$l.size() ${l.get(0).size()}");
        assertEquals(null, one.failure);
        assertEquals("1 2000000", one.out.getHead());

        Outcome map = render("limits-map.vm",
                "#set($m = {})#foreach($i in [1..1000])#foreach($j in [1..1001])"
                        + "#set($x = $m.put(\"$i-$j\", $j))#end#end$m.size()");
        assertLimit(map, "rendering added more than 1000000 items");
    }

    @Test
    public void aTemplateThatKeepsManyLongStringsStopsAtTheLimit() throws Exception {
        // each string is new, so the list would hold 50 times 81,920 characters
        Outcome outcome = renderWithLimit("limits-kept-strings.vm", EIGHTY_THOUSAND_DIGITS
                + "#set($l = [])#foreach($i in [1..50])#set($x = $l.add(\"$s$i\"))#end"
                + "$l.size()", 100_000);
        assertLimit(outcome, "rendering built more than 1000000 characters of strings");
    }

    @Test
    public void comparingTwoLargeListsElementByElementIsRefused() throws Exception {
        Outcome outcome = render("limits-contains-all.vm",
                "#set($a = [1..100000])#set($b = [1..200])$a.containsAll($b)");
        assertLimit(outcome, "containsAll would compare more than 10000000 pairs");

        Outcome ordinary = render("limits-contains-all-ordinary.vm",
                "#set($a = [1..1000])#set($b = [1..200])$a.containsAll($b)");
        assertEquals(null, ordinary.failure);
        assertEquals("true", ordinary.out.getHead());
    }

    @Test
    public void aTemplateThatBuildsAHugeNumberStopsAtTheLimit() throws Exception {
        String big = "#set($b = 9223372036854775807 + 1)";
        String[] growth = {
            "$b.shiftLeft(200000)",
            "$b.pow(5000)",
            "$b.setBit(200000)",
            "#foreach($i in [1..15])#set($b = $b * $b)#end",
        };
        for (int i = 0; i < growth.length; i++) {
            Outcome outcome = render("limits-number-" + i + ".vm", big + growth[i]);
            assertLimit(outcome, "a number would have more than 100000 bits");
        }
        Outcome prime = render("limits-number-prime.vm", big + "$b.isProbablePrime(50)");
        assertLimit(prime, "isProbablePrime is not available to templates");

        Outcome ordinary = render("limits-number-ordinary.vm", big + "$b.shiftLeft(2) ${b.pow(2)}");
        assertEquals(null, ordinary.failure);
        assertEquals("36893488147419103232 85070591730234615865843651857942052864",
                ordinary.out.getHead());
    }

    @Test
    public void utilitiesSplitIntoMorePiecesThanALoopCanVisitIsRefused() throws Exception {
        Map<String, Object> context = new HashMap<>();
        context.put("utils", new UtilitiesModel());
        context.put("s", "a,".repeat(150_000));
        Outcome outcome = render("limits-utils-split.vm", "$utils.split($s, ',').size()",
                context);
        assertLimit(outcome, "split produced more than 100000 pieces");

        context.put("s", ",,a,,b,c,,");
        Outcome ordinary = render("limits-utils-split-ordinary.vm",
                "$utils.split($s, ',').size()", context);
        assertEquals(null, ordinary.failure);
        assertEquals("3", ordinary.out.getHead());
    }

    @Test
    public void stringMethodsThatSplitWithoutTheLimitsAreNotAvailable() throws Exception {
        Outcome outcome = render("limits-split-delimiters.vm",
                "#set($s = 'a,b')[$s.splitWithDelimiters(',', 0)][$s.lines()]");
        assertEquals(null, outcome.failure);
        assertEquals("[$s.splitWithDelimiters(',', 0)][$s.lines()]", outcome.out.getHead());
    }

    /** Velocity calls a class's static methods on its Class object. */
    @Test
    public void staticStringMethodsAreNotAvailableThroughTheClass() throws Exception {
        Outcome outcome = render("limits-class-static.vm",
                "#set($s = 'x')[$s.class.format('%s', 'y')][$s.getClass().join('-', ['a'])]"
                        + "[$s.class.name][$s.getClass().getName()]");
        assertEquals(null, outcome.failure);
        assertEquals("[$s.class.format('%s', 'y')][$s.getClass().join('-', ['a'])]"
                + "[java.lang.String][java.lang.String]", outcome.out.getHead());
    }

    /**
     * A replace with a group that doesn't exist, or with no replacement,
     * shows the template error page as before, rather than failing the page.
     */
    @Test
    public void aRegexReplaceTheStringMethodRefusesShowsTheTemplateError() throws Exception {
        String[] calls = {"$s.replaceAll('b', '$1')", "$s.replaceAll('b', $nothing)",
            "$s.replaceFirst('x', $nothing)"};
        for (int i = 0; i < calls.length; i++) {
            Outcome outcome = render("limits-regex-error-" + i + ".vm",
                    "#set($s = 'abc')[" + calls[i] + "]");
            assertEquals(null, outcome.failure, calls[i]);
            assertTrue(outcome.out.getHead().contains("<!-- error-parse -->"),
                    calls[i] + " wrote " + outcome.out.getHead());
        }
        Outcome nothingToReplace = render("limits-regex-null-unmatched.vm",
                "#set($s = 'abc')[$s.replaceAll('x', $nothing)]");
        assertEquals(null, nothingToReplace.failure);
        assertEquals("[abc]", nothingToReplace.out.getHead());
    }

    @Test
    public void aRegexThatRepeatsAnEmptyMatchIsRefused() throws Exception {
        Outcome outcome = render("limits-regex-empty.vm",
                "#set($s = '')$s.matches('(?:a?|b?)(?:a?|b?)c')");
        assertLimit(outcome, "a regular expression repeats a part that can match an empty");
    }

    /** A template error can quote a long value, which the error page shortens. */
    @Test
    public void aTemplateErrorAboutALongValueStaysShort() throws Exception {
        Outcome outcome = render("limits-long-error.vm", TEN_THOUSAND_DIGITS
                + "#set($t = \"x$s\")#set($i = 1)$i.parseInt($t)");
        assertEquals(null, outcome.failure);
        assertTrue(outcome.out.getHead().contains("<!-- error-parse -->"),
                "rendering wrote " + outcome.out.getHead());
        assertTrue(outcome.out.getCount() < 5000, "the error page wrote "
                + outcome.out.getCount() + " chars");
    }

    @Test
    public void aBlankOrInvalidLimitFallsBackToTheDefault() throws Exception {
        for (String value : new String[] {"", " ", "ten"}) {
            try (AutoCloseable limit = RollerVelocityTestSupport.property(
                    RenderSession.MAX_OUTPUT_CHARS, value)) {
                RenderSession session = RenderSession.begin(null);
                try {
                    assertEquals(10L * 1024 * 1024, session.getMaxOutputChars(), value);
                } finally {
                    session.end();
                }
            }
        }
    }

    @Test
    public void aSessionStartedInsideAnotherSharesItsBudget() throws Exception {
        try (AutoCloseable limit = RollerVelocityTestSupport.property(
                RenderSession.MAX_OUTPUT_CHARS, "1000")) {
            RenderSession outer = RenderSession.begin(null);
            try {
                RenderSession.chargeBuilt(9000);
                RenderSession inner = RenderSession.begin(null);
                try {
                    assertThrows(RenderLimitException.class,
                            () -> RenderSession.chargeBuilt(2000));
                } finally {
                    inner.end();
                }
            } finally {
                outer.end();
            }
        }
    }
}
