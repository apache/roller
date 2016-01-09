/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.ui.rendering;

import java.io.IOException;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Handles rendering requests for Roller pages/feeds by routing to the appropriate Servlet.
 *
 * This request mapper is used to map all weblog specific urls of the form
 * /<weblog handle>/* to the appropriate processor for handling the actual request.
 */
public class WeblogRequestMapper implements RequestMapper {
    
    private static Log log = LogFactory.getLog(WeblogRequestMapper.class);
    
    public static final String PAGE_PROCESSOR = "/roller-ui/rendering/page";
    public static final String COMMENT_PROCESSOR = "/roller-ui/rendering/comment";
    public static final String TRACKBACK_PROCESSOR = "/roller-ui/rendering/trackback";
    public static final String FEED_PROCESSOR = "/roller-ui/rendering/feed";
    public static final String MEDIA_PROCESSOR = "/roller-ui/rendering/media-resources";
    public static final String SEARCH_PROCESSOR = "/roller-ui/rendering/search";

    // url patterns that are not allowed to be considered weblog handles
    Set restricted;
    
    public WeblogRequestMapper(Set restrictedUrls) {
        restricted = restrictedUrls;
    }

    public boolean handleRequest(HttpServletRequest request, HttpServletResponse response)
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

        if(servlet != null && servlet.trim().length() > 1) {
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
        if(restricted.contains(weblogHandle) || !this.isWeblog(weblogHandle)) {
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
            // a tags query at /<weblog>/tags/tag1+tag2, buth that's it
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
        
        // if we don't have a forward url then the request was invalid somehow
        if (forwardUrl == null) {
            return false;
        }
        
        // dispatch to forward url
        log.debug("forwarding to " + forwardUrl);
        RequestDispatcher dispatch = request.getRequestDispatcher(forwardUrl);
        dispatch.forward(request, response);
        
        // we dealt with this request ourselves, so return "true"
        return true;
    }

    
    /**
     * Convenience method for caculating the servlet forward url given a set
     * of information to make the decision with.
     *
     * handle is always assumed valid, all other params may be null.
     */
    private String calculateForwardUrl(HttpServletRequest request, String handle, String context, String data) {
        String forwardUrl = null;
        
        // POST urls, like comment and trackback servlets
        if ("POST".equals(request.getMethod())) {
            // posting to permalink, this means comment or trackback
            if (context.equals("entry")) {
                // trackback requests are required to have an "excerpt" param
                if (request.getParameter("excerpt") != null) {
                    forwardUrl = generateForwardUrl(TRACKBACK_PROCESSOR, handle, context, data);
                // comment requests are required to have a "content" param
                } else if (request.getParameter("content") != null) {
                    forwardUrl = generateForwardUrl(COMMENT_PROCESSOR, handle, context, data);
                }
            } else {
                // someone posting data where they aren't supposed to
                return null;
            }
        } else {
            // no context means weblog homepage
            if (context == null || context.equals("page") || context.equals("entry") ||
                    context.equals("date") || context.equals("category") || context.equals("tags")) {
                forwardUrl = generateForwardUrl(PAGE_PROCESSOR, handle, context, data);
            } else if (context.equals("feed")) {
                forwardUrl = generateForwardUrl(FEED_PROCESSOR, handle, null, data);
            } else if (context.equals("mediaresource")) {
                forwardUrl = generateForwardUrl(MEDIA_PROCESSOR, handle, null, data);
            } else if (context.equals("search")) {
                forwardUrl = generateForwardUrl(SEARCH_PROCESSOR, handle, null, null);
            }
        }

        return forwardUrl;
    }
    
    private String generateForwardUrl(String processor, String handle, String context, String data) {
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
    private boolean isWeblog(String potentialHandle) {
        boolean isWeblog = false;
        try {
            Weblog weblog = WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(potentialHandle);
            if(weblog != null) {
                isWeblog = true;
            }
        } catch(Exception ex) {
            // doesn't really matter to us why it's not a valid website
        }
        return isWeblog;
    }
}
