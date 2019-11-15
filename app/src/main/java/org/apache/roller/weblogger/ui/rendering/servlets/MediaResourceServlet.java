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
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.util.ModDateHeaderUtil;
import org.apache.roller.weblogger.ui.rendering.util.WeblogMediaResourceRequest;

/**
 * Serves media files uploaded by users.
 * 
 * Since we keep resources in a location outside of the webapp context we need a
 * way to serve them up. This servlet assumes that resources are stored on a
 * filesystem in the "uploads.dir" directory.
 */
public class MediaResourceServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(MediaResourceServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        log.info("Initializing ResourceServlet");

    }

    /**
     * Handles requests for user uploaded media file resources.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        MediaFileManager mfMgr = WebloggerFactory.getWeblogger()
                .getMediaFileManager();

        Weblog weblog;

        WeblogMediaResourceRequest resourceRequest;
        try {
            // parse the incoming request and extract the relevant data
            resourceRequest = new WeblogMediaResourceRequest(request);

            weblog = resourceRequest.getWeblog();
            if (weblog == null) {
                throw new WebloggerException("unable to lookup weblog: "
                        + resourceRequest.getWeblogHandle());
            }

        } catch (Exception e) {
            // invalid resource request or weblog doesn't exist
            log.debug("error creating weblog resource request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long resourceLastMod;
        InputStream resourceStream = null;
        MediaFile mediaFile;

        try {
            mediaFile = mfMgr.getMediaFile(resourceRequest.getResourceId(),
                    true);
            resourceLastMod = mediaFile.getLastModified();

        } catch (Exception ex) {
            // still not found? then we don't have it, 404.
            log.debug("Unable to get resource", ex);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
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
        if (resourceRequest.isThumbnail()) {
            response.setContentType("image/png");
            try {
                resourceStream = mediaFile.getThumbnailInputStream();
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "ERROR loading thumbnail for " + mediaFile.getId(),
                            e);
                } else {
                    log.warn("ERROR loading thumbnail for " + mediaFile.getId());
                }
            }
        }

        if (resourceStream == null) {
            response.setContentType(mediaFile.getContentType());
            resourceStream = mediaFile.getInputStream();
        }

        OutputStream out;
        try {
            // ok, lets serve up the file
            byte[] buf = new byte[RollerConstants.EIGHT_KB_IN_BYTES];
            int length;
            out = response.getOutputStream();
            while ((length = resourceStream.read(buf)) > 0) {
                out.write(buf, 0, length);
            }

            // close output stream
            out.close();

        } catch (Exception ex) {
            log.error("ERROR", ex);
            if (!response.isCommitted()) {
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } finally {
            // make sure stream to resource file is closed
            resourceStream.close();
        }

    }

}
