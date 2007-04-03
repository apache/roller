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
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import org.apache.struts.util.RequestUtils;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.security.CustomUserRegistry;
import org.apache.roller.ui.authoring.struts.formbeans.UserFormEx;
import org.apache.roller.util.MailUtil;
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
            if (   !StringUtils.isEmpty(form.getPasswordText())
                && !StringUtils.isEmpty(form.getPasswordConfirm())) {
                ud.resetPassword(RollerFactory.getRoller(),
                        form.getPasswordText(), form.getPasswordConfirm());
            }
            
            boolean activationEnabled = RollerConfig.getBooleanProperty(
                    "user.account.activation.enabled");
            if (activationEnabled) {
                // User account will be enabled after the activation process
                ud.setEnabled(Boolean.FALSE);
                
                // Create & save the activation data
                String activationCode = UUID.randomUUID().toString();
                
                if (mgr.getUserByActivationCode(activationCode) != null) {
                    // In the *extremely* unlikely event that we generate an
                    // activation code that is already use, we'll retry 3 times.
                    int numOfRetries = 3;                    
                    if (numOfRetries < 1) numOfRetries = 1;                    
                    for (int i = 0; i < numOfRetries; i++) {
                        activationCode = UUID.randomUUID().toString();
                        if (mgr.getUserByActivationCode(activationCode) == null) {
                            break;
                        } else {
                            activationCode = null;
                        }
                    }
                    // In more unlikely event that three retries isn't enough                  
                    if (activationCode == null){
                        throw new RollerException("error.add.user.activationCodeInUse");
                    }
                }                
                ud.setActivationCode(activationCode);
            }
                        
            if (activationEnabled && ud.getActivationCode() != null) {
                // send activation mail to the user
                sendActivationMail(request, ud, errors);
                
                // activationStatus = 1:activated, 0:has to be activated, -1:error
                request.setAttribute("activationStatus", "0");
            }
            
            if (errors.size() > 0) {
                // Error occured so save it and bail out
                saveErrors(request, errors);
                
            } else {
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

                    // Invalidate session, otherwise new user who was originally 
                    // authenticated via LDAP/SSO will remain logged in with 
                    // a but without a valid Roller role.
                    request.getSession().invalidate();

                    return mapping.findForward("welcome.page");
                }
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
    
    /**
     * Send activation mail
     */
    private void sendActivationMail(
            HttpServletRequest request, UserData user, ActionMessages errors) {
        
        try {
            javax.naming.Context ctx = (javax.naming.Context)
                new InitialContext().lookup("java:comp/env");
            Session mailSession = (Session) ctx.lookup("mail/Session");
            if (mailSession != null) {
                ResourceBundle resources = ResourceBundle.getBundle(
                        "ApplicationResources", getLocaleInstance(user.getLocale()));
                
                String from = RollerRuntimeConfig.getProperty(
                        "user.account.activation.mail.from");
                
                String cc[] = new String[0];
                String bcc[] = new String[0];
                String to[] = new String[] { user.getEmailAddress() };
                String subject = resources.getString(
                        "user.account.activation.mail.subject");
                String content;
                
                String rootURL = RollerRuntimeConfig.getAbsoluteContextURL();
                
                if (rootURL == null || rootURL.trim().length() == 0) {
                    rootURL = RequestUtils.serverURL(request)
                    + request.getContextPath();
                }
                
                StringBuffer sb = new StringBuffer();
                
                // activationURL=
                // rootURL/roller-ui/user.do?method=activateUser&activationCode=*****
                String activationURL = rootURL
                        + "/roller-ui/user.do?method=activateUser&activationCode="
                        + user.getActivationCode();
                sb.append(MessageFormat.format(
                        resources.getString("user.account.activation.mail.content"),
                        new Object[] { user.getFullName(), user.getUserName(),
                        activationURL }));
                content = sb.toString();

                MailUtil.sendHTMLMessage(mailSession, from, to, cc, bcc, subject, content);
            }
            
        } catch (MessagingException me) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                "error.add.user.mailSendException"));      
            log.debug("ERROR sending email", me);
        } catch (NamingException ne) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                "error.add.user.mailSetupException"));
            log.error("ERROR in mail setup?", ne);
        } catch (MalformedURLException ue) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                "error.add.user.mailSetupException"));
            log.error("ERROR in absolute URL setting?", ue);
        }
    }
    
    /**
     * Process GET of new user account activation page
     * (allows to activate the user account )
     */
    public ActionForward activateUser(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        
        ActionMessages errors = new ActionMessages();
        
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            
            if (request.getParameter("activationCode") == null) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                        "error.activate.user.missingActivationCode"));
            } else {
                String activationCode = request.getParameter("activationCode").toString();                
                UserData user = mgr.getUserByActivationCode(activationCode);
                
                if (user != null) {
                    // enable user account
                    user.setEnabled(Boolean.TRUE);
                    user.setActivationCode(null);
                    mgr.saveUser(user);
                    RollerFactory.getRoller().flush();
                    
                    /**
                     * activationStatus = 1:activated, 0:has to be activated,
                     * -1:error
                     */
                    request.setAttribute("activationStatus", "1");
                    
                } else {
                    errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                            "error.activate.user.invalidActivationCode"));
                }
            }
            
        } catch (RollerException e) {
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(e.getMessage()));
            saveErrors(request, errors);
            mLogger.error("ERROR in activateUser", e);
        }
        
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            /**
             * activationStatus = 1:activated, 0:has to be activated, -1:error
             */
            request.setAttribute("activationStatus", "-1");
        }
        
        return mapping.findForward("welcome.page");
    }
    
    /**
     * Copied from WebsiteData.java by sedat
     */
    private Locale getLocaleInstance(String locale) {
        if (locale != null) {
            String[] localeStr = StringUtils.split(locale, "_");
            if (localeStr.length == 1) {
                if (localeStr[0] == null)
                    localeStr[0] = "";
                return new Locale(localeStr[0]);
            } else if (localeStr.length == 2) {
                if (localeStr[0] == null)
                    localeStr[0] = "";
                if (localeStr[1] == null)
                    localeStr[1] = "";
                return new Locale(localeStr[0], localeStr[1]);
            } else if (localeStr.length == 3) {
                if (localeStr[0] == null)
                    localeStr[0] = "";
                if (localeStr[1] == null)
                    localeStr[1] = "";
                if (localeStr[2] == null)
                    localeStr[2] = "";
                return new Locale(localeStr[0], localeStr[1], localeStr[2]);
            }
        }
        return Locale.getDefault();
    }
    
}