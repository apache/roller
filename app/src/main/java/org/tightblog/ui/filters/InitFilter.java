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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.ui.filters;

import org.tightblog.business.WebloggerStaticConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A special initialization filter which ensures that we have an opportunity to
 * extract a few pieces of information about the environment we are running in
 * when the first request is sent.
 */
public class InitFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(InitFilter.class);

    private boolean initialized = false;

    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        if (!initialized) {

            // first request, lets do our initialization
            HttpServletRequest request = (HttpServletRequest) req;
            // HttpServletResponse response = (HttpServletResponse) res;

            // determine absolute and relative url paths to the app
            String relPath = request.getContextPath();
            String absPath = this.getAbsoluteUrl(request);

            // set them in our config
            WebloggerStaticConfig.setAbsoluteContextURL(absPath);
            WebloggerStaticConfig.setRelativeContextURL(relPath);

            log.debug("relPath = {}", relPath);
            log.debug("absPath = {}", absPath);
            this.initialized = true;
        }

        chain.doFilter(req, res);
    }

    private String getAbsoluteUrl(HttpServletRequest request) {
        return getAbsoluteUrl(request.getServerName(), request.getContextPath(),
                request.getRequestURI(), request.getRequestURL().toString());
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }

    /**
     * This method determines the root URL for the installation used for constructing links.
     *
     * Algorithm:
     * 1.) If site.absoluteurl property defined in tightblog-custom.properties, return that.
     * 2.) Check if a port number explicitly given in the URL such as foo:8080 or bar:8443,
     *     making use of protocol-relative URLs largely unusable for switching between http and https
     *     so create an absolute URL based on requestURLString.
     * 3.) If no port number in URL, use protocol-relative URLs ("//foo") allowing for either
     *     http or https usage as configured in the web.xml.
     */
    static String getAbsoluteUrl(String serverName, String contextPath, String requestURI, String requestURLString) {

        // Use override site.absoluteurl property if defined
        String definedAbsoluteURL = WebloggerStaticConfig.getProperty("site.absoluteurl", "");

        if (definedAbsoluteURL.length() > 0) {
            return definedAbsoluteURL;
        }

        String fullUrl = requestURLString;

        // See if port number present
        try {
            URL urlPortTest = new URL(requestURLString);

            if (urlPortTest.getPort() < 0) {
                // no port provided, so strip out the http: or https: to switch to a protocol-relative URL (//)
                int schemeDelimiter = fullUrl.indexOf("//");
                fullUrl = fullUrl.substring(schemeDelimiter);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot derive URL from request URL string: " + requestURLString);
        }

        // if the uri is only "/" then we are basically done
        if ("/".equals(requestURI)) {
            log.debug("requestURI is only '/'. fullUrl: {}", fullUrl);
            return removeTrailingSlash(fullUrl);
        }

        String url;

        // find first "/" starting after hostname is specified
        int index = fullUrl.indexOf('/', fullUrl.indexOf(serverName));

        if (index != -1) {
            // extract just the part leading up to uri
            url = fullUrl.substring(0, index);
        } else {
            url = fullUrl.trim();
        }

        // then just add on the context path
        url += contextPath;

        // make certain that we don't end with a /
        return removeTrailingSlash(url);
    }

    static String removeTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
