/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.rendering.model;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Themes written for older Roller versions pass message keys that no longer
 * exist as date patterns. {@code $text.get} returns the key itself, which is not
 * a valid pattern. That must not fail the whole page.
 */
public class UtilitiesModelFormatDateTest {

    // 2026-11-14 18:30 UTC
    private static final Date DATE = new Date(1794681000000L);
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private UtilitiesModel utils;

    @BeforeEach
    public void setUp() throws Exception {
        Weblog weblog = new Weblog();
        weblog.setHandle("test");
        weblog.setLocale("en_US");
        weblog.setTimeZone("UTC");
        utils = new UtilitiesModel();
        Field field = UtilitiesModel.class.getDeclaredField("weblog");
        field.setAccessible(true);
        field.set(utils, weblog);
    }

    @Test
    public void validPatternIsUsed() {
        assertEquals("2026-11-14 18:30", utils.formatDate(DATE, "yyyy-MM-dd HH:mm", UTC));
    }

    @Test
    public void invalidPatternFallsBackToTheDefaultFormat() {
        DateFormat expected = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US);
        expected.setTimeZone(UTC);
        assertEquals(expected.format(DATE),
                utils.formatDate(DATE, "macro.weblog.date.timestampFormat", UTC));
    }

    @Test
    public void entryListStillRendersWithARemovedMessageKey() {
        VelocityEngine engine = new VelocityEngine();
        engine.init();
        VelocityContext ctx = new VelocityContext();
        ctx.put("utils", utils);
        ctx.put("entries", List.of("First entry", "Second entry"));
        ctx.put("date", DATE);
        StringWriter out = new StringWriter();
        engine.evaluate(ctx, out, "old-theme",
                "#foreach($e in $entries)<h2>$e</h2>"
                + "<p>$utils.formatDate($date, 'macro.weblog.date.timestampFormat')</p>#end");
        String html = out.toString();
        assertTrue(html.contains("<h2>First entry</h2>") && html.contains("<h2>Second entry</h2>"), html);
        assertTrue(html.contains("2026"), html);
    }
}
