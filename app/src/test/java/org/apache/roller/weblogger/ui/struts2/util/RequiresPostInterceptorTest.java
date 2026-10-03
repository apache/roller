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

package org.apache.roller.weblogger.ui.struts2.util;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import org.apache.struts2.StrutsStatics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequiresPostInterceptorTest {

    /** An action with one marked and one unmarked method. */
    public static class SampleAction {
        @RequiresPost
        public String save() {
            return "saved";
        }

        public String execute() {
            return "shown";
        }
    }

    private final RequiresPostInterceptor interceptor = new RequiresPostInterceptor();

    @Test
    void markedMethodRefusesGet() throws Exception {
        ActionInvocation invocation = invocation("save", "GET");

        assertEquals(UIAction.DENIED, interceptor.intercept(invocation));
        verify(invocation, never()).invoke();
    }

    @Test
    void markedMethodAcceptsPost() throws Exception {
        ActionInvocation invocation = invocation("save", "POST");

        assertEquals("next", interceptor.intercept(invocation));
        verify(invocation).invoke();
    }

    @Test
    void unmarkedMethodAcceptsGet() throws Exception {
        ActionInvocation invocation = invocation("execute", "GET");

        assertEquals("next", interceptor.intercept(invocation));
        verify(invocation).invoke();
    }

    @Test
    void defaultMethodIsTreatedAsExecute() throws Exception {
        ActionInvocation invocation = invocation(null, "GET");

        assertEquals("next", interceptor.intercept(invocation));
        verify(invocation).invoke();
    }

    @Test
    void markedMethodRefusedWhenRequestIsMissing() throws Exception {
        ActionInvocation invocation = invocation("save", null);

        assertEquals(UIAction.DENIED, interceptor.intercept(invocation));
        verify(invocation, never()).invoke();
    }

    private static ActionInvocation invocation(String methodName, String httpMethod) throws Exception {
        Map<String, Object> contextMap = new HashMap<>();
        if (httpMethod != null) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getMethod()).thenReturn(httpMethod);
            contextMap.put(StrutsStatics.HTTP_REQUEST, request);
        }
        ActionProxy proxy = mock(ActionProxy.class);
        when(proxy.getMethod()).thenReturn(methodName);
        when(proxy.getActionName()).thenReturn("sample");

        ActionInvocation invocation = mock(ActionInvocation.class);
        when(invocation.getProxy()).thenReturn(proxy);
        when(invocation.getAction()).thenReturn(new SampleAction());
        when(invocation.getInvocationContext()).thenReturn(new ActionContext(contextMap));
        when(invocation.invoke()).thenReturn("next");
        return invocation;
    }
}
