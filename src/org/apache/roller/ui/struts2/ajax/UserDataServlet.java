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

package org.apache.roller.ui.struts2.ajax;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.roller.RollerException;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.User;


/**
 * Return list of users matching a startsWith strings. <br />
 * Accepts request params (none required):<br />
 *     startsWith: string to be matched against username and email address<br />
 *     enabled: true include only enabled users (default: no restriction<br />
 *     offset: offset into results (for paging)<br />
 *     length: number of users to return (max is 50)<br /><br />
 * List format:<br />
 *     username0, emailaddress0 <br/>
 *     username1, emailaddress1 <br/>
 *     username2, emailaddress2 <br/>
 *     usernameN, emailaddressN <br/>
 */
public class UserDataServlet extends HttpServlet {
    
    private final int MAX_LENGTH = 50;
    
    public void doGet(HttpServletRequest request, 
                      HttpServletResponse response)
            throws ServletException, IOException {
        
        String startsWith = request.getParameter("startsWith");
        Boolean enabledOnly = null;
        int offset = 0;
        int length = MAX_LENGTH;
        if ("true".equals(request.getParameter("enabled"))) enabledOnly = Boolean.TRUE;
        if ("false".equals(request.getParameter("enabled"))) enabledOnly = Boolean.FALSE;
        try { offset = Integer.parseInt(request.getParameter("offset"));
        } catch (Throwable ignored) {}
        try { length = Integer.parseInt(request.getParameter("length"));
        } catch (Throwable ignored) {}
        
        Roller roller = RollerFactory.getRoller();
        try {
            UserManager umgr = roller.getUserManager();
            List users =
                    umgr.getUsersStartingWith(startsWith, enabledOnly, offset, length);
            Iterator userIter = users.iterator();
            while (userIter.hasNext()) {
                User user = (User)userIter.next();
                response.getWriter().print(user.getUserName());
                response.getWriter().print(",");
                response.getWriter().println(user.getEmailAddress());
            }
            response.flushBuffer();
        } catch (RollerException e) {
            throw new ServletException(e.getMessage());
        }
    }
    
}
