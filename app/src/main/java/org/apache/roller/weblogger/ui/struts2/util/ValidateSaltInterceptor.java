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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.core.filters.SaltValidator;
import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * Validates salts after Struts has parsed a multipart form request.
 */
public class ValidateSaltInterceptor extends AbstractInterceptor implements StrutsStatics {

    private static final long serialVersionUID = 2446434402795510394L;
    private static final Log log = LogFactory.getLog(ValidateSaltInterceptor.class);

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        ActionContext context = invocation.getInvocationContext();
        HttpServletRequest request = (HttpServletRequest) context.get(HTTP_REQUEST);

        if (SaltValidator.isMultipartFormPost(request)
                && !SaltValidator.consumeSubmittedSalt(request)) {
            if (log.isDebugEnabled()) {
                log.debug("Valid salt value not found on multipart POST to URL : "
                        + request.getServletPath());
            }
            throw new ServletException("Security Violation");
        }

        return invocation.invoke();
    }
}
