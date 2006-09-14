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

package org.apache.roller.ui.rendering.velocity.deprecated;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.util.URLUtilities;


/**
 * Redirect pre-3.0 urls to new location using 301 redirects.
 *
 * @web.servlet name="RedirectServlet" load-on-startup="9"
 * @web.servlet-mapping url-pattern="/language/*"
 * @web.servlet-mapping url-pattern="/comments/*"
 * @web.servlet-mapping url-pattern="/resources/*"
 * @web.servlet-mapping url-pattern="/rsd/*"
 * @web.servlet-mapping url-pattern="/flavor/*"
 * @web.servlet-mapping url-pattern="/rss/*"
 * @web.servlet-mapping url-pattern="/atom/*"
 * @web.servlet-mapping url-pattern="/page/*"
 * @web.servlet-mapping url-pattern="/search/*"
 * @web.servlet-mapping url-pattern="/xmlrpc/*"
 * @web.servlet-mapping url-pattern="/editor/*"
 * @web.servlet-mapping url-pattern="/admin/*"
 */
public class RedirectServlet extends HttpServlet {
    
    private static Log log = LogFactory.getLog(RedirectServlet.class);
    
    public static final String LanguageServlet = "language";
    public static final String CommentsServlet = "comments";
    public static final String ResourceServlet = "resources";
    public static final String RsdServlet = "rsd";
    public static final String FlavorServlet = "flavor";
    public static final String RssServlet = "rss";
    public static final String AtomServlet = "atom";
    public static final String PageServlet = "page";
    public static final String SearchServlet = "search";
    public static final String XmlrpcServlet = "xmlrpc";
    public static final String EditorUI = "editor";
    public static final String AdminUI = "admin";
    
    
    /**
     * Handle GET requests.
     *
     * All we are doing is calculating the new url for the given resource and
     * sending a 301 redirect to it's new location.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String redirectUrl = null;
        
        // figure out what servlet the request was destined for and parse
        String servlet = request.getServletPath();
        if(servlet != null && servlet.trim().length() > 1) {
            
            // strip off the leading slash
            servlet = servlet.substring(1);
            
            // strip off trailing slash if needed
            if(servlet.endsWith("/")) {
                servlet = servlet.substring(0, servlet.length() - 1);
            }
        } else {
            // bad request, 404
        }
        
        log.debug("uri = "+request.getRequestURI());
        log.debug("path info = "+request.getPathInfo());
        
        
        // language servlet
        if(LanguageServlet.equals(servlet)) {
            redirectUrl = figureLanguageRedirect(request);
            
        // comments servlet
        } else if(CommentsServlet.equals(servlet)) {
            // old comments page was an extension of page servlet
            // so redirects are the same
            redirectUrl = figurePageRedirect(request);
            
        // resource servlet
        } else if(ResourceServlet.equals(servlet)) {
            redirectUrl = figureResourceRedirect(request);
            
        // rsd servlet
        } else if(RsdServlet.equals(servlet)) {
            redirectUrl = figureRsdRedirect(request);
            
        // flavor servlet
        } else if(FlavorServlet.equals(servlet)) {
            redirectUrl = figureFeedRedirect(request);
            
        // rss servlet
        } else if(RssServlet.equals(servlet)) {
            redirectUrl = figureFeedRedirect(request);
            
        // atom servlet
        } else if(AtomServlet.equals(servlet)) {
            redirectUrl = figureFeedRedirect(request);
            
        // page servlet
        } else if(PageServlet.equals(servlet)) {
            redirectUrl = figurePageRedirect(request);
            
        // search servlet
        } else if(SearchServlet.equals(servlet)) {
            redirectUrl = figureSearchRedirect(request);
            
        // xmlrpc servlet
        } else if(XmlrpcServlet.equals(servlet)) {
            redirectUrl = figureXmlrpcRedirect(request);
            
        // editor UI
        } else if(EditorUI.equals(servlet)) {
            redirectUrl = figureEditorRedirect(request);
            
        // admin UI
        } else if(AdminUI.equals(servlet)) {
            redirectUrl = figureAdminRedirect(request);
        }
        
        if(redirectUrl != null) {
            log.debug("redirecting to "+redirectUrl);
            
            // send an HTTP 301 response
            response.setStatus(response.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", redirectUrl);
        } else {
            // no redirect, send 404
            response.sendError(response.SC_NOT_FOUND);
        }
    }
    
    
    // language servlet has no new equivalent, so just redirect to weblog homepage
    private String figureLanguageRedirect(HttpServletRequest request) {
        
        String newUrl = RollerRuntimeConfig.getRelativeContextURL();
        
        String pathInfo = request.getPathInfo();
        if(pathInfo == null) {
            return null;
        } else {
            pathInfo = pathInfo.substring(1);
        }
        
        String[] pathElements = pathInfo.split("/", 2);
        return newUrl+"/"+pathElements[0]+"/";
    }
    
    
    // redirect to new weblog resource location
    private String figureResourceRedirect(HttpServletRequest request) {
        
        String newUrl = RollerRuntimeConfig.getRelativeContextURL();
        
        String pathInfo = request.getPathInfo();
        if(pathInfo == null) {
            return null;
        } else {
            pathInfo = pathInfo.substring(1);
        }
        
        String[] pathElements = pathInfo.split("/", 2);
        if(pathElements.length != 2) {
            return null;
        }
        
        return newUrl+"/"+pathElements[0]+"/resource/"+pathElements[1];
    }
    
    
    // redirect to new weblog rsd location
    private String figureRsdRedirect(HttpServletRequest request) {
        
        String newUrl = RollerRuntimeConfig.getRelativeContextURL();
        
        String pathInfo = request.getPathInfo();
        if(pathInfo == null) {
            return null;
        } else {
            pathInfo = pathInfo.substring(1);
        }
        
        String[] pathElements = pathInfo.split("/", 2);
        return newUrl+"/"+pathElements[0]+"/rsd";
    }
    
    
    // redirect to new weblog feed location
    private String figureFeedRedirect(HttpServletRequest request) {
        
        OldFeedRequest feedRequest = null;
        try {
            // get parsed version of old feed request
            feedRequest = new OldFeedRequest(request);
        } catch (Exception ex) {
            return null;
        }
        
        String weblog = feedRequest.getWeblogHandle();
        if(weblog == null) {
            // must be site-wide feed
            weblog = RollerRuntimeConfig.getProperty("site.frontpage.weblog.handle");
        }
        
        String newUrl = RollerRuntimeConfig.getRelativeContextURL();
        newUrl += "/"+weblog+"/feed/entries/"+feedRequest.getFlavor();
        
        Map params = new HashMap();
        if(feedRequest.getWeblogCategory() != null) {
            params.put("cat", URLUtilities.encode(feedRequest.getWeblogCategory()));
        }
        if(feedRequest.isExcerpts()) {
            params.put("excerpts", "true");
        }
        
        return newUrl + URLUtilities.getQueryString(params);
    }
    
    
    // redirect to new weblog page location
    private String figurePageRedirect(HttpServletRequest request) {
        
        OldPageRequest pageRequest = null;
        try {
            // get parsed version of old page request
            pageRequest = new OldPageRequest(request);
        } catch (Exception ex) {
            return null;
        }
        
        StringBuffer url = new StringBuffer();
        Map params = new HashMap();
        
        url.append(RollerRuntimeConfig.getRelativeContextURL());
        url.append("/").append(pageRequest.getWeblogHandle()).append("/");
        
        if(pageRequest.getWeblogPage() != null && 
                !"Weblog".equals(pageRequest.getWeblogPage())) {
            
            // a custom page name, so they get the new /weblog/page/name url
            url.append("page/").append(pageRequest.getWeblogPage());
            
            // we also allow for params on custom pages
            if(pageRequest.getWeblogDate() != null) {
                params.put("date", pageRequest.getWeblogDate());
            }
            if(pageRequest.getWeblogCategory() != null) {
                params.put("cat", URLUtilities.encode(pageRequest.getWeblogCategory()));
            }
            if(pageRequest.getWeblogAnchor() != null) {
                params.put("entry", URLUtilities.encode(pageRequest.getWeblogAnchor()));
            }
            
        } else if(pageRequest.getWeblogAnchor() != null) {
            
            // permalink url
            url.append("entry/").append(URLUtilities.encode(pageRequest.getWeblogAnchor()));
            
        } else if(pageRequest.getWeblogCategory() != null && pageRequest.getWeblogDate() == null) {
            String cat = pageRequest.getWeblogCategory();
            if(pageRequest.getWeblogCategory().startsWith("/")) {
                cat = pageRequest.getWeblogCategory().substring(1);
            }
            
            url.append("category/").append(URLUtilities.encode(cat));
            
        } else if(pageRequest.getWeblogDate() != null && pageRequest.getWeblogCategory() == null) {
            url.append("date/").append(pageRequest.getWeblogDate());  
            
        } else {
            if(pageRequest.getWeblogDate() != null) {
                params.put("date", pageRequest.getWeblogDate());
            }
            if(pageRequest.getWeblogCategory() != null) {
                params.put("cat", URLUtilities.encode(pageRequest.getWeblogCategory()));
            }
        }
        
        return url.toString() + URLUtilities.getQueryString(params);
    }
    
    
    // redirect to new search servlet
    private String figureSearchRedirect(HttpServletRequest request) {
        
        String newUrl = RollerRuntimeConfig.getRelativeContextURL();
        
        String pathInfo = request.getPathInfo();
        if(pathInfo == null) {
            return null;
        } else {
            pathInfo = pathInfo.substring(1);
        }
        
        String[] pathElements = pathInfo.split("/", 2);
        newUrl += "/"+pathElements[0]+"/search";
        
        // query params
        Map params = new HashMap();
        if(request.getParameter("q") != null && 
                request.getParameter("q").trim().length() > 0) {
            
            params.put("q", request.getParameter("q"));
            
            if(request.getParameter("c") != null && 
                request.getParameter("c").trim().length() > 0) {
                params.put("cat", request.getParameter("c"));
            }
        }
        
        return newUrl + URLUtilities.getQueryString(params);
    }
    
    
    // redirect to new xmlrpc location
    private String figureXmlrpcRedirect(HttpServletRequest request) {
        
        return URLUtilities.getXmlrpcURL(true);
    }
    
    
    // redirect to new editor UI location
    private String figureEditorRedirect(HttpServletRequest request) {
        
        return RollerRuntimeConfig.getRelativeContextURL()+"/roller-ui/";
    }
    
    
    // redirect to new admin UI location
    private String figureAdminRedirect(HttpServletRequest request) {
        
        return RollerRuntimeConfig.getRelativeContextURL()+"/roller-ui/";
    }
    
}
