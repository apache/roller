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

package org.apache.roller.weblogger.ui.struts2.core;

import java.util.TimeZone;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.CharSetUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.core.security.CustomUserRegistry;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.MailUtil;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Actions for registering a new user.  This page is activated in Roller in two ways,
 * by explicitly selecting the "Register" button on the Main Menu, or if
 * upon a non-Roller DB login (say, LDAP) if the user does not exist in the
 * Roller DB.  In the latter case, this page is activated from login-redirect.jsp file.
 *
 * @see org.apache.roller.weblogger.ui.struts2.core.Login
 */
public class Register extends UIAction implements ServletRequestAware {
    
    private static Log log = LogFactory.getLog(Register.class);
    private static final String DISABLED_RETURN_CODE = "disabled";
    public static final String DEFAULT_ALLOWED_CHARS = "A-Za-z0-9";

    // this is a no-no, we should not need this
    private HttpServletRequest servletRequest = null;

    private AuthMethod authMethod = WebloggerConfig.getAuthMethod();

    private String activationStatus = null;
    
    private String activationCode = null;
    private ProfileBean bean = new ProfileBean();

    public Register() {
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
    
    public String getAuthMethod() {
        return authMethod.name();
    }

    @SkipValidation
    public String execute() {
        
        // if registration is disabled, then don't allow registration
        try {
            if (!WebloggerRuntimeConfig.getBooleanProperty("users.registration.enabled")
                // unless there are 0 users (need to allow creation of first user)
                && WebloggerFactory.getWeblogger().getUserManager().getUserCount() != 0) {
                addError("Register.disabled");
                return DISABLED_RETURN_CODE;
            }
        } catch (Exception e) {
            log.error("Error checking user count", e);
            addError("generic.error.check.logs");
            return DISABLED_RETURN_CODE;
        }
                
        // For new user default to locale set in browser
        bean.setLocale(getServletRequest().getLocale().toString());
        
        // For new user default to timezone of server
        bean.setTimeZone(TimeZone.getDefault().getID());
        
        /* TODO: when Spring Security 2.1 is release comment out this stuff, 
         * which pre-populates the user bean with info from OpenID provider.
         *
        Collection attrsCollect = (Collection)WebloggerFactory.getWeblogger()
                .getUserManager().userAttributes.get(UserAttribute.Attributes.openidUrl.toString());
        
        if (attrsCollect != null) {
            ArrayList attrs = new ArrayList(attrsCollect);
            for (OpenIDUserAttribute attr : attrs) {
                if (attr.getName().equals(OpenIDUserAttribute.Attributes.country.toString())) {
                    getBean().setLocale(UIUtils.getLocale(attr.getValue()));
                }                
               if (attr.getName().equals(OpenIDUserAttribute.Attributes.email.toString())) {
                    getBean().setEmailAddress(attr.getValue());
                }
                if (attr.getName().equals(OpenIDUserAttribute.Attributes.fullname.toString())) {
                    getBean().setFullName(attr.getValue());
                }
                if (attr.getName().equals(OpenIDUserAttribute.Attributes.nickname.toString())) {
                    getBean().setUserName(attr.getValue());
                }
                if (attr.getName().equals(OpenIDUserAttribute.Attributes.timezone.toString())) {
                    getBean().setTimeZone(UIUtils.getTimeZone(attr.getValue()));
                }
                if (attr.getName().equals(OpenIDUserAttribute.Attributes.openidname.toString())) {
                    getBean().setOpenidUrl(attr.getValue());
                }
                
            }
        }*/
            
        try {

            if (WebloggerConfig.getAuthMethod() == AuthMethod.LDAP) {
                // See if user is already logged in via Spring Security
                User fromSSOUser = CustomUserRegistry.getUserDetailsFromAuthentication(getServletRequest());
                if (fromSSOUser != null) {
                    // Copy user details from Spring Security, including LDAP attributes
                    getBean().copyFrom(fromSSOUser);
                }
            } else if (WebloggerConfig.getAuthMethod() == AuthMethod.CMA) {
                // See if user is already logged in via CMA
                if (getServletRequest().getUserPrincipal() != null) {
                    // Only detail we get is username, sadly no LDAP attributes
                    getBean().setUserName(getServletRequest().getUserPrincipal().getName());
                    getBean().setScreenName(getServletRequest().getUserPrincipal().getName());
                }
            }
            
        } catch (Exception ex) {
            log.error("Error reading SSO user data", ex);
            addError("error.editing.user", ex.toString());
        }
        
        return INPUT;
    }
    
    
    public String save() {
        
        // if registration is disabled, then don't allow registration
        try {
            if (!WebloggerRuntimeConfig.getBooleanProperty("users.registration.enabled")
                // unless there are 0 users (need to allow creation of first user)
                && WebloggerFactory.getWeblogger().getUserManager().getUserCount() != 0) {
                return DISABLED_RETURN_CODE;
            }
        } catch (Exception e) {
            log.error("Error checking user count", e);
            return DISABLED_RETURN_CODE;
        }
                
        myValidate();
        
        if (!hasActionErrors()) {
            try {

                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();

                // copy form data into new user pojo
                User ud = new User();
                // copyTo skips password
                getBean().copyTo(ud);
                ud.setUserName(getBean().getUserName());
                ud.setDateCreated(new java.util.Date());
                ud.setEnabled(Boolean.TRUE);

                // If user set both password and passwordConfirm then reset password
                if (!StringUtils.isEmpty(getBean().getPasswordText()) &&
                        !StringUtils.isEmpty(getBean().getPasswordConfirm())) {
                    ud.resetPassword(getBean().getPasswordText());
                }

                // are we using email activation?
                boolean activationEnabled = WebloggerRuntimeConfig.getBooleanProperty(
                        "user.account.email.activation");
                if (activationEnabled) {
                    // User account will be enabled after the activation process
                    ud.setEnabled(Boolean.FALSE);

                    // Create & save the activation data
                    String inActivationCode = UUID.randomUUID().toString();

                    if (mgr.getUserByActivationCode(inActivationCode) != null) {
                        // In the *extremely* unlikely event that we generate an
                        // activation code that is already use, we'll retry 3 times.
                        int numOfRetries = 3;
                        if (numOfRetries < 1) {
                            numOfRetries = 1;
                        }
                        for (int i = 0; i < numOfRetries; i++) {
                            inActivationCode = UUID.randomUUID().toString();
                            if (mgr.getUserByActivationCode(inActivationCode) == null) {
                                break;
                            } else {
                                inActivationCode = null;
                            }
                        }
                        // In more unlikely event that three retries isn't enough
                        if (inActivationCode == null){
                            throw new WebloggerException("error.add.user.activationCodeInUse");
                        }
                    }
                    ud.setActivationCode(inActivationCode);
                }

                String openidurl = getBean().getOpenIdUrl();
                if (openidurl != null) {
                    if (openidurl.endsWith("/")) {
                        openidurl = openidurl.substring(0, openidurl.length() - 1);
                    }
                    ud.setOpenIdUrl(openidurl);
                }

                // save new user
                mgr.addUser(ud);

                WebloggerFactory.getWeblogger().flush();

                // now send activation email if necessary
                if (activationEnabled && ud.getActivationCode() != null) {
                    try {
                        // send activation mail to the user
                        MailUtil.sendUserActivationEmail(ud);
                    } catch (WebloggerException ex) {
                        log.error("Error sending activation email to - " + ud.getEmailAddress(), ex);
                    }

                    setActivationStatus("pending");
                }

                // Invalidate session, otherwise new user who was originally
                // authenticated via LDAP/SSO will remain logged in but
                // without a valid Roller role.
                getServletRequest().getSession().removeAttribute(RollerSession.ROLLER_SESSION);
                getServletRequest().getSession().invalidate();

                // set a special page title
                setPageTitle("welcome.title");

                return SUCCESS;

            } catch (WebloggerException ex) {
                log.error("Error adding new user", ex);
                addError("generic.error.check.logs");
            }
        }
        
        return INPUT;
    }
    
    
    @SkipValidation
    public String activate() {
        
        try {
            UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
            
            if (getActivationCode() == null) {
                addError("error.activate.user.missingActivationCode");
            } else {
                User user = mgr.getUserByActivationCode(getActivationCode());
                
                if (user != null) {
                    // enable user account
                    user.setEnabled(Boolean.TRUE);
                    user.setActivationCode(null);
                    mgr.saveUser(user);
                    WebloggerFactory.getWeblogger().flush();
                    
                    setActivationStatus("active");
                    
                } else {
                    addError("error.activate.user.invalidActivationCode");
                }
            }
            
        } catch (WebloggerException e) {
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
    
    
    public void myValidate() {
        
        // if using external auth, we don't want to error on empty password/username from HTML form.
        boolean usingSSO = authMethod == AuthMethod.LDAP || authMethod == AuthMethod.CMA;
        if (usingSSO) {
            // store an unused marker in the Roller DB for the passphrase in
            // the LDAP or CMA cases, as actual passwords are stored externally
            String unusedPassword = WebloggerConfig.getProperty("users.passwords.externalAuthValue", "<externalAuth>");
            
            // Preserve username and password, Spring Security case
            User fromSSOUser = CustomUserRegistry.getUserDetailsFromAuthentication(getServletRequest());
            if (fromSSOUser != null) {
                getBean().setPasswordText(unusedPassword);
                getBean().setPasswordConfirm(unusedPassword);
                getBean().setUserName(fromSSOUser.getUserName());
            }

            // Preserve username and password, CMA case             
            else if (getServletRequest().getUserPrincipal() != null) {
                getBean().setUserName(getServletRequest().getUserPrincipal().getName());
                getBean().setPasswordText(unusedPassword);
                getBean().setPasswordConfirm(unusedPassword);
            }
        }
        
        String allowed = WebloggerConfig.getProperty("username.allowedChars");
        if (allowed == null || allowed.trim().length() == 0) {
            allowed = DEFAULT_ALLOWED_CHARS;
        }
        
        // check that username only contains safe characters
        String safe = CharSetUtils.keep(getBean().getUserName(), allowed);
        if (!safe.equals(getBean().getUserName()) ) {
            addError("error.add.user.badUserName");
        }
        
        // check password, it is required if OpenID and SSO are disabled
        if (AuthMethod.ROLLERDB.name().equals(getAuthMethod())
                && StringUtils.isEmpty(getBean().getPasswordText())) {
                addError("error.add.user.passwordEmpty");
                return;
        }
        
        // User.password does not allow null, so generate one
        if (getAuthMethod().equals(AuthMethod.OPENID.name()) ||
                (getAuthMethod().equals(AuthMethod.DB_OPENID.name()) && !StringUtils.isEmpty(getBean().getOpenIdUrl()))) {
            String randomString = RandomStringUtils.randomAlphanumeric(255);
            getBean().setPasswordText(randomString);
            getBean().setPasswordConfirm(randomString);
        }
        
        // check that passwords match 
        if (!getBean().getPasswordText().equals(getBean().getPasswordConfirm())) {
            addError("userRegister.error.mismatchedPasswords");
        }
        
        // check that username is not taken
        if (!StringUtils.isEmpty(getBean().getUserName())) {
            try {
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                if (mgr.getUserByUserName(getBean().getUserName(), null) != null) {
                    addError("error.add.user.userNameInUse");
                    // reset user name
                    getBean().setUserName(null);
                }
            } catch (WebloggerException ex) {
                log.error("error checking for user", ex);
                addError("generic.error.check.logs");
            }
        }
    }
    
    
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
    
    public ProfileBean getBean() {
        return bean;
    }

    public void setBean(ProfileBean bean) {
        this.bean = bean;
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
