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

package org.apache.roller.ui.rendering.velocity;

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.util.Utilities;


/**
 * Planet Roller (i.e. NOT for Planet Tool) RSS feed.
 *
 * @web.servlet name="PlanetFeedServlet"
 * @web.servlet-mapping url-pattern="/planetrss/*"
 */
public class PlanetFeedServlet extends VelocityServlet {
    
    private static Log mLogger = LogFactory.getLog(RollerRequest.class);
    
    
    public Template handleRequest(HttpServletRequest request,
            HttpServletResponse response, Context context) {
        
        RollerRequest rreq = null;
        try {
            rreq = RollerRequest.getRollerRequest(request, getServletContext());
        } catch (RollerException e) {
            // An error initializing the request is considered to be a 404
            if (mLogger.isDebugEnabled()) {
                mLogger.debug("RollerRequest threw Exception", e);
            }
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e1) {
                if (mLogger.isDebugEnabled()) {
                    mLogger.debug("IOException sending error", e);
                }
            }
            return null;
        }
        try {
            response.setContentType("application/rss+xml;charset=utf-8");
            PlanetManager planet =
                    RollerFactory.getRoller().getPlanetManager();
            if (request.getParameter("group") != null) {
                context.put("group",
                        planet.getGroup(request.getParameter("group")));
            }
            context.put("planet", planet);
            context.put("date", new Date());
            context.put("utilities", new Utilities());
            
            int entryCount =
                    RollerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries");
            int maxEntries =
                    RollerRuntimeConfig.getIntProperty("site.newsfeeds.maxEntries");
            String sCount = request.getParameter("count");
            if (sCount!=null) {
                try {
                    entryCount = Integer.parseInt(sCount);
                } catch (NumberFormatException e) {
                    mLogger.warn("Improperly formatted count parameter");
                }
                if ( entryCount > maxEntries ) entryCount = maxEntries;
                if ( entryCount < 0 ) entryCount = 0;
            }
            context.put("entryCount", new Integer(entryCount));
            
            return getTemplate("templates/planet/planetrss.vm");
        } catch (Exception e) {
            mLogger.error("ERROR in PlanetFeedServlet", e);
        }
        return null;
    }
    
    
    /**
     * Handle error in Velocity processing.
     */
    protected void error( HttpServletRequest req, HttpServletResponse res,
            Exception e) throws ServletException, IOException {
        mLogger.warn("ERROR in PlanetFeedServlet",e);
    }
    
}
