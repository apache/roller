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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.xrds.XrdsServiceEndpoint;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.util.OpenID4JavaUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RollerXrdsParserTest {

    private static final String SERVER_TYPE = "http://specs.openid.net/auth/2.0/server";

    private static final String XRDS_BODY =
            "<xrds:XRDS xmlns:xrds=\"xri://$xrds\" xmlns=\"xri://$xrd*($v*2.0)\">"
            + "<XRD><Service priority=\"0\">"
            + "<Type>" + SERVER_TYPE + "</Type>"
            + "<URI>https://openid.example.com/server</URI>"
            + "</Service></XRD></xrds:XRDS>";

    private final Set<String> types = Collections.singleton(SERVER_TYPE);

    @Test
    void openid4javaIsConfiguredToUseThisParser() throws ReflectiveOperationException {
        assertEquals(RollerXrdsParser.class.getName(),
                OpenID4JavaUtils.getProperty("discovery.xrds.parser"));

        // the parser YadisResolver loaded from that setting
        Field parser = YadisResolver.class.getDeclaredField("XRDS_PARSER");
        parser.setAccessible(true);
        assertInstanceOf(RollerXrdsParser.class, parser.get(null));
    }

    @Test
    void readsServiceEndpoints() throws DiscoveryException {
        List<?> endpoints = new RollerXrdsParser().parseXrds(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + XRDS_BODY, types);

        assertEquals(1, endpoints.size());
        assertEquals("https://openid.example.com/server",
                ((XrdsServiceEndpoint) endpoints.get(0)).getUri());
    }

    @Test
    void refusesDocumentTypeDeclarations() {
        String withDoctype = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE xrds:XRDS [<!ENTITY greeting \"hello\">]>" + XRDS_BODY;

        assertThrows(DiscoveryException.class,
                () -> new RollerXrdsParser().parseXrds(withDoctype, types));
    }

    @Test
    void refusesSchemaLocations() {
        String xsi = "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ";
        String located = XRDS_BODY.replace("<xrds:XRDS ", "<xrds:XRDS " + xsi
                + "xsi:schemaLocation=\"xri://$xrds http://schema.example.com/xrds.xsd\" ");
        String unnamespaced = XRDS_BODY.replace("<XRD>", "<XRD " + xsi
                + "xsi:noNamespaceSchemaLocation=\"http://schema.example.com/xrd.xsd\">");

        // refused before openid4java's parser sees them
        assertThrows(DiscoveryException.class, () -> RollerXrdsParser.check(located));
        assertThrows(DiscoveryException.class, () -> RollerXrdsParser.check(unnamespaced));
    }

    @Test
    void refusesMalformedDocuments() {
        assertThrows(DiscoveryException.class,
                () -> new RollerXrdsParser().parseXrds("<xrds:XRDS", types));
    }
}
