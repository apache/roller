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
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.security.CustomUserRegistry;
import org.apache.roller.ui.authoring.struts.formbeans.UserFormEx;
import org.apache.commons.lang.StringUtils;


/**
 * Actions for creating a new user.
 *
 * @struts.action name="userFormEx" path="/roller-ui/user"
 * 		scope="session" parameter="method"
 *
 * @struts.action-forward name="registerUser.page" path=".UserNew"
 * @struts.action-forward name="welcome.page" path=".welcome"
 */
public class UserNewAction extends UserBaseAction {
    
    private static Log mLogger = LogFactory.getLog(UserNewAction.class);
    
    
    /** Process GET of new user page (allows admin to create a user) */
    public ActionForward createUser(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        UserFormEx userForm = (UserFormEx)actionForm;
        userForm.setAdminCreated(true);
        return registerUser(mapping, actionForm, request, response);
    }
    
    
    public ActionForward cancel(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        return mapping.findForward("main");
    }
    
    
    /** Process GET of user registration page (allows users to register themselves). */
    public ActionForward registerUser(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        ActionForward forward = mapping.findForward("registerUser.page");
        ActionErrors errors = new ActionErrors();
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        try {
            UserFormEx userForm = (UserFormEx)actionForm;
            
            userForm.setLocale(Locale.getDefault().toString());
            userForm.setTimeZone(TimeZone.getDefault().getID());
            userForm.setDataFromSSO(false);
            
            // Let's see if there's any user-authentication available from Acegi
            // and retrieve custom user data to pre-populate form.
            boolean usingSSO = RollerConfig.getBooleanProperty("users.sso.enabled");
            if(usingSSO) {            
              UserData fromSSO = CustomUserRegistry.getUserDetailsFromAuthentication();
              if(fromSSO != null) {
                userForm.copyFrom(fromSSO, request.getLocale());
                userForm.setDataFromSSO(true);
              }
            }
            
            userForm.setPasswordText(null);
            userForm.setPasswordConfirm(null);
            request.setAttribute("model", new BasePageModel(
                    "newUser.addNewUser", request, response, mapping));
        } catch (Exception e) {
            errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.editing.user", e.toString()));
            mLogger.error("ERROR in newUser", e);
        }
        return forward;
    }
    
    
    /** Process POST of new user information. */
    public ActionForward add(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        UserFormEx form = (UserFormEx)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        ServletContext ctx = rreq.getServletContext();
        RollerContext rollerContext = RollerContext.getRollerContext();
        
        boolean reg_allowed =
                RollerRuntimeConfig.getBooleanProperty("users.registration.enabled");
        
        if ( !reg_allowed && !request.isUserInRole("admin")) {
            throw new ServletException("New users disabled!");
        }
        
        ActionMessages msgs = new ActionMessages();
        ActionMessages errors = validate(form, new ActionErrors());
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        } else try {
            // Add new user
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            
            UserData ud = new UserData();
            form.copyTo(ud, request.getLocale()); // doesn't copy password
            ud.setId(null);
            ud.setDateCreated(new java.util.Date());
            ud.setEnabled(Boolean.TRUE);
            
            // If user set both password and passwordConfirm then reset password
            if (    !StringUtils.isEmpty(form.getPasswordText())
            && !StringUtils.isEmpty(form.getPasswordConfirm())) {
                ud.resetPassword(RollerFactory.getRoller(),
                        form.getPasswordText(), form.getPasswordConfirm());
            }
            
            // save new user
            mgr.addUser(ud);
            RollerFactory.getRoller().flush();
            
            if (form.getAdminCreated()) {
                // User created for admin, so return to new user page with empty form
                msgs.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("newUser.created"));
                saveMessages(request, msgs);
                form.reset(mapping, request);
                return createUser(mapping, actionForm, request, response);
            } else {
                // User registered, so go to welcome page
                request.setAttribute("contextURL",
                        RollerRuntimeConfig.getAbsoluteContextURL());
                
                // Invalidate session, otherwise new user who was originally authenticated 
                // via LDAP/SSO will remain logged in with a but without a valid Roller role.
                request.getSession().invalidate();
                
                return mapping.findForward("welcome.page");
            }
                       
        } catch (RollerException e) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(e.getMessage()));
            saveErrors(request,errors);
            mLogger.error("ERROR in addUser", e);
            form.setUserName("");
        }
        
        if (form.getAdminCreated()) {
            return mapping.findForward("createUser");
        } else {
            // Error occured, send user back to new user form
            return mapping.findForward("registerUser");
        }
    }
    
    
    /** Validate user form. TODO: replace with Struts validation. */
    protected ActionMessages validate( UserFormEx form, ActionMessages errors ) {
      
        // if usingSSO, we don't want to error on empty password/username from HTML form.
        form.setDataFromSSO(false);
        boolean usingSSO = RollerConfig.getBooleanProperty("users.sso.enabled");
        if(usingSSO) {
          boolean storePassword = RollerConfig.getBooleanProperty("users.sso.passwords.saveInRollerDb");
          UserData fromSSO = CustomUserRegistry.getUserDetailsFromAuthentication();
          if(fromSSO != null) {
            String password = RollerConfig.getProperty("users.sso.passwords.defaultValue", "<unknown>");
            if(storePassword) {
              password = fromSSO.getPassword();
            }
            form.setPasswordText(password);
            form.setPasswordConfirm(password);
            form.setUserName(fromSSO.getUserName());
            form.setDataFromSSO(true);
          }
        }
      
        super.validate(form, errors);
        if (    StringUtils.isEmpty(form.getPasswordText())
        && StringUtils.isEmpty(form.getPasswordConfirm())) {
            errors.add( ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.add.user.missingPassword"));
        }
        return errors;
    }
    
}