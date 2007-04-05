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

package org.apache.roller.ui.rendering.servlets;

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
import org.apache.roller.RollerException;
import org.apache.roller.business.FileManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.ThemeResource;
import org.apache.roller.pojos.WeblogTheme;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.rendering.util.ModDateHeaderUtil;
import org.apache.roller.ui.rendering.util.WeblogPreviewResourceRequest;


/**
 * Special previewing servlet which serves files uploaded by users as well as 
 * static resources in shared themes.  This servlet differs from the normal
 * ResourceServlet because it can accept urls parameters which affect how it
 * behaves which are used for previewing.
 *
 * @web.servlet name="PreviewResourceServlet" load-on-startup="9"
 * @web.servlet-mapping url-pattern="/roller-ui/authoring/previewresource/*"
 */
public class PreviewResourceServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(PreviewResourceServlet.class);
    
    private ServletContext context = null;


    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        log.info("Initializing PreviewResourceServlet");
        
        this.context = config.getServletContext();
    }


    /**
     * Handles requests for user uploaded resources.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        WebsiteData weblog = null;
        String context = request.getContextPath();
        String servlet = request.getServletPath();
        String reqURI = request.getRequestURI();
        
        WeblogPreviewResourceRequest resourceRequest = null;
        try {
            // parse the incoming request and extract the relevant data
            resourceRequest = new WeblogPreviewResourceRequest(request);

            weblog = resourceRequest.getWeblog();
            if(weblog == null) {
                throw new RollerException("unable to lookup weblog: "+
                        resourceRequest.getWeblogHandle());
            }

        } catch(Exception e) {
            // invalid resource request or weblog doesn't exist
            log.debug("error creating weblog resource request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        log.debug("Resource requested ["+resourceRequest.getResourcePath()+"]");
        
        long resourceLastMod = 0;
        InputStream resourceStream = null;
        
        // first, see if we have a preview theme to operate from
        if(resourceRequest.getThemeName() != null) {
            Theme theme = resourceRequest.getTheme();
            ThemeResource resource = theme.getResource(resourceRequest.getResourcePath());
            if(resource != null) {
                resourceLastMod = resource.getLastModified();
                resourceStream = resource.getInputStream();
            }
        }
        
        // second, see if resource comes from weblog's configured shared theme
        if(resourceStream == null) {
            try {
                WeblogTheme weblogTheme = weblog.getTheme();
                if(weblogTheme != null) {
                    ThemeResource resource = weblogTheme.getResource(resourceRequest.getResourcePath());
                    if(resource != null) {
                        resourceLastMod = resource.getLastModified();
                        resourceStream = resource.getInputStream();
                    }
                }
            } catch (Exception ex) {
                // hmmm, some kind of error getting theme.  that's an error.
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }
        
        // if not from theme then see if resource is in weblog's upload dir
        if(resourceStream == null) {
            try {
                FileManager fileMgr = RollerFactory.getRoller().getFileManager();
                ThemeResource resource = fileMgr.getFile(weblog, 
                        resourceRequest.getResourcePath());
                resourceLastMod = resource.getLastModified();
                resourceStream = resource.getInputStream();
            } catch (Exception ex) {
                // still not found? then we don't have it, 404.
                log.debug("Unable to get resource", ex);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        
        // Respond with 304 Not Modified if it is not modified.
        if (ModDateHeaderUtil.respondIfNotModified(request, response, resourceLastMod)) {
            return;
        } else {
            // set last-modified date
            ModDateHeaderUtil.setLastModifiedHeader(response, resourceLastMod);
        }
        

        // set the content type based on whatever is in our web.xml mime defs
        response.setContentType(this.context.getMimeType(resourceRequest.getResourcePath()));
        
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
