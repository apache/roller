/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tightblog.domain.MediaDirectory;
import org.tightblog.domain.MediaFile;
import org.tightblog.domain.Weblog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tightblog.repository.MediaDirectoryRepository;
import org.tightblog.repository.MediaFileRepository;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.repository.WebloggerPropertiesRepository;

import javax.imageio.ImageIO;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for media file and media directory management.
 */
@Component
public class MediaManager {

    private FileService fileService;
    private MediaDirectoryRepository mediaDirectoryRepository;
    private MediaFileRepository mediaFileRepository;
    private WeblogRepository weblogRepository;
    private WebloggerPropertiesRepository webloggerPropertiesRepository;

    private static Logger log = LoggerFactory.getLogger(MediaManager.class);

    @Autowired
    public MediaManager(FileService fileService,
                        MediaDirectoryRepository mediaDirectoryRepository, MediaFileRepository mediaFileRepository,
                        WeblogRepository weblogRepository,
                        WebloggerPropertiesRepository webloggerPropertiesRepository) {
        this.fileService = fileService;
        this.mediaDirectoryRepository = mediaDirectoryRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.weblogRepository = weblogRepository;
        this.webloggerPropertiesRepository = webloggerPropertiesRepository;
    }

    /**
     * Move a set of media files to a new directory.
     */
    public void moveMediaFiles(Collection<MediaFile> mediaFiles, MediaDirectory targetDirectory) {

        List<MediaFile> moved = new ArrayList<>(mediaFiles);

        MediaDirectory oldDirectory = null;

        for (MediaFile mediaFile : moved) {
            oldDirectory = mediaFile.getDirectory();

            mediaFile.setDirectory(targetDirectory);
            targetDirectory.getMediaFiles().add(mediaFile);
            oldDirectory.getMediaFiles().remove(mediaFile);
        }

        mediaDirectoryRepository.saveAndFlush(targetDirectory);
        mediaDirectoryRepository.saveAndFlush(oldDirectory);
    }

    /**
     * Create a media file directory with a given name.
     */
    public MediaDirectory createMediaDirectory(Weblog weblog, String requestedName) {
        requestedName = requestedName.startsWith("/") ? requestedName.substring(1) : requestedName;

        if (!webloggerPropertiesRepository.findOrNull().isUsersUploadMediaFiles()) {
            throw new IllegalArgumentException("error.upload.disabled");
        }

        MediaDirectory newDirectory;
        if (weblog.hasMediaDirectory(requestedName)) {
            throw new IllegalArgumentException("mediaFileView.directoryCreate.error.exists");
        } else {
            newDirectory = new MediaDirectory(weblog, requestedName);

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<MediaDirectory>> errors = validator.validate(newDirectory);
            if (errors.size() > 0) {
                // strip away { and } from message string
                String origMessage = errors.iterator().next().getMessage();
                throw new IllegalArgumentException(origMessage.substring(1, origMessage.length() - 1));
            }
            weblog.getMediaDirectories().add(newDirectory);
            weblogRepository.saveAndFlush(weblog);
            log.debug("Created new Directory {}", requestedName);
        }
        return newDirectory;
    }

    /**
     * Update metadata for a media file and content.
     * @param mediaFile - Media File to update
     * @param updatedStream - if non-null, file's contents will be replaced with the contents of this InputStream.
     * @param errors object to receive message bundle keys and argument values or null if not desired to receive them
     */
    public void saveMediaFile(MediaFile mediaFile, InputStream updatedStream, Map<String, List<String>> errors)
            throws IOException {
        Weblog weblog = mediaFile.getDirectory().getWeblog();

        if (!fileService.canSave(weblog, mediaFile.getName(),
                mediaFile.getContentType(), mediaFile.getLength(), errors)) {
            return;
        }

        mediaFile.getDirectory().getMediaFiles().add(mediaFile);
        mediaFile.setLastUpdated(Instant.now());

        if (updatedStream != null) {
            Map<String, List<String>> msgs = new HashMap<>();
            if (!fileService.canSave(weblog, mediaFile.getName(),
                    mediaFile.getContentType(), mediaFile.getLength(), msgs)) {
                throw new IOException(msgs.toString());
            }
            fileService.saveFileContent(weblog, mediaFile.getId(), updatedStream);

            if (mediaFile.isImageFile()) {
                updateThumbnail(mediaFile);
            }
        }

          mediaDirectoryRepository.saveAndFlush(mediaFile.getDirectory());
    }

