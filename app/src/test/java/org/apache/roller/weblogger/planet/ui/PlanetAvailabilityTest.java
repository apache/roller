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
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.planet.ui;

import java.io.InputStream;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import com.opensymphony.xwork2.ActionInvocation;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.planet.tasks.RefreshRollerPlanetTask;
import org.apache.roller.weblogger.planet.tasks.SyncWebsitesTask;
import org.apache.roller.weblogger.ui.rendering.servlets.PlanetFeedServlet;
import org.apache.roller.weblogger.ui.struts2.util.RequiresPost;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.ui.struts2.util.UISecurityInterceptor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanetAvailabilityTest {

    private static final String ENABLED = "planet.aggregator.enabled";

    @Test
    void planetIsOffByDefault() throws Exception {
        Properties defaults = new Properties();
        try (InputStream in = WebloggerConfig.class.getResourceAsStream(
                "/org/apache/roller/weblogger/config/roller.properties")) {
            defaults.load(in);
        }
        assertEquals("false", defaults.getProperty(ENABLED));
    }

    @Test
    void planetActionsFollowTheSetting() {
        PlanetUIAction action = new PlanetGroups();
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(() -> WebloggerConfig.getBooleanProperty(ENABLED)).thenReturn(false);
            assertFalse(action.isFeatureEnabled());

            config.when(() -> WebloggerConfig.getBooleanProperty(ENABLED)).thenReturn(true);
            assertTrue(action.isFeatureEnabled());
        }
    }

    @Test
    void disabledFeatureIsRefusedBeforeAnythingElse() throws Exception {
        UIAction action = new UIAction() {
            @Override
            public boolean isFeatureEnabled() {
                return false;
            }
        };
        ActionInvocation invocation = mock(ActionInvocation.class);
        when(invocation.getAction()).thenReturn(action);

        assertEquals(UIAction.DENIED, new UISecurityInterceptor().doIntercept(invocation));
        verify(invocation, never()).invoke();
    }

    @Test
    void planetChangesRequirePost() throws Exception {
        assertTrue(PlanetConfig.class.getMethod("save").isAnnotationPresent(RequiresPost.class));
        assertTrue(PlanetGroupSubs.class.getMethod("saveGroup").isAnnotationPresent(RequiresPost.class));
        assertTrue(PlanetGroupSubs.class.getMethod("saveSubscription").isAnnotationPresent(RequiresPost.class));
        assertTrue(PlanetGroupSubs.class.getMethod("deleteSubscription").isAnnotationPresent(RequiresPost.class));
        assertTrue(PlanetGroups.class.getMethod("delete").isAnnotationPresent(RequiresPost.class));
    }

    @Test
    void rollerStackIncludesThePostCheck() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        Document doc;
        try (InputStream in = getClass().getResourceAsStream("/struts.xml")) {
            doc = factory.newDocumentBuilder().parse(in);
        }
        NodeList stacks = doc.getElementsByTagName("interceptor-stack");
        boolean found = false;
        for (int i = 0; i < stacks.getLength(); i++) {
            Element stack = (Element) stacks.item(i);
            if (!"rollerStack".equals(stack.getAttribute("name"))) {
                continue;
            }
            NodeList refs = stack.getElementsByTagName("interceptor-ref");
            for (int j = 0; j < refs.getLength(); j++) {
                if ("RequiresPostInterceptor".equals(((Element) refs.item(j)).getAttribute("name"))) {
                    found = true;
                }
            }
        }
        assertTrue(found);
    }

    @Test
    void planetFeedIsNotServedWhileOff() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class);
             MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            config.when(() -> WebloggerConfig.getBooleanProperty(ENABLED)).thenReturn(false);

            new PlanetFeedServlet().doGet(request, response);

            verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
            factory.verifyNoInteractions();
        }
    }

    @Test
    void planetTasksDoNothingWhileOff() {
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class);
             MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            config.when(() -> WebloggerConfig.getBooleanProperty(ENABLED)).thenReturn(false);

            new RefreshRollerPlanetTask().runTask();
            new SyncWebsitesTask().runTask();

            factory.verifyNoInteractions();
        }
    }
}
