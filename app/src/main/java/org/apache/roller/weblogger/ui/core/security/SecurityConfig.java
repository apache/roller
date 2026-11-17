/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.ui.core.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Roller's Spring Security configuration, loaded by the web application
 * context that web.xml sets up.
 *
 * <p>Logins use the Roller database, or the LDAP directory configured by the
 * users.ldap.* properties when authentication.method is ldap. OpenID Connect
 * login is available when oidc.* client registrations are configured.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    static final String LOGIN_PAGE = "/roller-ui/login.rol";

    static final String LOGIN_PROCESSING_URL = "/roller_j_security_check";

    private static final String ACCESS_PREFIX = "security.access.";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return RollerPasswordEncoders.create();
    }

    // Not a bean: a UserDetailsService bean would give Spring Security a
    // second, database-backed login provider, including when LDAP is used.
    UserDetailsService userDetailsService() {
        return new RollerUserDetailsService();
    }

    @Bean
    public RollerClientRegistrationRepository clientRegistrationRepository() {
        return new RollerClientRegistrationRepository();
    }

    @Bean
    public RollerOidcUserService rollerOidcUserService() {
        return new RollerOidcUserService();
    }

    @Bean
    public WebSecurityCustomizer staticResources() {
        return web -> web.ignoring().requestMatchers("/images/**", "/scripts/**", "/styles/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PasswordEncoder passwordEncoder,
            RollerClientRegistrationRepository clientRegistrationRepository,
            RollerOidcUserService rollerOidcUserService) throws Exception {

        UserDetailsService userDetailsService = userDetailsService();

        List<AccessRule> siteRules = siteAccessRules();
        http.authorizeHttpRequests(requests -> {
            for (AccessRule rule : siteRules) {
                requests.requestMatchers(rule.matcher()).hasAnyAuthority(rule.authorities());
            }
            requests
                .requestMatchers("/roller-ui/login-redirect**").hasAnyAuthority("admin", "editor")
                .requestMatchers("/roller-ui/profile**").hasAnyAuthority("admin", "editor")
                .requestMatchers("/roller-ui/createWeblog**").hasAnyAuthority("admin", "editor")
                .requestMatchers("/roller-ui/menu**").hasAnyAuthority("admin", "editor")
                .requestMatchers("/roller-ui/authoring/**").hasAnyAuthority("admin", "editor")
                .requestMatchers("/roller-ui/admin/**").hasAuthority("admin")
                .requestMatchers("/rewrite-status*").hasAuthority("admin")
                .anyRequest().permitAll();
        });

        http.authenticationProvider(loginProvider(userDetailsService, passwordEncoder));

        http.formLogin(form -> form
                .loginPage(LOGIN_PAGE)
                .usernameParameter("j_username")
                .passwordParameter("j_password")
                .failureUrl(LOGIN_PAGE + "?error=true")
                .loginProcessingUrl(LOGIN_PROCESSING_URL));

        LoginUrlAuthenticationEntryPoint entryPoint = new LoginUrlAuthenticationEntryPoint(LOGIN_PAGE);
        entryPoint.setForceHttps(WebloggerConfig.getBooleanProperty("securelogin.enabled"));
        // a login post without a valid token (the login page was left open
        // past the session timeout, or the server restarted) goes back to the
        // login form instead of the bare 403 page
        AccessDeniedHandler accessDenied = new AccessDeniedHandlerImpl();
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler((request, response, e) -> {
                    if (e instanceof CsrfException) {
                        response.sendRedirect(request.getContextPath() + LOGIN_PAGE + "?error=expired");
                    } else {
                        accessDenied.handle(request, response, e);
                    }
                }));

        if (WebloggerConfig.getBooleanProperty("rememberme.enabled")) {
            http.rememberMe(rememberMe -> rememberMe
                    .rememberMeServices(new RollerRememberMeServices(userDetailsService)));
        }

        // OAuth2/OIDC login (active only when oidc.*.client-id properties are configured)
        SavedRequestAwareAuthenticationSuccessHandler oidcSuccess =
                new SavedRequestAwareAuthenticationSuccessHandler();
        oidcSuccess.setDefaultTargetUrl("/roller-ui/menu.rol");
        http.oauth2Login(oauth2 -> oauth2
                .clientRegistrationRepository(clientRegistrationRepository)
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(rollerOidcUserService))
                .successHandler(oidcSuccess)
                // OIDC failures get their own marker so the login page does
                // not diagnose them as a wrong password
                .failureHandler(new SimpleUrlAuthenticationFailureHandler(LOGIN_PAGE + "?error=oidc"))
                .loginPage(LOGIN_PAGE));

        // Roller has its own logout action
        http.logout(logout -> logout.disable());

        // Roller protects its own forms with the salt that LoadSaltFilter and
        // ValidateSaltFilter check on every POST under /roller-ui, so Spring's
        // CSRF token is only required for the login form, which posts outside
        // /roller-ui. The token is created only when the login page asks for
        // it, so other pages don't start a session for it.
        http.csrf(csrf -> csrf.requireCsrfProtectionMatcher(
                PathPatternRequestMatcher.pathPattern(HttpMethod.POST, LOGIN_PROCESSING_URL)));

        // some Roller UI (i.e. media file editor) uses iframes
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    /** A security.access.* rule: requests matching the path need one of the authorities. */
    record AccessRule(String pattern, String[] authorities) {

        RequestMatcher matcher() {
            PathPatternParser parser = new PathPatternParser();
            parser.setCaseSensitive(false);
            return PathPatternRequestMatcher.withPathPatternParser(parser).matcher(pattern);
        }
    }

    /**
     * Site-specific rules from roller.properties, for example to keep a weblog
     * private: security.access./fam/**=family. The key after security.access.
     * is a path pattern, matched without regard to case; the value lists the
     * authorities (role names) allowed, separated by commas. These rules are
     * checked before Roller's own, longest pattern first.
     */
    static List<AccessRule> siteAccessRules() {
        List<AccessRule> rules = new ArrayList<>();
        Enumeration<Object> keys = WebloggerConfig.keys();
        while (keys.hasMoreElements()) {
            String key = String.valueOf(keys.nextElement());
            if (!key.startsWith(ACCESS_PREFIX)) {
                continue;
            }
            String pattern = key.substring(ACCESS_PREFIX.length());
            String[] authorities = Arrays.stream(
                    StringUtils.split(StringUtils.defaultString(WebloggerConfig.getProperty(key)), ','))
                    .map(String::trim).filter(StringUtils::isNotEmpty).toArray(String[]::new);
            if (!pattern.startsWith("/") || authorities.length == 0) {
                throw new IllegalStateException(key + " needs a path starting with / and at least one "
                        + "role, for example security.access./private/**=admin");
            }
            rules.add(new AccessRule(pattern, authorities));
        }
        rules.sort(Comparator.comparingInt((AccessRule r) -> r.pattern().length()).reversed()
                .thenComparing(AccessRule::pattern));
        return rules;
    }

    private static AuthenticationProvider loginProvider(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        if (WebloggerConfig.getAuthMethod() == AuthMethod.LDAP) {
            return ldapProvider();
        }
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        // store passwords re-encoded after login (passwds.encryption.lazyUpgradeFrom)
        if (userDetailsService instanceof UserDetailsPasswordService passwordService) {
            provider.setUserDetailsPasswordService(passwordService);
        }
        return provider;
    }

    /**
     * The LDAP login provider, from the users.ldap.* properties.
     */
    static LdapAuthenticationProvider ldapProvider() {
        String url = WebloggerConfig.getProperty("users.ldap.url");
        if (StringUtils.isBlank(url)) {
            throw new IllegalStateException(
                    "authentication.method=ldap needs users.ldap.url in roller-custom.properties");
        }
        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(url);
        String managerDn = WebloggerConfig.getProperty("users.ldap.managerDn");
        if (StringUtils.isNotBlank(managerDn)) {
            contextSource.setUserDn(managerDn);
            contextSource.setPassword(WebloggerConfig.getProperty("users.ldap.managerPassword", ""));
        }
        contextSource.afterPropertiesSet();

        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
                WebloggerConfig.getProperty("users.ldap.userSearchBase", ""),
                WebloggerConfig.getProperty("users.ldap.userSearchFilter", "(uid={0})"),
                contextSource);
        userSearch.setSearchSubtree(true);

        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(userSearch);

        AuthoritiesPopulator authorities = new AuthoritiesPopulator();
        authorities.setDefaultRole(WebloggerConfig.getProperty("users.ldap.defaultRole", "editor"));

        return new LdapAuthenticationProvider(authenticator, authorities);
    }
}
