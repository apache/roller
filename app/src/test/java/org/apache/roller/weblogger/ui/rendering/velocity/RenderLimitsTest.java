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

import org.apache.roller.weblogger.ui.rendering.RenderingException;
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
        RollerVelocityTestSupport.addTemplate(name, source);
        Outcome outcome = new Outcome();
        VelocityRenderer renderer = new VelocityRenderer(
                RollerVelocityTestSupport.page(name), DeviceType.standard);
        try {
            renderer.render(new HashMap<>(), outcome.out);
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
}
