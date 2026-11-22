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

package org.apache.roller.weblogger.ui.core.filters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;

/**
 * Gates the XML-RPC endpoint on <code>webservices.enableXmlRpc</code>, ahead of
 * the servlet. Buffers a bounded request body before the XML-RPC parser reads it.
 */
public class XmlRpcEnabledFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(XmlRpcEnabledFilter.class);
    private static final int MIB = 1024 * 1024;
    private static final int MAX_REQUEST_BYTES = 32 * MIB;
    private static final int ENVELOPE_BYTES = 64 * 1024;

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to configure
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (!WebloggerRuntimeConfig.getBooleanProperty("webservices.enableXmlRpc")) {
            LOG.warn("XML-RPC service is disabled; rejecting request");
            sendText(httpResponse, HttpServletResponse.SC_NOT_FOUND, "XML-RPC service is disabled");
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (!"POST".equals(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String encoding = httpRequest.getHeader("Content-Encoding");
        if (encoding != null && !encoding.isBlank() && !"identity".equalsIgnoreCase(encoding.trim())) {
            sendText(httpResponse, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Content-Encoding");
            return;
        }

        int limit = maxRequestBytes();
        if (httpRequest.getContentLengthLong() > limit) {
            sendText(httpResponse, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "XML-RPC request is too large");
            return;
        }
        byte[] body = httpRequest.getInputStream().readNBytes(limit + 1);
        if (body.length > limit) {
            sendText(httpResponse, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "XML-RPC request is too large");
            return;
        }

        chain.doFilter(new HttpServletRequestWrapper(httpRequest) {
            private final ServletInputStream input = bodyStream(body);

            @Override
            public ServletInputStream getInputStream() {
                return input;
            }
        }, response);
    }

    private static int maxRequestBytes() {
        if (!WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")) {
            return MIB;
        }
        String value = WebloggerRuntimeConfig.getProperty("uploads.file.maxsize");
        if (value == null) {
            return MIB;
        }
        double megabytes;
        try {
            megabytes = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return MIB;
        }
        if (!(megabytes > 0)) {
            return MIB;
        }
        double uploadBytes = Math.ceil(megabytes * MIB);
        // Allow base64, line breaks at 76 columns, and the surrounding XML.
        double base64Bytes = Math.ceil(uploadBytes / 3) * 4 * 78 / 76;
        return (int) Math.min(MAX_REQUEST_BYTES, Math.max(MIB, base64Bytes + ENVELOPE_BYTES));
    }

    private static ServletInputStream bodyStream(byte[] body) {
        ByteArrayInputStream input = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() {
                return input.read();
            }

            @Override
            public int read(byte[] buffer, int offset, int length) {
                return input.read(buffer, offset, length);
            }

            @Override
            public boolean isFinished() {
                return input.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new IllegalStateException("XML-RPC reads requests synchronously");
            }
        };
    }

    private static void sendText(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }

    @Override
    public void destroy() {
        // nothing to release
    }
}
