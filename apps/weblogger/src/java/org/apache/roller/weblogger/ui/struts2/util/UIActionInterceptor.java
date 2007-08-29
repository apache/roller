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

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.struts2.StrutsStatics;


/**
 * A struts2 interceptor for configuring specifics of the weblogger ui.
 */
public class UIActionInterceptor extends AbstractInterceptor 
        implements StrutsStatics {
    
    private static Log log = LogFactory.getLog(UIActionInterceptor.class);
    
    
    public String intercept(ActionInvocation invocation) throws Exception {
        
        log.debug("Entering UIActionInterceptor");
        
        final Object action = invocation.getAction();
        final ActionContext context = invocation.getInvocationContext();
        
        HttpServletRequest request = (HttpServletRequest) context.get(HTTP_REQUEST);
        
        // is this one of our own UIAction classes?
        if (action instanceof UIAction) {
            
            log.debug("action is a UIAction, setting relevant attributes");
            
            UIAction theAction = (UIAction) action;
            
            // extract the authenticated user and set it
            RollerSession rses = RollerSession.getRollerSession(request);
            if(rses != null) {
                theAction.setAuthenticatedUser(rses.getAuthenticatedUser());
            }
            
            // extract the work weblog and set it
            String weblogHandle = theAction.getWeblog();
            if(!StringUtils.isEmpty(weblogHandle)) {
                Weblog weblog = null;
                try {
                    weblog = WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(weblogHandle);
                    if(weblog != null) {
                        theAction.setActionWeblog(weblog);
                    }
                } catch(Exception e) {
                    log.error("Error looking up action weblog - "+weblogHandle, e);
                }
            }
        }
        
        return invocation.invoke();
    }
    
}
