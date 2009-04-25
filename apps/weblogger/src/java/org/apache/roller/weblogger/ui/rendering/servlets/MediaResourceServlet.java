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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.ui.rendering.util.ModDateHeaderUtil;


/**
 * Serves media files uploaded by users.
 *
 * Since we keep resources in a location outside of the webapp
 * context we need a way to serve them up.  This servlet assumes that
 * resources are stored on a filesystem in the "uploads.dir" directory.
 *
 * @web.servlet name="MediaResourceServlet" load-on-startup="5"
 * @web.servlet-mapping url-pattern="/roller-ui/rendering/media-resources/*"
 */
public class MediaResourceServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(MediaResourceServlet.class);
    
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        log.info("Initializing ResourceServlet");
        
    }


    /**
     * Handles requests for user uploaded media file resources.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        MediaFileManager mfMgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        String fileId = request.getPathInfo();
        
        log.debug("parsing file id -  " + fileId);
        
        // first, cleanup extra slashes and extract the weblog weblogHandle
        if(fileId != null && fileId.trim().length() > 1) {
            
            // strip off the leading slash
        	fileId = fileId.substring(1);
            
            // strip off trailing slash if needed
            if(fileId.endsWith("/")) {
            	fileId = fileId.substring(0, fileId.length() - 1);
            }
        }

        log.debug("File requested [" + fileId + "]");
    
        long resourceLastMod = 0;
        InputStream resourceStream = null;
        MediaFile mediaFile = null;
        
        try {
            mediaFile = mfMgr.getMediaFile(fileId, true);
            resourceLastMod = mediaFile.getLastModified();
        } catch (Exception ex) {
            // still not found? then we don't have it, 404.
            log.debug("Unable to get resource", ex);
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
        

        // set the content type based on whatever is in our web.xml mime defs
        response.setContentType(mediaFile.getContentType());
        resourceStream = mediaFile.getInputStream();
        
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
