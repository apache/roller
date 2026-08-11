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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Bridges OIDC-authenticated users to Roller's user store.
 *
 * <p>The OIDC subject (formatted as {@code issuer#sub}) is matched against the
 * User.openIdUrl column. Users who authenticate for the first time are
 * provisioned just-in-time from their OIDC claims. Either way the returned
 * OidcUser carries the Roller roles as authorities, so authorization works on
 * the very first request after login.
 */
public class RollerOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final Log log = LogFactory.getLog(RollerOidcUserService.class);
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return resolveUser(delegate.loadUser(userRequest));
    }

    /**
     * Resolves the Roller account behind an authenticated OIDC user and returns
     * a principal carrying that account's Roller roles as authorities.
     */
    OidcUser resolveUser(OidcUser oidcUser) throws OAuth2AuthenticationException {
        if (!WebloggerFactory.isBootstrapped()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("roller_not_bootstrapped"),
                    "Roller is not bootstrapped; cannot resolve OIDC user");
        }

        String oidcSubject = toOidcSubject(oidcUser);

        try {
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            User rollerUser = umgr.getUserByOpenIdUrl(oidcSubject);

            if (rollerUser == null) {
                rollerUser = linkExistingUser(umgr, oidcUser, oidcSubject);
            }
            if (rollerUser == null) {
                rollerUser = provisionUser(umgr, oidcUser, oidcSubject);
            }
            if (!Boolean.TRUE.equals(rollerUser.getEnabled())) {
                throw new OAuth2AuthenticationException(new OAuth2Error("user_disabled"),
                        "Roller user is disabled: " + rollerUser.getUserName());
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            for (String role : umgr.getRoles(rollerUser)) {
                authorities.add(new SimpleGrantedAuthority(role));
            }
            return new RollerOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(),
                    rollerUser.getUserName());

        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error resolving Roller user for OIDC subject: " + oidcSubject, e);
            throw new OAuth2AuthenticationException(new OAuth2Error("user_resolution_failed"),
                    "Could not resolve Roller user for OIDC subject: " + oidcSubject, e);
        }
    }

    /**
     * Adopts a pre-existing Roller account whose username matches the one
     * asserted by the provider, which is how database users carry over when a
     * site turns on OIDC. Linking requires the provider to have verified an
     * email address matching the account, so that control of an unverified
     * address at the provider cannot be used to take over a Roller account.
     *
     * @return the linked user, or null if there is no account to adopt
     */
    private User linkExistingUser(UserManager umgr, OidcUser oidcUser, String oidcSubject) throws Exception {
        String username = usernameOf(oidcUser);
        User existing = umgr.getUserByUserName(username);
        if (existing == null) {
            return null;
        }

        String email = oidcUser.getEmail();
        boolean emailVerified = Boolean.TRUE.equals(oidcUser.getEmailVerified())
                && email != null && email.equalsIgnoreCase(existing.getEmailAddress());

        if (!emailVerified) {
            throw new OAuth2AuthenticationException(new OAuth2Error("account_link_required"),
                    "A Roller account named '" + username + "' already exists but is not linked to "
                            + oidcSubject + ". An administrator must set its federated identity, or the"
                            + " provider must assert a verified email address matching the account.");
        }

        existing.setOpenIdUrl(oidcSubject);
        umgr.saveUser(existing);
        WebloggerFactory.getWeblogger().flush();
        log.info("Linked existing Roller user '" + username + "' to OIDC subject " + oidcSubject);
        return existing;
    }

    /**
     * Creates a Roller account from the OIDC claims. The account is linked to
     * the identity provider by subject, and gets a random password since it is
     * never used for authentication.
     */
    private User provisionUser(UserManager umgr, OidcUser oidcUser, String oidcSubject) throws Exception {
        String username = usernameOf(oidcUser);

        // the identity provider decides who may sign in, so provisioning is a
        // static config choice like users.ldap.autoProvision.enabled, not tied
        // to the runtime form-registration toggle
        if (!WebloggerConfig.getBooleanProperty("users.oidc.autoProvision.enabled")) {
            throw new OAuth2AuthenticationException(new OAuth2Error("auto_provision_disabled"),
                    "OIDC auto-provisioning is disabled; no Roller account exists for " + oidcSubject);
        }

        User user = new User();
        user.setId(UUIDGenerator.generateUUID());
        user.setUserName(username);

        String fullName = oidcUser.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = username;
        }
        user.setFullName(fullName);
        user.setScreenName(username);

        String email = oidcUser.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("missing_email"),
                    "OIDC provider did not supply an email address for " + username);
        }
        user.setEmailAddress(email);

        user.setOpenIdUrl(oidcSubject);
        user.setPassword(UUIDGenerator.generateUUID());
        user.setDateCreated(new Timestamp(System.currentTimeMillis()));
        user.setLocale(Locale.getDefault().toString());
        user.setTimeZone(TimeZone.getDefault().getID());
        user.setEnabled(Boolean.TRUE);

        // grants the "editor" role, and "admin" if this is the first user
        umgr.addUser(user);

        // flush before granting so the roles addUser() just created are visible
        // to grantRole()'s duplicate check, which queries the database
        WebloggerFactory.getWeblogger().flush();

        Collection<String> claimRoles = extractRoles(oidcUser);
        if (claimRoles.contains("admin")) {
            umgr.grantRole("admin", user);
            WebloggerFactory.getWeblogger().flush();
        }
        log.info("Auto-provisioned OIDC user '" + username + "' from claims (roles: "
                + claimRoles + ", subject: " + oidcSubject + ")");
        return user;
    }

    private String usernameOf(OidcUser oidcUser) {
        String username = oidcUser.getPreferredUsername();
        return (username == null || username.isBlank()) ? oidcUser.getSubject() : username;
    }

    /**
     * Reads role names from the token. Providers differ in where they put them,
     * so both a flat "roles" claim and Keycloak's nested "realm_access.roles"
     * are supported.
     */
    @SuppressWarnings("unchecked")
    private Collection<String> extractRoles(OidcUser oidcUser) {
        Object roles = oidcUser.getClaim("roles");
        if (roles instanceof Collection) {
            return (Collection<String>) roles;
        }
        Object realmAccess = oidcUser.getClaim("realm_access");
        if (realmAccess instanceof Map) {
            Object realmRoles = ((Map<String, Object>) realmAccess).get("roles");
            if (realmRoles instanceof Collection) {
                return (Collection<String>) realmRoles;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Formats the OIDC issuer and subject as {@code issuer#sub} for storage
     * in the User.openIdUrl column.
     */
    public static String toOidcSubject(OidcUser oidcUser) {
        return oidcUser.getIssuer().toString() + "#" + oidcUser.getSubject();
    }

    /**
     * An OidcUser named after its Roller account. Roller looks users up by
     * principal name throughout (ParsedRequest, RoleAssignmentFilter, Struts
     * actions), and an OidcUser's default name is the "sub" claim: an opaque
     * provider ID that matches no Roller account.
     */
    private static class RollerOidcUser extends DefaultOidcUser {

        private final String rollerUserName;

        RollerOidcUser(Collection<? extends GrantedAuthority> authorities,
                OidcIdToken idToken, OidcUserInfo userInfo, String rollerUserName) {
            super(authorities, idToken, userInfo);
            this.rollerUserName = rollerUserName;
        }

        @Override
        public String getName() {
            return rollerUserName;
        }
    }
}
