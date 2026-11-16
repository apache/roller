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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.roller.weblogger.util.SafeSAXBuilder;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

/**
 * Reads an XML-RPC <code>methodCall</code>.
 *
 * <p>Only the value types of the XML-RPC specification are accepted:
 * string, int (or i4), boolean, double, dateTime.iso8601, base64, struct and
 * array. Vendor extensions such as nil, i8 or serialized Java objects are
 * refused. Documents are parsed with {@link SafeSAXBuilder}, which refuses
 * document type declarations and resolves nothing a document names.
 *
 * <p>Structs become {@link Map}s and arrays become {@code Object[]}, as they
 * did with the Apache XML-RPC library, so the handlers' casts still hold.
 */
final class XmlRpcRequestParser {

    /** Deepest nesting of structs and arrays accepted. Blogger and MetaWeblog calls need four. */
    static final int MAX_DEPTH = 32;

    /** Characters the XML-RPC specification allows in a method name. */
    private static final Pattern METHOD_NAME = Pattern.compile("[A-Za-z0-9_.:/]{1,128}");

    /**
     * The specification's <code>yyyyMMddTHH:mm:ss</code>, plus what clients send
     * in practice: dashes and colons, fractional seconds and a zone offset.
     */
    private static final Pattern DATE_TIME = Pattern.compile(
            "(\\d{4})-?(\\d{2})-?(\\d{2})T(\\d{2}):?(\\d{2}):?(\\d{2})(?:\\.(\\d{1,9}))?"
                    + "(Z|[+-]\\d{2}:?\\d{2})?");

    private XmlRpcRequestParser() {
    }

    static XmlRpcRequest parse(byte[] body) throws XmlRpcException {
        Document document;
        try {
            SAXBuilder builder = new SafeSAXBuilder();
            // SafeSAXBuilder already refuses document type declarations; saying
            // so here as well keeps the setting next to the untrusted input
            builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            document = builder.build(new ByteArrayInputStream(body));
        } catch (JDOMException | IOException | RuntimeException e) {
            // JDOM refuses some documents the XML parser accepts, such as an
            // XML 1.1 document holding a control character, and does it with
            // an unchecked exception. To the client that is still a request
            // Roller could not read, not a server error.
            throw new XmlRpcException("Failed to parse XML-RPC request");
        }

        Element root = document.getRootElement();
        if (!is(root, "methodCall")) {
            throw new XmlRpcException("Not an XML-RPC methodCall");
        }
        Element nameElement = null;
        Element paramsElement = null;
        for (Element child : root.getChildren()) {
            if (is(child, "methodName") && nameElement == null) {
                nameElement = child;
            } else if (is(child, "params") && paramsElement == null) {
                paramsElement = child;
            } else {
                throw new XmlRpcException("Unexpected element in methodCall");
            }
        }
        String methodName = nameElement == null ? "" : nameElement.getTextTrim();
        if (!METHOD_NAME.matcher(methodName).matches()) {
            throw new XmlRpcException("Missing or invalid methodName");
        }

        List<Object> params = new ArrayList<>();
        if (paramsElement != null) {
            for (Element param : paramsElement.getChildren()) {
                if (!is(param, "param") || param.getChildren().size() != 1
                        || !is(param.getChildren().get(0), "value")) {
                    throw new XmlRpcException("Each param must hold one value");
                }
                params.add(parseValue(param.getChildren().get(0), 1));
            }
        }
        return new XmlRpcRequest(methodName, List.copyOf(params));
    }

    private static Object parseValue(Element value, int depth) throws XmlRpcException {
        if (depth > MAX_DEPTH) {
            throw new XmlRpcException("XML-RPC value is nested too deeply");
        }
        List<Element> children = value.getChildren();
        if (children.isEmpty()) {
            // A value without a type element is a string, whitespace included
            return value.getText();
        }
        if (children.size() != 1) {
            throw new XmlRpcException("A value must hold one type");
        }
        Element typed = children.get(0);
        if (typed.getNamespace() != Namespace.NO_NAMESPACE) {
            throw unsupported(typed);
        }
        switch (typed.getName()) {
            case "struct":
                return parseStruct(typed, depth);
            case "array":
                return parseArray(typed, depth);
            default:
                break;
        }
        if (!typed.getChildren().isEmpty()) {
            throw new XmlRpcException("Invalid " + typed.getName() + " value");
        }
        String text = typed.getText();
        switch (typed.getName()) {
            case "string":
                return text;
            case "int":
            case "i4":
                return parseInt(text);
            case "boolean":
                return parseBoolean(text);
            case "double":
                return parseDouble(text);
            case "dateTime.iso8601":
                return parseDateTime(text);
            case "base64":
                return parseBase64(text);
            default:
                throw unsupported(typed);
        }
    }

