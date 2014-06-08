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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;

/**
 * A struts2 interceptor for configuring specifics of the weblogger ui.
 */
public class UISecurityInterceptor extends MethodFilterInterceptor {

    private static final long serialVersionUID = -7787813271277874462L;
    private static Log log = LogFactory.getLog(UISecurityInterceptor.class);

    public String doIntercept(ActionInvocation invocation) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Entering UISecurityInterceptor");
        }

        final Object action = invocation.getAction();

        // is this one of our own UIAction classes?
        if (action instanceof UISecurityEnforced && action instanceof UIAction) {

            if (log.isDebugEnabled()) {
                log.debug("action is UISecurityEnforced ... enforcing security rules");
            }

            final UISecurityEnforced theAction = (UISecurityEnforced) action;

            // are we requiring an authenticated user?
            if (theAction.isUserRequired()) {

                UserManager umgr = WebloggerFactory.getWeblogger()
                        .getUserManager();

                User authenticatedUser = ((UIAction) theAction)
                        .getAuthenticatedUser();
                if (authenticatedUser == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("DENIED: required user not found");
                    }
                    return "access-denied";
                }

                // are we also enforcing global permissions?
                if (theAction.requiredGlobalPermissionActions() != null
                        && !theAction.requiredGlobalPermissionActions()
                                .isEmpty()) {
                    GlobalPermission perm = new GlobalPermission(
                            theAction.requiredGlobalPermissionActions());
                    if (!umgr.checkPermission(perm, authenticatedUser)) {
                        if (log.isDebugEnabled()) {
                            log.debug("DENIED: user does not have permission = "
                                    + perm.toString());
                        }
                        return "access-denied";
                    }
                }

                // are we requiring a valid action weblog?
                if (theAction.isWeblogRequired()) {

                    Weblog actionWeblog = ((UIAction) theAction)
                            .getActionWeblog();
                    if (actionWeblog == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("DENIED: required action weblog not found");
                        }
                        return "access-denied";
                    }

                    // are we also enforcing a specific weblog permission?
                    if (theAction.requiredWeblogPermissionActions() != null
                            && !theAction.requiredWeblogPermissionActions()
                                    .isEmpty()) {
                        WeblogPermission required = new WeblogPermission(
                                actionWeblog,
                                theAction.requiredWeblogPermissionActions());

                        if (!umgr.checkPermission(required, authenticatedUser)) {
                            if (log.isDebugEnabled()) {
                                log.debug("DENIED: user does not have required weblog permissions = "
                                        + required);
                            }
                            return "access-denied";
                        }
                    }
                }

            }

        }

        return invocation.invoke();
    }

}
