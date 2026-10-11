/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 *  under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.  For additional information regarding
 *  copyright in this work, please see the NOTICE file in the top level
 *  directory of this distribution.
 */
package org.apache.roller.weblogger.webservices.xmlrpc;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.parser.XmlRpcRequestParser;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.ui.core.filters.XmlRpcEnabledFilter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
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
 * <p>Roller's XML-RPC API uses only the standard value types, so the library's
 * vendor extension types are switched off in <code>web.xml</code> and the
 * endpoint is closed entirely while the service is disabled.
 */
public class XmlRpcExtensionTypeTest {

    private static final Path WEB_XML = Paths.get(
            System.getProperty("project.basedir", System.getProperty("user.dir")),
            "src", "main", "webapp", "WEB-INF", "web.xml");

    private String parseRequest(String xml, boolean extensionsEnabled) throws Exception {
        XmlRpcServer server = new XmlRpcServer();
        XmlRpcHttpRequestConfigImpl config = new XmlRpcHttpRequestConfigImpl();
        config.setEnabledForExtensions(extensionsEnabled);

        XmlRpcRequestParser parser = new XmlRpcRequestParser(
                config, new TypeFactoryImpl((XmlRpcController) server));
        javax.xml.parsers.SAXParserFactory saxFactory =
                javax.xml.parsers.SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        XMLReader reader = saxFactory.newSAXParser().getXMLReader();
        reader.setContentHandler(parser);
        reader.parse(new InputSource(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        return parser.getMethodName();
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

    /** The shipped configuration must not enable the extension types. */
    @Test
    public void shippedConfigurationDisablesExtensionTypes() throws Exception {
        Document document = webXml();
        NodeList params = document.getElementsByTagNameNS("*", "init-param");
        boolean found = false;
        for (int i = 0; i < params.getLength(); i++) {
            Element param = (Element) params.item(i);
            NodeList names = param.getElementsByTagNameNS("*", "param-name");
            if (names.getLength() > 0 && "enabledForExtensions".equals(names.item(0).getTextContent().trim())) {
                found = true;
                NodeList values = param.getElementsByTagNameNS("*", "param-value");
                assertEquals("false", values.item(0).getTextContent().trim());
            }
        }
        assertTrue(found, "enabledForExtensions init-param not found in web.xml");
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
        String methodName = parseRequest(ORDINARY_REQUEST, false);
        assertNotNull(methodName, "an ordinary XML-RPC call must still parse");
        assertEquals("blogger.getUsersBlogs", methodName);
    }

    /** Vendor extension values must be rejected by the shipped parser policy. */
    @Test
    public void extensionValuesAreRejectedWhenDisabled() {
        assertThrows(Exception.class, () -> parseRequest(EXTENSION_REQUEST, false));
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
