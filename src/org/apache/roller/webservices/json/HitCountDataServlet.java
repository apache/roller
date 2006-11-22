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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.roller.RollerException;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.WebsiteData;


/**
 * Returns hit count data for a single weblog.
 * 
 * @web.servlet name="HitCountDataServlet" 
 * @web.servlet-mapping url-pattern="/roller-services/json/hitcountdata"
 */
public class HitCountDataServlet extends HttpServlet {
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {    
        
        Roller roller = RollerFactory.getRoller();
        try {
            UserManager umgr = roller.getUserManager();
            WebsiteData w = umgr.getWebsiteByHandle(request.getParameter("weblog"));
            String json = "{ handle: \"" + w.getHandle() + "\"," + "count: \"" + w.getTodaysHits() + "\" }"; 
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().print(json);   
            response.flushBuffer();
            response.getWriter().flush();
            response.getWriter().close();
        } catch (RollerException e) {
            throw new ServletException(e.getMessage());
        }
    }
}
