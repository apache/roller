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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.ui.authoring.struts.formbeans.UserFormEx;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerSession;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Allows user to edit his/her profile.
 *
 * @struts.action name="userFormEx" path="/roller-ui/yourProfile" parameter="method"
 * @struts.action-forward name="yourProfile.page" path=".YourProfile"
 */
public class YourProfileAction extends UserBaseAction {
    
    private static Log mLogger = LogFactory.getLog(YourProfileAction.class);
    
    
    /** If method param is not specified, use HTTP verb to pick method to call */
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        if (request.getMethod().equals("GET")) {
            return edit(mapping, actionForm, request, response);
        }
        return save(mapping, actionForm, request, response);
    }
    
    
    public ActionForward cancel(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        return mapping.findForward("yourWebsites");
    }
    
    
    /** Load form with authenticated user and forward to your-profile page */
    public ActionForward edit(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        ActionForward forward = mapping.findForward("yourProfile.page");
        try {
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            UserData ud = rollerSession.getAuthenticatedUser();
            UserFormEx form = (UserFormEx)actionForm;
            form.copyFrom(ud, request.getLocale());
            form.setPasswordText(null);
            form.setPasswordConfirm(null);
            form.setLocale(ud.getLocale());
            form.setTimeZone(ud.getTimeZone());
            request.setAttribute("model", new BasePageModel(
                    "yourProfile.title", request, response, mapping));
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        // if user logged in with a cookie, display a warning that they
        // can't change passwords
        if (mLogger.isDebugEnabled()) {
            log.debug("checking for cookieLogin...");
        }
        if (request.getSession().getAttribute("cookieLogin") != null) {
            ActionMessages messages = new ActionMessages();
            
            // add warning messages
            messages.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("yourProfile.cookieLogin"));
            saveMessages(request, messages);
        }
        return forward;
    }
    
    
    /** Update user based on posted form data */
    public ActionForward save(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        UserFormEx form = (UserFormEx)actionForm;
        ActionForward forward = mapping.findForward("yourProfile.page");
        ActionMessages msgs = new ActionMessages();
        try {
            ActionMessages errors = validate(form, new ActionErrors());
            if (errors.size() == 0) {
                // We ONLY modify the user currently logged in
                RollerSession rollerSession = RollerSession.getRollerSession(request);
                UserData data = rollerSession.getAuthenticatedUser();
                
                // We want to be VERY selective about what data gets updated
                data.setFullName(form.getFullName());
                data.setEmailAddress(form.getEmailAddress());
                data.setLocale(form.getLocale());
                data.setTimeZone(form.getTimeZone());
                
                // If user set both password and passwordConfirm then reset password
                if (    !StringUtils.isEmpty(form.getPasswordText())
                && !StringUtils.isEmpty(form.getPasswordConfirm())) {
                    try {
                        data.resetPassword(RollerFactory.getRoller(),
                                form.getPasswordText(),
                                form.getPasswordConfirm());
                    } catch (RollerException e) {
                        msgs.add(ActionMessages.GLOBAL_MESSAGE,
                                new ActionMessage("yourProfile.passwordResetError"));
                    }
                }
                
                // save the updated profile
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                mgr.saveUser(data);
                RollerFactory.getRoller().flush();
                
                request.setAttribute("model", new BasePageModel(
                        "yourProfile.title", request, response, mapping));
                
                saveMessages(request, msgs);
            } else {
                saveErrors(request, errors);
            }
            return mapping.findForward("yourWebsites");
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
    }
    
}
