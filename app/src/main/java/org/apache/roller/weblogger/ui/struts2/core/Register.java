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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.struts2.core;

import java.util.UUID;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import org.apache.commons.lang3.CharSetUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerCommon.AuthMethod;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.core.security.LDAPRegistrationHelper;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Actions for registering a new user.  This page is activated in Roller in two ways,
 * by explicitly selecting the "Register" button on the Main Menu, or if
 * upon a non-Roller DB login (say, LDAP) if the user does not exist in the
 * Roller DB.  In the latter case, this page is activated from login-redirect.jsp file.
 *
 * @see org.apache.roller.weblogger.ui.struts2.core.Login
 */
public class Register extends UIAction implements ServletRequestAware {

    private static Logger log = LoggerFactory.getLogger(Register.class);

    private static final String DISABLED_RETURN_CODE = "disabled";
    public static final String DEFAULT_ALLOWED_CHARS = "A-Za-z0-9";

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    private LDAPRegistrationHelper ldapRegistrationHelper;

    public void setLdapRegistrationHelper(LDAPRegistrationHelper ldapRegistrationHelper) {
        this.ldapRegistrationHelper = ldapRegistrationHelper;
    }

    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    // this is a no-no, we should not need this
    private HttpServletRequest servletRequest = null;

    private AuthMethod authMethod = WebloggerStaticConfig.getAuthMethod();

    private String activationStatus = null;
    
    private String activationCode = null;
    private User bean = new User();

    public Register() {
        this.pageTitle = "newUser.addNewUser";
    }
    
