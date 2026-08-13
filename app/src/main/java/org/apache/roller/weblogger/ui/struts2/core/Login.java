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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.core.security.RollerClientRegistrationRepository;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

/**
 * Handle user logins.
 *
 * The standard blog login buttons route to login-redirect.rol, which is
 * intercepted by the Spring security.xml to first require a standard login (this class).
 * After successful authentication, login-redirect will either route the user to
 * registration (if the user logged in via an external method such as LDAP and doesn't
 * yet have a Roller account), or directly to the user's blog.
 *
 * @see org.apache.roller.weblogger.ui.struts2.core.Register
 */
// TODO: make this work @AllowedMethods({"execute"})
public class Login extends UIAction {

    private String error = null;

    private AuthMethod authMethod = WebloggerConfig.getAuthMethod();

    public Login() {
        this.pageTitle = "loginPage.title";
    }

    // override default security, we do not require an authenticated user
    @Override
    public boolean isUserRequired() {
        return false;
    }

    // override default security, we do not require an action weblog
    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    public String getAuthMethod() {
        return authMethod.name();
    }

    /**
     * Providers to offer sign-in buttons for. Only registrations the repository
     * could actually resolve are listed, so the page never advertises a provider
     * whose discovery endpoint was unreachable.
     */
    public List<Map<String, String>> getOidcProviders() {
        List<Map<String, String>> providers = new ArrayList<>();
        RollerClientRegistrationRepository repository = RollerContext.getClientRegistrationRepository();
        if (repository == null) {
            return providers;
        }
        for (ClientRegistration registration : repository.getRegistrations().values()) {
            Map<String, String> provider = new LinkedHashMap<>();
            provider.put("id", registration.getRegistrationId());
            provider.put("name", registration.getClientName());
            providers.add(provider);
        }
        return providers;
    }

    @Override
    public String execute() {

        // set action error message if there was login error; OAuth2/OIDC
        // failures redirect here with error=oidc, form login with error=true
        if (getError() != null) {
            addError("oidc".equals(getError()) ? "error.oidc.login" : "error.password.mismatch");
        }

        return SUCCESS;
    }


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
