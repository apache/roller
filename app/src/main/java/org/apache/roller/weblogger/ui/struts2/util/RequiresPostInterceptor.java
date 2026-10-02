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

import static org.apache.struts2.StrutsStatics.HTTP_REQUEST;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Refuses requests that are not POSTs to action methods marked
 * {@link RequiresPost}. Unmarked methods are not affected.
 */
public class RequiresPostInterceptor extends AbstractInterceptor {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(RequiresPostInterceptor.class);

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        if (requiresPost(invocation)) {
            HttpServletRequest request = (HttpServletRequest)
                    invocation.getInvocationContext().get(HTTP_REQUEST);
            if (request == null || !"POST".equalsIgnoreCase(request.getMethod())) {
                if (log.isDebugEnabled()) {
                    log.debug("Refusing " + (request == null ? "unknown" : request.getMethod())
                            + " request to " + invocation.getProxy().getActionName()
                            + "!" + invocation.getProxy().getMethod());
                }
                return UIAction.DENIED;
            }
        }
        return invocation.invoke();
    }

    static boolean requiresPost(ActionInvocation invocation) {
        String methodName = invocation.getProxy().getMethod();
        if (methodName == null) {
            methodName = "execute";
        }
        try {
            Method method = invocation.getAction().getClass().getMethod(methodName);
            return method.isAnnotationPresent(RequiresPost.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
