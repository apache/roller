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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.planet.tasks.RefreshRollerPlanetTask;
import org.apache.roller.weblogger.planet.tasks.SyncWebsitesTask;
import org.apache.roller.weblogger.ui.rendering.servlets.PlanetFeedServlet;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.ui.struts2.util.UISecurityInterceptor;
import org.apache.struts2.StrutsStatics;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

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
    void planetChangesAreRefusedUnlessPosted() {
        HttpServletRequest get = mock(HttpServletRequest.class);
        when(get.getMethod()).thenReturn("GET");
        Map<String, Object> context = new HashMap<>();
        context.put(StrutsStatics.HTTP_REQUEST, get);
        ActionContext.setContext(new ActionContext(context));
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            assertEquals(UIAction.DENIED, new PlanetConfig().save());
            assertEquals(UIAction.DENIED, new PlanetGroups().delete());
            assertEquals(UIAction.DENIED, new PlanetGroupSubs().saveGroup());
            assertEquals(UIAction.DENIED, new PlanetGroupSubs().saveSubscription());
            assertEquals(UIAction.DENIED, new PlanetGroupSubs().deleteSubscription());
            factory.verifyNoInteractions();
        } finally {
            ActionContext.setContext(null);
        }
    }

    @Test
    void onlyAPostCountsAsAPostRequest() {
        PlanetUIAction action = new PlanetGroups();
        try {
            ActionContext.setContext(new ActionContext(new HashMap<>()));
            assertFalse(action.isPostRequest());

            HttpServletRequest post = mock(HttpServletRequest.class);
            when(post.getMethod()).thenReturn("post");
            Map<String, Object> context = new HashMap<>();
            context.put(StrutsStatics.HTTP_REQUEST, post);
            ActionContext.setContext(new ActionContext(context));
            assertTrue(action.isPostRequest());
        } finally {
            ActionContext.setContext(null);
        }
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
