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

package org.apache.roller.weblogger.ui.struts2.ajax;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;

/**
 * Return list of users matching a startsWith strings. <br />
 * Accepts request params (none required):<br />
 * startsWith: string to be matched against username and email address<br />
 * enabled: true include only enabled users (default: no restriction<br />
 * offset: offset into results (for paging)<br />
 * length: number of users to return (max is 50)<br />
 * <br />
 * List format:<br />
 * username0, emailaddress0 <br/>
 * username1, emailaddress1 <br/>
 * username2, emailaddress2 <br/>
 * usernameN, emailaddressN <br/>
 * 
 * web.xml: <url-pattern>/roller-ui/authoring/userdata/*</url-pattern>
 * security.xml: <intercept-url pattern="/roller-ui/authoring/**"
 * access="admin,editor"/>
 */
public class UserDataServlet extends HttpServlet {

    private static final long serialVersionUID = -7596671919118637768L;
    private static final int MAX_LENGTH = 50;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean admin = false;

        // This user check can be removed as we protected by spring (see above).
        WeblogRequest weblogRequest = null;
        try {
            weblogRequest = new WeblogRequest(request);

            // Make sure we have the correct authority
            User user = weblogRequest.getUser();
            if (user == null) {
                // user not found
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            } else if (user.hasGlobalPermission("admin")) {
                // admin
                admin = true;
            }

        } catch (Exception e) {
            // some kind of error just return
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String startsWith = request.getParameter("startsWith");
        Boolean enabledOnly = null;
        int offset = 0;
        int length = MAX_LENGTH;
        if ("true".equals(request.getParameter("enabled"))) {
            enabledOnly = Boolean.TRUE;
        }
        if ("false".equals(request.getParameter("enabled"))) {
            enabledOnly = Boolean.FALSE;
        }
        try {
            offset = Integer.parseInt(request.getParameter("offset"));
        } catch (Exception ignored) {
        }
        try {
            length = Integer.parseInt(request.getParameter("length"));
        } catch (Exception ignored) {
        }

        Weblogger roller = WebloggerFactory.getWeblogger();
        try {
            UserManager umgr = roller.getUserManager();
            List<User> users = umgr.getUsersStartingWith(startsWith,
                    enabledOnly, offset, length);
            for (User user : users) {
                response.getWriter().print(user.getUserName());
                if (admin) {
                    response.getWriter().print(",");
                    response.getWriter().println(user.getEmailAddress());
                } else{
                    response.getWriter().print(",");
                    response.getWriter().println(user.getScreenName());
                }
            }
            response.flushBuffer();
        } catch (WebloggerException e) {
            throw new ServletException(e.getMessage());
        }
    }

}
