/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.webservices.atomprotocol;

import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.apache.roller.weblogger.ui.core.RollerContext;

class RollerAtomHandlerTest {

    private static final String USER_NAME = "alice";
    private static final String PASSWORD = "test-password";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Weblogger weblogger;

    @Mock
    private UserManager userManager;

    @Mock
    private URLStrategy urlStrategy;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        User user = new User();
        user.setUserName(USER_NAME);
        user.setPassword(PASSWORD);
        user.setEnabled(true);

        when(weblogger.getUserManager()).thenReturn(userManager);
        when(weblogger.getUrlStrategy()).thenReturn(urlStrategy);
        when(urlStrategy.getAtomProtocolURL(true)).thenReturn("https://example.test/app");
        when(userManager.getUserByUserName(USER_NAME)).thenReturn(user);
    }

    @Test
    void wsseAuthenticationModeIsRejected() throws Exception {
        String created = Instant.now().toString();
        byte[] nonce = "test-nonce".getBytes(StandardCharsets.UTF_8);
        MessageDigest digester = MessageDigest.getInstance("SHA-1");
        digester.update(nonce);
        digester.update(created.getBytes(StandardCharsets.UTF_8));
        digester.update(PASSWORD.getBytes(StandardCharsets.UTF_8));
        String digest = Base64.getEncoder().encodeToString(digester.digest());
        when(request.getHeader("X-WSSE")).thenReturn(
                "UsernameToken Username=\"" + USER_NAME
                + "\", PasswordDigest=\"" + digest
                + "\", Nonce=\"" + Base64.getEncoder().encodeToString(nonce)
                + "\", Created=\"" + created + "\"");

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            config.when(() -> WebloggerRuntimeConfig.getProperty("webservices.atomPubAuth"))
                    .thenReturn("wsse");

            RollerAtomHandler handler = new RollerAtomHandler(request, response);

            assertNull(handler.getAuthenticatedUsername());
            verify(request, never()).getHeader("Authorization");
        }
    }

    @Test
    void basicAuthenticationAcceptsNormalizedMethodAndUsesResolvedUser() {
        String credentials = Base64.getEncoder().encodeToString(
                (USER_NAME + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8));
        when(request.getHeader("Authorization")).thenReturn("Basic " + credentials);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.matches(PASSWORD, PASSWORD)).thenReturn(true);

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class);
             MockedStatic<RollerContext> context = mockStatic(RollerContext.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            config.when(() -> WebloggerRuntimeConfig.getProperty("webservices.atomPubAuth"))
                    .thenReturn("  BaSiC  ");
            context.when(RollerContext::getPasswordEncoder).thenReturn(encoder);

            RollerAtomHandler handler = new RollerAtomHandler(request, response);

            assertNotNull(handler.getAuthenticatedUsername());
        }
    }

    @Test
    void nullAuthenticationMethodIsRejected() {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            config.when(() -> WebloggerRuntimeConfig.getProperty("webservices.atomPubAuth"))
                    .thenReturn(null);

            RollerAtomHandler handler = new RollerAtomHandler(request, response);

            assertNull(handler.getAuthenticatedUsername());
            verify(request, never()).getHeader("Authorization");
        }
    }
}
