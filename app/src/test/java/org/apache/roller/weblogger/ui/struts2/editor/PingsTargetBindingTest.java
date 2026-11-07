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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.Collections;

import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.util.ValueStackFactory;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.reflection.ReflectionContextState;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * The ping target that the Pings action works on must be one of the stored
 * ping targets, selected by id.
 */
class PingsTargetBindingTest {

    private MockedStatic<WebloggerFactory> factory;
    private PingTargetManager pingTargets;
    private ValueStack stack;

    @BeforeEach
    void setUp() throws Exception {
        factory = mockStatic(WebloggerFactory.class);
        pingTargets = mock(PingTargetManager.class);
        when(pingTargets.getCommonPingTargets()).thenReturn(Collections.emptyList());
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPingTargetManager()).thenReturn(pingTargets);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);

        ConfigurationManager configurationManager = new ConfigurationManager("xwork");
        configurationManager.addContainerProvider(new XWorkConfigurationProvider());
        Container container = configurationManager.getConfiguration().getContainer();
        stack = container.getInstance(ValueStackFactory.class).createValueStack();
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    /** Set request parameters on the action the way the params interceptor does. */
    private void bind(Pings action, String name, String value) {
        stack.push(action);
        ReflectionContextState.setCreatingNullObjects(stack.getContext(), true);
        ReflectionContextState.setDenyMethodExecution(stack.getContext(), true);
        try {
            stack.setParameter(name, value);
        } finally {
            ReflectionContextState.setCreatingNullObjects(stack.getContext(), false);
            stack.pop();
        }
    }

    @Test
    void pingTargetFieldsInTheRequestDoNotMakeATarget() throws Exception {
        Pings action = new Pings();
        bind(action, "pingTarget.pingUrl", "http://localhost:1/");
        action.myPrepare();

        assertNull(action.getPingTarget());
    }

    @Test
    void pingTargetIdSelectsAStoredTarget() throws Exception {
        PingTarget stored = new PingTarget("target-1", "Stored", "http://ping.example.com/", false);
        when(pingTargets.getPingTarget("target-1")).thenReturn(stored);

        Pings action = new Pings();
        bind(action, "pingTargetId", "target-1");
        bind(action, "pingTarget.pingUrl", "http://localhost:1/");
        action.myPrepare();

        assertSame(stored, action.getPingTarget());
        assertSame("http://ping.example.com/", action.getPingTarget().getPingUrl());
    }
}
