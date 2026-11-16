/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.roller.weblogger.webservices.xmlrpc;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.ui.core.filters.XmlRpcEnabledFilter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

/**
 * Covers how the XML-RPC endpoint treats vendor extension types.
 *
 * <p>Roller's XML-RPC API uses only the standard value types, so vendor
 * extension types are refused, and the endpoint is closed entirely while the
 * service is disabled.
 */
public class XmlRpcExtensionTypeTest {

    private static final Path WEB_XML = Paths.get(
            System.getProperty("project.basedir", System.getProperty("user.dir")),
            "src", "main", "webapp", "WEB-INF", "web.xml");

    private String parseRequest(String xml) throws Exception {
        return XmlRpcRequestParser.parse(xml.getBytes(StandardCharsets.UTF_8)).methodName();
    }

    private static final String ORDINARY_REQUEST =
            "<?xml version=\"1.0\"?><methodCall>"
                    + "<methodName>blogger.getUsersBlogs</methodName>"
                    + "<params><param><value><string>hello</string></value></param></params>"
                    + "</methodCall>";

    private static final String EXTENSION_REQUEST =
            "<?xml version=\"1.0\"?><methodCall>"
                    + "<methodName>blogger.getUsersBlogs</methodName><params><param><value>"
                    + "<ex:nil xmlns:ex=\"http://ws.apache.org/xmlrpc/namespaces/extensions\"/>"
                    + "</value></param></params></methodCall>";

    private Document webXml() throws Exception {
        try (java.io.InputStream in = Files.newInputStream(WEB_XML)) {
            assertNotNull(in, "web.xml must be available in the Maven build directory");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(in);
        }
    }

    /** No init-param may switch the endpoint into a vendor-extension mode. */
    @Test
    public void servletHasNoInitParameters() throws Exception {
        NodeList servlets = webXml().getElementsByTagNameNS("*", "servlet");
        boolean found = false;
        for (int i = 0; i < servlets.getLength(); i++) {
            Element servlet = (Element) servlets.item(i);
            String servletClass = servlet.getElementsByTagNameNS("*", "servlet-class")
                    .item(0).getTextContent().trim();
            if (RollerXmlRpcServlet.class.getName().equals(servletClass)) {
                found = true;
                assertEquals(0, servlet.getElementsByTagNameNS("*", "init-param").getLength());
            }
        }
        assertTrue(found, "the XML-RPC servlet was not found in web.xml");
    }

    /** The endpoint is closed by a filter while the service is switched off. */
    @Test
    public void endpointIsGatedWhileTheServiceIsDisabled() throws Exception {
        Document document = webXml();
        NodeList mappings = document.getElementsByTagNameNS("*", "filter-mapping");
        boolean found = false;
        for (int i = 0; i < mappings.getLength(); i++) {
            Element mapping = (Element) mappings.item(i);
            if ("XmlRpcEnabledFilter".equals(mapping.getElementsByTagNameNS("*", "filter-name")
                    .item(0).getTextContent().trim())) {
                found = true;
                assertEquals("/roller-services/xmlrpc",
                        mapping.getElementsByTagNameNS("*", "url-pattern").item(0)
                                .getTextContent().trim());
                java.util.Set<String> dispatchers = new java.util.HashSet<>();
                NodeList nodes = mapping.getElementsByTagNameNS("*", "dispatcher");
                for (int j = 0; j < nodes.getLength(); j++) {
                    dispatchers.add(nodes.item(j).getTextContent().trim());
                }
                assertEquals(java.util.Set.of("REQUEST", "FORWARD", "INCLUDE"), dispatchers);
            }
        }
        assertTrue(found, "the XML-RPC filter mapping was not found");
    }

    /** Ordinary XML-RPC calls must still parse with extensions disabled. */
    @Test
    public void ordinaryCallsStillParseWithExtensionsDisabled() throws Exception {
        String methodName = parseRequest(ORDINARY_REQUEST);
        assertNotNull(methodName, "an ordinary XML-RPC call must still parse");
        assertEquals("blogger.getUsersBlogs", methodName);
    }

    /** Vendor extension values are always refused. */
    @Test
    public void extensionValuesAreRejected() {
        XmlRpcException e = assertThrows(XmlRpcException.class, () -> parseRequest(EXTENSION_REQUEST));
        assertTrue(e.getMessage().contains("ex:nil"), e.getMessage());
    }

    /** Disabled requests receive a non-HTML response and never reach the chain. */
    @Test
    public void disabledFilterReturnsPlainResponse() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(new StringWriter()));
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("webservices.enableXmlRpc"))
                    .thenReturn(false);
            new XmlRpcEnabledFilter().doFilter(mock(ServletRequest.class), response, chain);
        }
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).setContentType("text/plain;charset=UTF-8");
        verify(response).getWriter();
        verifyNoInteractions(chain);
    }
}
