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

package org.apache.roller.ui.rendering;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.WebsiteData;


/**
 * Roller's weblog request mapper.
 *
 * This request mapper is used to map all weblog specific urls of the form
 * /<weblog handle>/* to the appropriate servlet for handling the actual
 * request.
 */
public class WeblogRequestMapper implements RequestMapper {
    
    private static Log log = LogFactory.getLog(WeblogRequestMapper.class);
    
    // url patterns that are not allowed to be considered weblog handles
    Set restricted = null;
    
    
    public WeblogRequestMapper() {
        
        this.restricted = new HashSet();
        
        // build restricted list
        String restrictList = RollerConfig.getProperty("weblogurls.restricted");
        if(restrictList != null && restrictList.trim().length() > 0) {
            String[] restrict = restrictList.split(",");
            for(int i=0; i < restrict.length; i++) {
                this.restricted.add(restrict[i]);
            }
        }
    }
    
    
    public boolean handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String weblogHandle = null;
        String weblogLocale = null;
        String weblogRequestContext = null;
        String weblogRequestData = null;
        
        log.debug("evaluating ["+request.getRequestURI()+"]");
        
        // figure out potential weblog handle
        String servlet = request.getServletPath();
        String pathInfo = null;
        if(servlet != null && servlet.trim().length() > 1) {
            
            // strip off the leading slash
            servlet = servlet.substring(1);
            
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
            return false;
        }
        
        log.debug("WEBLOG_URL "+request.getRequestURI());
        
        // parse the rest of the url and build forward url
        if(pathInfo != null) {
            // parse the next portion of the url
            // we expect [locale]/<context>/<extra>/<info>
            String[] urlPath = pathInfo.split("/", 3);
            
            // if we have a locale, deal with it
            if(urlPath[0].indexOf("_") != -1) {
                weblogLocale = urlPath[0];
                
                if(urlPath.length == 2) {
                    weblogRequestContext = urlPath[1];
                    weblogRequestData = null;
                } else if(urlPath.length == 3) {
                    weblogRequestContext = urlPath[1];
                    weblogRequestData = urlPath[2];
                }
            
            // otherwise locale is empty
            } else {
                weblogLocale = null;
                weblogRequestContext = urlPath[0];
                
                // if we didn't have a locale then we split too much
                if(urlPath.length > 2) {
                    weblogRequestData = urlPath[1] + "/" + urlPath[2];
                } else {
                    weblogRequestData = urlPath[1];
                }
            }
            
        }
        
        // calculate forward url
        String forwardUrl = calculateForwardUrl(weblogHandle, weblogLocale,
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

    
    private String calculateForwardUrl(String handle, String locale,
                                       String context, String data) {
        
        String forwardUrl = null;
        
        // no context means weblog homepage
        if(context == null) {
            
            forwardUrl = "/page/"+handle;
            
        // requests handled by PageServlet
        } else if(context.equals("page") || context.equals("entry") ||
                context.equals("date") || context.equals("category")) {
            
            forwardUrl = "/page/"+handle;
            
            if(locale != null) {
                forwardUrl += "/"+locale;
            }
            
            forwardUrl += "/"+context+"/"+data;
            
        // requests handled by FeedServlet
        } else if(context.equals("feed")) {
            
            forwardUrl = "/feed/"+handle;
            
            if(locale != null) {
                forwardUrl += "/"+locale;
            }
            
            forwardUrl += "/"+context+"/"+data;
            
        // requests handled by ResourceServlet
        } else if(context.equals("resource")) {
            
            forwardUrl = "/resource/"+handle+"/"+data;
            
        // requests handled by SearchServlet
        } else if(context.equals("search")) {
            
            forwardUrl = "/search/"+handle;
        }
        
        return forwardUrl;
    }
    
    
    /**
     * convenience method which determines if the given string is a valid
     * weblog handle.
     *
     * TODO 3.0: some kind of caching
     */
    private boolean isWeblog(String potentialHandle) {
        
        boolean isWeblog = false;
        
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            WebsiteData weblog = mgr.getWebsiteByHandle(potentialHandle);
            
            if(weblog != null) {
                isWeblog = true;
            }
        } catch(Exception ex) {
            // doesn't really matter to us why it's not a valid website
        }
        
        return isWeblog;
    }
    
}
