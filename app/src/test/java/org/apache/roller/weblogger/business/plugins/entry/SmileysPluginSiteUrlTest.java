/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.business.plugins.entry;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Emoticon images link to the site URL of the request being rendered, not to
 * the one seen by whichever request first loaded the plugin.
 */
class SmileysPluginSiteUrlTest {

    private MockedStatic<WebloggerFactory> factory;

    @BeforeEach
    void setUp() throws Exception {
        PropertiesManager properties = mock(PropertiesManager.class);
        when(properties.getProperty("site.absoluteurl")).thenAnswer(
                invocation -> new RuntimeConfigProperty("site.absoluteurl", ""));
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPropertiesManager()).thenReturn(properties);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
    }

    @AfterEach
    void tearDown() {
        WebloggerRuntimeConfig.clearRequestAbsoluteContextURL();
        factory.close();
    }

    private static String renderDuring(String requestURL) throws Exception {
        WebloggerRuntimeConfig.setRequestAbsoluteContextURL(requestURL);
        try {
            SmileysPlugin plugin = new SmileysPlugin();
            plugin.init(null);
            return plugin.render(null, "Hello :-)");
        } finally {
            WebloggerRuntimeConfig.clearRequestAbsoluteContextURL();
        }
    }

    @Test
    void eachRequestGetsItsOwnHost() throws Exception {
        String first = renderDuring("http://first.example.com");
        String second = renderDuring("http://second.example.com");

        assertTrue(first.contains("src=\"http://first.example.com/images/smileys/smile.gif\""), first);
        assertTrue(second.contains("src=\"http://second.example.com/images/smileys/smile.gif\""), second);
        assertFalse(second.contains("first.example.com"), second);
    }

    @Test
    void renderingOutsideARequestDoesNotLinkToNull() throws Exception {
        SmileysPlugin plugin = new SmileysPlugin();
        plugin.init(null);
        String text = plugin.render(null, "Hello :-)");

        assertTrue(text.contains("/images/smileys/smile.gif"), text);
        assertFalse(text.contains("null/"), text);
    }
}
