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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.ui.struts2.util;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A struts2 interceptor for configuring specifics of the weblogger ui.
 */
public class UISecurityInterceptor extends MethodFilterInterceptor {

    private static final long serialVersionUID = -7787813271277874462L;
    private static Logger logger = LoggerFactory.getLogger(UISecurityInterceptor.class);

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public String doIntercept(ActionInvocation invocation) throws Exception {

        logger.debug("Entering UISecurityInterceptor");

        final Object action = invocation.getAction();

        // is this one of our own UIAction classes?
        if (action instanceof UIAction) {

            logger.debug("Checking security rules for the action...");

            final UIAction theAction = (UIAction) action;

            // are we requiring an authenticated user?
            if (theAction.getRequiredGlobalRole() != GlobalRole.NOAUTHNEEDED) {

                User authenticatedUser = theAction.getAuthenticatedUser();
                if (authenticatedUser == null) {
                    logger.debug("DENIED: required user not found");
                    return "access-denied";
                }

                if (!authenticatedUser.hasEffectiveGlobalRole(theAction.getRequiredGlobalRole())) {
                    logger.debug("DENIED: user {} does not have {} role", authenticatedUser.getUserName(),
                        theAction.getRequiredGlobalRole().name());
                    return "access-denied";
                }

                // are we requiring a valid action weblog?
                if (theAction.getRequiredWeblogRole() != WeblogRole.NOBLOGNEEDED) {

                    Weblog actionWeblog = theAction.getActionWeblog();
                    if (actionWeblog == null) {
                        logger.warn("User {} unable to process action because no weblog was defined " +
                                "(Check JSP form provided weblog value.)", authenticatedUser.getUserName(),
                                theAction.getActionName());
                        return "access-denied";
                    }

                    // are we also enforcing a specific weblog role?
                    UserWeblogRole uwr = userManager.getWeblogRole(authenticatedUser, actionWeblog);
                    if (uwr == null || !uwr.hasEffectiveWeblogRole(theAction.getRequiredWeblogRole())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("DENIED: user {} does not have {} role on weblog {} ", authenticatedUser,
                                    theAction.getRequiredWeblogRole(), actionWeblog.getHandle());
                        }
                        return "access-denied";
                    } else {
                        theAction.setActionWeblogRole(uwr.getWeblogRole());
                    }
                }

            }

        }

        return invocation.invoke();
    }

}
