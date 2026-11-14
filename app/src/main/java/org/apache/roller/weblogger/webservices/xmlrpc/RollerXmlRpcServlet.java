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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;

/**
 * The Blogger and MetaWeblog XML-RPC endpoint.
 *
 * <p>Calls are answered by {@link BloggerAPIHandler} under
 * <code>blogger.*</code> and {@link MetaWeblogAPIHandler} under
 * <code>metaWeblog.*</code>; see {@link XmlRpcServer}. The endpoint is gated by
 * <code>XmlRpcEnabledFilter</code> while <code>webservices.enableXmlRpc</code>
 * is off.
 *
 * <p>Request bodies are capped before they are parsed: at 1 MiB, or, while
 * uploads are on, at what the largest allowed upload takes as base64 plus room
 * for the call around it. The cap applies to the decompressed size of a
 * gzip-encoded request too.
 */
public class RollerXmlRpcServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(RollerXmlRpcServlet.class);

    /** Smallest request cap, in bytes; also the cap while uploads are off. */
    static final int MIN_MAX_REQUEST_BYTES = 1024 * 1024;

    /** Room for the methodCall around a base64 upload, in bytes. */
    static final int ENVELOPE_BYTES = 64 * 1024;

    static final Map<String, Class<?>> HANDLERS = Map.of(
            "blogger", BloggerAPIHandler.class,
            "metaWeblog", MetaWeblogAPIHandler.class);

    private transient XmlRpcServer server;

    @Override
    public void init() {
        server = new XmlRpcServer(HANDLERS);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int maxBytes = maxRequestBytes();
        if (request.getContentLengthLong() > maxBytes) {
            sendText(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Request is too large");
            return;
        }

        InputStream in = request.getInputStream();
        String encoding = request.getHeader("Content-Encoding");
        boolean gzip = false;
        if (encoding != null && !encoding.isBlank()
                && !"identity".equalsIgnoreCase(encoding.trim())) {
            String name = encoding.trim().toLowerCase(Locale.ROOT);
            if (!"gzip".equals(name) && !"x-gzip".equals(name)) {
                sendText(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                        "Unsupported Content-Encoding");
                return;
            }
            gzip = true;
        }

        byte[] body;
        try {
            if (gzip) {
                in = new GZIPInputStream(in);
            }
            // Read one byte past the cap, so an oversized body can be detected
            // without holding more than that in memory.
            body = in.readNBytes(maxBytes + 1);
        } catch (IOException e) {
            if (!gzip) {
                throw e;
            }
            log.debug("Rejecting XML-RPC request with an unreadable gzip body", e);
            sendText(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid gzip request body");
            return;
        }
        if (body.length > maxBytes) {
            sendText(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Request is too large");
            return;
        }

        byte[] result = server.execute(body);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/xml;charset=UTF-8");
        response.setContentLength(result.length);
        response.getOutputStream().write(result);
    }

    /** The largest request body accepted, in bytes. */
    int maxRequestBytes() {
        if (!WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")) {
            return MIN_MAX_REQUEST_BYTES;
        }
        double megabytes;
        try {
            megabytes = Double.parseDouble(WebloggerRuntimeConfig.getProperty("uploads.file.maxsize"));
        } catch (RuntimeException e) {
            return MIN_MAX_REQUEST_BYTES;
        }
        if (!(megabytes > 0)) {
            return MIN_MAX_REQUEST_BYTES;
        }
        double uploadBytes = Math.ceil(megabytes * 1024 * 1024);
        // base64 takes 4 bytes per 3, plus CRLF after every 76 characters if the client wraps it
        double base64Bytes = Math.ceil(uploadBytes / 3) * 4 * 78 / 76;
        double limit = Math.max(MIN_MAX_REQUEST_BYTES, base64Bytes + ENVELOPE_BYTES);
        return (int) Math.min(limit, Integer.MAX_VALUE - 1);
    }

    private static void sendText(HttpServletResponse response, int status, String text)
            throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(text);
    }
}
