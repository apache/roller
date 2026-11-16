/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.roller.weblogger.ui.struts2.ajax;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.business.themes.ThemeNotFoundException;
import org.apache.roller.weblogger.pojos.ThemeResource;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Return theme information (id, name, description, relative path to thumbnail)
 * in JSON format.  Usage:
 * <ul>
 * <li>/authoring/themedata - get array of all shared theme data</li>
 * <li>/authoring/themedata?theme=xxx - get data for specific theme</li>
 * </ul>
 */
public class ThemeDataServlet extends HttpServlet {

    private static final ObjectMapper JSON = new ObjectMapper();

    private record ThemeData(
            String id, String name, String description, String previewPath) {
    }

    private static ThemeData dataFor(SharedTheme theme) {
        String id = theme.getId();
        ThemeResource preview = theme.getPreviewImage();
        String previewPath = preview == null
                ? null
                : "/themes/" + id + "/" + preview.getPath();

        return new ThemeData(
                id, theme.getName(), theme.getDescription(), previewPath);
    }

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

        String themeId = request.getParameter("theme");

        ThemeManager themeMgr = WebloggerFactory.getWeblogger().getThemeManager();
        Object payload;
        if (themeId == null) {
            payload = themeMgr.getEnabledThemesList().stream()
                    .map(ThemeDataServlet::dataFor)
                    .toList();
        } else {
            try {
                payload = dataFor(themeMgr.getTheme(themeId));
            } catch (ThemeNotFoundException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such theme");
                return;
            } catch (WebloggerException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ERROR fetching theme data");
                return;
            }
        }

        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(JSON.writeValueAsString(payload));
        response.flushBuffer();
    }
}
