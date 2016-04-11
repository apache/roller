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
package org.apache.roller.weblogger.ui.rendering.requests;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a request for a Planet Roller feed
 *
 * currently ... /planetrss
 */
public class PlanetRequest extends ParsedRequest {
    private static Log log = LogFactory.getLog(PlanetRequest.class);

    private String planet = null;
    private String flavor = null;

    /**
     * Construct the PlanetRequest by parsing the incoming url
     */
    public PlanetRequest(HttpServletRequest request) {
        super(request);
        
        // parse the request object and figure out what we've got
        log.debug("parsing url: " + request.getRequestURL());
        
        String servlet = request.getServletPath();
        
        // what servlet is our destination?
        if (servlet != null) {
            // strip off the leading slash
            servlet = servlet.substring(1);
            
            if ("planetrss".equals(servlet)) {
                this.flavor = "rss";
            } else {
                // not a request to a feed servlet
                throw new IllegalArgumentException("Not a planet request: " + request.getRequestURL());
            }
        } else {
            throw new IllegalArgumentException("Not a planet request: " + request.getRequestURL());
        }
        
        // planet to include
        if (request.getParameter("planet") != null) {
            this.planet = request.getParameter("planet");
        }
    }
    
    public String getFlavor() {
        return flavor;
    }
    
    public String getPlanet() {
        return planet;
    }    
}
