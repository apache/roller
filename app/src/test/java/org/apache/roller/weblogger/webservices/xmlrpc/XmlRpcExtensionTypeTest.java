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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.xmlrpc.parser.XmlRpcRequestParser;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers how the XML-RPC endpoint treats vendor extension types.
 *
 * <p>Roller's XML-RPC API uses only the standard value types, so the library's
 * vendor extension types are switched off in <code>web.xml</code> and the
 * endpoint is closed entirely while the service is disabled.
 */
public class XmlRpcExtensionTypeTest {

    private static final Path WEB_XML =
            Paths.get("src", "main", "webapp", "WEB-INF", "web.xml");

    private String parseRequest(String xml, boolean extensionsEnabled) throws Exception {
        XmlRpcServer server = new XmlRpcServer();
        XmlRpcHttpRequestConfigImpl config = new XmlRpcHttpRequestConfigImpl();
        config.setEnabledForExtensions(extensionsEnabled);

        XmlRpcRequestParser parser = new XmlRpcRequestParser(
                config, new TypeFactoryImpl((XmlRpcController) server));
        XMLReader reader = XMLReaderFactory.createXMLReader();
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

    /** The shipped configuration must not enable the extension types. */
    @Test
    public void shippedConfigurationDisablesExtensionTypes() throws Exception {
        String webXml = new String(Files.readAllBytes(WEB_XML), StandardCharsets.UTF_8);
        int idx = webXml.indexOf("enabledForExtensions");
        assertTrue(idx > 0, "enabledForExtensions param not found in web.xml");
        String tail = webXml.substring(idx, Math.min(idx + 300, webXml.length()));
        assertTrue(tail.contains("<param-value>false</param-value>"),
                "the XML-RPC servlet must not enable vendor extension types:\n" + tail);
    }

    /** The endpoint is closed by a filter while the service is switched off. */
    @Test
    public void endpointIsGatedWhileTheServiceIsDisabled() throws Exception {
        String webXml = new String(Files.readAllBytes(WEB_XML), StandardCharsets.UTF_8);
        assertTrue(webXml.contains("XmlRpcEnabledFilter"),
                "a filter must gate the XML-RPC endpoint");
        int mapping = webXml.indexOf("<filter-name>XmlRpcEnabledFilter</filter-name>",
                webXml.indexOf("<filter-mapping>"));
        assertTrue(mapping > 0, "the gating filter must be mapped");
        assertTrue(webXml.indexOf("/roller-services/xmlrpc", mapping) > 0,
                "the gating filter must be mapped to the XML-RPC endpoint");
    }

    /** Ordinary XML-RPC calls must still parse with extensions disabled. */
    @Test
    public void ordinaryCallsStillParseWithExtensionsDisabled() throws Exception {
        String methodName = parseRequest(ORDINARY_REQUEST, false);
        assertNotNull(methodName, "an ordinary XML-RPC call must still parse");
        assertEquals("blogger.getUsersBlogs", methodName);
    }
}