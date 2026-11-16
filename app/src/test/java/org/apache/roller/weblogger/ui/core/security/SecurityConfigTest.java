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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
 * replaced, so it also checks that the move kept Roller's behaviour.
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
        outcome(chain, login("bob", "wrong", session), wrong);
        assertEquals("/roller-ui/login.rol?error=true", wrong.getRedirectedUrl());
        assertTrue(wrong.getHeaders("Set-Cookie").isEmpty(), "no remember-me cookie when it is off");

        MockHttpServletResponse right = new MockHttpServletResponse();
        outcome(chain, login("bob", "secret", session), right);
        assertEquals("http://localhost/roller-ui/authoring/entries.rol?weblog=myblog&continue",
                right.getRedirectedUrl());
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
    void siteAccessRulesComeFirstAndIgnoreCase() throws Exception {
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
    void rememberMeWhenEnabled() throws Exception {
        setProperty("rememberme.enabled", "true");
        setProperty("rememberme.key", "test-remember-me-key");
        FilterChainProxy chain = chain(WithUser.class);

        MockHttpServletRequest login = login("bob", "secret", new MockHttpSession(servletContext));
        login.setParameter("remember-me", "on");
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

        // bob only exists in the database login, which LDAP replaces
        MockHttpServletResponse response = new MockHttpServletResponse();
        outcome(chain, login("bob", "secret", new MockHttpSession(servletContext)), response);
        assertEquals("/roller-ui/login.rol?error=true", response.getRedirectedUrl());
    }

    @Test
    void ldapWithoutUrlIsRefused() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("users.ldap.url", "");
        IllegalStateException e = assertThrows(IllegalStateException.class, SecurityConfig::ldapProvider);
        assertEquals("authentication.method=ldap needs users.ldap.url in roller-custom.properties",
                e.getMessage());
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

    private MockHttpServletRequest login(String user, String password, MockHttpSession session) {
        MockHttpServletRequest login = request("POST", "/roller_j_security_check", "");
        login.setParameter("j_username", user);
        login.setParameter("j_password", password);
        login.setSession(session);
        return login;
    }

    private static String outcome(FilterChainProxy chain, MockHttpServletRequest request,
            MockHttpServletResponse response) throws Exception {
        MockFilterChain end = new MockFilterChain();
        chain.doFilter(request, response, end);
        return end.getRequest() != null ? PASS : response.getStatus() + " " + response.getRedirectedUrl();
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
