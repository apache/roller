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
import org.apache.roller.planet.ui.rendering.util.InvalidRequestException;
import org.apache.roller.planet.ui.rendering.util.PlanetRequest;


/**
 * Represents a request for a Roller planet group page.
 * 
 * /planet-ui/rendering/pages/*
 *
 * We use this class as a helper to parse an incoming url and sort out the
 * information embedded in the url for later use.
 */
public class PlanetGroupPageRequest extends PlanetGroupRequest {
    
    private static Log log = LogFactory.getLog(PlanetGroupPageRequest.class);
    
    // lightweight attributes
    
    
    public PlanetGroupPageRequest() {}
    
    
    /**
     * Construct the WeblogFeedRequest by parsing the incoming url
     */
    public PlanetGroupPageRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // let our parent take care of their business first
        // parent determines planet handle
        super(request);
        
        // we only want the path info left over from after our parents parsing
        String pathInfo = this.getPathInfo();
        
        // parse the request object and figure out what we've got
        log.debug("parsing path "+pathInfo);
        
        if(pathInfo != null) {
            throw new InvalidRequestException("not a valid planet group page, "+
                    request.getRequestURL());
        }
    }
    
}
