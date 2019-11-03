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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.util.Utilities;

/**
 * Manages contents of files uploaded to TightBlog.
 */
@Component
public class FileService {

    private static Logger log = LoggerFactory.getLogger(FileService.class);
    private WebloggerPropertiesDao webloggerPropertiesDao;
    private String storageDir;
    private Set<String> allowedMimeTypes;
    private long maxFileSizeMb;
    private boolean allowUploads;

    @Autowired
    public FileService(WebloggerPropertiesDao webloggerPropertiesDao,
                       @Value("${media.file.showTab}") boolean allowUploads,
                       @Value("${mediafiles.storage.dir}") String storageDir,
                       @Value("#{'${media.file.allowedMimeTypes}'.split(',')}") Set<String> allowedMimeTypes,
                       @Value("${media.file.maxFileSizeMb:3}") long maxFileSizeMb
                       ) {

        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.allowUploads = allowUploads;
        this.storageDir = storageDir;
        this.allowedMimeTypes = allowedMimeTypes;
        this.maxFileSizeMb = maxFileSizeMb;

        if (allowUploads) {
            log.info("Allowed MIME types for media files = {}", ObjectUtils.isEmpty(allowedMimeTypes) ?
                    "NONE (all blocked)" : Arrays.toString(allowedMimeTypes.toArray()));
        } else {
            log.info("Property media.file.showTab=false so media file uploading is disabled");
        }

        // Note: System property expansion is now handled by WebloggerStaticConfig.
        if (StringUtils.isEmpty(this.storageDir)) {
            this.storageDir = System.getProperty("user.home") + File.separator + "tightblog_data" + File.separator + "mediafiles";
        }
        if (!this.storageDir.endsWith(File.separator)) {
            this.storageDir += File.separator;
        }
    }

    /**
     * Get a reference to the content of a specific file in a weblog's uploads area.
     * <p>
     * This method returns a valid file content object or null if object does not exist or
     * is otherwise inaccessible.
     *
     * @param weblog The weblog we are working on.
     * @param fileId file identifier from database.
     * @return File object if retrievable, null otherwise.
     */
    public File getFileContent(Weblog weblog, String fileId) {
        File resourceFile = null;

        try {
            // get a reference to the file, checks that file exists & is readable
            resourceFile = this.getRealFile(weblog.getHandle(), fileId);

            // make sure file is not a directory
            if (resourceFile.isDirectory()) {
                log.warn("Invalid file id [" + fileId + "], path is a directory.");
                resourceFile = null;
            }
        } catch (FileNotFoundException e) {
            log.warn("Problem retrieving file id [" + fileId + "]: {}", e.getMessage());
        } catch (IOException e) {
            log.warn("Problem retrieving file id [" + fileId + "]", e);
        }

        return resourceFile;
    }

