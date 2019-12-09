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
package org.tightblog.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tightblog.rendering.controller.CommentController;
import org.tightblog.rendering.controller.FeedController;
import org.tightblog.rendering.controller.ExternalSourceController;
import org.tightblog.rendering.controller.MediaFileController;
import org.tightblog.rendering.controller.PageController;
import org.tightblog.rendering.controller.SearchController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Handles weblog specific URLs for the form /<weblog handle>/*
 * Re-forwards requests to the appropriate processor based on the URL.
 */
@Component
public class RequestMappingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestMappingFilter.class);

    private static final Pattern TRAILING_SLASHES = Pattern.compile("/+$");

    @Value("#{'${invalid.weblog.handles}'.split(',')}")
    private Set<String> invalidWeblogHandles;

    void setInvalidWeblogHandles(Set<String> invalidWeblogHandles) {
        if (invalidWeblogHandles != null) {
            this.invalidWeblogHandles = invalidWeblogHandles;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    /**
     * Inspect incoming urls and see if they should be routed.
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!handleRequest(request, response)) {
            // nobody handled the request, so let it continue as usual
            chain.doFilter(request, response);
        }
    }

    boolean handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String weblogHandle = null;
        String weblogRequestContext = null;
        String weblogRequestData = null;

        // remove all training slashes from URI
        String servlet = TRAILING_SLASHES.matcher(request.getRequestURI()).replaceAll("");
        LOG.debug("evaluating [{}]", servlet);

        // figure out potential weblog handle
        String pathInfo = null;

        /* Following if block checks if the root URL for the web application is being accessed, if so
           returns false to activate the welcome-file-list in the web.xml (which is used to forward to
           the default front-page blog defined for the installation.)

           In determining whether the root URL is being accessed, cases where the application is the
           default one for the servlet container (e.g., https://www.example.com/) and where it is not
           (e.g., https://www.example.com/tightblog) are both covered.
        */
        if (servlet != null) {
            if (servlet.trim().length() > 1) {
                String contextPath = request.getContextPath();
                if (contextPath != null) {
                    servlet = servlet.substring(contextPath.length());
                }

                if (servlet.length() == 0) {
                    // Application default one for servlet container, continue to default blog
                    return false;
                } else {
                    // strip off the leading slash
                    servlet = servlet.substring(1);
                }

                if (servlet.indexOf('/') != -1) {
                    weblogHandle = servlet.substring(0, servlet.indexOf('/'));
                    pathInfo = servlet.substring(servlet.indexOf('/') + 1);
                } else {
                    weblogHandle = servlet;
                }
            } else {
                // Application is default one for servlet container, continue to default blog
                return false;
            }
        }

        // Skip if weblog handle is actually referring to a static folder under webapp (i.e., not a weblog request)
        final String test = weblogHandle;
        if (test != null && invalidWeblogHandles != null && invalidWeblogHandles.stream().anyMatch(test::equalsIgnoreCase)) {
            LOG.debug("SKIPPED {}", weblogHandle);
            return false;
        }

        // parse the rest of the url and build forward url
        if (pathInfo != null) {
            // parse the next portion of the url, we expect <context>[/<extra>]
            // examples:  entry/my-blog-article, page/mycss.css, category/sports, date/201802, search
            String[] urlPath = pathInfo.split("/", 2);

            weblogRequestContext = urlPath[0];

            if (urlPath.length == 2) {
                weblogRequestData = urlPath[1];
            }
        }

        // calculate forward url
        String forwardUrl = calculateForwardUrl(request, weblogHandle, weblogRequestContext, weblogRequestData);
        if (forwardUrl != null) {
            LOG.debug("forwarding to {}", forwardUrl);
            RequestDispatcher dispatch = request.getRequestDispatcher(forwardUrl);
            dispatch.forward(request, response);
            // we dealt with this request ourselves, so return
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convenience method for calculating the servlet forward url given a set
     * of information to make the decision with.
     * <p>
     * handle is always assumed valid, all other params may be null.
     */
    String calculateForwardUrl(HttpServletRequest request, String handle, String context, String data) {
        String forwardUrl = null;

        // POST url is presently just for commenting
        if ("POST".equals(request.getMethod())) {
            // posting to permalink, should mean comment (which must have a content param)
            if (context.equals("entrycomment") && request.getParameter("content") != null) {
                forwardUrl = generateForwardUrl(CommentController.PATH, handle, "entry", data);
            }
        } else {
            // null context means weblog homepage
            if (context == null || context.equals("page") || context.equals("entry") ||
                    context.equals("date") || context.equals("category") || context.equals("tag")) {
                forwardUrl = generateForwardUrl(PageController.PATH, handle, context, data);
            } else if (context.equals("feed")) {
                forwardUrl = generateForwardUrl(FeedController.PATH, handle, null, data);
            } else if (context.equals("mediafile")) {
                forwardUrl = generateForwardUrl(MediaFileController.PATH, handle, null, data);
            } else if (context.equals("search")) {
                forwardUrl = generateForwardUrl(SearchController.PATH, handle, null, null);
            } else if (context.equals("external")) {
                forwardUrl = generateForwardUrl(ExternalSourceController.PATH, null, null, data);
            }
        }

        return forwardUrl;
    }

    String generateForwardUrl(String processor, String handle, String context, String data) {
        String forwardUrl = processor + (handle != null ? ("/" + handle) : "");
        if (context != null) {
            forwardUrl += "/" + context;
        }
        if (data != null) {
            forwardUrl += "/" + data;
        }
        return forwardUrl;
    }
}