    private void updateThumbnail(MediaFile mediaFile) {
        try {
            File fileContent = fileService.getFileContent(mediaFile.getDirectory().getWeblog(), mediaFile.getId());
            BufferedImage img;

            FileInputStream fis = new FileInputStream(fileContent);
            img = ImageIO.read(fis);

            // Some graphics (e.g., Microsoft icons) not processable by ImageIO
            if (img != null) {
                // determine and save width and height
                mediaFile.setWidth(img.getWidth());
                mediaFile.setHeight(img.getHeight());

                int newWidth = mediaFile.getThumbnailWidth();
                int newHeight = mediaFile.getThumbnailHeight();

                // create thumbnail image
                Image newImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                BufferedImage tmp = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = tmp.createGraphics();
                g2.drawImage(newImage, 0, 0, newWidth, newHeight, null);
                g2.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(tmp, "png", baos);

                fileService.saveFileContent(mediaFile.getDirectory().getWeblog(), mediaFile.getId() +
                        "_sm", new ByteArrayInputStream(baos.toByteArray()));

            } else {
                FileInputStream fis2 = new FileInputStream(fileContent);
                fileService.saveFileContent(mediaFile.getDirectory().getWeblog(), mediaFile.getId() + "_sm",
                        fis2);

                mediaFile.setWidth(MediaFile.MAX_THUMBNAIL_WIDTH);
                mediaFile.setHeight(MediaFile.MAX_THUMBNAIL_HEIGHT);
            }

        } catch (Exception e) {
            log.debug("ERROR creating thumbnail", e);
        }
    }

    /**
     * Get media file metadata optionally including the actual content
     * @return MediaFile object or null if unavailable/inaccessible.
     */
    public MediaFile getMediaFileWithContent(String id) {
        MediaFile mediaFile = mediaFileRepository.findByIdOrNull(id);

        File content = fileService.getFileContent(mediaFile.getDirectory().getWeblog(), id);
        mediaFile.setContent(content);

        File thumbnail = fileService.getFileContent(mediaFile.getDirectory().getWeblog(),
                id + "_sm");
        mediaFile.setThumbnail(thumbnail);

        return mediaFile;
    }

    /**
     * Remove all media content (files and directories) associated with a weblog.
     */
    public void removeAllFiles(Weblog weblog) {
        List<MediaDirectory> list = mediaDirectoryRepository.findByWeblog(weblog);

        for (MediaDirectory directory : list) {
            removeAllFiles(directory);
        }
    }

    /**
     * Delete a directory and all of its associated file contents
     */
    public void removeAllFiles(MediaDirectory dir) {
        List<MediaFile> files = mediaFileRepository.findByDirectory(dir);
        for (MediaFile mf : files) {
            removeMediaFile(dir.getWeblog(), mf);
        }
        mediaDirectoryRepository.delete(dir);
    }

    /**
     * Delete a media file and its associated file contents
     */
    public void removeMediaFile(Weblog weblog, MediaFile mediaFile) {
        try {
            fileService.deleteFile(weblog, mediaFile.getId());
            // Now thumbnail
            fileService.deleteFile(weblog, mediaFile.getId() + "_sm");
        } catch (Exception e) {
            log.debug("File to be deleted already unavailable in the file store");
        }
        mediaFileRepository.delete(mediaFile);
    }
}
