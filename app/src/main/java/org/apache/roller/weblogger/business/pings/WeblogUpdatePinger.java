/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business.pings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.PingConfig;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.SecureXmlParsers;
import org.apache.xmlrpc.XmlRpcException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility for sending a weblog update ping.
 *
 * <p>This implements the <code>weblogUpdates.ping</code> XML-RPC call described
 * at <a href="http://www.xmlrpc.com/weblogsCom">www.xmlrpc.com</a>, as well as
 * some variants required to interoperate with certain buggy but popular ping
 * targets.
 *
 * <p>Roller builds the request and reads the response itself rather than through
 * an XML-RPC client library. The response is parsed with {@link SecureXmlParsers},
 * which refuses document type declarations and resolves nothing the document
 * names, and only the <code>flerror</code> and <code>message</code> fields the
 * protocol defines are read; any other content is ignored.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 * @author llavandowska (for code refactored from the now-defunct <code>RollerXmlRpcClient</code>)
 */
public final class WeblogUpdatePinger {
    public static final Log LOGGER = LogFactory.getLog(WeblogUpdatePinger.class);

    private static final String PING_METHOD_NAME = "weblogUpdates.ping";

    /** How long to wait to open a connection to a ping target, in milliseconds. */
    private static final int CONNECT_TIMEOUT_MS = 15000;

    /** How long to wait for a ping target's response, in milliseconds. */
    private static final int READ_TIMEOUT_MS = 30000;

    /** Upper bound on the response we will read from a ping target, in bytes. */
    private static final int MAX_RESPONSE_BYTES = 1024 * 1024;

    /**
     * Conveys a ping result.
     */
    public static class PingResult {
        boolean error;
        String message;

