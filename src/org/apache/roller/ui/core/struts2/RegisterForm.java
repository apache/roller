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

package org.apache.roller.ui.core.struts2;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.UserData;
import org.apache.roller.ui.core.security.CustomUserRegistry;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.MailUtil;
import org.apache.struts2.interceptor.ServletRequestAware;


/**
 * Actions for registering a new user.
 */
public class RegisterForm extends UIAction implements ServletRequestAware {
    
    private static Log log = LogFactory.getLog(RegisterForm.class);
    
    protected static String DEFAULT_ALLOWED_CHARS = "A-Za-z0-9";
    
    // this is a no-no, we should not need this
    private HttpServletRequest servletRequest = null;
    
    private boolean fromSS0 = false;
    private String activationStatus = null;
    
    private String activationCode = null;
    private RegisterFormBean bean = new RegisterFormBean();
    
    
    public RegisterForm() {
        this.pageTitle = "newUser.addNewUser";
    }
    
    
    // override default security, we do not require an authenticated user
    public boolean isUserRequired() {
        return false;
    }
    
    // override default security, we do not require an action weblog
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    public String execute() {
        
        try {
            getBean().setPasswordText(null);
            getBean().setPasswordConfirm(null);
            getBean().setLocale(Locale.getDefault().toString());
            getBean().setTimeZone(TimeZone.getDefault().getID());
            
            // Let's see if there's any user-authentication available from Acegi
            // and retrieve custom user data to pre-populate form.
            boolean usingSSO = RollerConfig.getBooleanProperty("users.sso.enabled");
            if(usingSSO) {
                UserData fromSSO = CustomUserRegistry.getUserDetailsFromAuthentication();
                if(fromSSO != null) {
                    getBean().copyFrom(fromSSO, getLocale());
                    setFromSS0(true);
                }
            }
            
        } catch (Exception e) {
            addError("error.editing.user", e.toString());
            log.error("ERROR in newUser", e);
        }
        
        return INPUT;
    }
    
    
    public String cancel() {
        return "cancel";
    }
    
    
    public String save() {
        
        boolean reg_allowed =
                RollerRuntimeConfig.getBooleanProperty("users.registration.enabled");
        
        if ( !reg_allowed ) {
            return "disabled";
        }
        
        // run some validation
        myValidate();
        
        if (!hasActionErrors()) try {
            
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            
            // copy form data into new user pojo
            UserData ud = new UserData();
            getBean().copyTo(ud, getLocale()); // doesn't copy password
            ud.setId(null);
            ud.setDateCreated(new java.util.Date());
            ud.setEnabled(Boolean.TRUE);
            
            // If user set both password and passwordConfirm then reset password
            if (!StringUtils.isEmpty(getBean().getPasswordText()) && 
                    !StringUtils.isEmpty(getBean().getPasswordConfirm())) {
                ud.resetPassword(RollerFactory.getRoller(),
                        getBean().getPasswordText(), getBean().getPasswordConfirm());
            }
            
            // are we using email activation?
            boolean activationEnabled = RollerRuntimeConfig.getBooleanProperty(
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
            
            // save new user
            mgr.addUser(ud);
            RollerFactory.getRoller().flush();
            
            // now send activation email if necessary
            if (activationEnabled && ud.getActivationCode() != null) {
                // send activation mail to the user
                sendActivationMail(ud);
                
                setActivationStatus("pending");
            }
             
            // Invalidate session, otherwise new user who was originally
            // authenticated via LDAP/SSO will remain logged in with
            // a but without a valid Roller role.
            getServletRequest().getSession().invalidate();
            
            // set a special page title
            setPageTitle("welcome.title");
            
            return SUCCESS;
            
        } catch (RollerException e) {
            addError(e.getMessage());
            log.error("ERROR in addUser", e);
        }
        
        return INPUT;
    }
    
    
    public String activate() {
        
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            
            if (getActivationCode() == null) {
                addError("error.activate.user.missingActivationCode");
            } else {
                UserData user = mgr.getUserByActivationCode(getActivationCode());
                
                if (user != null) {
                    // enable user account
                    user.setEnabled(Boolean.TRUE);
                    user.setActivationCode(null);
                    mgr.saveUser(user);
                    RollerFactory.getRoller().flush();
                    
                    setActivationStatus("active");
                    
                } else {
                    addError("error.activate.user.invalidActivationCode");
                }
            }
            
        } catch (RollerException e) {
            addError(e.getMessage());
            log.error("ERROR in activateUser", e);
        }
        
        if (hasActionErrors()) {
            setActivationStatus("error");
        }
        
        // set a special page title
        setPageTitle("welcome.title");
            
        return SUCCESS;
    }
    
    
    // TODO: replace with struts2 validation
    private void myValidate() {
        
        // if usingSSO, we don't want to error on empty password/username from HTML form.
        setFromSS0(false);
        boolean usingSSO = RollerConfig.getBooleanProperty("users.sso.enabled");
        if(usingSSO) {
            boolean storePassword = RollerConfig.getBooleanProperty("users.sso.passwords.saveInRollerDb");
            UserData fromSSO = CustomUserRegistry.getUserDetailsFromAuthentication();
            if(fromSSO != null) {
                String password = RollerConfig.getProperty("users.sso.passwords.defaultValue", "<unknown>");
                if(storePassword) {
                    password = fromSSO.getPassword();
                }
                getBean().setPasswordText(password);
                getBean().setPasswordConfirm(password);
                getBean().setUserName(fromSSO.getUserName());
                setFromSS0(true);
            }
        }
        
        String allowed = RollerConfig.getProperty("username.allowedChars");
        if(allowed == null || allowed.trim().length() == 0) {
            allowed = DEFAULT_ALLOWED_CHARS;
        }
        String safe = CharSetUtils.keep(getBean().getUserName(), allowed);
        
        if (StringUtils.isEmpty(getBean().getUserName())) {
            addError("error.add.user.missingUserName");
        } else if (!safe.equals(getBean().getUserName()) ) {
            addError("error.add.user.badUserName");
        }
        
        if (StringUtils.isEmpty(getBean().getEmailAddress())) {
            addError("error.add.user.missingEmailAddress");
        }
        
        if (StringUtils.isEmpty(getBean().getPasswordText()) && 
                StringUtils.isEmpty(getBean().getPasswordConfirm())) {
            addError("error.add.user.missingPassword");
        }
    }
    
    
    /**
     * Send activation mail
     */
    private void sendActivationMail(UserData user) {
        
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
                
                StringBuffer sb = new StringBuffer();
                
                // activationURL=
                String activationURL = rootURL
                        + "/roller-ui/register!activate.rol?activationCode="
                        + user.getActivationCode();
                sb.append(MessageFormat.format(
                        resources.getString("user.account.activation.mail.content"),
                        new Object[] { user.getFullName(), user.getUserName(),
                        activationURL }));
                content = sb.toString();
                
                MailUtil.sendHTMLMessage(mailSession, from, to, cc, bcc, subject, content);
            }
            
        } catch (MessagingException me) {
            addError("error.add.user.mailSendException");
            log.debug("ERROR sending email", me);
        } catch (NamingException ne) {
            addError("error.add.user.mailSetupException");
            log.error("ERROR in mail setup?", ne);
        }
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
    
    
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
    
    public RegisterFormBean getBean() {
        return bean;
    }

    public void setBean(RegisterFormBean bean) {
        this.bean = bean;
    }

    public boolean isFromSS0() {
        return fromSS0;
    }

    public void setFromSS0(boolean fromSS0) {
        this.fromSS0 = fromSS0;
    }

    public String getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(String activationStatus) {
        this.activationStatus = activationStatus;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
    
}
