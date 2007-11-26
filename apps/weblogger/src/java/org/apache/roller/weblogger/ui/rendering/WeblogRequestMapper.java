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

package org.apache.roller.weblogger.ui.rendering;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Handles rendering requests for Roller pages/feeds by routing to the appropriate Servlet.
 *
 * This request mapper is used to map all weblog specific urls of the form
 * /<weblog handle>/* to the appropriate servlet for handling the actual
 * request.
 *
 * TODO: we should try and make this class easier to extend and build upon
 */
public class WeblogRequestMapper implements RequestMapper {
    
    private static Log log = LogFactory.getLog(WeblogRequestMapper.class);
    
    private static final String PAGE_SERVLET = "/roller-ui/rendering/page";
    private static final String FEED_SERVLET = "/roller-ui/rendering/feed";
    private static final String RESOURCE_SERVLET = "/roller-ui/rendering/resources";
    private static final String SEARCH_SERVLET = "/roller-ui/rendering/search";
    private static final String RSD_SERVLET = "/roller-ui/rendering/rsd";
    
    private static final String COMMENT_SERVLET = "/roller-ui/rendering/comment";
    private static final String TRACKBACK_SERVLET = "/roller-ui/rendering/trackback";
    
    
    // url patterns that are not allowed to be considered weblog handles
    Set restricted = null;
    
    
    public WeblogRequestMapper() {
        
        this.restricted = new HashSet();
        
        // build roller restricted list
        String restrictList = 
                WebloggerConfig.getProperty("rendering.weblogMapper.rollerProtectedUrls");
        if(restrictList != null && restrictList.trim().length() > 0) {
            String[] restrict = restrictList.split(",");
            for(int i=0; i < restrict.length; i++) {
                this.restricted.add(restrict[i]);
            }
        }
        
        // add user restricted list
        restrictList = 
                WebloggerConfig.getProperty("rendering.weblogMapper.userProtectedUrls");
        if(restrictList != null && restrictList.trim().length() > 0) {
            String[] restrict = restrictList.split(",");
            for(int i=0; i < restrict.length; i++) {
                this.restricted.add(restrict[i]);
            }
        }
    }
    
    
    public boolean handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // kinda silly, but we need to keep track of whether or not the url had
        // a trailing slash so that we can act accordingly
        boolean trailingSlash = false;
        
        String weblogHandle = null;
        String weblogLocale = null;
        String weblogRequestContext = null;
        String weblogRequestData = null;
        
        log.debug("evaluating ["+request.getRequestURI()+"]");
        
        // figure out potential weblog handle
        String servlet = request.getRequestURI();
        String pathInfo = null;
                
        if(servlet != null && servlet.trim().length() > 1) {
            
            if(request.getContextPath() != null)
                servlet = servlet.substring(request.getContextPath().length());
            
            // strip off the leading slash
            servlet = servlet.substring(1);
            
            // strip off trailing slash if needed
            if(servlet.endsWith("/")) {
                servlet = servlet.substring(0, servlet.length() - 1);
                trailingSlash = true;
            }
            
            if(servlet.indexOf("/") != -1) {
                weblogHandle = servlet.substring(0, servlet.indexOf("/"));
                pathInfo = servlet.substring(servlet.indexOf("/")+1);
            } else {
                weblogHandle = servlet;
            }
        }
        
        log.debug("potential weblog handle = "+weblogHandle);
        
        // check if it's a valid weblog handle
        if(restricted.contains(weblogHandle) || !this.isWeblog(weblogHandle)) {
            log.debug("SKIPPED "+weblogHandle);
            return false;
        }
        
        log.debug("WEBLOG_URL "+request.getServletPath());
        
