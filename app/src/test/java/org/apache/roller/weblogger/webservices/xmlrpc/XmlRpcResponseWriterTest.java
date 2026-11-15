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

package org.apache.roller.weblogger.webservices.xmlrpc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlRpcResponseWriterTest {

    private static String value(Object value) {
        String xml = new String(XmlRpcResponseWriter.response(value), StandardCharsets.UTF_8);
        String start = "<params><param><value>";
        String end = "</value></param></params>";
        return xml.substring(xml.indexOf(start) + start.length(), xml.lastIndexOf(end));
    }

    @Test
    public void writesTheStandardTypes() {
        assertEquals("<string>text</string>", value("text"));
        assertEquals("<int>42</int>", value(42));
        assertEquals("<int>7</int>", value(7L));
        assertEquals("<boolean>1</boolean>", value(true));
        assertEquals("<boolean>0</boolean>", value(false));
        assertEquals("<double>1.5</double>", value(1.5d));
        assertEquals("<double>10000000000</double>", value(1.0e10));
        assertEquals("<base64>AQID</base64>", value(new byte[] {1, 2, 3}));
    }

    @Test
    public void writesDatesInTheServersTimeZone() {
        Date date = Date.from(LocalDateTime.of(2026, 11, 14, 9, 5, 7)
                .atZone(ZoneId.systemDefault()).toInstant());
        assertEquals("<dateTime.iso8601>20261114T09:05:07</dateTime.iso8601>", value(date));
    }

    @Test
    public void writesStructsAndArrays() {
        Map<String, Object> struct = new LinkedHashMap<>();
        struct.put("title", "Post");
        struct.put("categories", List.of("a", "b"));
        assertEquals("<struct><member><name>title</name><value><string>Post</string></value></member>"
                + "<member><name>categories</name><value><array><data>"
                + "<value><string>a</string></value><value><string>b</string></value>"
                + "</data></array></value></member></struct>", value(struct));
        assertEquals("<array><data><value><int>1</int></value></data></array>", value(new Object[] {1}));
    }

    @Test
    public void escapesMarkupAndKeepsTheDocumentWellFormed() throws Exception {
        assertEquals("<string>a &lt;b&gt; &amp; c</string>", value("a <b> & c"));
        assertEquals("<string>line&#13;\nend</string>", value("line\r\nend"));
        // a control character that XML 1.0 forbids is dropped
        assertEquals("<string>ab</string>", value("a\u0001b"));

        byte[] doc = XmlRpcResponseWriter.response(Map.of("name", "a <b> & \u0001 c"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(doc));
    }

    @Test
    public void writesFaults() {
        String xml = new String(XmlRpcResponseWriter.fault(4000, "Upload denied"), StandardCharsets.UTF_8);
        assertTrue(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><fault>"), xml);
        assertTrue(xml.contains("<member><name>faultCode</name><value><int>4000</int></value></member>"), xml);
        assertTrue(xml.contains("<member><name>faultString</name><value><string>Upload denied</string>"), xml);
    }

    @Test
    public void refusesValuesWithoutAnXmlRpcType() {
        assertThrows(IllegalArgumentException.class, () -> XmlRpcResponseWriter.response(null));
        assertThrows(IllegalArgumentException.class, () -> XmlRpcResponseWriter.response(new Object()));
        assertThrows(IllegalArgumentException.class, () -> XmlRpcResponseWriter.response(1L << 40));
        assertThrows(IllegalArgumentException.class, () -> XmlRpcResponseWriter.response(Double.NaN));
    }
}
