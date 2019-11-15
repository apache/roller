/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Return theme information (id, name, description, relative path to thumbnail)
 * in JSON format.  Usage:
 * <ul>
 * <li>/authoring/themedata - get array of all shared theme data</li>
 * <li>/authoring/themedata?theme=xxx - get data for specific theme</li>
 * </ul>
 */
public class ThemeDataServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void doGet(
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<SharedTheme> themes;
        String themeId;

        themeId = request.getParameter("theme");

        ThemeManager themeMgr = WebloggerFactory.getWeblogger().getThemeManager();
        if (themeId == null) {
            themes = themeMgr.getEnabledThemesList();
        } else {
            themes = new ArrayList<SharedTheme>(1);
            try {
                SharedTheme theme = themeMgr.getTheme(themeId);
                themes.add(theme);
            } catch (WebloggerException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ERROR fetching theme data");
                return;
            }
        }

        response.setContentType("application/json; charset=utf-8");
        PrintWriter pw = response.getWriter();
        if (themeId == null) {
            pw.println("[" );
        }
        for (Iterator it = themes.iterator(); it.hasNext();) {
            SharedTheme theme = (SharedTheme) it.next();
            pw.print("    { \"id\" : \"");
            pw.print(theme.getId());
            pw.print("\", ");
            pw.print("\"name\" : \"");
            pw.print(theme.getName());
            pw.print("\", ");
            pw.print("\"description\" : \"");
            pw.print(theme.getDescription());
            pw.print("\", ");
            pw.print("\"previewPath\" : \"");
            pw.print("/themes" + "/" + theme.getId() + "/" + theme.getPreviewImage().getPath());
            pw.print("\" }");
            if (it.hasNext()) {
                pw.println(", ");
            }
        }
        if (themeId == null) {
            pw.println("]" );
        }
        response.flushBuffer();
    }
}
