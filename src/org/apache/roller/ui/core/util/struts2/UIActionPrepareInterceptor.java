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
 * A struts2 interceptor for doing custom prepare logic.
 */
public class UIActionPrepareInterceptor extends AbstractInterceptor {
    
    private static Log log = LogFactory.getLog(UIActionPrepareInterceptor.class);
    
    
    public String intercept(ActionInvocation invocation) throws Exception {
        
        log.debug("Entering UIActionPrepareInterceptor");
        
        final Object action = invocation.getAction();
        final ActionContext context = invocation.getInvocationContext();
        
        // is this one of our own UIAction classes?
        if (action instanceof UIActionPreparable) {
            
            log.debug("action is UIActionPreparable, calling myPrepare() method");
            
            UIActionPreparable theAction = (UIActionPreparable) action;
            theAction.myPrepare();
        }
        
        return invocation.invoke();
    }
    
}