    @Override
    public WeblogRole getRequiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }
    
    public String getAuthMethod() {
        return authMethod.name();
    }

    @Override
    public GlobalRole getRequiredGlobalRole() {
        return GlobalRole.NOAUTHNEEDED;
    }

    @SkipValidation
    public String execute() {
        
        // if registration is disabled, then don't allow registration
        try {
            if (!propertiesManager.getBooleanProperty("users.registration.enabled")
                // unless there are 0 users (need to allow creation of first user)
                && userManager.getUserCount() != 0) {
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
        
        try {
            if (WebloggerStaticConfig.getAuthMethod() == AuthMethod.LDAP) {
                // See if user is already logged in via Spring Security
                User fromSSOUser = ldapRegistrationHelper.getUserDetailsFromAuthentication(getServletRequest());
                if (fromSSOUser != null) {
                    // Copy user details from Spring Security, including LDAP attributes
                    bean.setId(fromSSOUser.getId());
                    bean.setUserName(fromSSOUser.getUserName());
                    bean.setScreenName(fromSSOUser.getScreenName());
                    bean.setEmailAddress(fromSSOUser.getEmailAddress());
                    bean.setLocale(fromSSOUser.getLocale());
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
            if (!propertiesManager.getBooleanProperty("users.registration.enabled")
                // unless there are 0 users (need to allow creation of first user)
                && userManager.getUserCount() != 0) {
                return DISABLED_RETURN_CODE;
            }
        } catch (Exception e) {
            log.error("Error checking user count", e);
            return DISABLED_RETURN_CODE;
        }

        myValidate();

        if (!hasActionErrors()) {
            // copy form data into new user pojo
            User ud = new User();
            ud.setId(WebloggerCommon.generateUUID());
            ud.setUserName(bean.getUserName().trim());
            ud.setScreenName(bean.getScreenName().trim());
            ud.setEmailAddress(bean.getEmailAddress().trim());
            ud.setLocale(bean.getLocale());
            ud.setDateCreated(new java.util.Date());
            ud.setEnabled(Boolean.TRUE);
            ud.setGlobalRole(GlobalRole.BLOGGER);

            // If user set both password and passwordConfirm then reset password
            if (!StringUtils.isEmpty(bean.getPasswordText()) &&
                    !StringUtils.isEmpty(bean.getPasswordConfirm())) {
                ud.resetPassword(bean.getPasswordText().trim());
                bean.setPasswordText(null);
                bean.setPasswordConfirm(null);
            }

            // are we using email activation?
            boolean activationEnabled = propertiesManager.getBooleanProperty(
                    "user.account.email.activation");
            if (activationEnabled) {
                // User account will be enabled after the activation process
                ud.setEnabled(Boolean.FALSE);

                // Create & save the activation data
                String inActivationCode = UUID.randomUUID().toString();

                if (userManager.getUserByActivationCode(inActivationCode) != null) {
                    // In the *extremely* unlikely event that we generate an
                    // activation code that is already used, we'll retry 3 times.
                    int numOfRetries = 3;

                    for (int i = 0; i < numOfRetries; i++) {
                        inActivationCode = UUID.randomUUID().toString();
                        if (userManager.getUserByActivationCode(inActivationCode) == null) {
                            break;
                        } else {
                            inActivationCode = null;
                        }
                    }
                    // In more unlikely event that three retries isn't enough
                    if (inActivationCode == null){
                        throw new IllegalStateException("error.add.user.activationCodeInUse");
                    }
                }
                ud.setActivationCode(inActivationCode);
            }

            // save new user
            userManager.addUser(ud);

            WebloggerFactory.flush();

            // now send activation email if necessary
            if (activationEnabled && ud.getActivationCode() != null) {
                try {
                    // send activation mail to the user
                    mailManager.sendUserActivationEmail(ud);
                } catch (MessagingException ex) {
                    log.error("Error sending activation email to  {}", ud.getEmailAddress(), ex);
                }

                setActivationStatus("pending");
            }

            // Invalidate session, otherwise new user who was originally
            // authenticated via LDAP/SSO will remain logged in but
            // without a valid Roller role.
            getServletRequest().getSession().invalidate();

            // set a special page title
            setPageTitle("welcome.title");

            return SUCCESS;
        }
        
        return INPUT;
    }
    
    
    @SkipValidation
    public String activate() {
        
        if (getActivationCode() == null) {
            addError("error.activate.user.missingActivationCode");
        } else {
            User user = userManager.getUserByActivationCode(getActivationCode());

            if (user != null) {
                // enable user account
                user.setEnabled(Boolean.TRUE);
                user.setActivationCode(null);
                userManager.saveUser(user);
                WebloggerFactory.flush();

                setActivationStatus("active");

            } else {
                addError("error.activate.user.invalidActivationCode");
            }
        }

        if (hasActionErrors()) {
            setActivationStatus("error");
        }
        
        // set a special page title
        setPageTitle("welcome.title");
            
        return SUCCESS;
    }


    @Validations(
            emails = { @EmailValidator(fieldName="bean.emailAddress", key="Register.error.emailAddressBad")}
    )
    public void myValidate() {
        // if using external auth, we don't want to error on empty password/username from HTML form.
        if (authMethod == AuthMethod.LDAP) {
            // Obtain username and generate password
            User fromSSOUser = ldapRegistrationHelper.getUserDetailsFromAuthentication(getServletRequest());
            if (fromSSOUser != null) {
                // store a random string in the Roller DB for the passphrase in
                // the LDAP case, as actual passwords are stored externally
                // string will be encrypted and unused in DB unless auth switched to DB.
                String unusedPassword = RandomStringUtils.randomAscii(15);
                bean.setPasswordText(unusedPassword);
                bean.setPasswordConfirm(unusedPassword);
                bean.setUserName(fromSSOUser.getUserName());
            }
        }

        // Blocking "anonymousUser" as Spring Security uses it for an unauthenticated user
        if ("anonymousUser".equalsIgnoreCase(bean.getUserName())) {
            addError("error.add.user.badUserName");
        }

        // check that username only contains safe characters
        String allowed = WebloggerStaticConfig.getProperty("username.allowedChars");
        if (allowed == null || allowed.trim().length() == 0) {
            allowed = DEFAULT_ALLOWED_CHARS;
        }
        String safe = CharSetUtils.keep(bean.getUserName(), allowed);
        if (!safe.equals(bean.getUserName()) ) {
            addError("error.add.user.badUserName");
        }

        if (StringUtils.isEmpty(bean.getEmailAddress())) {
            addError("Register.error.emailAddressNull");
        }

        if (AuthMethod.DATABASE.name().equals(getAuthMethod())) {
            if (StringUtils.isEmpty(bean.getPasswordText())) {
                addError("error.add.user.passwordEmpty");
                return;
            }

            // check that passwords match
            if (!bean.getPasswordText().equals(bean.getPasswordConfirm())) {
                addError("userRegister.error.mismatchedPasswords");
            }
        }

        // check that username is not taken
        if (!StringUtils.isEmpty(bean.getUserName())) {
            if (userManager.getUserByUserName(bean.getUserName(), null) != null) {
                addError("error.add.user.userNameInUse");
                bean.setUserName(null);
            }
        }

        // check that screen name is not taken
        if (!StringUtils.isEmpty(bean.getScreenName())) {
            if (userManager.getUserByScreenName(bean.getScreenName()) != null) {
                addError("error.add.user.screenNameInUse");
                bean.setScreenName(null);
            }
        }
    }
    
    
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
    
    public User getBean() {
        return bean;
    }

    public void setBean(User bean) {
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
