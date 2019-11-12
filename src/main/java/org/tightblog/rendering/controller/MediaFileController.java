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
package org.tightblog.rendering.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.tightblog.service.MediaManager;
import org.tightblog.domain.MediaFile;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.dao.WeblogDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;

/**
 * Serves media files uploaded by users.
 * <p>
 * Since we keep resources in a location outside of the webapp context we need a
 * way to serve them up.
 */
@RestController("RenderingMediaFileController")
@RequestMapping(path = MediaFileController.PATH)
// Validate constraint annotations on method parameters
@Validated
public class MediaFileController extends AbstractController {

    private static Logger log = LoggerFactory.getLogger(MediaFileController.class);

    public static final String PATH = "/tb-ui/rendering/mediafile";

    private WeblogDao weblogDao;
    private LazyExpiringCache weblogMediaCache;
    private MediaManager mediaManager;

    @Autowired
    MediaFileController(WeblogDao weblogDao, LazyExpiringCache weblogMediaCache,
                        MediaManager mediaManager) {
        this.weblogDao = weblogDao;
        this.weblogMediaCache = weblogMediaCache;
        this.mediaManager = mediaManager;
    }

    @GetMapping(path = "/{weblogHandle}/{mediaFileId}")
    ResponseEntity<Resource> getMediaFile(
            @PathVariable String weblogHandle, @PathVariable @NotBlank String mediaFileId,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        Weblog weblog = weblogDao.findByHandleAndVisibleTrue(weblogHandle);
        if (weblog == null) {
            return ResponseEntity.notFound().build();
        }

        MediaFile mediaFile = mediaManager.getMediaFileWithContent(mediaFileId);
        if (mediaFile == null) {
            log.info("Could not obtain media file for weblog {} and media file ID {}", weblogHandle,
                    mediaFileId);
            return ResponseEntity.notFound().build();
        }

        weblogMediaCache.incrementIncomingRequests();

        // DB stores last modified in millis, browser if-modified-since in seconds, so need to truncate millis from the former.
        long inDb = mediaFile.getLastUpdated().truncatedTo(ChronoUnit.SECONDS).toEpochMilli();
        long inBrowser = getBrowserCacheExpireDate(request);

        if (inDb <= inBrowser) {
            weblogMediaCache.incrementRequestsHandledBy304();
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        boolean useThumbnail = false;
        if (mediaFile.isImageFile() && "true".equals(request.getParameter("tn"))) {
            useThumbnail = true;
        }

        File desiredFile = useThumbnail ? mediaFile.getThumbnail() : mediaFile.getContent();
        if (desiredFile == null) {
            log.info("Could not obtain {} file content for resource path {}", useThumbnail ? "thumbnail" : "",
                    request.getRequestURL());
            return ResponseEntity.notFound().build();
        }

        // alternative: https://stackoverflow.com/a/35683261
        Path path = Paths.get(desiredFile.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity.ok()
                .contentType(useThumbnail ? MediaFile.THUMBNAIL_CONTENT_TYPE
                        : MediaType.valueOf(mediaFile.getContentType()))
                .contentLength(desiredFile.length())
                .lastModified(mediaFile.getLastUpdated().toEpochMilli())
                .cacheControl(CacheControl.noCache())
                .body(resource);
    }
}