        // parse the rest of the url and build forward url
        if(pathInfo != null) {
            
            // parse the next portion of the url
            // we expect [locale/]<context>/<extra>/<info>
            String[] urlPath = pathInfo.split("/", 3);
            
            // if we have a locale, deal with it
            if(this.isLocale(urlPath[0])) {
                weblogLocale = urlPath[0];
                
                // no extra path info specified
                if(urlPath.length == 2) {
                    weblogRequestContext = urlPath[1];
                    weblogRequestData = null;
                    
                // request contains extra path info
                } else if(urlPath.length == 3) {
                    weblogRequestContext = urlPath[1];
                    weblogRequestData = urlPath[2];
                }
            
            // otherwise locale is empty
            } else {
                weblogLocale = null;
                weblogRequestContext = urlPath[0];
                
                // last part of request is extra path info
                if(urlPath.length == 2) {
                    weblogRequestData = urlPath[1];
                    
                // if we didn't have a locale then we have split too much
                // so we reassemble the last 2 path elements together
                } else if(urlPath.length == 3) {
                    weblogRequestData = urlPath[1] + "/" + urlPath[2];
                }
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
            
        } else if(weblogRequestContext != null &&
                "tags".equals(weblogRequestContext)) {
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
        String forwardUrl = calculateForwardUrl(request, weblogHandle, weblogLocale,
                weblogRequestContext, weblogRequestData);
        
        // if we don't have a forward url then the request was invalid somehow
        if(forwardUrl == null) {
            return false;
        }
        
        // dispatch to forward url
        log.debug("forwarding to "+forwardUrl);
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
    private String calculateForwardUrl(HttpServletRequest request,
                                       String handle, String locale,
                                       String context, String data) {
        
        log.debug(handle+","+locale+","+context+","+data);
        
        StringBuffer forwardUrl = new StringBuffer();
        
        // POST urls, like comment and trackback servlets
        if("POST".equals(request.getMethod())) {
            // posting to permalink, this means comment or trackback
            if(context.equals("entry")) {
                // trackback requests are required to have an "excerpt" param
                if(request.getParameter("excerpt") != null) {
                    
                    forwardUrl.append(TRACKBACK_SERVLET);
                    forwardUrl.append("/");
                    forwardUrl.append(handle);
                    if(locale != null) {
                        forwardUrl.append("/");
                        forwardUrl.append(locale);
                    }
                    forwardUrl.append("/");
                    forwardUrl.append(context);
                    if(data != null) {
                        forwardUrl.append("/");
                        forwardUrl.append(data);
                    }
                    
                // comment requests are required to have a "content" param
                } else if(request.getParameter("content") != null) {
                    
                    forwardUrl.append(COMMENT_SERVLET);
                    forwardUrl.append("/");
                    forwardUrl.append(handle);
                    if(locale != null) {
                        forwardUrl.append("/");
                        forwardUrl.append(locale);
                    }
                    forwardUrl.append("/");
                    forwardUrl.append(context);
                    if(data != null) {
                        forwardUrl.append("/");
                        forwardUrl.append(data);
                    }
                }
                
            } else {
                // someone posting data where they aren't supposed to
                return null;
            }
            
        } else {
            // no context means weblog homepage
            if(context == null) {
                
                forwardUrl.append(PAGE_SERVLET);
                forwardUrl.append("/");
                forwardUrl.append(handle);
                if(locale != null) {
                    forwardUrl.append("/");
                    forwardUrl.append(locale);
                }
                
                // requests handled by PageServlet
            } else if(context.equals("page") || context.equals("entry") ||
                    context.equals("date") || context.equals("category")
                    || context.equals("tags")) {
                
                forwardUrl.append(PAGE_SERVLET);
                forwardUrl.append("/");
                forwardUrl.append(handle);
                if(locale != null) {
                    forwardUrl.append("/");
                    forwardUrl.append(locale);
                }
                forwardUrl.append("/");
                forwardUrl.append(context);
                if(data != null) {
                    forwardUrl.append("/");
                    forwardUrl.append(data);
                }
                
                // requests handled by FeedServlet
            } else if(context.equals("feed")) {
                
                forwardUrl.append(FEED_SERVLET);
                forwardUrl.append("/");
                forwardUrl.append(handle);
                if(locale != null) {
                    forwardUrl.append("/");
                    forwardUrl.append(locale);
                }
                if(data != null) {
                    forwardUrl.append("/");
                    forwardUrl.append(data);
                }
                
                // requests handled by ResourceServlet
            } else if(context.equals("resource")) {
                
                forwardUrl.append(RESOURCE_SERVLET);
                forwardUrl.append("/");
                forwardUrl.append(handle);
                if(data != null) {
                    forwardUrl.append("/");
                    forwardUrl.append(data);
                }
                
                // requests handled by SearchServlet
            } else if(context.equals("search")) {
                
                forwardUrl.append(SEARCH_SERVLET);
                forwardUrl.append("/");
                forwardUrl.append(handle);
                
                // requests handled by RSDServlet
            } else if(context.equals("rsd")) {
                
                forwardUrl.append(RSD_SERVLET);
                forwardUrl.append("/");
                forwardUrl.append(handle);
                
                // unsupported url
            } else {
                return null;
            }
        }
        
        log.debug("FORWARD_URL "+forwardUrl.toString());
        
        return forwardUrl.toString();
    }
    
    
    /**
     * convenience method which determines if the given string is a valid
     * weblog handle.
     *
     * TODO 3.0: some kind of caching
     */
    private boolean isWeblog(String potentialHandle) {
        
        log.debug("checking weblog handle "+potentialHandle);
        
        boolean isWeblog = false;
        
        try {
            Weblog weblog = WebloggerFactory.getWeblogger().getWeblogManager()
                    .getWeblogByHandle(potentialHandle);
            
            if(weblog != null) {
                isWeblog = true;
            }
        } catch(Exception ex) {
            // doesn't really matter to us why it's not a valid website
        }
        
        return isWeblog;
    }
    
    
    /**
     * Convenience method which determines if the given string is a valid
     * locale string.
     */
    private boolean isLocale(String potentialLocale) {
        
        boolean isLocale = false;
        
        // we only support 2 or 5 character locale strings, so check that first
        if(potentialLocale != null && 
                (potentialLocale.length() == 2 || potentialLocale.length() == 5)) {
            
            // now make sure that the format is proper ... e.g. "en_US"
            // we are not going to be picky about capitalization
            String[] langCountry = potentialLocale.split("_");
            if(langCountry.length == 1 && 
                    langCountry[0] != null && langCountry[0].length() == 2) {
                isLocale = true;
                
            } else if(langCountry.length == 2 && 
                    langCountry[0] != null && langCountry[0].length() == 2 && 
                    langCountry[1] != null && langCountry[1].length() == 2) {
                
                isLocale = true;
            }
        }
        
        return isLocale;
    }
    
}
