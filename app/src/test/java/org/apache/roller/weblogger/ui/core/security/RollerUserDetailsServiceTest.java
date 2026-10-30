/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.core.security;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Sign-in through an OpenID URL honors the account's enabled flag.
 */
class RollerUserDetailsServiceTest {

    private static final String OPENID = "https://id.example.com/reader";

    private MockedStatic<WebloggerFactory> factory;
    private UserManager users;

    @BeforeEach
    void setUp() throws Exception {
        users = mock(UserManager.class);
        Weblogger roller = mock(Weblogger.class);
        when(roller.getUserManager()).thenReturn(users);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    private User user(Boolean enabled) throws Exception {
        User user = new User();
        user.setUserName("reader");
        user.setPassword("x");
        user.setEnabled(enabled);
        when(users.getUserByOpenIdUrl(OPENID)).thenReturn(user);
        when(users.getRoles(user)).thenReturn(List.of("editor"));
        return user;
    }

    @Test
    void enabledOpenIdUserSignsIn() throws Exception {
        user(true);
        UserDetails details = new RollerUserDetailsService().loadUserByUsername(OPENID);
        assertEquals("reader", details.getUsername());
    }

    @Test
    void disabledOpenIdUserIsRefused() throws Exception {
        user(false);
        assertThrows(DisabledException.class,
                () -> new RollerUserDetailsService().loadUserByUsername(OPENID));
    }

    @Test
    void enabledOpenIdUserIsMarkedEnabled() throws Exception {
        user(true);
        assertTrue(new RollerUserDetailsService().loadUserByUsername(OPENID).isEnabled());
    }

    @Test
    void openIdUserWithNoEnabledFlagIsRefused() throws Exception {
        user(null);
        assertThrows(DisabledException.class,
                () -> new RollerUserDetailsService().loadUserByUsername(OPENID));
    }

    @Test
    void disabledOpenIdUserIsRefusedWithTrailingSlash() throws Exception {
        user(false);
        assertThrows(DisabledException.class,
                () -> new RollerUserDetailsService().loadUserByUsername(OPENID + "/"));
    }

    @Test
    void unknownOpenIdUrlGetsTheRegistrationPlaceholder() throws Exception {
        UserDetails details = new RollerUserDetailsService().loadUserByUsername(OPENID);
        assertEquals("openid", details.getUsername());
        assertEquals(List.of("rollerOpenidLogin"), details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    }
}
