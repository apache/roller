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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.processors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogRequest;
import org.apache.roller.weblogger.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves media files uploaded by users.
 *
 * Since we keep resources in a location outside of the webapp context we need a
 * way to serve them up.
 */
@RestController
@RequestMapping(path="/tb-ui/rendering/media-resources/**")
public class MediaResourceProcessor {

    private static Log log = LogFactory.getLog(MediaResourceProcessor.class);

    public static final String PATH = "/tb-ui/rendering/media-resources";

    @Autowired
    private MediaFileManager mediaFileManager;

    public void setMediaFileManager(MediaFileManager mediaFileManager) {
        this.mediaFileManager = mediaFileManager;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing MediaResourceProcessor...");
    }

    @RequestMapping(method = { RequestMethod.GET, RequestMethod.HEAD })
        public void getMediaResource(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Weblog weblog;

        WeblogRequest resourceRequest;
        String resourceId;
        boolean thumbnail = false;

        try {
            // parse the incoming request and extract the relevant data
            resourceRequest = new WeblogRequest(request);

            weblog = resourceRequest.getWeblog();
            if (weblog == null) {
                throw new WebloggerException("unable to lookup weblog: " + resourceRequest.getWeblogHandle());
            }

            // we want only the path info left over from after the weblog parsing
            String pathInfo = resourceRequest.getPathInfo();

            // parse the request object and figure out what we've got
            log.debug("parsing path " + pathInfo);

            // any id is okay...
            if (pathInfo != null && pathInfo.trim().length() > 1) {
                resourceId = pathInfo;
                if (pathInfo.startsWith("/")) {
                    resourceId = pathInfo.substring(1);
                }
            } else {
                throw new IllegalArgumentException("Invalid resource path info: " + request.getRequestURL());
            }

            if ("true".equals(request.getParameter("t"))) {
                thumbnail = true;
            }

            if (log.isDebugEnabled()) {
                log.debug("resourceId = " + resourceId + ", thumbnail = " + thumbnail);
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
            mediaFile = mediaFileManager.getMediaFile(resourceId, true);
            resourceLastMod = mediaFile.getLastModified();
        } catch (Exception ex) {
            // Not found? then we don't have it, 404.
            log.debug("Unable to get resource", ex);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Respond with 304 Not Modified if it is not modified.
        if (Utilities.respondIfNotModified(request, response, resourceLastMod, resourceRequest.getDeviceType())) {
            return;
        } else {
            // set last-modified date
            Utilities.setLastModifiedHeader(response, resourceLastMod, resourceRequest.getDeviceType());
        }

        // set the content type based on whatever is in our web.xml mime defs
        if (thumbnail) {
            response.setContentType("image/png");
            try {
                resourceStream = mediaFile.getThumbnailInputStream();
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("ERROR loading thumbnail for " + mediaFile.getId(), e);
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
            byte[] buf = new byte[WebloggerCommon.EIGHT_KB_IN_BYTES];
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
