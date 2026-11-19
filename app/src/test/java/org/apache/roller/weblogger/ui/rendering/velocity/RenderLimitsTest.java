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

import org.apache.roller.weblogger.ui.rendering.RenderingException;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository.DeviceType;
import org.apache.roller.weblogger.ui.rendering.velocity.RollerVelocityTestSupport.CeilingWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private static boolean reachedTestCeiling(Throwable t) {
        for (; t != null; t = t.getCause()) {
            if (t instanceof IllegalStateException
                    && "test ceiling reached".equals(t.getMessage())) {
                return true;
            }
        }
        return false;
    }

    private static void assertStoppedByRoller(Outcome outcome) {
        assertFalse(outcome.out.getCount() > OUTPUT_LIMIT,
                "rendering wrote " + outcome.out.getCount() + " chars before stopping");
        assertNotNull(outcome.failure, "rendering should have failed");
        assertFalse(reachedTestCeiling(outcome.failure),
                "only the test's own ceiling stopped rendering");
    }

    @Test
    public void aHugeForeachRangeStopsAtTheLoopLimit() {
        assertTimeoutPreemptively(WITHIN, () -> {
            Outcome outcome = render("limits-foreach.vm",
                    "#foreach($i in [1..2000000000])0123456789#end");
            assertFalse(reachedTestCeiling(outcome.failure),
                    "only the test's own ceiling stopped rendering");
            assertTrue(outcome.out.getCount() <= 100000 * 10,
                    "#foreach ran past 100000 passes: wrote " + outcome.out.getCount());
        });
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
}
