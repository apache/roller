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

import org.apache.roller.weblogger.business.WebloggerStaticConfig.AuthMethod;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Handle user logins.
 *
 * The standard blog login buttons route to login-redirect.rol, which is
 * intercepted by the Spring security.xml to first require a standard login (this class).
 * After successful authentication, login-redirect will either route the user to
 * registration (if the user logged in via an external method such as LDAP and doesn't
 * yet have a Roller account), or directly to the user's blog.
 */
public class Login extends UIAction {

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    private String error = null;

    private AuthMethod authMethod = WebloggerStaticConfig.getAuthMethod();

    public Login() {
        this.pageTitle = "loginPage.title";
    }

    private String activationCode = null;

    private String activationStatus = null;

    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
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

    public String execute() {
        // set action error message if there was login error
        if(getError() != null) {
            addError("error.password.mismatch");
        }

        return SUCCESS;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(String activationStatus) {
        this.activationStatus = activationStatus;
    }

    public String activate() {
        if (getActivationCode() == null) {
            addError("error.activate.user.missingActivationCode");
        } else {
            User user = userManager.getUserByActivationCode(getActivationCode());

            if (user != null) {
                // enable user account
                user.setActivationCode(null);
                if (user.isApproved()) {
                    user.setEnabled(Boolean.TRUE);
                    setActivationStatus("active");
                } else {
                    setActivationStatus("activePending");
                    mailManager.sendRegistrationApprovalRequest(user);
                }
                userManager.saveUser(user);
            } else {
                addError("error.activate.user.invalidActivationCode");
            }
        }

        if (hasActionErrors()) {
            setActivationStatus("error");
        }

        return "successActivate";
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
}
