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

package org.apache.roller.ui.core.struts.actions;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.acegisecurity.ui.rememberme.TokenBasedRememberMeServices;
import org.apache.roller.ui.core.RollerSession;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/**
 * Handle user logouts.
 *
 * @struts.action path="/roller-ui/logout" name="LogoutAction"
 */
public class LogoutAction extends Action {
    
    
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm actionForm,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {
        
        // kill any current session
        HttpSession session = request.getSession(false);
        if(session != null) {
            request.getSession().removeAttribute(RollerSession.ROLLER_SESSION);
            request.getSession().invalidate();
        }
        
        // make sure remember me is discarded
        Cookie terminate = new Cookie(TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY, null);
        terminate.setMaxAge(0);
        response.addCookie(terminate);
        
        // redirect user back to homepage
        response.sendRedirect(request.getContextPath());
        
        return null;
    }
    
}
