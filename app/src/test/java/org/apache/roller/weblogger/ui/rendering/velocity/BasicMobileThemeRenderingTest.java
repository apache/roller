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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renders the mobile macros the Basic Mobile theme uses from the real
 * weblog.vm, to check they work on a page that loads no jQuery.
 */
public class BasicMobileThemeRenderingTest {

    private static VelocityEngine engine;

    @BeforeAll
    public static void setUpEngine() {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "file");
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("resource.loader.file.path",
                "src/main/webapp/WEB-INF/velocity,src/main/webapp/themes/basicmobile");
        engine = new VelocityEngine();
        engine.init(props);
    }

    public static class StubModel {
        public String getTerm() { return null; }
    }

    public static class StubUrl {
        public String getSearch() { return "/blog/search"; }
    }

    public static class StubText {
        public String get(String key) { return key; }
    }

    private static String render(String template) {
        VelocityContext ctx = new VelocityContext();
        ctx.put("model", new StubModel());
        ctx.put("url", new StubUrl());
        ctx.put("text", new StubText());
        StringWriter out = new StringWriter();
        engine.evaluate(ctx, out, "test", "#parse('weblog.vm')" + template);
        return out.toString();
    }

    @Test
    public void emptySearchOpensTheAlertWithoutJQuery() {
        String html = render("#showMobileSearchForm($weblog false)");
        assertTrue(html.contains("function validateSearch(form)"), html);
        assertTrue(html.contains("data-role=\"popup\" id=\"searchDialog\""), html);
        assertTrue(html.contains("location.replace(\"#searchDialog\")"), html);
        // jQuery Mobile's popup is used only where a page still loads it
        assertTrue(html.contains("if (window.jQuery && jQuery.fn.popup) {"), html);
        assertFalse(html.contains("$("), html);
    }

    @Test
    public void themeStylesheetShowsTheTargetedAlert() throws IOException {
        String css = Files.readString(
                Path.of("src/main/webapp/themes/basicmobile/basic-mobile-custom.css"));
        String hidden = "[data-role=\"popup\"] {\n    display: none;\n}";
        String shown = "[data-role=\"popup\"]:target {\n    display: block;";
        assertTrue(css.contains(hidden), css);
        assertTrue(css.contains(shown), css);
    }
}
