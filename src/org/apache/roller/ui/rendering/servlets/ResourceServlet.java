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

import java.io.File;
import java.io.FileInputStream;
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
import org.apache.roller.pojos.ThemeResource;
import org.apache.roller.pojos.WeblogTheme;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.rendering.util.ModDateHeaderUtil;
import org.apache.roller.ui.rendering.util.WeblogResourceRequest;


/**
 * Serves files uploaded by users as well as static resources in shared themes.
 *
 * Since we keep resources in a location outside of the webapp
 * context we need a way to serve them up.  This servlet assumes that
 * resources are stored on a filesystem in the "uploads.dir" directory.
 *
 * @web.servlet name="ResourcesServlet" load-on-startup="5"
 * @web.servlet-mapping url-pattern="/roller-ui/rendering/resources/*"
 */
public class ResourceServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(ResourceServlet.class);
    
    private ServletContext context = null;


    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        log.info("Initializing ResourceServlet");
        
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
        
        WeblogResourceRequest resourceRequest = null;
        try {
            // parse the incoming request and extract the relevant data
            resourceRequest = new WeblogResourceRequest(request);

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
        
        // first see if resource comes from weblog's shared theme
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
            
            // close output stream
            out.close();
            
        } catch (Exception ex) {
            if(!response.isCommitted()) {
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } finally {
            // make sure stream to resource file is closed
            resourceStream.close();
        }

    }

}
