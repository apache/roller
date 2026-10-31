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

import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.core.filters.SaltValidator;
import org.apache.struts2.StrutsStatics;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionInvocation;
import org.apache.struts2.action.Action;
import org.apache.struts2.interceptor.AbstractInterceptor;

/**
 * Validates salts after Struts has wrapped a multipart form request, and
 * refuses requests other than POST to action methods that change data.
 */
public class ValidateSaltInterceptor extends AbstractInterceptor implements StrutsStatics {

    private static final long serialVersionUID = 2446434402795510394L;
    private static final Log log = LogFactory.getLog(ValidateSaltInterceptor.class);

    /**
     * Action methods that a GET may call. They only display data, except
     * "activate", which the account activation e-mail links to and which is
     * guarded by its one-time activation code. Any other method needs a POST,
     * which ValidateSaltFilter checks for a salt.
     */
    static final Set<String> DISPLAY_METHODS = Set.of(
            "execute", "input", "view", "search", "query", "edit", "cancel",
            "firstSave", "folderCreated", "deleteConfirm",
            "fetchDirectoryContentLight", "activate");

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        ActionContext context = invocation.getInvocationContext();
        HttpServletRequest request = (HttpServletRequest) context.get(HTTP_REQUEST);

        if (SaltValidator.isMultipartFormPost(request)
                && !Boolean.TRUE.equals(request.getAttribute(
                        SaltValidator.VALIDATED_REQUEST_ATTRIBUTE))) {
            if (request instanceof MultiPartRequestWrapper
                    && ((MultiPartRequestWrapper) request).hasErrors()) {
                return invocation.invoke();
            }

            try {
                SaltValidator.requireSubmittedSalt(request);
            } catch (ServletException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Valid salt value not found on multipart POST to URL : "
                            + request.getServletPath());
                }
                throw e;
            }
            request.setAttribute(
                    SaltValidator.VALIDATED_REQUEST_ATTRIBUTE, Boolean.TRUE);
        }

        if (!"POST".equalsIgnoreCase(request.getMethod())
                && !DISPLAY_METHODS.contains(invocation.getProxy().getMethod())) {
            if (log.isDebugEnabled()) {
                log.debug("Refused " + request.getMethod() + " to URL : "
                        + request.getServletPath() + "; the action method needs a POST");
            }
            HttpServletResponse response = (HttpServletResponse) context.get(HTTP_RESPONSE);
            response.setHeader("Allow", "POST");
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return Action.NONE;
        }

        return invocation.invoke();
    }
}
