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

package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The weblog's "about" text and an entry's search description are written into
 * a {@code <meta content="...">} attribute. They are stored as typed when weblog
 * admins are trusted, and HTML-encoded on output when they aren't, so the
 * templates must encode them exactly once: a quote must not end the attribute,
 * and text the sanitizer already encoded must not be encoded twice.
 */
public class MetaDescriptionEscapingTest {

    /** Minimal stand-ins for the model objects the templates read. */
    public static class StubModel {
        private final boolean permalink;
        private final StubWeblog weblog;
        private final StubEntry entry;

        StubModel(boolean permalink, String text) {
            this.permalink = permalink;
            this.weblog = new StubWeblog(text);
            this.entry = new StubEntry(text);
        }

        public boolean isPermalink() {
            return permalink;
        }

        public StubWeblog getWeblog() {
            return weblog;
        }

        public StubEntry getWeblogEntry() {
            return entry;
        }
    }

    public static class StubWeblog {
        private final String about;

        StubWeblog(String about) {
            this.about = about;
        }

        public String getAbout() {
            return about;
        }
    }

    public static class StubEntry {
        private final String searchDescription;

        StubEntry(String searchDescription) {
            this.searchDescription = searchDescription;
        }

        public String getSearchDescription() {
            return searchDescription;
        }
    }

    private static String render(String loaderPath, String source, boolean permalink, String text) {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "file");
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("resource.loader.file.path", loaderPath);
        VelocityEngine engine = new VelocityEngine();
        engine.init(props);

        VelocityContext ctx = new VelocityContext();
        ctx.put("model", new StubModel(permalink, text));
        ctx.put("utils", new UtilitiesModel());
        StringWriter out = new StringWriter();
        engine.evaluate(ctx, out, "test", source);
        return out.toString();
    }

    private static String sharedMacro(boolean permalink, String text) {
        return render("src/main/webapp/WEB-INF/velocity",
                "#parse('weblog.vm')#showMetaDescription()", permalink, text);
    }

    private static String gaurav(boolean permalink, String text) {
        return render("src/main/webapp/themes/gaurav", "#parse('std_head.vm')", permalink, text);
    }

    /** Stored value, and what the attribute must contain. */
    private static final String[][] CASES = {
        // stored as typed (trusted admins): a quote must not close the attribute
        {"Tips \" autofocus onfocus=\"alert(1)", "Tips &quot; autofocus onfocus=&quot;alert(1)"},
        // already encoded by the sanitizer (untrusted admins): encoded once, not twice
        {"Tips &quot; autofocus onfocus=&quot;alert(1)", "Tips &quot; autofocus onfocus=&quot;alert(1)"},
        {"Matt's \"weblog\" & notes", "Matt's &quot;weblog&quot; &amp; notes"},
        {"<b>Bold</b> text", "Bold  text"},
    };

    @Test
    public void metaDescriptionIsEncodedOnce() {
        for (String[] c : CASES) {
            String[] outputs = {
                sharedMacro(false, c[0]), sharedMacro(true, c[0]),
                gaurav(false, c[0]), gaurav(true, c[0])
            };
            for (String out : outputs) {
                assertTrue(out.contains("content=\"" + c[1] + "\">"), c[0] + " gave: " + out);
            }
        }
    }
}
