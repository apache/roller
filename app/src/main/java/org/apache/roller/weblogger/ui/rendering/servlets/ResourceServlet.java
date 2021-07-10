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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.ui.rendering.util.ModDateHeaderUtil;
import org.apache.roller.weblogger.ui.rendering.util.WeblogResourceRequest;

/**
 * Serves fixed-path files such as old-style uploads and theme resources, which
 * must exist at a fixed-path even if moved in media file folders.
 */
public class ResourceServlet extends HttpServlet {

    private static final long serialVersionUID = 1350679411381917714L;

    private static final Log log = LogFactory.getLog(ResourceServlet.class);

    private ServletContext context = null;

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        log.info("Initializing ResourceServlet");

        this.context = config.getServletContext();
    }

    /**
     * Handles requests for user uploaded resources.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Weblog weblog;
        //String ctx = request.getContextPath();
        //String servlet = request.getServletPath();
        //String reqURI = request.getRequestURI();

        WeblogResourceRequest resourceRequest;
        try {
            // parse the incoming request and extract the relevant data
            resourceRequest = new WeblogResourceRequest(request);

            weblog = resourceRequest.getWeblog();
            if (weblog == null) {
                throw new WebloggerException("unable to lookup weblog: "
                        + resourceRequest.getWeblogHandle());
            }

        } catch (Exception e) {
            // invalid resource request or weblog doesn't exist
            if (!response.isCommitted()) {
                response.reset();
            }
            log.debug("error creating weblog resource request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        log.debug("Resource requested [" + resourceRequest.getResourcePath()
                + "]");

        long resourceLastMod = 0;
        InputStream resourceStream = null;

        // first see if resource comes from weblog's shared theme
        try {
            WeblogTheme weblogTheme = weblog.getTheme();
            if (weblogTheme != null) {
                ThemeResource resource = weblogTheme
                        .getResource(resourceRequest.getResourcePath());
                if (resource != null) {
                    resourceLastMod = resource.getLastModified();
                    resourceStream = resource.getInputStream();
                }
            }
        } catch (Exception ex) {
            // hmmm, some kind of error getting theme. that's an error.
            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            if(resourceStream != null) {
                resourceStream.close();
            }
            return;
        }

        // if not from theme then see if resource is in weblog's upload dir
        if (resourceStream == null) {
            try {
                MediaFileManager mmgr = WebloggerFactory.getWeblogger()
                        .getMediaFileManager();
                MediaFile mf = mmgr.getMediaFileByOriginalPath(weblog,
                        resourceRequest.getResourcePath());
                resourceLastMod = mf.getLastModified();
                resourceStream = mf.getInputStream();

            } catch (Exception ex) {
                // still not found? then we don't have it, 404.
                if (!response.isCommitted()) {
                    response.reset();
                }
                log.debug("Unable to get resource", ex);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                if(resourceStream != null) {
                    resourceStream.close();
                }
                return;
            }
        }

        // Respond with 304 Not Modified if it is not modified.
        if (ModDateHeaderUtil.respondIfNotModified(request, response,
                resourceLastMod, resourceRequest.getDeviceType())) {
            return;
        } else {
            // set last-modified date
            ModDateHeaderUtil.setLastModifiedHeader(response, resourceLastMod,
                    resourceRequest.getDeviceType());
        }

        // set the content type based on whatever is in our web.xml mime defs
        response.setContentType(this.context.getMimeType(resourceRequest
                .getResourcePath()));

        try {
            // ok, lets serve up the file
            resourceStream.transferTo(response.getOutputStream());

        } catch (IOException ex) {
            if (!response.isCommitted()) {
                response.reset();
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            // make sure stream to resource file is closed
            resourceStream.close();
        }

    }

}
