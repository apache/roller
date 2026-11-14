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

package org.apache.roller.weblogger.ui.rendering.servlets;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * The RSD document and the link to it describe the Blogger and MetaWeblog
 * endpoint, so they must only appear while the site and the weblog both
 * answer it.
 */
public class RSDServletTest {

    private static Weblog weblog(Boolean clientSupport) {
        Weblog weblog = mock(Weblog.class);
        when(weblog.getEnableBloggerApi()).thenReturn(clientSupport);
        return weblog;
    }

    private static boolean describes(boolean siteEnabled, Boolean weblogEnabled) {
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("webservices.enableXmlRpc"))
                    .thenReturn(siteEnabled);
            return RSDServlet.describesXmlRpc(weblog(weblogEnabled));
        }
    }

    @Test
    public void describesXmlRpcOnlyWhenTheSiteAndTheWeblogAnswerIt() {
        assertTrue(describes(true, true));
        assertFalse(describes(false, true));
        assertFalse(describes(true, false));
        assertFalse(describes(true, null));
        assertFalse(describes(false, false));
    }

    /** Stands in for ConfigModel. */
    public static class StubConfig {
        private final boolean xmlRpcEnabled;

        StubConfig(boolean xmlRpcEnabled) {
            this.xmlRpcEnabled = xmlRpcEnabled;
        }

        public boolean getXmlRpcEnabled() {
            return xmlRpcEnabled;
        }
    }

    /** Stands in for the URL model; only the RSD link matters here. */
    public static class StubUrl {
        public String getRsd() {
            return "http://example.org/roller/blog/rsd";
        }
    }

    private static String autodiscovery(boolean siteEnabled, boolean weblogEnabled) {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "file");
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("resource.loader.file.path", "src/main/webapp/WEB-INF/velocity");
        VelocityEngine engine = new VelocityEngine();
        engine.init(props);

        VelocityContext ctx = new VelocityContext();
        ctx.put("weblog", weblog(weblogEnabled));
        ctx.put("config", new StubConfig(siteEnabled));
        ctx.put("url", new StubUrl());
        StringWriter out = new StringWriter();
        engine.evaluate(ctx, out, "test", "#parse('weblog.vm')#showAutodiscoveryLinks($weblog)");
        return out.toString();
    }

    @Test
    public void autodiscoveryLinksToTheRsdOnlyWhenItWouldBeServed() {
        String rsdLink = "rel=\"EditURI\"   type=\"application/rsd+xml\" title=\"RSD\" "
                + "href=\"http://example.org/roller/blog/rsd\"";
        assertTrue(autodiscovery(true, true).contains(rsdLink), autodiscovery(true, true));
        assertFalse(autodiscovery(false, true).contains("EditURI"));
        assertFalse(autodiscovery(true, false).contains("EditURI"));
        assertEquals(autodiscovery(false, true), autodiscovery(true, false));
    }
}
