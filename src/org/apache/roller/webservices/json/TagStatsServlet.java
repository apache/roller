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
package org.apache.roller.webservices.json;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.roller.RollerException;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.TagStat;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.Utilities;

/**
 * Return list of tags matching a startsWith strings. <br />
 * 
 * @web.servlet name="TagStatsServlet" 
 * @web.servlet-mapping url-pattern="/roller-services/json/tags/*"
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 */
public class TagStatsServlet extends HttpServlet {
    
    private final int MAX_LENGTH = 100;
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {    
        
        int limit = MAX_LENGTH;       
        try { 
            limit = Integer.parseInt(request.getParameter("limit"));
        } catch (Throwable ignored) {}
        
        String pathInfo = request.getPathInfo();
        String[] path = new String[0];
        
        if(!StringUtils.isEmpty(pathInfo)) {
            path = StringUtils.split(pathInfo, '/');
        }
                                        
        Roller roller = RollerFactory.getRoller();
        try {
            response.setContentType("text/html; charset=utf-8");
            
            WeblogManager wmgr = roller.getWeblogManager();
            WebsiteData website = null;
            String startsWith = null;            
            
            // website handle is always the first path segment,
            // only throw an exception when not found if we have a tag prefix 
            if(path.length > 0) {
                try {
                    UserManager umgr = RollerFactory.getRoller().getUserManager();
                    website = umgr.getWebsiteByHandle(path[0], Boolean.TRUE);
                    if (website == null && path.length > 1)
                        throw new RollerException();                
                } catch (RollerException ex) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Weblog handle not found.");
                    return;
                }    
            }
            
            if(path.length == 2) {
                startsWith = path[1].trim();
            } else if(path.length == 1 && website == null) {
                startsWith = path[0].trim();
            }
                                    
            List tags = wmgr.getTags(website, null, startsWith, limit);
            
            response.getWriter().println("{");
            response.getWriter().print("  prefix : \"");
            response.getWriter().print(startsWith == null ? "" : startsWith);
            response.getWriter().println("\",");
            response.getWriter().print("  weblog : \"");
            response.getWriter().print(website != null ? website.getHandle() : "");
            response.getWriter().println("\",");            
            response.getWriter().println("  tagcounts : [");
            for(Iterator it = tags.iterator(); it.hasNext();) {
                TagStat stat = (TagStat) it.next();
                response.getWriter().print("    { tag : \"");
                response.getWriter().print(stat.getName());
                response.getWriter().print("\", ");
                response.getWriter().print("count : ");
                response.getWriter().print(stat.getCount());
                response.getWriter().print(" }");
                if(it.hasNext())
                   response.getWriter().println(", ");
            }
            response.getWriter().println("\n  ]\n}");
            
            response.flushBuffer();
        } catch (RollerException e) {
            throw new ServletException(e.getMessage());
        }
    }
}
