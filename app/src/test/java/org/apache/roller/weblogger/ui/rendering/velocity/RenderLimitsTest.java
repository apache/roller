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

    @Test
    public void endlessMacroRecursionStopsAtTheDepthLimit() throws Exception {
        Outcome outcome = render("limits-macro-depth.vm", "#macro(r)#r()#end#r()");
        // Velocity's own error for a macro that calls itself too deeply gets
        // the renderer's error page
        assertEquals(null, outcome.failure);
        assertTrue(outcome.out.getHead().contains("<!-- error-parse -->"),
                "rendering wrote " + outcome.out.getHead());
    }

    @Test
    public void utilitiesAddNofollowIsNotAvailable() throws Exception {
        Map<String, Object> context = new HashMap<>();
        context.put("utils", new UtilitiesModel());
        Outcome outcome = render("limits-add-nofollow.vm",
                "#set($s = '<a href=\"/x\">x</a>')[$utils.addNofollow($s)]",
                context);
        assertEquals(null, outcome.failure);
        assertEquals("[$utils.addNofollow($s)]", outcome.out.getHead());
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
}