    /**
     * Save a file's content to weblog's uploads area.
     *
     * @param weblog The weblog we are working on.
     * @param fileId file identifier from database.
     * @param is     InputStream to read the file from.
     * @throws FileNotFoundException If path to save location does not exist.
     * @throws IOException           If there is an unexpected error during the save.
     */
    public void saveFileContent(Weblog weblog, String fileId, InputStream is) throws IOException {

        // make sure uploads area exists for this weblog
        File dirPath = this.getRealFile(weblog.getHandle(), null);

        // create File that we are about to save
        File saveFile = new File(dirPath.getAbsolutePath() + File.separator + fileId);

        byte[] buffer = new byte[Utilities.EIGHT_KB_IN_BYTES];
        int bytesRead;

        try (OutputStream bos = new FileOutputStream(saveFile)) {
            while ((bytesRead = is.read(buffer, 0,
                    Utilities.EIGHT_KB_IN_BYTES)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.flush();
            log.info("File saved: [{}]", saveFile.getAbsolutePath());
        } catch (Exception e) {
            throw new IOException("ERROR uploading file", e);
        }
    }

    /**
     * Delete file content from weblog's uploads area.
     *
     * @param weblog The weblog we are working on.
     * @param fileId file identifier from database.
     * @throws FileNotFoundException If file does not exist.
     * @throws IOException           If path does not exist or there is an unexpected error during the delete.
     */
    public void deleteFile(Weblog weblog, String fileId) throws IOException {

        // get path to delete file, checks that path exists and is readable
        File delFile = this.getRealFile(weblog.getHandle(), fileId);

        if (!delFile.delete()) {
            log.warn("Delete appears to have failed for [{}]", fileId);
        }
    }

    /**
     * Determine if file can be saved given current blogger settings.
     *
     * @param fileToTest MediaFile attempting to save
     * @param messages    output parameter for resource bundle messages, or null if not necessary to receive them
     * @return true if the file can be saved, false otherwise.
     */
    public boolean canSave(MultipartFile fileToTest, String weblogHandle, Map<String, List<String>> messages) {

        WebloggerProperties webloggerProperties = webloggerPropertiesDao.findOrNull();

        // first check, is uploading enabled?
        if (!allowUploads) {
            if (messages != null) {
                messages.put("error.upload.disabled", null);
            }
            return false;
        }

        // second check, is upload type allowed?
        if (!checkFileType(fileToTest.getOriginalFilename(), fileToTest.getContentType())) {
            if (messages != null) {
                messages.put("error.upload.forbiddenFile", Collections.singletonList(fileToTest.getContentType()));
            }
            return false;
        }

        // third check, does upload exceed max size for file?
        log.debug("File size = {}, Max allowed = {}MB", fileToTest.getSize(), maxFileSizeMb);
        if (fileToTest.getSize() > maxFileSizeMb * Utilities.ONE_MB_IN_BYTES) {
            if (messages != null) {
                messages.put("error.upload.filemax", Collections.singletonList(Long.toString(maxFileSizeMb)));
            }
            return false;
        }

        // fourth check, does file cause weblog to exceed quota?
        long maxAllocationMB = webloggerProperties.getMaxFileUploadsSizeMb();
        long maxAllocationBytes = maxAllocationMB * Utilities.ONE_MB_IN_BYTES;

        try {
            File storageDirectory = this.getRealFile(weblogHandle, null);
            long alreadyUsedSpace = getDirSize(storageDirectory);
            log.debug("File size = {}, current allocation space taken = {}, max allowed = {}MB",
                    fileToTest.getSize(), alreadyUsedSpace, maxAllocationMB);
            if (alreadyUsedSpace + fileToTest.getSize() > maxAllocationBytes) {
                if (messages != null) {
                    messages.put("error.upload.blogmax", Collections.singletonList(Long.toString(maxAllocationMB)));
                }
                return false;
            }
        } catch (Exception ex) {
            // shouldn't ever happen, means the weblogs uploads dir is bad somehow
            throw new RuntimeException(ex);
        }

        return true;
    }

    /**
     * Get the size in bytes of given directory including its subdirectories
     */
    private long getDirSize(File dir) {

        long size = 0;

        if (dir.exists() && dir.isDirectory() && dir.canRead()) {
            long dirSize = 0L;
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.isDirectory()) {
                        dirSize += file.length();
                    } else {
                        // count a subdirectory
                        dirSize += getDirSize(file);
                    }
                }
            }
            size += dirSize;
        }

        return size;
    }

    /**
     * Return true if file is allowed to be uploaded given specified allowed MIME types
     */
    private boolean checkFileType(String fileName, String contentType) {
        String fileDesc = String.format("Media File %s (content type %s)", fileName, contentType);

        // if content type is invalid, reject file
        if (contentType == null || contentType.indexOf('/') == -1) {
            log.warn("{} blocked from uploading because of invalid content type", fileDesc);
            return false;
        }

        // default to false
        boolean allowFile = false;

        if (!ObjectUtils.isEmpty(allowedMimeTypes)) {
            for (String allowedMimeType : allowedMimeTypes) {
                if (matchContentType(contentType, allowedMimeType)) {
                    allowFile = true;
                    break;
                }
            }
            log.warn("{} blocked from uploading because not in allowed MIME types", fileDesc);
        }

        return allowFile;
    }

    /**
     * Super simple contentType range rule matching
     */
    private boolean matchContentType(String incomingFileContentType, String allowedMimeType) {
        if (allowedMimeType.indexOf('/') < 0) {
            return false;
        }
        if (allowedMimeType.equals(incomingFileContentType)) {
            return true;
        }
        String[] ruleParts = allowedMimeType.split("/");
        String[] typeParts = incomingFileContentType.split("/");
        return ruleParts[0].equals(typeParts[0]) && ruleParts[1].equals("*");
    }

    /**
     * Construct the full real path to a resource in a weblog's uploads area.
     */
    private File getRealFile(String weblogHandle, String fileId) throws IOException {

        // make sure uploads area exists for this weblog
        File weblogDir = new File(this.storageDir + weblogHandle);
        if (!weblogDir.exists()) {
            if (!weblogDir.mkdirs()) {
                throw new IOException("Cannot create directory " + weblogDir.getAbsolutePath());
            }
        }

        // now form the absolute path
        String filePath = weblogDir.getAbsolutePath();
        if (fileId != null) {
            filePath += File.separator + fileId;
        }

        // make sure path exists and is readable
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Invalid path [" + filePath + "], " + "file does not exist.");
        } else if (!file.canRead()) {
            throw new IOException("Invalid path [" + filePath + "], cannot read from it.");
        }

        // make sure someone isn't trying to sneak outside the uploads dir
        if (!file.getCanonicalPath().startsWith(
                weblogDir.getCanonicalPath())) {
            throw new IllegalArgumentException("Invalid path " + filePath +
                    "], access attempt outside defined uploads dir.");
        }

        return file;
    }
}
