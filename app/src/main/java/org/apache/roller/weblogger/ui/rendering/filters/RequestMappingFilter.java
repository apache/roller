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
package org.apache.roller.weblogger.ui.rendering.filters;

import java.io.IOException;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.processors.CommentProcessor;
import org.apache.roller.weblogger.ui.rendering.processors.FeedProcessor;
import org.apache.roller.weblogger.ui.rendering.processors.IncomingTrackbackProcessor;
import org.apache.roller.weblogger.ui.rendering.processors.MediaResourceProcessor;
import org.apache.roller.weblogger.ui.rendering.processors.PageProcessor;
import org.apache.roller.weblogger.ui.rendering.processors.SearchProcessor;

/**
 * Handles weblog specific URLs for the form /<weblog handle>/*
 * Re-forwards requests to the appropriate processor based on the URL.
 */
public class RequestMappingFilter implements Filter {

    private static Log log = LogFactory.getLog(RequestMappingFilter.class);

    // url patterns that are not allowed to be considered weblog handles
    Set<String> restrictedUrls;

    private WeblogManager weblogManager;

    public void setRestrictedUrls(Set<String> restrictedUrls) {
        this.restrictedUrls = restrictedUrls;
    }

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    public void init(FilterConfig filterConfig) {}

    public void destroy() {}

    /**
     * Inspect incoming urls and see if they should be routed.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!handleRequest(request, response)) {
            // nobody handled the request, so let it continue as usual
            chain.doFilter(request, response);
        }
    }

    protected boolean handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // kinda silly, but we need to keep track of whether or not the url had
        // a trailing slash so that we can act accordingly
        boolean trailingSlash = false;

        String weblogHandle = null;
        String weblogRequestContext = null;
        String weblogRequestData = null;

        log.debug("evaluating ["+request.getRequestURI()+"]");

        // figure out potential weblog handle
        String servlet = request.getRequestURI();
        String pathInfo = null;

        if (servlet != null && servlet.trim().length() > 1) {
            if(request.getContextPath() != null) {
                servlet = servlet.substring(request.getContextPath().length());
            }

            if (servlet.length() == 0) {
                // rely on defined front-page blog
                return false;
            } else {
                // strip off the leading slash
                servlet = servlet.substring(1);
            }

            // strip off trailing slash if needed
            if(servlet.endsWith("/")) {
                servlet = servlet.substring(0, servlet.length() - 1);
                trailingSlash = true;
            }

            if(servlet.indexOf('/') != -1) {
                weblogHandle = servlet.substring(0, servlet.indexOf('/'));
                pathInfo = servlet.substring(servlet.indexOf('/')+1);
            } else {
                weblogHandle = servlet;
            }
        }

        log.debug("potential weblog handle = " + weblogHandle);

        // check if it's a valid weblog handle
        if(restrictedUrls.contains(weblogHandle) || !this.isWeblog(weblogHandle)) {
            log.debug("SKIPPED " + weblogHandle);
            return false;
        }

        // parse the rest of the url and build forward url
        if(pathInfo != null) {
            // parse the next portion of the url, we expect <context>/<extra>/<info>
            String[] urlPath = pathInfo.split("/", 2);

            weblogRequestContext = urlPath[0];

            // last part of request is extra path info
            if(urlPath.length == 2) {
                weblogRequestData = urlPath[1];
            }
        }

        // special handling for trailing slash issue
        // we need this because by http standards the urls /foo and /foo/ are
        // supposed to be considered different, so we must enforce that
        if(weblogRequestContext == null && !trailingSlash) {
            // this means someone referred to a weblog index page with the
            // shortest form of url /<weblog> or /<weblog>/<locale> and we need
            // to do a redirect to /<weblog>/ or /<weblog>/<locale>/
            String redirectUrl = request.getRequestURI() + "/";
            if(request.getQueryString() != null) {
                redirectUrl += "?"+request.getQueryString();
            }
            response.sendRedirect(redirectUrl);
            return true;
        } else if(weblogRequestContext != null && "tags".equals(weblogRequestContext)) {
            // tags section can have an index page at /<weblog>/tags/ and
            // a tags query at /<weblog>/tags/tag1+tag2, but that's it
            if((weblogRequestData == null && !trailingSlash) ||
                    (weblogRequestData != null && trailingSlash)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return true;
            }
        } else if(weblogRequestContext != null && trailingSlash) {
            // this means that someone has accessed a weblog url and included
            // a trailing slash, like /<weblog>/entry/<anchor>/ which is not
            // supported, so we need to offer up a 404 Not Found
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return true;
        }

        // calculate forward url
        String forwardUrl = calculateForwardUrl(request, weblogHandle, weblogRequestContext, weblogRequestData);
        if (forwardUrl != null) {
            log.debug("forwarding to " + forwardUrl);
            RequestDispatcher dispatch = request.getRequestDispatcher(forwardUrl);
            dispatch.forward(request, response);
            // we dealt with this request ourselves, so return
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convenience method for caculating the servlet forward url given a set
     * of information to make the decision with.
     *
     * handle is always assumed valid, all other params may be null.
     */
    protected String calculateForwardUrl(HttpServletRequest request, String handle, String context, String data) {
        String forwardUrl = null;

        // POST urls, like comment and trackback servlets
        if ("POST".equals(request.getMethod())) {
            // posting to permalink, this means comment or trackback
            if (context.equals("entry")) {
                // trackback requests are required to have an "excerpt" param
                if (request.getParameter("excerpt") != null) {
                    forwardUrl = generateForwardUrl(IncomingTrackbackProcessor.PATH, handle, context, data);
                    // comment requests are required to have a "content" param
                } else if (request.getParameter("content") != null) {
                    forwardUrl = generateForwardUrl(CommentProcessor.PATH, handle, context, data);
                }
            } else {
                // someone posting data where they aren't supposed to
                return null;
            }
        } else {
            // no context means weblog homepage
            if (context == null || context.equals("page") || context.equals("entry") ||
                    context.equals("date") || context.equals("category") || context.equals("tags")) {
                forwardUrl = generateForwardUrl(PageProcessor.PATH, handle, context, data);
            } else if (context.equals("feed")) {
                forwardUrl = generateForwardUrl(FeedProcessor.PATH, handle, null, data);
            } else if (context.equals("mediaresource")) {
                forwardUrl = generateForwardUrl(MediaResourceProcessor.PATH, handle, null, data);
            } else if (context.equals("search")) {
                forwardUrl = generateForwardUrl(SearchProcessor.PATH, handle, null, null);
            }
        }

        return forwardUrl;
    }

    protected String generateForwardUrl(String processor, String handle, String context, String data) {
        String forwardUrl = processor + "/" + handle;
        if (context != null) {
            forwardUrl += "/" + context;
        }
        if (data != null) {
            forwardUrl += "/" + data;
        }
        return forwardUrl;
    }

    /**
     * Convenience method which determines if the given string is a valid weblog handle.
     */
    protected boolean isWeblog(String potentialHandle) {
        boolean isWeblog = false;
        try {
            Weblog weblog = weblogManager.getWeblogByHandle(potentialHandle);
            if(weblog != null) {
                isWeblog = true;
            }
        } catch(Exception ex) {
            // doesn't really matter to us why it's not a valid website
        }
        return isWeblog;
    }

}
