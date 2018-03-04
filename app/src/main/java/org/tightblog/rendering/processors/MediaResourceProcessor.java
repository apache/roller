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
package org.tightblog.rendering.processors;

import org.tightblog.business.MediaFileManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.MediaFile;
import org.tightblog.pojos.Weblog;
import org.tightblog.rendering.requests.WeblogRequest;
import org.tightblog.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

/**
 * Serves media files uploaded by users.
 * <p>
 * Since we keep resources in a location outside of the webapp context we need a
 * way to serve them up.
 */
@RestController
@RequestMapping(path = "/tb-ui/rendering/media-resources/**")
public class MediaResourceProcessor extends AbstractProcessor {

    private static Logger log = LoggerFactory.getLogger(MediaResourceProcessor.class);

    public static final String PATH = "/tb-ui/rendering/media-resources";

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private MediaFileManager mediaFileManager;

    public void setMediaFileManager(MediaFileManager mediaFileManager) {
        this.mediaFileManager = mediaFileManager;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing MediaResourceProcessor...");
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public void getMediaResource(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Weblog weblog;

        WeblogRequest resourceRequest;
        String resourceId;
        boolean thumbnail = false;

        try {
            // parse the incoming request and extract the relevant data
            resourceRequest = new WeblogRequest(request);

            weblog = weblogManager.getWeblogByHandle(resourceRequest.getWeblogHandle(), true);
            if (weblog == null) {
                throw new IllegalArgumentException("unable to lookup weblog: " + resourceRequest.getWeblogHandle());
            } else {
                resourceRequest.setWeblog(weblog);
            }

            // we want only the path info left over from after the weblog parsing
            String pathInfo = resourceRequest.getPathInfo();

            // parse the request object and figure out what we've got
            log.debug("parsing path {}", pathInfo);

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

            log.debug("resourceId = {}, thumbnail = {}", resourceId, thumbnail);
        } catch (Exception e) {
            // invalid resource request or weblog doesn't exist
            log.debug("error creating weblog resource request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Instant resourceLastMod;
        MediaFile mediaFile;

        try {
            mediaFile = mediaFileManager.getMediaFile(resourceId, true);
            resourceLastMod = mediaFile.getLastUpdated();
        } catch (Exception ex) {
            // Not found? then we don't have it, 404.
            log.debug("Unable to get resource", ex);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Respond with 304 Not Modified if it is not modified.
        if (respondIfNotModified(request, response, resourceLastMod, resourceRequest.getDeviceType())) {
            return;
        } else {
            // set last-modified date
            setLastModifiedHeader(response, resourceLastMod, resourceRequest.getDeviceType());
        }

        try (InputStream resourceStream = getInputStream(mediaFile, thumbnail)) {
            response.setContentType(thumbnail ? "image/png" : mediaFile.getContentType());

            byte[] buf = new byte[Utilities.EIGHT_KB_IN_BYTES];
            int length;
            OutputStream out = response.getOutputStream();
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
        }
    }

    private InputStream getInputStream(MediaFile mediaFile, boolean thumbnail) {
        InputStream resourceStream = null;

        // set the content type based on whatever is in our web.xml mime defs
        if (thumbnail) {
            try {
                resourceStream = mediaFile.getThumbnailInputStream();
            } catch (Exception e) {
                log.warn("ERROR loading thumbnail for {}", mediaFile.getId());
            }
        }

        if (resourceStream == null) {
            resourceStream = mediaFile.getInputStream();
        }

        return resourceStream;
    }

}
