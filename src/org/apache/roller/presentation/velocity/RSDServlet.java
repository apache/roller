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
 * RSDServlet.java
 *
 * Created on December 14, 2005, 6:08 PM
 */

package org.apache.roller.presentation.velocity;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.servlet.VelocityServlet;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.presentation.InvalidRequestException;
import org.apache.roller.presentation.RollerContext;
import org.apache.roller.presentation.WeblogRequest;


/**
 * Generates simple rsd feeds for a given weblog.
 *
 *
 * @web.servlet name="RSDServlet" load-on-startup="0"
 *
 * @web.servlet-init-param name="org.apache.velocity.properties" 
 * 		                  value="/WEB-INF/velocity.properties"
 *  
 * @web.servlet-mapping url-pattern="/rsd/*"
 *
 * @author Allen Gilliland
 */
public class RSDServlet extends VelocityServlet {
    
    private static Log mLogger = LogFactory.getLog(RSDServlet.class);
    
    
    /**
     * Process a request for a Weblog page.
     */
    public Template handleRequest(HttpServletRequest request,
                                HttpServletResponse response, 
                                Context ctx) 
            throws IOException {
        
        Template template = null;
        WeblogRequest weblogRequest = null;
        WebsiteData weblog = null;
        
        // first off lets parse the incoming request and validate it
        try {
            weblogRequest = new WeblogRequest(request);
            
            // now make sure the specified weblog really exists
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            weblog = userMgr.getWebsiteByHandle(weblogRequest.getWeblogHandle(), Boolean.TRUE);
            
        } catch(InvalidRequestException ire) {
            // An error initializing the request is considered to be a 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("DisplayException", ire);
            mLogger.error("Bad Request: "+ire.getMessage());
            
            return null;
            
        } catch(RollerException re) {
            // error looking up the weblog, we assume it doesn't exist
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("DisplayException", re);
            mLogger.warn("Unable to lookup weblog ["+
                    weblogRequest.getWeblogHandle()+"] "+re.getMessage());
            
            return null;
        }
        
        
        // request appears to be valid, lets render
        try {
            
            // setup context
            ctx.put("website", weblog);
            
            RollerContext rollerContext = new RollerContext();
            ctx.put("absBaseURL", rollerContext.getAbsoluteContextUrl(request));
            
            // lookup our rsd template
            template = getTemplate("/flavors/rsd.vm");
            
            // make sure response content type is properly set
            response.setContentType("application/rsd+xml");
            
        } catch(ResourceNotFoundException rnfe ) {
            
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("DisplayException", rnfe);
            mLogger.warn("ResourceNotFound: "+ request.getRequestURL());
            mLogger.debug(rnfe);
        } catch(Exception e) {
            
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("DisplayException", e);
            mLogger.error("Unexpected exception", e);
        }
        
        return template;
    }
    
}
