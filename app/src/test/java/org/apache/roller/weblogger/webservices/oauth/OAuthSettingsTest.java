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

import com.opensymphony.xwork2.ActionInvocation;
import java.nio.file.Path;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.ui.struts2.core.OAuthSettingsInterceptor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class OAuthSettingsTest {
    @Test
    void servicesAndActionsUseAtomPubSettings() throws Exception {
        assertRouteSettings(false, "basic", false);
        assertRouteSettings(false, "oauth", false);
        assertRouteSettings(true, "basic", false);
        assertRouteSettings(true, "oauth", true);
    }

    private void assertRouteSettings(boolean atomPub, String scheme, boolean enabled) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        ActionInvocation invocation = mock(ActionInvocation.class);
        when(invocation.invoke()).thenReturn("success");
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("webservices.enableAtomPub"))
                    .thenReturn(atomPub);
            config.when(() -> WebloggerRuntimeConfig.getProperty("webservices.atomPubAuth")).thenReturn(scheme);
            config.when(WebloggerRuntimeConfig::isAtomPubOAuthEnabled).thenCallRealMethod();

            new OAuthSettingsFilter().doFilter(request, response, chain);
            String result = new OAuthSettingsInterceptor().intercept(invocation);

            if (enabled) {
                verify(chain).doFilter(request, response);
                assertEquals("success", result);
            } else {
                verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
                verifyNoInteractions(chain);
                verify(invocation, never()).invoke();
                assertEquals("oauth-disabled", result);
            }
        }
    }

    @Test
    void descriptorsConnectTheOAuthRoutes() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        Document web = factory.newDocumentBuilder().parse(
                Path.of("src/main/webapp/WEB-INF/web.xml").toFile());
        Document struts = factory.newDocumentBuilder().parse(
                Path.of("src/main/resources/struts.xml").toFile());
        XPath xpath = XPathFactory.newInstance().newXPath();

        String mapping = "/web-app/filter-mapping[filter-name='OAuthSettingsFilter']";
        assertEquals("/roller-services/oauth/*", xpath.evaluate(mapping + "/url-pattern[1]", web));
        assertEquals("/roller-ui/oauthKeys.rol", xpath.evaluate(mapping + "/url-pattern[2]", web));
        assertEquals("/roller-ui/oauthAuthorize.rol", xpath.evaluate(mapping + "/url-pattern[3]", web));
        assertTrue(Boolean.parseBoolean(xpath.evaluate(
                "boolean(" + mapping + "/following-sibling::filter-mapping[filter-name='ValidateSaltFilter'])",
                web)));
        String ui = "/struts/package[@namespace='/roller-ui']";
        assertEquals("OAuthSettingsInterceptor", xpath.evaluate(
                ui + "/interceptors/interceptor-stack[@name='oauthStack']/interceptor-ref[1]/@name", struts));
        for (String name : new String[]{"oauthKeys", "oauthAuthorize"}) {
            String action = ui + "/action[@name='" + name + "']";
            assertEquals("oauthStack", xpath.evaluate(action + "/interceptor-ref/@name", struts));
            assertEquals("404", xpath.evaluate(
                    action + "/result[@name='oauth-disabled' and @type='httpheader']/param[@name='status']",
                    struts));
        }
    }
}
