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

package org.apache.roller.planet.ui.rendering.util;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;


/**
 * Represents a request to a weblog.
 * 
 * This is a fairly generic parsed request which is only trying to figure out
 * the elements of a weblog request which apply to all weblogs.  We try to 
 * determine the weblogHandle, if a locale was specified, and then what extra 
 * path info remains.  The basic format is like this ...
 * 
 * /<planetHandle>[/extra/path/info]
 * 
 * All weblog urls require a weblogHandle, so we ensure that part of the url is
 * properly specified.  locale is always optional, so we do our best to see
 * if a locale is specified.  and path info is always optional.
 *
 * NOTE: this class purposely exposes a getPathInfo() method which provides the
 * path info specified by the request that has not been parsed by this
 * particular class.  this makes it relatively easy for subclasses to extend
 * this class and simply pick up where it left off in the parsing process.
 */
public class PlanetRequest extends ParsedRequest {
    
    private static Log log = LogFactory.getLog(PlanetRequest.class);
    
    // lightweight attributes
    private String planetHandle = null;
    protected String pathInfo = null;
    
    // heavyweight attributes
    private PlanetData planet = null;
    
    
    public PlanetRequest() {}
    
    
    public PlanetRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // let our parent take care of their business first
        super(request);
        
        String path = request.getPathInfo();
        
        log.debug("parsing path "+path);
        
        // first, cleanup extra slashes and extract the planet handle
        if(path != null && path.trim().length() > 1) {
            
            // strip off the leading slash
            path = path.substring(1);
            
            // strip off trailing slash if needed
            if(path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            
            String[] pathElements = path.split("/", 2);
            if(pathElements[0].trim().length() > 0) {
                this.planetHandle = pathElements[0];
            } else {
                // no planetHandle in path info
                throw new InvalidRequestException("not a planet request, "+
                        request.getRequestURL());
            }
            
            // if there is more left of the path info then hold onto it
            if(pathElements.length == 2) {
                pathInfo = pathElements[1];
            } else {
                pathInfo = null;
            }
        }
        
        if(log.isDebugEnabled()) {
            log.debug("planetHandle = "+this.planetHandle);
            log.debug("pathInfo = "+this.pathInfo);
        }
    }
    
    
    public String getPlanetHandle() {
        return planetHandle;
    }

    public void setPlanetHandle(String planetHandle) {
        this.planetHandle = planetHandle;
    }

    public PlanetData getPlanet() {
        
        if(planet == null && planetHandle != null) {
            try {
                PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
                planet = mgr.getPlanet(planetHandle);
            } catch (RollerException ex) {
                log.error("Error looking up planet "+planetHandle, ex);
            }
        }
        
        return planet;
    }

    public void setPlanet(PlanetData planet) {
        this.planet = planet;
    }
    
    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }
    
}