        public PingResult(Boolean error, String message) {
            this.error = error != null && error;
            this.message = message != null ? message : "";
        }

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "PingResult{" + "error=" + error + ", message='" + message + "'" + "}";
        }
    }

    // Inhibit construction
    private WeblogUpdatePinger() {
    }

    /**
     * Send a weblog update ping.
     *
     * @param pingTarget         the target site to ping
     * @param website            the website that changed (from which the ping originates)
     * @return the result message string sent by the server.
     * @throws IOException if an IOException occurs during the ping
     * @throws XmlRpcException if the ping target reports a fault
     */
    public static PingResult sendPing(PingTarget pingTarget, Weblog website) throws IOException, XmlRpcException {
        String websiteUrl = website.getAbsoluteURL();
        String pingTargetUrl = pingTarget.getPingUrl();
        Set<String> variantOptions = PingConfig.getVariantOptions(pingTargetUrl);

        // Set up the ping parameters.
        List<String> params = new ArrayList<>();
        if (!variantOptions.contains("noname")) {
            // ping variant for icerocket and anyone with similar bug, where we must omit the blog name.
            params.add(website.getName());
        }
        params.add(websiteUrl);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing ping to '" + pingTargetUrl + "' for website '" + websiteUrl + "' (" + website.getName() + ")" + (variantOptions.isEmpty() ? "" : " with variant options " + variantOptions));
        }

        // Send the ping and read the response.
        byte[] response = exchange(pingTargetUrl, buildRequest(params));
        PingResult pingResult = parseResponse(response);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Ping result is: " + pingResult);
        }
        return pingResult;
    }

    /**
     * Build the XML-RPC <code>weblogUpdates.ping</code> request body for the
     * given string parameters.
     */
    static String buildRequest(List<String> params) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<methodCall><methodName>").append(PING_METHOD_NAME).append("</methodName>");
        sb.append("<params>");
        for (String param : params) {
            sb.append("<param><value><string>");
            xmlEscape(sb, param);
            sb.append("</string></value></param>");
        }
        sb.append("</params></methodCall>");
        return sb.toString();
    }

    /**
     * POST the request body to the ping target and return the response bytes,
     * capped at {@link #MAX_RESPONSE_BYTES}.
     */
    static byte[] exchange(String pingTargetUrl, String requestBody) throws IOException {
        URL url = new URL(pingTargetUrl);
        URLConnection connection = url.openConnection();
        if (!(connection instanceof HttpURLConnection)) {
            // Ping targets are HTTP(S) XML-RPC endpoints. Refuse anything else
            // (for example file: or ftp:) with an IOException rather than failing
            // with a ClassCastException that callers do not handle.
            throw new IOException("Ping target URL is not an HTTP URL: " + pingTargetUrl);
        }
        HttpURLConnection conn = (HttpURLConnection) connection;
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            conn.setRequestProperty("User-Agent", "Apache Roller");

            byte[] body = requestBody.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(body.length);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(body);
            }

            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                // A non-2xx status is a transport-level failure, not a ping
                // result. Surface it so the ping is treated as failed (and
                // retried by the queue processor), as the old client did.
                throw new IOException("Ping target returned HTTP status " + status);
            }
            return readCapped(conn.getInputStream());
        } finally {
            conn.disconnect();
        }
    }

    /** Read at most {@link #MAX_RESPONSE_BYTES} bytes from the stream. */
    private static byte[] readCapped(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int total = 0;
        int read;
        while ((read = in.read(chunk)) != -1) {
            total += read;
            if (total > MAX_RESPONSE_BYTES) {
                throw new IOException("Ping target response exceeded " + MAX_RESPONSE_BYTES + " bytes");
            }
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    /**
     * Parse an XML-RPC ping response. Only <code>flerror</code> and
     * <code>message</code> are read from a normal response; a fault is turned
     * into an {@link XmlRpcException}.
     *
     * @throws XmlRpcException if the response is a fault
     */
    static PingResult parseResponse(byte[] response) throws IOException, XmlRpcException {
        if (response == null || response.length == 0) {
            return new PingResult(null, null);
        }
        PingResponseHandler handler = new PingResponseHandler();
        try {
            SAXParser parser = SecureXmlParsers.newSAXParserFactory().newSAXParser();
            parser.parse(new InputSource(new ByteArrayInputStream(response)), handler);
        } catch (SAXException | javax.xml.parsers.ParserConfigurationException ex) {
            // The target responded with something that is not a well-formed, safe
            // XML-RPC response. Treat it as a non-error transmission, mirroring the
            // prior lenient handling of buggy targets, without exposing the body.
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not parse ping response as XML-RPC; proceeding.", ex);
            }
            return new PingResult(null, null);
        }

        if (handler.isFault()) {
            throw new XmlRpcException(handler.getFaultCode(), handler.getFaultString());
        }
        return new PingResult(handler.getFlerror(), handler.getMessage());
    }

    private static void xmlEscape(StringBuilder sb, String value) {
        if (value == null) {
            return;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&apos;"); break;
                default: sb.append(c);
            }
        }
    }

    /**
     * Collects just the fields Roller needs from an XML-RPC ping response.
     * Scalar values, including any {@code <base64>}, are read only as text.
     */
    private static final class PingResponseHandler extends DefaultHandler {
        private final StringBuilder text = new StringBuilder();
        private final Map<String, String> members = new HashMap<>();
        private boolean inFault;
        private boolean inStruct;
        private String memberName;
        private String scalar;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            text.setLength(0);
            if ("fault".equals(qName)) {
                inFault = true;
            } else if ("struct".equals(qName)) {
                inStruct = true;
            } else if ("member".equals(qName)) {
                memberName = null;
                scalar = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            text.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            switch (qName) {
                case "name":
                    memberName = text.toString().trim();
                    break;
                case "string":
                case "boolean":
                case "int":
                case "i4":
                case "double":
                case "dateTime.iso8601":
                case "base64":
                    // Record the raw text only; base64 is not decoded.
                    scalar = text.toString();
                    break;
                case "value":
                    if (scalar == null) {
                        // An XML-RPC <value> with no type child is a string.
                        scalar = text.toString();
                    }
                    break;
                case "member":
                    if (memberName != null && inStruct) {
                        members.put(memberName, scalar != null ? scalar : "");
                    }
                    scalar = null;
                    break;
                case "struct":
                    inStruct = false;
                    break;
                default:
                    break;
            }
        }

        boolean isFault() {
            return inFault;
        }

        int getFaultCode() {
            String code = members.get("faultCode");
            if (code != null) {
                try {
                    return Integer.parseInt(code.trim());
                } catch (NumberFormatException ignored) {
                    // fall through to default
                }
            }
            return 0;
        }

        String getFaultString() {
            return members.get("faultString");
        }

        Boolean getFlerror() {
            String value = members.get("flerror");
            if (value == null) {
                return null;
            }
            value = value.trim();
            return "1".equals(value) || "true".equalsIgnoreCase(value);
        }

        String getMessage() {
            return members.get("message");
        }
    }

    /**
     * Decide if the given exception appears to warrant later retrial attempts.
     *
     * @param ex an exception thrown by the <code>sendPing</code> operation
     * @return true if the error warrants retrial
     */
    public static boolean shouldRetry(Exception ex) {
        // Determine if error appears transient (warranting retrial)
        // We give most errors the "benefit of the doubt" by considering them transient
        // This picks out a few that we consider non-transient
        if (ex instanceof UnknownHostException) {
            // User probably mistyped the url in the custom target.
            return false;
        } else if (ex instanceof MalformedURLException) {
            // This should never happen due to validations but if we get here, retrial won't fix it.
            return false;
        }
        return true;
    }

}
