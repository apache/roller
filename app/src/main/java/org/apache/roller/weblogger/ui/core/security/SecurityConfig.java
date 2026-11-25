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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
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
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PatternParseException;

import static org.springframework.security.authorization.AuthorizationManagers.allOf;

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

    private static final Log log = LogFactory.getLog(SecurityConfig.class);

    static final String LOGIN_PAGE = "/roller-ui/login.rol";

    static final String LOGIN_PROCESSING_URL = "/roller_j_security_check";

    private static final String ACCESS_PREFIX = "security.access.";

    private static final String LDAP_ENV_PREFIX = "users.ldap.env.";

    private static final AuthorizationManager<RequestAuthorizationContext> ADMIN_OR_EDITOR =
            AuthorityAuthorizationManager.hasAnyAuthority("admin", "editor");

    private static final AuthorizationManager<RequestAuthorizationContext> ADMIN =
            AuthorityAuthorizationManager.hasAuthority("admin");

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
        // Roller's static files, served without security headers, so they can
        // be cached. Each path is under a reserved name
        // (rendering.weblogMapper.rollerProtectedUrls), so no weblog can sit
        // under a path that skips the security.access rules.
        return web -> web.ignoring().requestMatchers("/images/**", "/theme/**", "/webjars/**",
                "/roller-ui/styles/**", "/roller-ui/scripts/**", "/roller-ui/images/**", "/roller-ui/theme/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PasswordEncoder passwordEncoder,
            RollerClientRegistrationRepository clientRegistrationRepository,
            RollerOidcUserService rollerOidcUserService, ServletContext servletContext) throws Exception {

        refuseOldSecurityXml(servletContext);
        UserDetailsService userDetailsService = userDetailsService();

        // security.access.* rules add to Roller's own rules: a request needs
        // to pass both. The login endpoints and the files the login page loads
        // are left out of them, so a rule for /** can't lock everyone out.
        AuthorizationManager<RequestAuthorizationContext> site =
                new SiteAccess(siteAccessRules(), SETUP_PATHS, this::setupInProgress);
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(LOGIN_PAGE, LOGIN_PROCESSING_URL, "/roller-ui/logout.rol",
                        "/oauth2/**", "/login/oauth2/**", "/favicon.*").permitAll()
                .requestMatchers("/roller-ui/login-redirect**").access(allOf(site, ADMIN_OR_EDITOR))
                .requestMatchers("/roller-ui/profile**").access(allOf(site, ADMIN_OR_EDITOR))
                .requestMatchers("/roller-ui/createWeblog**").access(allOf(site, ADMIN_OR_EDITOR))
                .requestMatchers("/roller-ui/menu**").access(allOf(site, ADMIN_OR_EDITOR))
                .requestMatchers("/roller-ui/authoring/**").access(allOf(site, ADMIN_OR_EDITOR))
                .requestMatchers("/roller-ui/admin/**").access(allOf(site, ADMIN))
                .requestMatchers("/rewrite-status*").access(allOf(site, ADMIN))
                .anyRequest().access(site));

        http.authenticationProvider(loginProvider(userDetailsService, passwordEncoder));

        http.formLogin(form -> form
                .loginPage(LOGIN_PAGE)
                .usernameParameter("j_username")
                .passwordParameter("j_password")
                .failureUrl(LOGIN_PAGE + "?error=true")
                // records the login in its session, so a later password
                // change ends that session, then sends the user on to the
                // request that triggered login
                .successHandler(new RollerLoginSuccessHandler(
                        new SavedRequestAwareAuthenticationSuccessHandler()))
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

        if (rememberMeEnabled()) {
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
                .successHandler(new RollerLoginSuccessHandler(oidcSuccess))
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

    /**
     * Roller no longer reads WEB-INF/security.xml. A copy left in place would
     * otherwise be ignored without a word, along with any access rules or LDAP
     * login it sets up.
     */
    static void refuseOldSecurityXml(ServletContext servletContext) throws MalformedURLException {
        if (servletContext != null && servletContext.getResource("/WEB-INF/security.xml") != null) {
            String message = "Roller no longer reads WEB-INF/security.xml. Move its "
                    + "changes to roller-custom.properties (see \"Alternative Authentication Options\" in "
                    + "the installation guide), then delete the file.";
            // also in Roller's own log, not only in the container's stack trace
            log.fatal(message);
            throw new IllegalStateException(message);
        }
    }

    /**
     * Whether the login page offers "Remember me". Not with LDAP: the cookie
     * is checked against Roller's database only, so it would keep working
     * after the directory account is changed or removed.
     */
    public static boolean rememberMeEnabled() {
        return WebloggerConfig.getBooleanProperty("rememberme.enabled")
                && WebloggerConfig.getAuthMethod() != AuthMethod.LDAP;
    }

    /**
     * Whether Roller is being installed, upgraded or given its first
     * administrator, when nobody can log in yet. Not a bean, so tests can
     * override it.
     */
    boolean setupInProgress() {
        return !WebloggerFactory.isBootstrapped() || !BootstrapSecurity.isCompleted();
    }

    /** The pages that set Roller up, which security.access rules leave alone during setup. */
    private static final RequestMatcher SETUP_PATHS = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern("/roller-ui/install/**"),
            PathPatternRequestMatcher.pathPattern("/roller-ui/bootstrap-token*"),
            PathPatternRequestMatcher.pathPattern("/roller-ui/register*"),
            PathPatternRequestMatcher.pathPattern("/roller-ui/setup*"));

    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

    /**
     * Rendering servlets whose path starts with the weblog's handle. The
     * rest of the path can hold any character, line breaks included.
     */
    private static final Pattern RENDERING_PATH = Pattern.compile(
            "^/roller-ui/rendering/(page|feed|resources|media-resources|search|rsd|comment)/([^/]+)(/.*)?$",
            Pattern.DOTALL);

    /** A leading locale, as in /en/entries/atom or /en_US. */
    private static final Pattern LOCALE_PATH = Pattern.compile(
            "^(/[^/_]{2}(?:_[^/_]{2})?)(/.*)?$", Pattern.DOTALL);

    /**
     * The path element that sends a weblog's public address to each rendering
     * servlet, as WeblogRequestMapper does. The page and comment servlets
     * take the address as it is.
     */
    private static final Map<String, String> RENDERING_CONTEXTS = Map.of(
            "feed", "/feed", "resources", "/resource", "media-resources", "/mediaresource",
            "search", "/search", "rsd", "/rsd");

    /**
     * The public path of the weblog a rendering servlet request is for, for
     * example /family/feed/entries/atom for
     * /roller-ui/rendering/feed/family/entries/atom, or null for other
     * requests.
     */
    static String weblogPath(String requestPath) {
        Matcher m = RENDERING_PATH.matcher(requestPath);
        if (!m.matches()) {
            return null;
        }
        String servlet = m.group(1);
        String weblog = "/" + m.group(2);
        String rest = StringUtils.defaultString(m.group(3));
        String context = RENDERING_CONTEXTS.get(servlet);
        if (context == null) {
            return weblog + rest;
        }
        // a locale comes before the feed or search element in the public path
        if ("feed".equals(servlet) || "search".equals(servlet)) {
            Matcher locale = LOCALE_PATH.matcher(rest);
            if (locale.matches()) {
                return weblog + locale.group(1) + context + StringUtils.defaultString(locale.group(2));
            }
        }
        return weblog + context + rest;
    }

    /** A security.access.* rule: requests matching the path need one of the authorities. */
    record AccessRule(String key, PathPattern pattern, RequestMatcher matcher, String[] authorities,
            AuthorizationManager<RequestAuthorizationContext> check) {

        AccessRule(String key, PathPattern pattern, RequestMatcher matcher, String[] authorities) {
            this(key, pattern, matcher, authorities, AuthorityAuthorizationManager.hasAnyAuthority(authorities));
        }
    }

    /**
     * Applies the most specific security.access.* rule matching the path a
     * visitor requested, and allows requests no rule matches. During setup
     * the setup pages are allowed, so a rule for /** can't block installing
     * or upgrading Roller.
     */
    record SiteAccess(List<AccessRule> rules, RequestMatcher setupPaths, BooleanSupplier setupInProgress)
            implements AuthorizationManager<RequestAuthorizationContext> {

        @Override
        public AuthorizationResult authorize(Supplier<? extends Authentication> authentication,
                RequestAuthorizationContext context) {
            // the rules are about the paths visitors ask for, not the JSPs and
            // servlets Roller forwards those requests to
            if (context.getRequest().getDispatcherType() != DispatcherType.REQUEST) {
                return new AuthorizationDecision(true);
            }
            if (setupInProgress.getAsBoolean() && setupPaths.matches(context.getRequest())) {
                return new AuthorizationDecision(true);
            }
            // Roller renders a weblog at /roller-ui/rendering/<servlet>/<handle>/...
            // as well as at /<handle>/..., so the rules are matched against the
            // weblog's own path there, and the most specific rule for that path
            // applies
            String weblogPath = weblogPath(URL_PATH_HELPER.getPathWithinApplication(context.getRequest()));
            PathContainer path = weblogPath == null ? null : PathContainer.parsePath(weblogPath);
            for (AccessRule rule : rules) {
                if (path != null ? rule.pattern().matches(path) : rule.matcher().matches(context.getRequest())) {
                    return rule.check().authorize(authentication, context);
                }
            }
            return new AuthorizationDecision(true);
        }
    }

    /**
     * Site-specific rules from roller.properties, for example to keep a weblog
     * private: security.access./fam/**=family. The key after security.access.
     * is a path pattern, matched without regard to case; the value lists the
     * authorities (role names) allowed, separated by commas. Only the most
     * specific rule matching a request applies, and a request has to pass
     * Roller's own rules as well.
     */
    static List<AccessRule> siteAccessRules() {
        PathPatternParser parser = new PathPatternParser();
        parser.setCaseSensitive(false);
        PathPatternRequestMatcher.Builder matchers = PathPatternRequestMatcher.withPathPatternParser(parser);

        List<AccessRule> rules = new ArrayList<>();
        Enumeration<Object> keys = WebloggerConfig.keys();
        while (keys.hasMoreElements()) {
            String key = String.valueOf(keys.nextElement());
            if (!key.startsWith(ACCESS_PREFIX)) {
                continue;
            }
            String path = key.substring(ACCESS_PREFIX.length());
            String[] authorities = Arrays.stream(
                    StringUtils.split(StringUtils.defaultString(WebloggerConfig.getProperty(key)), ','))
                    .map(String::trim).filter(StringUtils::isNotEmpty).toArray(String[]::new);
            if (!path.startsWith("/") || authorities.length == 0) {
                throw new IllegalStateException(key + " needs a path starting with / and at least one "
                        + "role, for example security.access./private/**=admin");
            }
            PathPattern pattern;
            try {
                pattern = parser.parse(path);
            } catch (PatternParseException e) {
                throw new IllegalStateException(key + " has a path Roller can't read: " + e.getMessage()
                        + ". ** can only come at the end, for example security.access./private/**=admin");
            }
            rules.add(new AccessRule(key, pattern, matchers.matcher(path), authorities));
        }
        rules.sort(Comparator.comparing(AccessRule::pattern, PathPattern.SPECIFICITY_COMPARATOR)
                .thenComparing(AccessRule::key));
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
     * JNDI settings for the directory connection: connect and read timeouts,
     * so a directory that stops answering can't hold login requests forever,
     * and any users.ldap.env.* properties, for example
     * users.ldap.env.java.naming.referral=follow.
     */
    static Map<String, Object> ldapEnvironment() {
        Map<String, Object> env = new HashMap<>();
        env.put("com.sun.jndi.ldap.connect.timeout", WebloggerConfig.getProperty("users.ldap.connectTimeout", "5000"));
        env.put("com.sun.jndi.ldap.read.timeout", WebloggerConfig.getProperty("users.ldap.readTimeout", "10000"));
        Enumeration<Object> keys = WebloggerConfig.keys();
        while (keys.hasMoreElements()) {
            String key = String.valueOf(keys.nextElement());
            if (key.startsWith(LDAP_ENV_PREFIX) && key.length() > LDAP_ENV_PREFIX.length()) {
                env.put(key.substring(LDAP_ENV_PREFIX.length()), WebloggerConfig.getProperty(key));
            }
        }
        return env;
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
        contextSource.setBaseEnvironmentProperties(ldapEnvironment());
        contextSource.afterPropertiesSet();

        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
                WebloggerConfig.getProperty("users.ldap.userSearchBase", ""),
                WebloggerConfig.getProperty("users.ldap.userSearchFilter", "(uid={0})"),
                contextSource);
        userSearch.setSearchSubtree(true);

        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserSearch(userSearch);

        AuthoritiesPopulator authorities = new AuthoritiesPopulator();
        String defaultRole = WebloggerConfig.getProperty("users.ldap.defaultRole", "editor");
        // a blank value gives directory users only the roles Roller has for them
        if (StringUtils.isNotBlank(defaultRole)) {
            authorities.setDefaultRole(defaultRole.trim());
        }

        return new LdapAuthenticationProvider(authenticator, authorities);
    }
}
