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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.roller.weblogger.util.XmlCharacters;

/**
 * Writes XML-RPC <code>methodResponse</code> documents, as UTF-8.
 *
 * <p>Values map to the specification's types: String to string, Integer
 * (and Short, Byte, and Long within the int range) to int, Boolean to
 * boolean, Double and Float to double, Date to dateTime.iso8601, byte[] to
 * base64, Map to struct, and Collection or Object[] to array. Anything else,
 * including null, is refused with an {@link IllegalArgumentException}.
 */
final class XmlRpcResponseWriter {

    /** The specification's format, in the server's time zone, as the Apache XML-RPC library wrote it. */
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH:mm:ss");

    private static final String PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private XmlRpcResponseWriter() {
    }

    static byte[] response(Object value) {
        StringBuilder out = new StringBuilder(PROLOG).append("<methodResponse><params><param>");
        appendValue(out, value, 1);
        out.append("</param></params></methodResponse>");
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    static byte[] fault(int code, String message) {
        StringBuilder out = new StringBuilder(PROLOG)
                .append("<methodResponse><fault><value><struct>")
                .append("<member><name>faultCode</name><value><int>").append(code).append("</int></value></member>")
                .append("<member><name>faultString</name><value><string>")
                .append(escape(message == null ? "" : message))
                .append("</string></value></member>")
                .append("</struct></value></fault></methodResponse>");
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void appendValue(StringBuilder out, Object value, int depth) {
        if (depth > XmlRpcRequestParser.MAX_DEPTH) {
            throw new IllegalArgumentException("XML-RPC response is nested too deeply");
        }
        out.append("<value>");
        if (value instanceof String) {
            element(out, "string", escape((String) value));
        } else if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
            element(out, "int", value.toString());
        } else if (value instanceof Long) {
            long l = (Long) value;
            if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("XML-RPC int is out of range: " + l);
            }
            element(out, "int", Long.toString(l));
        } else if (value instanceof Boolean) {
            element(out, "boolean", (Boolean) value ? "1" : "0");
        } else if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (!Double.isFinite(d)) {
                throw new IllegalArgumentException("XML-RPC double must be finite");
            }
            element(out, "double", BigDecimal.valueOf(d).toPlainString());
        } else if (value instanceof Date) {
            element(out, "dateTime.iso8601",
                    DATE_TIME.format(((Date) value).toInstant().atZone(ZoneId.systemDefault())));
        } else if (value instanceof byte[]) {
            element(out, "base64", Base64.getEncoder().encodeToString((byte[]) value));
        } else if (value instanceof Map) {
            out.append("<struct>");
            for (Map.Entry<?, ?> member : ((Map<?, ?>) value).entrySet()) {
                if (member.getKey() == null) {
                    throw new IllegalArgumentException("XML-RPC struct member has no name");
                }
                out.append("<member><name>").append(escape(member.getKey().toString())).append("</name>");
                appendValue(out, member.getValue(), depth + 1);
                out.append("</member>");
            }
            out.append("</struct>");
        } else if (value instanceof Collection || value instanceof Object[]) {
            Iterable<?> items = value instanceof Collection
                    ? (Collection<?>) value : java.util.Arrays.asList((Object[]) value);
            out.append("<array><data>");
            for (Object item : items) {
                appendValue(out, item, depth + 1);
            }
            out.append("</data></array>");
        } else {
            throw new IllegalArgumentException("Cannot write "
                    + (value == null ? "null" : value.getClass().getName()) + " as an XML-RPC value");
        }
        out.append("</value>");
    }

    private static void element(StringBuilder out, String name, String text) {
        out.append('<').append(name).append('>').append(text).append("</").append(name).append('>');
    }

    /** Escapes markup and drops characters that XML 1.0 does not allow. */
    static String escape(String text) {
        String legal = XmlCharacters.withoutIllegalCharacters(text);
        StringBuilder out = new StringBuilder(legal.length() + 16);
        for (int i = 0; i < legal.length(); i++) {
            char c = legal.charAt(i);
            switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '\r':
                    // a literal CR would be read back as LF
                    out.append("&#13;");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }
}
