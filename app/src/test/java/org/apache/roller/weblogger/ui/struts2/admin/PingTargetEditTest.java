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

import javax.servlet.http.HttpServletRequest;

import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PingTargetEditTest {

    @Test
    void getDoesNotSave() throws Exception {
        PingTargetManager pingTargets = pingTargetManager();
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            Weblogger weblogger = weblogger(factory, pingTargets);
            PingTargetEdit action = action("GET");

            assertEquals(PingTargetEdit.INPUT, action.execute());
            verify(pingTargets, never()).savePingTarget(any());
            verify(weblogger, never()).flush();
        }
    }

    @Test
    void postSaves() throws Exception {
        PingTargetManager pingTargets = pingTargetManager();
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            Weblogger weblogger = weblogger(factory, pingTargets);
            PingTargetEdit action = action("POST");

            assertEquals(PingTargetEdit.SUCCESS, action.execute());
            verify(pingTargets).savePingTarget(any(PingTarget.class));
            verify(weblogger).flush();
        }
    }

    private static PingTargetManager pingTargetManager() throws Exception {
        PingTargetManager pingTargets = mock(PingTargetManager.class);
        when(pingTargets.isUrlWellFormed(anyString())).thenReturn(true);
        when(pingTargets.isHostnameKnown(anyString())).thenReturn(true);
        return pingTargets;
    }

    private static Weblogger weblogger(MockedStatic<WebloggerFactory> factory,
            PingTargetManager pingTargets) {
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPingTargetManager()).thenReturn(pingTargets);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        return weblogger;
    }

    private static PingTargetEdit action(String method) {
        PingTargetEdit action = spy(new PingTargetEdit());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyString(), anyString());
        action.setActionName("commonPingTargetAdd");
        action.getBean().setName("example");
        action.getBean().setPingUrl("http://ping.example.com/rpc");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        action.setServletRequest(request);
        action.myPrepare();
        return action;
    }
}
