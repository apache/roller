/*
 * UIActionInterceptor.java
 *
 * Created on April 16, 2007, 5:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.ui.core.util.struts2;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.ui.core.RollerSession;

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
                
                UserData authenticatedUser = ((UIAction)theAction).getAuthenticatedUser();
                if(authenticatedUser == null) {
                    log.debug("DENIED: required user not found");
                    return "access-denied";
                }
                
                // are we also enforcing a specific role?
                if(theAction.requiredUserRole() != null) {
                    log.debug("DENIED: user does not have role = "+theAction.requiredUserRole());
                    if(!authenticatedUser.hasRole(theAction.requiredUserRole())) {
                        return "access-denied";
                    }
                }
                
                // are we requiring a valid action weblog?
                if(theAction.isWeblogRequired()) {
                    
                    WebsiteData actionWeblog = ((UIAction)theAction).getActionWeblog();
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
