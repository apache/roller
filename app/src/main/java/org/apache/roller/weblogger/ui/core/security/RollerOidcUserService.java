/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.ui.core.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Bridges OIDC-authenticated users to Roller's user store. If the OIDC subject
 * matches an existing Roller user (via the openIdUrl/openid_url column), the
 * returned OidcUser carries that user's Roller authorities. Otherwise, the
 * default OIDC scopes are returned and the success handler redirects to
 * registration.
 */
public class RollerOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final Log log = LogFactory.getLog(RollerOidcUserService.class);
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        if (!WebloggerFactory.isBootstrapped()) {
            return oidcUser;
        }

        String oidcSubject = toOidcSubject(oidcUser);

        try {
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            User rollerUser = umgr.getUserByOpenIdUrl(oidcSubject);

            if (rollerUser != null && rollerUser.getEnabled()) {
                List<GrantedAuthority> authorities = new ArrayList<>();
                for (String role : umgr.getRoles(rollerUser)) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }
                return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
            }
        } catch (Exception e) {
            log.error("Error looking up Roller user for OIDC subject: " + oidcSubject, e);
        }

        return oidcUser;
    }

    /**
     * Formats the OIDC issuer and subject as {@code issuer#sub} for storage
     * in the User.openIdUrl column.
     */
    public static String toOidcSubject(OidcUser oidcUser) {
        return oidcUser.getIssuer().toString() + "#" + oidcUser.getSubject();
    }
}
