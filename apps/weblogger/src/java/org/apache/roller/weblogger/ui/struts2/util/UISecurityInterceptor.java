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

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.core.RequestConstants;
import org.apache.roller.weblogger.ui.core.RollerSession;


/**
 * A struts2 interceptor for configuring specifics of the weblogger ui.
 */
public class UISecurityInterceptor extends AbstractInterceptor {
    
    private static Log log = LogFactory.getLog(UISecurityInterceptor.class);
    
    
    public String intercept(ActionInvocation invocation) throws Exception {
        
        log.debug("Entering UISecurityInterceptor");
        
        final Object action = invocation.getAction();
        
        // is this one of our own UIAction classes?
        if (action instanceof UISecurityEnforced &&
                action instanceof UIAction) {
            
            log.debug("action is UISecurityEnforced ... enforcing security rules");
            
            final UISecurityEnforced theAction = (UISecurityEnforced) action;
            
            // are we requiring an authenticated user?
            if(theAction.isUserRequired()) {
                
                User authenticatedUser = ((UIAction)theAction).getAuthenticatedUser();
                if(authenticatedUser == null) {
                    log.debug("DENIED: required user not found");
                    return "access-denied";
                }
                
                // are we also enforcing a specific role?
                if(theAction.requiredUserRole() != null) {
                    if(!authenticatedUser.hasRole(theAction.requiredUserRole())) {
                        log.debug("DENIED: user does not have role = "+theAction.requiredUserRole());
                        return "access-denied";
                    }
                }
                
                // are we requiring a valid action weblog?
                if(theAction.isWeblogRequired()) {
                    
                    Weblog actionWeblog = ((UIAction)theAction).getActionWeblog();
                    if(actionWeblog == null) {
                        log.debug("DENIED: required action weblog not found");
                        return "access-denied";
                    }
                    
                    // are we also enforcing a specific weblog permission?
                    if(theAction.requiredWeblogPermissions() > -1) {
                        
                        if(!actionWeblog.hasUserPermissions(authenticatedUser,
                                theAction.requiredWeblogPermissions())) {
                            log.debug("DENIED: user does not have required weblog permissions = "+
                                    theAction.requiredWeblogPermissions());
                            return "access-denied";
                        }
                    }
                }
                
            }
            
        }
        
        return invocation.invoke();
    }
    
}
