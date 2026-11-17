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

import jakarta.servlet.http.Cookie;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Remember-me with LDAP login, where the password Roller stores for directory
 * users is a placeholder.
 */
class RollerRememberMeServicesTest {

    private final Map<String, String> savedProperties = new HashMap<>();
    private final InMemoryUserDetailsManager users = new InMemoryUserDetailsManager();

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        setProperty("authentication.method", "ldap");
        setProperty("rememberme.enabled", "true");
        setProperty("rememberme.key", "test-remember-me-key");
    }

    @AfterEach
    void tearDown() throws ReflectiveOperationException {
        Properties config = config();
        savedProperties.forEach((k, v) -> {
            if (v == null) {
                config.remove(k);
            } else {
                config.setProperty(k, v);
            }
        });
    }

    @Test
    void aRegisteredDirectoryUserIsRemembered() {
        users.createUser(User.withUsername("dana").password("<externalAuth>").authorities("editor").build());
        RollerRememberMeServices services = new RollerRememberMeServices(users);

        MockHttpServletResponse response = new MockHttpServletResponse();
        services.loginSuccess(loginRequest(), response, directoryLogin("dana"));
        Cookie cookie = response.getCookie("remember-me");
        assertNotNull(cookie);

        MockHttpServletRequest later = new MockHttpServletRequest("GET", "/roller-ui/menu.rol");
        later.setCookies(cookie);
        Authentication remembered = services.autoLogin(later, new MockHttpServletResponse());
        assertNotNull(remembered, "the cookie logs the user in again");
        assertEquals("dana", remembered.getName());
    }

    @Test
    void aDirectoryUserWithoutARollerAccountGetsNoCookie() {
        RollerRememberMeServices services = new RollerRememberMeServices(users);

        MockHttpServletResponse response = new MockHttpServletResponse();
        services.loginSuccess(loginRequest(), response, directoryLogin("newcomer"));
        assertNull(response.getCookie("remember-me"));
    }

    private static MockHttpServletRequest loginRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/roller_j_security_check");
        request.setParameter("remember-me", "on");
        return request;
    }

    /** What the LDAP provider returns once Spring has erased the password. */
    private static Authentication directoryLogin(String name) {
        return UsernamePasswordAuthenticationToken.authenticated(name, null,
                AuthorityUtils.createAuthorityList("editor"));
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
