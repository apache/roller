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

package org.apache.roller.weblogger.ui.struts2.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.roller.weblogger.util.URLUtilities;


/**
 * Handles redirects for old struts1 urls.
 */
public class StrutsRedirectServlet extends HttpServlet {
    
    // only handle GET requests
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        String redirectUrl = null;
        
        String servlet = request.getServletPath();
        if(servlet != null && "/roller-ui/authoring/commentManagement.do".equals(servlet)) {
            // redirect to new comment management action
            Map<String, String> params = new HashMap();
            params.put("weblog", request.getParameter("weblog"));
            params.put("bean.entryId", request.getParameter("entryId"));
            redirectUrl = URLUtilities.getActionURL("comments", "/roller-ui/authoring", null, params, true);
            
        } else if(servlet != null && "/roller-ui/yourWebsites.do".equals(servlet)) {
            // redirect to new main menu action
            redirectUrl = URLUtilities.getActionURL("menu", "/roller-ui", null, null, true);
        }
        
        if(redirectUrl != null) {
            response.setStatus(response.SC_MOVED_PERMANENTLY);
            response.setHeader("Location", redirectUrl);
        } else {
            response.sendError(response.SC_NOT_FOUND);
        }
    }
    
}
