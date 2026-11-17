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

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.Cookie;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sends requests through the filter chain SecurityConfig builds.
 *
 * <p>The request table was recorded from the security.xml this configuration
 * replaced, so it also checks that the move kept Roller's behaviour. The
 * deliberate changes since: the login form needs Spring's CSRF token, and a
 * POST that needed a login isn't repeated after it.
 */
class SecurityConfigTest {

    private static final String PASS = "PASS";
    private static final String LOGIN = "302 /roller-ui/login.rol";

    /** A configuration whose database login knows one editor, bob/secret. */
    @Configuration
    static class WithUser extends SecurityConfig {
        @Override
        UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(User.withUsername("bob")
                    .password(RollerPasswordEncoders.create().encode("secret"))
                    .authorities("editor").build());
        }
    }

    /** One editor, carol/secret, whose password is an unsalted SHA-1 hash from an old Roller. */
    @Configuration
    static class WithLegacyUser extends SecurityConfig {
        static final InMemoryUserDetailsManager USERS = new InMemoryUserDetailsManager();

        @Override
        UserDetailsService userDetailsService() {
            return USERS;
        }
    }

    private final MockServletContext servletContext = new MockServletContext();
    private final Map<String, String> savedProperties = new HashMap<>();
    private AnnotationConfigWebApplicationContext context;

    @AfterEach
    void tearDown() throws ReflectiveOperationException {
        if (context != null) {
            context.close();
        }
        Properties config = config();
        savedProperties.forEach((k, v) -> {
            if (v == null) {
                config.remove(k);
            } else {
                config.setProperty(k, v);
            }
        });
    }

    @TestFactory
    Stream<DynamicTest> requestsAreHandledAsBefore() {
        String[][] table = {
            // method, path, role, dispatcher, outcome
            {"GET", "/roller-ui/login.rol", "", "REQUEST", PASS},
            {"GET", "/roller-ui/register.rol", "", "REQUEST", PASS},
            {"GET", "/roller-ui/menu.rol", "", "REQUEST", LOGIN},
            {"GET", "/roller-ui/menu.rol", "editor", "REQUEST", PASS},
            {"GET", "/roller-ui/login-redirect.rol", "", "REQUEST", LOGIN},
            {"GET", "/roller-ui/login-redirect.rol", "editor", "REQUEST", PASS},
            {"GET", "/roller-ui/profile.rol", "", "REQUEST", LOGIN},
            {"POST", "/roller-ui/profile!save.rol", "editor", "REQUEST", PASS},
            {"GET", "/roller-ui/createWeblog.rol", "", "REQUEST", LOGIN},
            {"GET", "/roller-ui/authoring/entries.rol", "", "REQUEST", LOGIN},
            {"GET", "/roller-ui/authoring/entries.rol", "editor", "REQUEST", PASS},
            {"POST", "/roller-ui/authoring/entryEdit!save.rol", "editor", "REQUEST", PASS},
            {"GET", "/roller-ui/admin/userAdmin.rol", "", "REQUEST", LOGIN},
            {"GET", "/roller-ui/admin/userAdmin.rol", "editor", "REQUEST", "403 null"},
            {"GET", "/roller-ui/admin/userAdmin.rol", "admin", "REQUEST", PASS},
            {"GET", "/rewrite-status", "", "REQUEST", LOGIN},
            {"GET", "/rewrite-status", "editor", "REQUEST", "403 null"},
            {"GET", "/rewrite-status", "admin", "REQUEST", PASS},
            {"GET", "/myblog/entry/hello", "", "REQUEST", PASS},
            {"GET", "/myblog/feed/entries/atom", "", "REQUEST", PASS},
            {"GET", "/styles/roller.css", "", "REQUEST", PASS},
            {"GET", "/images/x.png", "", "REQUEST", PASS},
            {"GET", "/scripts/x.js", "", "REQUEST", PASS},
            {"GET", "/logout", "", "REQUEST", PASS},
            {"GET", "/login", "", "REQUEST", PASS},
            {"GET", "/login/oauth2/code/keycloak", "", "REQUEST", "302 /roller-ui/login.rol?error=oidc"},
            {"GET", "/roller-ui/rendering/page/myblog", "", "FORWARD", PASS},
            {"GET", "/roller-ui/rendering/page/myblog", "", "ERROR", PASS},
            {"GET", "/roller-ui/menu.rol", "", "FORWARD", LOGIN},
            {"GET", "/roller-ui/admin/userAdmin.rol", "editor", "FORWARD", "403 null"},
            {"GET", "/rd/entry/fran%C3%A7ais", "", "REQUEST", PASS},
            {"GET", "/rd/category/Java+Stuff", "", "REQUEST", PASS},
            {"GET", "/rd/category/Java%20Stuff", "", "REQUEST", PASS},
            {"GET", "/rd/entry/hello;jsessionid=ABC123", "", "REQUEST", "400 null"},
            {"GET", "/rd/a%2Fb", "", "REQUEST", "400 null"},
        };
        return Stream.of(table).map(row -> DynamicTest.dynamicTest(String.join(" ", row[0], row[1], row[2], row[3]),
                () -> {
                    FilterChainProxy chain = chain(SecurityConfig.class);
                    MockHttpServletRequest request = request(row[0], row[1], row[2]);
                    request.setDispatcherType(DispatcherType.valueOf(row[3]));
                    assertEquals(row[4], outcome(chain, request, new MockHttpServletResponse()));
                }));
    }

    @Test
    void pagesGetSecurityHeadersButStaticFilesAndAnonymousPagesNoSession() throws Exception {
        FilterChainProxy chain = chain(SecurityConfig.class);

        MockHttpServletRequest page = request("GET", "/myblog/entry/hello", "");
        MockHttpServletResponse pageResponse = new MockHttpServletResponse();
        assertEquals(PASS, outcome(chain, page, pageResponse));
        assertEquals("SAMEORIGIN", pageResponse.getHeader("X-Frame-Options"));
        assertEquals("nosniff", pageResponse.getHeader("X-Content-Type-Options"));
        assertNull(page.getSession(false), "an anonymous page must not create a session");

        MockHttpServletResponse styleResponse = new MockHttpServletResponse();
        assertEquals(PASS, outcome(chain, request("GET", "/styles/roller.css", ""), styleResponse));
        assertTrue(styleResponse.getHeaderNames().isEmpty(), "static files bypass Spring Security");
    }

    @Test
    void loginReturnsToTheSavedRequest() throws Exception {
        FilterChainProxy chain = chain(WithUser.class);
        MockHttpSession session = new MockHttpSession(servletContext);

        MockHttpServletRequest page = request("GET", "/roller-ui/authoring/entries.rol", "");
        page.setQueryString("weblog=myblog");
        page.setSession(session);
        assertEquals(LOGIN, outcome(chain, page, new MockHttpServletResponse()));

        MockHttpServletResponse wrong = new MockHttpServletResponse();
        outcome(chain, login(chain, "bob", "wrong", session), wrong);
        assertEquals("/roller-ui/login.rol?error=true", wrong.getRedirectedUrl());
        assertTrue(wrong.getHeaders("Set-Cookie").isEmpty(), "no remember-me cookie when it is off");

        MockHttpServletResponse right = new MockHttpServletResponse();
        outcome(chain, login(chain, "bob", "secret", session), right);
        assertEquals("http://localhost/roller-ui/authoring/entries.rol?weblog=myblog&continue",
                right.getRedirectedUrl());
    }

    @Test
    void aPostThatNeedsALoginIsNotReplayed() throws Exception {
        FilterChainProxy chain = chain(WithUser.class);
        MockHttpSession session = new MockHttpSession(servletContext);

        // an author whose session timed out submits a form; its one-time salt
        // can't be used again, so the login doesn't send the browser back to it
        MockHttpServletRequest post = request("POST", "/roller-ui/authoring/entryEdit!save.rol", "");
        post.setQueryString("weblog=myblog");
        post.setSession(session);
        assertEquals(LOGIN, outcome(chain, post, new MockHttpServletResponse()));

        MockHttpServletResponse right = new MockHttpServletResponse();
        outcome(chain, login(chain, "bob", "secret", session), right);
        assertEquals("/", right.getRedirectedUrl());
    }

    @Test
    void loginNeedsTheLoginPageToken() throws Exception {
        FilterChainProxy chain = chain(WithUser.class);
        MockHttpServletRequest login = request("POST", "/roller_j_security_check", "");
        login.setParameter("j_username", "bob");
        login.setParameter("j_password", "secret");

        MockHttpServletResponse response = new MockHttpServletResponse();
        outcome(chain, login, response);
        assertEquals("/roller-ui/login.rol?error=expired", response.getRedirectedUrl());
    }

    @Test
    void loginFromAnExpiredLoginPageGoesBackToTheLoginPage() throws Exception {
        FilterChainProxy chain = chain(WithUser.class);
        // the token came with a session that has since timed out
        MockHttpServletRequest login = login(chain, "bob", "secret", new MockHttpSession(servletContext));
        login.setSession(new MockHttpSession(servletContext));

        MockHttpServletResponse response = new MockHttpServletResponse();
        outcome(chain, login, response);
        assertEquals("/roller-ui/login.rol?error=expired", response.getRedirectedUrl());

        // the login page then gives a fresh token, and logging in works
        MockHttpServletResponse retry = new MockHttpServletResponse();
        outcome(chain, login(chain, "bob", "secret", (MockHttpSession) login.getSession()), retry);
        assertEquals("/", retry.getRedirectedUrl());
    }

    @Test
    void otherRefusedPostsStillGetTheAccessDeniedPage() throws Exception {
        FilterChainProxy chain = chain(WithUser.class);
        assertEquals("403 null", outcome(chain,
                request("POST", "/roller-ui/admin/userAdmin.rol", "editor"), new MockHttpServletResponse()));
    }

    @Test
    void loginUpgradesAnOldPasswordEncoding() throws Exception {
        setProperty("passwds.encryption.enabled", "true");
        setProperty("passwds.encryption.algorithm", "bcrypt");
        setProperty("passwds.encryption.lazyUpgradeFrom", "SHA");
        String sha1 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                .digest("secret".getBytes(StandardCharsets.UTF_8)));
        WithLegacyUser.USERS.createUser(User.withUsername("carol").password(sha1).authorities("editor").build());
        try {
            FilterChainProxy chain = chain(WithLegacyUser.class);
            MockHttpServletResponse response = new MockHttpServletResponse();
            outcome(chain, login(chain, "carol", "secret", new MockHttpSession(servletContext)), response);

            assertEquals("/", response.getRedirectedUrl());
            assertTrue(WithLegacyUser.USERS.loadUserByUsername("carol").getPassword().startsWith("{bcrypt}"));
        } finally {
            WithLegacyUser.USERS.deleteUser("carol");
        }
    }

    @Test
    void loginUsesTheSharedPasswordEncoder() throws Exception {
        context = context(WithUser.class);
        PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
        // test configuration stores new passwords without encryption
        assertTrue(encoder.encode("x").startsWith("{noop}"));
        assertTrue(encoder.matches("x", RollerPasswordEncoders.create().encode("x")));
    }

    @Test
    void siteAccessRulesIgnoreCase() throws Exception {
        setProperty("security.access./fam", "family");
        setProperty("security.access./fam/**", "family, admin");
        FilterChainProxy chain = chain(SecurityConfig.class);

        assertEquals(LOGIN, outcome(chain, request("GET", "/fam", ""), new MockHttpServletResponse()));
        assertEquals(LOGIN, outcome(chain, request("GET", "/fam/entry/x", ""), new MockHttpServletResponse()));
        assertEquals(LOGIN, outcome(chain, request("GET", "/FAM/", ""), new MockHttpServletResponse()));
        assertEquals("403 null", outcome(chain, request("GET", "/fam/", "editor"), new MockHttpServletResponse()));
        assertEquals(PASS, outcome(chain, request("GET", "/fam/", "family"), new MockHttpServletResponse()));
        assertEquals(PASS, outcome(chain, request("GET", "/Fam/entry/x", "admin"), new MockHttpServletResponse()));
        assertEquals(PASS, outcome(chain, request("GET", "/family-recipes/", ""), new MockHttpServletResponse()));
    }

    @Test
    void siteAccessRuleWithoutRoleIsRefused() throws Exception {
        setProperty("security.access./fam/**", " ");
        IllegalStateException e = assertThrows(IllegalStateException.class, SecurityConfig::siteAccessRules);
        assertEquals("security.access./fam/** needs a path starting with / and at least one role, "
                + "for example security.access./private/**=admin", e.getMessage());
    }

    @Test
    void siteAccessRulesCannotOpenRollerPages() throws Exception {
        setProperty("security.access./**", "member, editor");
        FilterChainProxy chain = chain(SecurityConfig.class);

        assertEquals(PASS, outcome(chain, request("GET", "/myblog/", "member"), new MockHttpServletResponse()));
        assertEquals(LOGIN, outcome(chain, request("GET", "/myblog/", ""), new MockHttpServletResponse()));
        assertEquals("403 null", outcome(chain,
                request("GET", "/roller-ui/admin/userAdmin.rol", "member"), new MockHttpServletResponse()));
        assertEquals("403 null", outcome(chain,
                request("GET", "/roller-ui/authoring/entries.rol", "member"), new MockHttpServletResponse()));
        assertEquals("403 null", outcome(chain,
                request("GET", "/roller-ui/admin/userAdmin.rol", "editor"), new MockHttpServletResponse()));
        assertEquals(PASS, outcome(chain,
                request("GET", "/roller-ui/authoring/entries.rol", "editor"), new MockHttpServletResponse()));
    }

    @Test
    void siteAccessRulesCanNarrowRollerPages() throws Exception {
        setProperty("security.access./roller-ui/authoring/**", "admin");
        FilterChainProxy chain = chain(SecurityConfig.class);

        assertEquals("403 null", outcome(chain,
                request("GET", "/roller-ui/authoring/entries.rol", "editor"), new MockHttpServletResponse()));
        assertEquals(PASS, outcome(chain,
                request("GET", "/roller-ui/authoring/entries.rol", "admin"), new MockHttpServletResponse()));
    }

    @Test
    void aRuleForEverythingLeavesLoginWorking() throws Exception {
        setProperty("security.access./**", "family");
        FilterChainProxy chain = chain(WithUser.class);

        for (String path : new String[] {"/roller-ui/login.rol", "/webjars/bootstrap/5.3.8/css/bootstrap.min.css",
                "/roller-ui/scripts/date-fields.js", "/theme/scripts/roller.js", "/favicon.svg",
                "/oauth2/authorization/keycloak", "/roller-ui/logout.rol"}) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            String result = outcome(chain, request("GET", path, ""), response);
            assertFalse(result.startsWith("302 /roller-ui/login.rol") || result.startsWith("403"),
                    path + ": " + result);
        }
        // the login action renders its page with a forward to a JSP
        MockHttpServletRequest forward = request("GET", "/roller-ui/login.rol", "");
        forward.setDispatcherType(DispatcherType.FORWARD);
        forward.setRequestURI("/WEB-INF/jsps/tiles/tiles-simplepage.jsp");
        forward.setServletPath("/WEB-INF/jsps/tiles/tiles-simplepage.jsp");
        assertEquals(PASS, outcome(chain, forward, new MockHttpServletResponse()), "forward to the login JSP");
        // while the path a visitor asks for is still covered
        MockHttpServletRequest page = request("GET", "/myblog/", "");
        assertEquals(LOGIN, outcome(chain, page, new MockHttpServletResponse()));
        // bob is an editor without the family role: he can log in, but not read the site
        MockHttpServletResponse login = new MockHttpServletResponse();
        outcome(chain, login(chain, "bob", "secret", new MockHttpSession(servletContext)), login);
        assertEquals("/", login.getRedirectedUrl());
        assertEquals("403 null", outcome(chain, request("GET", "/myblog/", "editor"), new MockHttpServletResponse()));
    }

    @Test
    void theMostSpecificSiteAccessRuleApplies() throws Exception {
        setProperty("security.access./{handle}/drafts/**", "admin");
        setProperty("security.access./public/drafts/**", "editor");
        FilterChainProxy chain = chain(SecurityConfig.class);

        assertEquals(PASS, outcome(chain,
                request("GET", "/public/drafts/x", "editor"), new MockHttpServletResponse()));
        assertEquals("403 null", outcome(chain,
                request("GET", "/other/drafts/x", "editor"), new MockHttpServletResponse()));
    }

    @Test
    void siteAccessRuleWithAnUnreadablePathIsRefused() throws Exception {
        setProperty("security.access./fam/**/drafts", "family");
        IllegalStateException e = assertThrows(IllegalStateException.class, SecurityConfig::siteAccessRules);
        assertTrue(e.getMessage().startsWith("security.access./fam/**/drafts has a path Roller can't read: "),
                e.getMessage());
    }

    @Test
    void anOldSecurityXmlStopsTheStart() {
        MockServletContext withOldFile = new MockServletContext() {
            @Override
            public java.net.URL getResource(String path) throws java.net.MalformedURLException {
                return "/WEB-INF/security.xml".equals(path)
                        ? new java.net.URL("file:/WEB-INF/security.xml") : super.getResource(path);
            }
        };
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.setServletContext(withOldFile);
        ctx.register(SecurityConfig.class);
        Exception e = assertThrows(Exception.class, ctx::refresh);
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        assertEquals("Roller no longer reads WEB-INF/security.xml. Move its changes to "
                + "roller-custom.properties (see \"Alternative Authentication Options\" in the installation "
                + "guide), then delete the file.", cause.getMessage());
    }

    @Test
    void rememberMeWhenEnabled() throws Exception {
        setProperty("rememberme.enabled", "true");
        setProperty("rememberme.key", "test-remember-me-key");
        FilterChainProxy chain = chain(WithUser.class);

        MockHttpServletRequest login = login(chain, "bob", "secret", new MockHttpSession(servletContext));
        // the checkbox the login page renders
        login.setParameter(rememberMeCheckboxName(), "on");
        MockHttpServletResponse response = new MockHttpServletResponse();
        outcome(chain, login, response);
        Cookie cookie = response.getCookie("remember-me");
        assertNotNull(cookie, "login with the checkbox ticked sets the remember-me cookie");

        MockHttpServletRequest later = request("GET", "/roller-ui/menu.rol", "");
        later.setCookies(cookie);
        assertEquals(PASS, outcome(chain, later, new MockHttpServletResponse()));
    }

    @Test
    void secureLoginRedirectsToHttps() throws Exception {
        setProperty("securelogin.enabled", "true");
        MockHttpServletResponse response = new MockHttpServletResponse();
        outcome(chain(SecurityConfig.class), request("GET", "/roller-ui/menu.rol", ""), response);
        assertEquals("https://localhost/roller-ui/login.rol", response.getRedirectedUrl());
    }

    @Test
    void ldapReplacesTheDatabaseLogin() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("users.ldap.url", "ldap://127.0.0.1:1/dc=example,dc=com");
        FilterChainProxy chain = chain(WithUser.class);

        UsernamePasswordAuthenticationFilter form = chain.getFilters(SecurityConfig.LOGIN_PROCESSING_URL).stream()
                .filter(UsernamePasswordAuthenticationFilter.class::isInstance)
                .map(UsernamePasswordAuthenticationFilter.class::cast).findFirst().orElseThrow();
        Field managerField = AbstractAuthenticationProcessingFilter.class.getDeclaredField("authenticationManager");
        managerField.setAccessible(true);
        ProviderManager manager = (ProviderManager) managerField.get(form);
        Field parentField = ProviderManager.class.getDeclaredField("parent");
        parentField.setAccessible(true);

        // the only provider that checks passwords is the directory, here and in any parent
        List<AuthenticationProvider> passwordProviders = new ArrayList<>();
        for (Object m = manager; m != null; m = parentField.get(m)) {
            ((ProviderManager) m).getProviders().stream()
                    .filter(p -> p.supports(UsernamePasswordAuthenticationToken.class))
                    .forEach(passwordProviders::add);
        }
        assertEquals(1, passwordProviders.size(), passwordProviders.toString());
        assertTrue(passwordProviders.get(0) instanceof LdapAuthenticationProvider);
    }

    @Test
    void ldapDefaultRoleCanBeLeftBlank() {
        setPropertyUnchecked("authentication.method", "ldap");
        setPropertyUnchecked("users.ldap.url", "ldap://127.0.0.1:1/dc=example,dc=com");
        setPropertyUnchecked("users.ldap.defaultRole", "");
        assertNotNull(SecurityConfig.ldapProvider());
    }

    @Test
    void ldapWithoutUrlIsRefused() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("users.ldap.url", "");
        IllegalStateException e = assertThrows(IllegalStateException.class, SecurityConfig::ldapProvider);
        assertEquals("authentication.method=ldap needs users.ldap.url in roller-custom.properties",
                e.getMessage());
    }

    private static String rememberMeCheckboxName() throws IOException {
        String page = new String(Files.readAllBytes(
                Paths.get("src/main/webapp/WEB-INF/jsps/core/Login.jsp")), StandardCharsets.UTF_8);
        Matcher checkbox = Pattern.compile("<input type=\"checkbox\" name=\"([^\"]+)\"").matcher(page);
        assertTrue(checkbox.find(), "Login.jsp has no checkbox");
        return checkbox.group(1);
    }

    private FilterChainProxy chain(Class<?> config) {
        context = context(config);
        return context.getBean("springSecurityFilterChain", FilterChainProxy.class);
    }

    private AnnotationConfigWebApplicationContext context(Class<?> config) {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.setServletContext(servletContext);
        ctx.register(config);
        ctx.refresh();
        return ctx;
    }

    private MockHttpServletRequest request(String method, String path, String role) {
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext, method, path);
        if (!role.isEmpty()) {
            MockHttpSession session = new MockHttpSession(servletContext);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    new SecurityContextImpl(UsernamePasswordAuthenticationToken.authenticated(
                            role, null, AuthorityUtils.createAuthorityList(role))));
            request.setSession(session);
        }
        return request;
    }

    /** A login form post, with the token the login page gives that session. */
    private MockHttpServletRequest login(FilterChainProxy chain, String user, String password,
            MockHttpSession session) throws Exception {
        MockHttpServletRequest page = request("GET", "/roller-ui/login.rol", "");
        page.setSession(session);
        outcome(chain, page, new MockHttpServletResponse());
        CsrfToken token = (CsrfToken) page.getAttribute(CsrfToken.class.getName());

        MockHttpServletRequest login = request("POST", "/roller_j_security_check", "");
        login.setParameter("j_username", user);
        login.setParameter("j_password", password);
        login.setParameter(token.getParameterName(), token.getToken());
        login.setSession(session);
        return login;
    }

    private static String outcome(FilterChainProxy chain, MockHttpServletRequest request,
            MockHttpServletResponse response) throws Exception {
        MockFilterChain end = new MockFilterChain();
        chain.doFilter(request, response, end);
        return end.getRequest() != null ? PASS : response.getStatus() + " " + response.getRedirectedUrl();
    }

    private void setPropertyUnchecked(String key, String value) {
        try {
            setProperty(key, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setProperty(String key, String value) throws ReflectiveOperationException {
        Properties config = config();
        savedProperties.putIfAbsent(key, config.getProperty(key));
        config.setProperty(key, value);
    }

    private static Properties config() throws ReflectiveOperationException {
        Field field = WebloggerConfig.class.getDeclaredField("config");
        field.setAccessible(true);
        return (Properties) field.get(null);
    }
}
