/*
 * UIActionInterceptor.java
 *
 * Created on April 16, 2007, 5:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.ui.core.util.struts2;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.ui.core.RollerSession;
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
            theAction.setAuthenticatedUser(rses.getAuthenticatedUser());
            
            // extract the work weblog and set it
            String weblogHandle = request.getParameter(RequestConstants.WEBLOG);
            if(!StringUtils.isEmpty(weblogHandle)) {
                WebsiteData weblog = null;
                try {
                    UserManager mgr = RollerFactory.getRoller().getUserManager();
                    weblog = mgr.getWebsiteByHandle(weblogHandle);
                } catch(Exception e) {
                    
                }
                theAction.setActionWeblog(weblog);
            }
        }
        
        return invocation.invoke();
    }
    
}
