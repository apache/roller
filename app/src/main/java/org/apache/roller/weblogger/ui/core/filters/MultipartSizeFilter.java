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

import java.io.IOException;
import java.util.Locale;

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
 * Limits the size of multipart requests to the Roller UI, ahead of the Struts
 * filter that parses them. The limit follows the site's
 * <code>uploads.file.maxsize</code> setting, so raising it in Site Settings
 * also raises this limit: room for five files of that size (the media upload
 * form has five file fields) plus 1 MiB for the other form fields.
 */
public class MultipartSizeFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(MultipartSizeFilter.class);
    private static final long MIB = 1024L * 1024L;
    static final int FILE_FIELDS = 5;
    private static final long FORM_BYTES = MIB;
    private static final double DEFAULT_FILE_MEGABYTES = 2.0;

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to configure
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getContentType();
        if (contentType == null
                || !contentType.toLowerCase(Locale.ROOT).startsWith("multipart/")) {
            chain.doFilter(request, response);
            return;
        }

        long limit = maxRequestBytes();
        if (httpRequest.getContentLengthLong() > limit) {
            LOG.warn("Refusing a multipart request of " + httpRequest.getContentLengthLong()
                    + " bytes; the limit is " + limit + " bytes (uploads.file.maxsize)");
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }

        // the declared length may be absent, so count what is actually read
        ServletInputStream input = new LimitedInputStream(httpRequest.getInputStream(), limit);
        chain.doFilter(new HttpServletRequestWrapper(httpRequest) {
            @Override
            public ServletInputStream getInputStream() {
                return input;
            }
        }, response);
    }

    /** @return the largest multipart request the current upload setting allows */
    static long maxRequestBytes() {
        double megabytes = DEFAULT_FILE_MEGABYTES;
        String value = WebloggerRuntimeConfig.getProperty("uploads.file.maxsize");
        if (value != null) {
            try {
                double configured = Double.parseDouble(value.trim());
                if (configured > 0) {
                    megabytes = configured;
                }
            } catch (NumberFormatException e) {
                LOG.debug("uploads.file.maxsize is not a number, using the default");
            }
        }
        return (long) Math.ceil(megabytes * MIB) * FILE_FIELDS + FORM_BYTES;
    }

    @Override
    public void destroy() {
        // nothing to release
    }

    /** Fails a read once more than the limit has been read. */
    private static final class LimitedInputStream extends ServletInputStream {

        private final ServletInputStream input;
        private final long limit;
        private long count = 0;

        LimitedInputStream(ServletInputStream input, long limit) {
            this.input = input;
            this.limit = limit;
        }

        private void counted(long n) throws IOException {
            if (n > 0) {
                count += n;
                if (count > limit) {
                    throw new IOException("Multipart request is larger than " + limit + " bytes");
                }
            }
        }

        @Override
        public int read() throws IOException {
            int b = input.read();
            if (b >= 0) {
                counted(1);
            }
            return b;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int n = input.read(buffer, offset, length);
            counted(n);
            return n;
        }

        @Override
        public boolean isFinished() {
            return input.isFinished();
        }

        @Override
        public boolean isReady() {
            return input.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            input.setReadListener(listener);
        }
    }
}
