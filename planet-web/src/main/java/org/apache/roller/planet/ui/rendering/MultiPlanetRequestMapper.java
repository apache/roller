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

package org.apache.roller.planet.ui.rendering;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;


/**
 * Multi-planet request mapper.
 *
 * This request mapper is used to map all planet specific urls of the form
 * /<planet handle>/* to the appropriate servlet for handling the actual
 * request.
 */
public class MultiPlanetRequestMapper implements RequestMapper {
    
    private static Log log = LogFactory.getLog(MultiPlanetRequestMapper.class);
    
    private static final String PAGE_SERVLET = "/planet-ui/rendering/page";
    private static final String FEED_SERVLET = "/planet-ui/rendering/feed";
    private static final String OPML_SERVLET = "/planet-ui/rendering/opml";
    
    // url patterns that are not allowed to be considered planet handles
    Set restricted = null;
    
    
    public MultiPlanetRequestMapper() {
        
        this.restricted = new HashSet();
        
        // build roller restricted list
        String restrictList = 
                PlanetConfig.getProperty("rendering.multiPlanetMapper.rollerProtectedUrls");
        if(restrictList != null && restrictList.trim().length() > 0) {
            String[] restrict = restrictList.split(",");
            for(int i=0; i < restrict.length; i++) {
                this.restricted.add(restrict[i]);
            }
        }
        
        // add user restricted list
        restrictList = 
                PlanetConfig.getProperty("rendering.multiPlanetMapper.userProtectedUrls");
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
        
        String planetHandle = null;
        String planetContext = null;
        String groupHandle = null;
        String groupContext = null;
        String extraRequestData = null;
        
        log.debug("evaluating ["+request.getRequestURI()+"]");
        
        // figure out potential planet handle
        String uri = request.getRequestURI();
        String pathInfo = null;
                
        if(uri != null && uri.trim().length() > 1) {
            
            if(request.getContextPath() != null)
                uri = uri.substring(request.getContextPath().length());
            
            // strip off the leading slash
            uri = uri.substring(1);
            
            // strip off trailing slash if needed
            if(uri.endsWith("/")) {
                uri = uri.substring(0, uri.length() - 1);
                trailingSlash = true;
            }
            
            if(uri.indexOf("/") != -1) {
                planetHandle = uri.substring(0, uri.indexOf("/"));
                pathInfo = uri.substring(uri.indexOf("/")+1);
            } else {
                planetHandle = uri;
            }
        }
        
        log.debug("potential planet handle = "+planetHandle);
        
        // check if it's a valid planet handle
        if(restricted.contains(planetHandle) || !this.isPlanet(planetHandle)) {
            log.debug("SKIPPED "+planetHandle);
            return false;
        }
        
        log.debug("PLANET_URL "+request.getServletPath());
        
        // parse the rest of the url
        if(pathInfo != null) {
            
            // parse the next portion of the url
            // we expect <context>/<groupHandle>/<groupContext>/<extra>/<info>
            String[] urlPath = pathInfo.split("/", 4);
            planetContext = urlPath[0];
            
            if(urlPath.length == 2) {
                groupHandle = urlPath[1];
            } else if(urlPath.length == 3) {
                groupHandle = urlPath[1];
                groupContext = urlPath[2];
            } else if(urlPath.length == 4) {
                groupHandle = urlPath[1];
                groupContext = urlPath[2];
                extraRequestData = urlPath[3];
            }
        }
        
        // special handling for trailing slash issue
        // we need this because by http standards the urls /foo and /foo/ are
        // supposed to be considered different, so we must enforce that
        if( (planetContext == null && !trailingSlash) ||
            (groupHandle != null && groupContext == null && !trailingSlash) ) {
            
            // this means someone referred to a planet or group index page 
            // with the shortest form of url /<planet> or /<planet>/group/<group>
            // and we need to add a slash to the url and redirect
            String redirectUrl = request.getRequestURI() + "/";
            if(request.getQueryString() != null) {
                redirectUrl += "?"+request.getQueryString();
            }
            
            response.sendRedirect(redirectUrl);
            return true;
            
        } else if(groupContext != null && trailingSlash) {
            // this means that someone has accessed a url and included a 
            // trailing slash, like /<planet>/group/<group>/feed/atom/ which is
            // not supported, so we need to offer up a 404 Not Found
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return true;
        }
        
        // calculate forward url
        String forwardUrl = calculateForwardUrl(request, planetHandle, 
                planetContext, groupHandle, groupContext, extraRequestData);
        
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
                                       String planetHandle, 
                                       String planetContext, 
                                       String groupHandle, 
                                       String groupContext, 
                                       String data) {
        
        log.debug(planetHandle+","+planetContext+","+groupHandle+","+groupContext+","+data);
        
        StringBuffer forwardUrl = new StringBuffer();
        
        // no context means planet homepage
        if(planetContext == null) {
            forwardUrl.append(PAGE_SERVLET);
            forwardUrl.append("/").append(planetHandle);
            
        // requests for a specific planet group
        } else if(planetContext.equals("group") && groupHandle != null) {
            
            // no group context means group homepage
            if(groupContext == null) {
                forwardUrl.append(PAGE_SERVLET);
                forwardUrl.append("/").append(planetHandle);
                forwardUrl.append("/").append(groupHandle);

            // request for planet group feed
            } else if("feed".equals(groupContext)) {
                forwardUrl.append(FEED_SERVLET);
                forwardUrl.append("/").append(planetHandle);
                forwardUrl.append("/").append(groupHandle);
                if(data != null) {
                    forwardUrl.append("/").append(data);
                }
                
            // request for planet group opml descriptor
            } else if("opml".equals(groupContext)) {
                forwardUrl.append(OPML_SERVLET);
                forwardUrl.append("/").append(planetHandle);
                forwardUrl.append("/").append(groupHandle);
                if(data != null) {
                    forwardUrl.append("/").append(data);
                }
                
            // unsupported planet group url
            } else {
                return null;
            }
            
        // unsupported planet url
        } else {
            return null;
        }
        
        log.debug("FORWARD_URL "+forwardUrl.toString());
        
        return forwardUrl.toString();
    }
    
    
    /**
     * convenience method which determines if the given string is a valid
     * planet handle.
     */
    private boolean isPlanet(String planetHandle) {
        
        log.debug("checking planet handle "+planetHandle);
        
        boolean isPlanet = false;
        
        try {
            PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
            Planet planet = mgr.getPlanet(planetHandle);
            
            if(planet != null) {
                isPlanet = true;
            }
        } catch(Exception ex) {
            // doesn't really matter to us why it's not a valid planet
        }
        
        return isPlanet;
    }
    
}
