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
import org.tightblog.pojos.MediaFile;
import org.tightblog.pojos.Weblog;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.requests.WeblogRequest;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.temporal.ChronoUnit;

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

    private WeblogRepository weblogRepository;
    private LazyExpiringCache weblogMediaCache;
    private MediaFileManager mediaFileManager;
    private WeblogRequest.Creator weblogRequestCreator;

    @Autowired
    public MediaFileProcessor(WeblogRepository weblogRepository, LazyExpiringCache weblogMediaCache,
                              MediaFileManager mediaFileManager) {
        this.weblogRequestCreator = new WeblogRequest.Creator();
        this.weblogRepository = weblogRepository;
        this.weblogMediaCache = weblogMediaCache;
        this.mediaFileManager = mediaFileManager;
    }

    void setWeblogRequestCreator(WeblogRequest.Creator creator) {
        this.weblogRequestCreator = creator;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    void getMediaFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WeblogRequest incomingRequest = weblogRequestCreator.create(request);

        Weblog weblog = weblogRepository.findByHandleAndVisibleTrue(incomingRequest.getWeblogHandle());
        if (weblog == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            incomingRequest.setWeblog(weblog);
        }

        MediaFile mediaFile = null;
        // we want only the path info left over from after the weblog parsing
        String pathInfo = incomingRequest.getExtraPathInfo();
        if (pathInfo != null) {
            if (pathInfo.startsWith("/")) {
                pathInfo = pathInfo.substring(1);
            }
            if (pathInfo.length() > 0) {
                // at this stage, pathInfo is the resourceId
                mediaFile = mediaFileManager.getMediaFileWithContent(pathInfo);
            }
        }

        if (mediaFile == null) {
            log.info("Could not obtain media file for resource path: ", request.getRequestURL());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        weblogMediaCache.incrementIncomingRequests();

        // DB stores last modified in millis, browser if-modified-since in seconds, so need to truncate millis from the former.
        long inDb = mediaFile.getLastUpdated().truncatedTo(ChronoUnit.SECONDS).toEpochMilli();
        long inBrowser = getBrowserCacheExpireDate(request);

        if (inDb <= inBrowser) {
            weblogMediaCache.incrementRequestsHandledBy304();
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        boolean useThumbnail = false;
        if (mediaFile.isImageFile() && "true".equals(request.getParameter("tn"))) {
            useThumbnail = true;
        }

        File desiredFile = useThumbnail ? mediaFile.getThumbnail() : mediaFile.getContent();
        if (desiredFile == null) {
            log.info("Could not obtain {} file content for resource path: ", useThumbnail ? "thumbnail" : "",
                    request.getRequestURL());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try (InputStream resourceStream = new FileInputStream(desiredFile);
             OutputStream out = response.getOutputStream()) {
            response.setContentType(useThumbnail ? MediaFile.THUMBNAIL_CONTENT_TYPE : mediaFile.getContentType());
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Last-Modified", mediaFile.getLastUpdated().toEpochMilli());

            byte[] buf = new byte[Utilities.EIGHT_KB_IN_BYTES];
            int length;
            while ((length = resourceStream.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        } catch (Exception ex) {
            log.error("Error obtaining media file {}", desiredFile.getAbsolutePath(), ex);
            if (!response.isCommitted()) {
                response.reset();
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
}
