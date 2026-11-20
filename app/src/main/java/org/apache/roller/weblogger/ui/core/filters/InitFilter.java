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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;

/**
 * A special initialization filter which records a few pieces of information
 * about the environment we are running in.
 *
 * The relative context url is taken from the first request. The absolute url
 * of each request is made available to WebloggerRuntimeConfig for the length
 * of that request only, so it can stand in for site.absoluteurl while that
 * property is unset. It is never stored beyond the request, because it comes
 * from the client's Host header.
 * 
 * @web.filter name="InitFilter"
 */
public class InitFilter implements Filter {

    private static final Log log = LogFactory.getLog(InitFilter.class);

    private static final UrlValidator URL_VALIDATOR = new UrlValidator(
            new String[]{"http", "https"},
            UrlValidator.ALLOW_LOCAL_URLS); // for integration tests

    private volatile boolean initialized = false;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String absPath = getAbsoluteUrl(request);

        if (!initialized && absPath != null) {

            // first request, lets do our initialization
            String relPath = request.getContextPath();
            WebloggerRuntimeConfig.setRelativeContextURL(relPath);

            if (log.isDebugEnabled()) {
                log.debug("relPath = " + relPath);
            }

            this.initialized = true;
        }

        WebloggerRuntimeConfig.setRequestAbsoluteContextURL(absPath);
        try {
            chain.doFilter(req, res);
        } finally {
            WebloggerRuntimeConfig.clearRequestAbsoluteContextURL();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    /**
     * The absolute url of the application as seen by this request: scheme,
     * host, port (when not the scheme's default) and context path. Returns
     * null if the request url is not a valid http or https url.
     */
    public static String getAbsoluteUrl(HttpServletRequest request) {

        if (!URL_VALIDATOR.isValid(request.getRequestURL().toString())) {
            return null;
        }

        String scheme = request.getScheme().toLowerCase(Locale.ROOT);
        int port = request.getServerPort();

        StringBuilder url = new StringBuilder(scheme)
                .append("://").append(request.getServerName());
        boolean defaultPort = ("http".equals(scheme) && port == 80)
                || ("https".equals(scheme) && port == 443);
        if (port > 0 && !defaultPort) {
            url.append(':').append(port);
        }
        url.append(request.getContextPath());

        // make certain that we don't end with a /
        return removeTrailingSlash(url.toString());
    }

    protected static String removeTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
