/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A struts2 interceptor for configuring specifics of the weblogger ui.
 */
public class UIActionInterceptor extends MethodFilterInterceptor implements
        StrutsStatics {

    private static final long serialVersionUID = -6452966127207525616L;
    private static Log log = LogFactory.getLog(UIActionInterceptor.class);

    public String doIntercept(ActionInvocation invocation) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Entering UIActionInterceptor");
        }

        final Object action = invocation.getAction();

        // is this one of our own UIAction classes?
        if (action instanceof UIAction) {

            if (log.isDebugEnabled()) {
                log.debug("action is a UIAction, setting relevant attributes");
            }

            UIAction theAction = (UIAction) action;

            // extract the authenticated user and set it
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
            String name = auth.getName();
            theAction.setAuthenticatedUser(mgr.getUserByUserName(name));

            // extract the work weblog and set it
            String weblogHandle = theAction.getWeblog();
            if (!StringUtils.isEmpty(weblogHandle)) {
                try {
                    Weblog weblog = WebloggerFactory.getWeblogger().getWeblogManager()
                            .getWeblogByHandle(weblogHandle);
                    if (weblog != null) {
                        theAction.setActionWeblog(weblog);
                    }
                } catch (Exception e) {
                    log.error("Error looking up action weblog - "
                            + weblogHandle, e);
                }
            }
        }

        return invocation.invoke();
    }

}
