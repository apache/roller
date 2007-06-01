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

package org.apache.roller.weblogger.ui.rendering.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.ui.rendering.util.ModDateHeaderUtil;

/**
 * Special previewing servlet which serves files uploaded by users as well as 
 * static resources in shared themes.  This servlet differs from the normal
 * ResourceServlet because it can accept urls parameters which affect how it
 * behaves which are used for previewing.
 */
public class PreviewThemeImageServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(PreviewThemeImageServlet.class);
    
    private ServletContext context = null;


    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        log.info("Initializing PreviewThemeImageServlet");
        
        this.context = config.getServletContext();
    }


    /**
     * Handles requests for user uploaded resources.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String theme = request.getParameter("theme");
        
        log.debug("Theme requested ["+theme+"]");
        
        long resourceLastMod = 0;
        InputStream resourceStream = null;
        String previewImagePath = null;
        
        // try looking up selected theme
        try {
            ThemeManager tmgr = RollerFactory.getRoller().getThemeManager();
            SharedTheme previewTheme = tmgr.getTheme(theme);
            ThemeResource previewImage = previewTheme.getPreviewImage();
            if(previewImage != null) {
                previewImagePath = previewImage.getPath();
                resourceLastMod = previewImage.getLastModified();
                resourceStream = previewImage.getInputStream();
            }
        } catch (Exception ex) {
            log.debug("error looking up preview image", ex);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // if we don't have a stream to the file then we can't continue
        if(resourceStream == null) {
            log.debug("Unable to get theme preview for theme - "+theme);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Respond with 304 Not Modified if it is not modified.
        if (ModDateHeaderUtil.respondIfNotModified(request, response, resourceLastMod)) {
            return;
        } else {
            // set last-modified date
            ModDateHeaderUtil.setLastModifiedHeader(response, resourceLastMod);
        }
        
        log.debug("Everything is cool, sending image");
        
        // set the content type based on whatever is in our web.xml mime defs
        response.setContentType(this.context.getMimeType(previewImagePath));
        
        OutputStream out = null;
        try {
            // ok, lets serve up the file
            byte[] buf = new byte[8192];
            int length = 0;
            out = response.getOutputStream();
            while((length = resourceStream.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
            
            // cleanup
            out.close();
            resourceStream.close();
            
        } catch (Exception ex) {
            log.error("Error writing resource file", ex);
            if(!response.isCommitted()) {
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

    }

}
