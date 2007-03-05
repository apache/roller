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
import org.apache.roller.planet.pojos.PlanetGroupData;


/**
 * Represents a request to a planet group.
 * 
 * /<planetHandle>/group/<groupHandle>[/extra/path/info]
 *
 */
public class PlanetGroupRequest extends PlanetRequest {
    
    private static Log log = LogFactory.getLog(PlanetGroupRequest.class);
    
    // lightweight attributes
    private String groupHandle = null;
    
    // heavyweight attributes
    private PlanetGroupData group = null;
    
    
    public PlanetGroupRequest() {}
    
    
    public PlanetGroupRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // let our parent take care of their business first
        super(request);
        
        String myPathInfo = this.getPathInfo();
        
        log.debug("parsing path "+myPathInfo);
        
        /* 
         * parse the path info.  must look like this ...
         *
         * <groupHandle>[/extra/info]
         */
        if(myPathInfo != null && myPathInfo.trim().length() > 1) {
            
            String[] urlPath = myPathInfo.split("/", 2);
            this.groupHandle = urlPath[0];
            this.pathInfo = null;
            
            if(urlPath.length == 2) {
                this.pathInfo = urlPath[1];
            }
            
        }
        
        if(log.isDebugEnabled()) {
            log.debug("groupHandle = "+this.groupHandle);
            log.debug("pathInfo = "+this.pathInfo);
        }
    }
    
    
    public String getGroupHandle() {
        return groupHandle;
    }

    public void setGroupHandle(String groupHandle) {
        this.groupHandle = groupHandle;
    }

    public PlanetGroupData getGroup() {
        
        if(group == null && groupHandle != null) {
            try {
                PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
                group = mgr.getGroup(getPlanet(), groupHandle);
            } catch (RollerException ex) {
                log.error("Error looking up group "+groupHandle, ex);
            }
        }
        
        return group;
    }

    public void setGroup(PlanetGroupData group) {
        this.group = group;
    }
    
}
