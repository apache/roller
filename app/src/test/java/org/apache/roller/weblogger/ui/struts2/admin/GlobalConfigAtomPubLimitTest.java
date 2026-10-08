/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.struts2.dispatcher.HttpParameters;
import org.apache.struts2.dispatcher.Parameter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GlobalConfigAtomPubLimitTest {

    private static final String LIMIT_PROPERTY = "webservices.atomPubMaxEntrySize";

    @Test
    void invalidLimitsAreNotSaved() throws Exception {
        PropertiesManager properties = mock(PropertiesManager.class);
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            Weblogger weblogger = mock(Weblogger.class);
            when(weblogger.getPropertiesManager()).thenReturn(properties);
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            for (String value : new String[] {"0", "-1", "2147483647", "2147483648", "not a number", ""}) {
                GlobalConfig action = action(value);
                assertEquals(GlobalConfig.ERROR, action.save(), value);
                assertTrue(action.hasActionErrors(), value);
                assertEquals("1048576", action.getProperties().get(LIMIT_PROPERTY).getValue());
            }
            verify(properties, never()).saveProperties(any());
        }
    }

    @Test
    void validLimitsAreSaved() throws Exception {
        PropertiesManager properties = mock(PropertiesManager.class);
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            Weblogger weblogger = mock(Weblogger.class);
            when(weblogger.getPropertiesManager()).thenReturn(properties);
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            for (String value : new String[] {"1", "1048576", "2097152", "2147483646"}) {
                clearInvocations(properties);
                GlobalConfig action = action(value);
                assertEquals(GlobalConfig.SUCCESS, action.save(), value);
                assertFalse(action.hasActionErrors(), value);
                assertEquals(value, action.getProperties().get(LIMIT_PROPERTY).getValue());
                verify(properties).saveProperties(action.getProperties());
            }
        }
    }

    @Test
    void serverSettingsDefineAOneMiBDefault() {
        assertEquals("1048576", WebloggerRuntimeConfig.getRuntimeConfigDefs()
                .getConfigDefs().get(0).getPropertyDef(LIMIT_PROPERTY).getDefaultValue());
    }

    private GlobalConfig action(String value) {
        GlobalConfig action = spy(new GlobalConfig());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        action.setGlobalConfigDef(WebloggerRuntimeConfig.getRuntimeConfigDefs().getConfigDefs().get(0));
        Map<String, RuntimeConfigProperty> values = new HashMap<>();
        values.put(LIMIT_PROPERTY, new RuntimeConfigProperty(LIMIT_PROPERTY, "1048576"));
        values.put("users.comments.plugins", new RuntimeConfigProperty("users.comments.plugins", ""));
        action.setProperties(values);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        action.setServletRequest(request);
        Parameter incomingLimit = mock(Parameter.class);
        when(incomingLimit.getValue()).thenReturn(value);
        HttpParameters parameters = mock(HttpParameters.class);
        when(parameters.get(LIMIT_PROPERTY)).thenReturn(incomingLimit);
        when(parameters.get("users.comments.plugins")).thenReturn(mock(Parameter.class));
        action.setParameters(parameters);
        return action;
    }
}
