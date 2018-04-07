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

import org.springframework.stereotype.Component;
import org.tightblog.rendering.processors.CommentProcessor;
import org.tightblog.rendering.processors.FeedProcessor;
import org.tightblog.rendering.processors.MediaFileProcessor;
import org.tightblog.rendering.processors.PageProcessor;
import org.tightblog.rendering.processors.SearchProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
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
@Component("requestMappingFilter")
public class RequestMappingFilter implements Filter {

    private final static Logger log = LoggerFactory.getLogger(RequestMappingFilter.class);

    private final static Pattern TRAILING_SLASHES = Pattern.compile("/+$");

    @Resource
    private Set<String> invalidWeblogHandles;

    void setInvalidWeblogHandles(Set<String> invalidWeblogHandles) {
        this.invalidWeblogHandles = invalidWeblogHandles;
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
        log.debug("evaluating [{}]", servlet);

        // figure out potential weblog handle
        String pathInfo = null;

        if (servlet != null && servlet.trim().length() > 1) {
            String contextPath = request.getContextPath();
            if (contextPath != null) {
                servlet = servlet.substring(contextPath.length());
            }

            if (servlet.length() == 0) {
                // rely on defined front-page blog
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
        }

        // Skip if weblog handle is actually referring to a static folder under webapp (i.e., not a weblog request)
        if (invalidWeblogHandles.contains(weblogHandle)) {
            log.debug("SKIPPED {}", weblogHandle);
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
            log.debug("forwarding to {}", forwardUrl);
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
                forwardUrl = generateForwardUrl(CommentProcessor.PATH, handle, "entry", data);
            }
        } else {
            // no context means weblog homepage
            if (context == null || context.equals("page") || context.equals("entry") ||
                    context.equals("date") || context.equals("category") || context.equals("tag")) {
                forwardUrl = generateForwardUrl(PageProcessor.PATH, handle, context, data);
            } else if (context.equals("feed")) {
                forwardUrl = generateForwardUrl(FeedProcessor.PATH, handle, null, data);
            } else if (context.equals("mediafile")) {
                forwardUrl = generateForwardUrl(MediaFileProcessor.PATH, handle, null, data);
            } else if (context.equals("search")) {
                forwardUrl = generateForwardUrl(SearchProcessor.PATH, handle, context, null);
            }
        }

        return forwardUrl;
    }

    String generateForwardUrl(String processor, String handle, String context, String data) {
        String forwardUrl = processor + "/" + handle;
        if (context != null) {
            forwardUrl += "/" + context;
        }
        if (data != null) {
            forwardUrl += "/" + data;
        }
        return forwardUrl;
    }
}