    private static Map<String, Object> parseStruct(Element struct, int depth) throws XmlRpcException {
        Map<String, Object> members = new LinkedHashMap<>();
        for (Element member : struct.getChildren()) {
            Element name = member.getChild("name");
            Element value = member.getChild("value");
            if (!is(member, "member") || name == null || value == null
                    || member.getChildren().size() != 2 || !name.getChildren().isEmpty()) {
                throw new XmlRpcException("Each struct member must hold a name and a value");
            }
            members.put(name.getText(), parseValue(value, depth + 1));
        }
        return members;
    }

    private static Object[] parseArray(Element array, int depth) throws XmlRpcException {
        List<Element> children = array.getChildren();
        if (children.size() != 1 || !is(children.get(0), "data")) {
            throw new XmlRpcException("An array must hold one data element");
        }
        List<Object> items = new ArrayList<>();
        for (Element value : children.get(0).getChildren()) {
            if (!is(value, "value")) {
                throw new XmlRpcException("Array data may hold only values");
            }
            items.add(parseValue(value, depth + 1));
        }
        return items.toArray();
    }

    private static Integer parseInt(String text) throws XmlRpcException {
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            throw new XmlRpcException("Invalid int value");
        }
    }

    private static Boolean parseBoolean(String text) throws XmlRpcException {
        switch (text.trim()) {
            case "1":
                return Boolean.TRUE;
            case "0":
                return Boolean.FALSE;
            default:
                throw new XmlRpcException("Invalid boolean value");
        }
    }

    private static Double parseDouble(String text) throws XmlRpcException {
        try {
            double d = Double.parseDouble(text.trim());
            if (Double.isFinite(d)) {
                return d;
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        throw new XmlRpcException("Invalid double value");
    }

    static Date parseDateTime(String text) throws XmlRpcException {
        Matcher m = DATE_TIME.matcher(text.trim());
        if (!m.matches()) {
            throw new XmlRpcException("Invalid dateTime.iso8601 value");
        }
        try {
            String fraction = m.group(7) == null ? "0" : (m.group(7) + "00000000").substring(0, 9);
            LocalDateTime local = LocalDateTime.of(
                    Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)),
                    Integer.parseInt(m.group(5)), Integer.parseInt(m.group(6)),
                    Integer.parseInt(fraction));
            // Without a zone the time is the server's local time, as the
            // specification leaves it and as the Apache XML-RPC library read it
            ZoneId zone = m.group(8) == null ? ZoneId.systemDefault()
                    : ZoneOffset.of(normalizeOffset(m.group(8)));
            return Date.from(local.atZone(zone).toInstant());
        } catch (DateTimeException e) {
            throw new XmlRpcException("Invalid dateTime.iso8601 value");
        }
    }

    private static String normalizeOffset(String offset) {
        if ("Z".equals(offset) || offset.indexOf(':') > 0) {
            return offset;
        }
        return offset.substring(0, 3) + ":" + offset.substring(3);
    }

    private static byte[] parseBase64(String text) throws XmlRpcException {
        try {
            return Base64.getDecoder().decode(text.replaceAll("\\s", ""));
        } catch (IllegalArgumentException e) {
            throw new XmlRpcException("Invalid base64 value");
        }
    }

    private static boolean is(Element element, String name) {
        return name.equals(element.getName()) && element.getNamespace() == Namespace.NO_NAMESPACE;
    }

    private static XmlRpcException unsupported(Element typed) {
        String name = typed.getQualifiedName();
        if (name.length() > 64) {
            name = name.substring(0, 64);
        }
        return new XmlRpcException("Unsupported XML-RPC value type: " + name);
    }
}
