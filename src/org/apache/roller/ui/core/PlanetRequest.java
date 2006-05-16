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
/*
 * PlanetRequest.java
 *
 * Created on December 12, 2005, 9:47 AM
 */

package org.apache.roller.ui.core;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Represents a request for a Planet Roller url.
 *
 * currently ... /planet.do and /planetrss
 *
 * @author Allen Gilliland
 */
public class PlanetRequest extends ParsedRequest {
    
    private static Log mLogger = LogFactory.getLog(PlanetRequest.class);
    
    private String context = null;
    private String type = null;
    private String flavor = null;
    private boolean excerpts = false;
    
    
    /**
     * Construct the PlanetRequest by parsing the incoming url
     */
    public PlanetRequest(HttpServletRequest request) throws InvalidRequestException {
        
        super(request);
        
        // parse the request object and figure out what we've got
        mLogger.debug("parsing url "+request.getRequestURL());
        
        String servlet = request.getServletPath();
        
        // what servlet is our destination?
        if(servlet != null) {
            // strip off the leading slash
            servlet = servlet.substring(1);
            
            if(servlet.equals("planet.do")) {
                this.context = "planet";
                this.type = "page";
            } else if(servlet.equals("planetrss")) {
                this.context = "planet";
                this.type = "feed";
                this.flavor = "rss";
            } else {
                // not a request to a feed servlet
                throw new InvalidRequestException("not a planet request, "+request.getRequestURL());
            }
            
        } else {
            throw new InvalidRequestException("not a planet request, "+request.getRequestURL());
        }
        
        
        /* 
         * parse request parameters
         *
         * the only params we currently care about are:
         *   excerpts - specifies the feed should only include excerpts
         *
         */
        if(request.getParameter("excerpts") != null) {
            this.excerpts = Boolean.valueOf(request.getParameter("excerpts")).booleanValue();
        }
        
    }

    
    public String getContext() {
        return context;
    }

    public String getType() {
        return type;
    }

    public String getFlavor() {
        return flavor;
    }

    public boolean isExcerpts() {
        return excerpts;
    }
    
}
