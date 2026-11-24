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

package org.apache.roller.weblogger.webservices.oauth;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.SimpleOAuthValidator;
import org.apache.roller.weblogger.business.OAuthManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RequestTokenServletTest {
    private static final String URL = "https://roller.example/roller-services/oauth/requestToken";
    private static final String CALLBACK = "https://client.example/return?state=ready";

    private final RequestTokenServlet servlet = new RequestTokenServlet();
    private final Weblogger weblogger = mock(Weblogger.class);
    private final OAuthManager manager = mock(OAuthManager.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final OAuthConsumer consumer = new OAuthConsumer(null, "reader", "client-key", null);
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() throws Exception {
        when(weblogger.getOAuthManager()).thenReturn(manager);
        when(manager.getConsumer(any())).thenReturn(consumer);
        when(manager.getValidator()).thenReturn(new SimpleOAuthValidator());
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int value) {
                body.write(value);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {
            }
        });
        doAnswer(call -> {
            OAuthAccessor accessor = call.getArgument(0);
            accessor.requestToken = "issued-token";
            accessor.tokenSecret = "issued-secret";
            return null;
        }).when(manager).generateRequestToken(any());
    }

    @Test
    void signedPostReceivesTemporaryCredentials() throws Exception {
        assertTemporaryCredentials("POST");
    }

    @Test
    void signedGetReceivesTemporaryCredentials() throws Exception {
        assertTemporaryCredentials("GET");
    }

    private void assertTemporaryCredentials(String method) throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);

            issue(method, request(method, CALLBACK, consumer));

            ArgumentCaptor<OAuthAccessor> generated = ArgumentCaptor.forClass(OAuthAccessor.class);
            verify(manager).generateRequestToken(generated.capture());
            assertNull(generated.getValue().accessToken);
            assertEquals(OAuth.formEncode(OAuth.newList("oauth_token", "issued-token",
                    "oauth_token_secret", "issued-secret")),
                    body.toString(StandardCharsets.UTF_8));
            verify(manager, never()).getAccessor(any());
            verify(weblogger).flush();
        }
    }

    @Test
    void postRequiresMatchingClientCredentials() throws Exception {
        assertMatchingClientCredentials("POST");
    }

    @Test
    void getRequiresMatchingClientCredentials() throws Exception {
        assertMatchingClientCredentials("GET");
    }

    private void assertMatchingClientCredentials(String method) throws Exception {
        OAuthConsumer other = new OAuthConsumer(null, consumer.consumerKey, "other-client-key", null);
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);

            issue(method, request(method, CALLBACK, other));

            verify(manager, never()).generateRequestToken(any());
            verify(weblogger, never()).flush();
            assertTrue(body.toString(StandardCharsets.UTF_8).contains("oauth_problem=signature_invalid"));
        }
    }

    private void issue(String method, HttpServletRequest request) throws Exception {
        if ("GET".equals(method)) {
            servlet.doGet(request, response);
        } else {
            servlet.doPost(request, response);
        }
    }

    private HttpServletRequest request(String method, String callback, OAuthConsumer signer) throws Exception {
        OAuthMessage message = new OAuthAccessor(signer).newRequestMessage(method, URL,
                OAuth.newList("oauth_signature_method", "HMAC-SHA1", "oauth_callback", callback));
        Map<String, String[]> parameters = new HashMap<>();
        message.getParameters().forEach(parameter ->
                parameters.put(parameter.getKey(), new String[]{parameter.getValue()}));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURL()).thenReturn(new StringBuffer(URL));
        when(request.getParameterMap()).thenReturn(parameters);
        return request;
    }
}
