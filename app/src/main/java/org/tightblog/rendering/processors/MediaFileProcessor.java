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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serves media files uploaded by users.
 * <p>
 * Since we keep resources in a location outside of the webapp context we need a
 * way to serve them up.
 */
@RestController
@RequestMapping(path = "/tb-ui/rendering/mediafile/**")
public class MediaFileProcessor extends AbstractProcessor {

    private static Logger log = LoggerFactory.getLogger(MediaFileProcessor.class);

    public static final String PATH = "/tb-ui/rendering/mediafile";

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

    private WeblogRequest.Creator weblogRequestCreator;

    public MediaFileProcessor() {
        this.weblogRequestCreator = new WeblogRequest.Creator();
    }

    public void setWeblogRequestCreator(WeblogRequest.Creator creator) {
        this.weblogRequestCreator = creator;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public void getMediaFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WeblogRequest incomingRequest = weblogRequestCreator.create(request);

        Weblog weblog = weblogManager.getWeblogByHandle(incomingRequest.getWeblogHandle(), true);
        if (weblog == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            incomingRequest.setWeblog(weblog);
        }

        MediaFile mediaFile = null;
        // we want only the path info left over from after the weblog parsing
        String pathInfo = incomingRequest.getPathInfo();
        if (pathInfo != null && pathInfo.trim().length() > 1) {
            String resourceId = pathInfo;
            if (pathInfo.startsWith("/")) {
                resourceId = pathInfo.substring(1);
            }
            mediaFile = mediaFileManager.getMediaFile(resourceId, true);
        }

        if (mediaFile == null) {
            log.info("Could not obtain media file for resource path: ", request.getRequestURL());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Respond with 304 Not Modified if it is not modified.
        if (mediaFile.getLastUpdated().toEpochMilli() <= getBrowserCacheExpireDate(request)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        boolean useThumbnail = false;
        if ("true".equals(request.getParameter("t"))) {
            useThumbnail = true;
        }

        try (InputStream resourceStream = getInputStream(mediaFile, useThumbnail)) {
            response.setContentType(useThumbnail ? "image/png" : mediaFile.getContentType());

            byte[] buf = new byte[Utilities.EIGHT_KB_IN_BYTES];
            int length;
            response.setHeader("Cache-Control","no-cache");
            response.setDateHeader("Last-Modified", mediaFile.getLastUpdated().toEpochMilli());
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
