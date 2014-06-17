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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.Theme;

/**
 * Returns a string with the theme description
 * 
 * web.xml: <url-pattern>/roller-ui/authoring/themedata/*</url-pattern>
 * security.xml: <intercept-url pattern="/roller-ui/authoring/**"
 * access="admin,editor"/>
 */
public class ThemeDescriptionServlet extends HttpServlet {

    private static final long serialVersionUID = -200891465040607014L;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String theTheme = request.getParameter("theme");
        if (StringUtils.isNotEmpty(theTheme)) {
            try {
                ThemeManager themeMgr = WebloggerFactory.getWeblogger()
                        .getThemeManager();
                Theme theme = themeMgr.getTheme(theTheme);
                if (theme != null) {
                    response.getWriter().print(theme.getDescription());
                    response.flushBuffer();
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (WebloggerException e) {
                throw new ServletException(e.getMessage());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
