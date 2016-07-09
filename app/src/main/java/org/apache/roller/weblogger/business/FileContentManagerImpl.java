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
package org.apache.roller.weblogger.business;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages contents of the file uploaded to TightBlog.
 * 
 * This base implementation writes file content to a file system.
 */
public class FileContentManagerImpl implements FileContentManager {

    private static Logger log = LoggerFactory.getLogger(FileContentManagerImpl.class);

    private PropertiesManager propertiesManager;

    private String storageDir = null;

    public FileContentManagerImpl(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;

        String inStorageDir = WebloggerStaticConfig.getProperty("mediafiles.storage.dir");
        // Note: System property expansion is now handled by WebloggerStaticConfig.
        if (inStorageDir == null || inStorageDir.trim().length() < 1) {
            inStorageDir = System.getProperty("user.home") + File.separator
                    + "roller_data" + File.separator + "mediafiles";
        }
        if (!inStorageDir.endsWith(File.separator)) {
            inStorageDir += File.separator;
        }
        this.storageDir = inStorageDir.replace('/', File.separatorChar);
    }

    @Override
    public File getFileContent(Weblog weblog, String fileId)
            throws IOException {

        // get a reference to the file, checks that file exists & is readable
        File resourceFile = this.getRealFile(weblog, fileId);

        // make sure file is not a directory
        if (resourceFile.isDirectory()) {
            throw new IOException("Invalid file id [" + fileId + "], "
                    + "path is a directory.");
        }

        // everything looks good, return resource
        return resourceFile;
    }

    @Override
    public void saveFileContent(Weblog weblog, String fileId, InputStream is)
            throws IOException {

        // make sure uploads area exists for this weblog
        File dirPath = this.getRealFile(weblog, null);

        // create File that we are about to save
        File saveFile = new File(dirPath.getAbsolutePath() + File.separator
                + fileId);

        byte[] buffer = new byte[Utilities.EIGHT_KB_IN_BYTES];
        int bytesRead;
        OutputStream bos = null;
        try {
            bos = new FileOutputStream(saveFile);
            while ((bytesRead = is.read(buffer, 0,
                    Utilities.EIGHT_KB_IN_BYTES)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            log.debug("The file has been written to [{}]", saveFile.getAbsolutePath());
        } catch (Exception e) {
            throw new IOException("ERROR uploading file", e);
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (Exception ignored) {
            }
        }

    }

    @Override
    public void deleteFile(Weblog weblog, String fileId) throws IOException {

        // get path to delete file, checks that path exists and is readable
        File delFile = this.getRealFile(weblog, fileId);

        if (!delFile.delete()) {
            log.warn("Delete appears to have failed for [{}]", fileId);
        }
    }

    @Override
    public boolean canSave(Weblog weblog, String fileName, String contentType, long size,
                           Map<String, List<String>> messages) {

        // first check, is uploading enabled?
        if (!propertiesManager.getBooleanProperty("uploads.enabled")) {
            if (messages != null) {
                messages.put("error.upload.disabled", null);
            }
            return false;
        }

        // second check, does upload exceed max size for file?
        BigDecimal maxFileMB = new BigDecimal(
                propertiesManager.getStringProperty("uploads.file.maxsize"));
        int maxFileBytes = (int) (Utilities.ONE_MB_IN_BYTES * maxFileMB.doubleValue());
        log.debug("max allowed file size = {}", maxFileBytes);
        log.debug("attempted save file size = {}", size);
        if (size > maxFileBytes) {
            if (messages != null) {
                messages.put("error.upload.filemax", Arrays.asList(fileName, maxFileMB.toString()));
            }
            return false;
        }

        // third check, does file cause weblog to exceed quota?
        BigDecimal maxDirMB = new BigDecimal(propertiesManager.getStringProperty("uploads.dir.maxsize"));
        long maxDirBytes = (long) (Utilities.ONE_MB_IN_BYTES * maxDirMB.doubleValue());
        try {
            File storageDirectory = this.getRealFile(weblog, null);
            long userDirSize = getDirSize(storageDirectory, true);
            if (userDirSize + size > maxDirBytes) {
                if (messages != null) {
                    messages.put("error.upload.dirmax", Collections.singletonList(maxDirMB.toString()));
                }
                return false;
            }
        } catch (Exception ex) {
            // shouldn't ever happen, means the weblogs uploads dir is bad
            // somehow
            // rethrow as a runtime exception
            throw new RuntimeException(ex);
        }

        // fourth check, is upload type allowed?
        String allows = propertiesManager.getStringProperty("uploads.types.allowed");
        String forbids = propertiesManager.getStringProperty("uploads.types.forbid");
        String[] allowFiles = StringUtils.split(
                StringUtils.deleteWhitespace(allows), ",");
        String[] forbidFiles = StringUtils.split(
                StringUtils.deleteWhitespace(forbids), ",");
        if (!checkFileType(allowFiles, forbidFiles, fileName, contentType)) {
            if (messages != null) {
                messages.put("error.upload.forbiddenFile", Arrays.asList(fileName, contentType));
            }
            return false;
        }

        return true;
    }

    /**
     * Get the size in bytes of given directory.
     * Optionally works recursively counting subdirectories if they exist.
     */
    private long getDirSize(File dir, boolean recurse) {

        long size = 0;

        if (dir.exists() && dir.isDirectory() && dir.canRead()) {
            long dirSize = 0L;
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.isDirectory()) {
                        dirSize += file.length();
                    } else if (recurse) {
                        // count a subdirectory
                        dirSize += getDirSize(file, true);
                    }
                }
            }
            size += dirSize;
        }

        return size;
    }

    /**
     * Return true if file is allowed to be uplaoded given specified allowed and
     * forbidden file types.
     */
    private boolean checkFileType(String[] allowFiles, String[] forbidFiles,
            String fileName, String contentType) {

        // if content type is invalid, reject file
        if (contentType == null || contentType.indexOf('/') == -1) {
            return false;
        }

        // default to false
        boolean allowFile = false;

        // if this person hasn't listed any allows, then assume they want
        // to allow *all* filetypes, except those listed under forbid
        if (allowFiles == null || allowFiles.length < 1) {
            allowFile = true;
        }

        // First check against what is ALLOWED

        // check file against allowed file extensions
        if (allowFiles != null && allowFiles.length > 0) {
            for (String file : allowFiles) {
                // oops, this allowed rule is a content-type, skip it
                if (file.indexOf('/') != -1) {
                    continue;
                }
                if (fileName.toLowerCase().endsWith(file.toLowerCase())) {
                    allowFile = true;
                    break;
                }
            }
        }

        // check file against allowed contentTypes
        if (allowFiles != null && allowFiles.length > 0) {
            for (String file : allowFiles) {
                // oops, this allowed rule is NOT a content-type, skip it
                if (file.indexOf('/') == -1) {
                    continue;
                }
                if (matchContentType(file, contentType)) {
                    allowFile = true;
                    break;
                }
            }
        }

        // First check against what is FORBIDDEN

        // check file against forbidden file extensions, overrides any allows
        if (forbidFiles != null && forbidFiles.length > 0) {
            for (String file : forbidFiles) {
                // oops, this forbid rule is a content-type, skip it
                if (file.indexOf('/') != -1) {
                    continue;
                }
                if (fileName.toLowerCase().endsWith(file.toLowerCase())) {
                    allowFile = false;
                    break;
                }
            }
        }

        // check file against forbidden contentTypes, overrides any allows
        if (forbidFiles != null && forbidFiles.length > 0) {
            for (String file : forbidFiles) {
                // oops, this forbid rule is NOT a content-type, skip it
                if (file.indexOf('/') == -1) {
                    continue;
                }
                if (matchContentType(file, contentType)) {
                    allowFile = false;
                    break;
                }
            }
        }

        return allowFile;
    }

    /**
     * Super simple contentType range rule matching
     */
    private boolean matchContentType(String rangeRule, String contentType) {
        if (rangeRule.equals("*/*")) {
            return true;
        }
        if (rangeRule.equals(contentType)) {
            return true;
        }
        String ruleParts[] = rangeRule.split("/");
        String typeParts[] = contentType.split("/");
        return ruleParts[0].equals(typeParts[0]) && ruleParts[1].equals("*");
    }

    /**
     * Construct the full real path to a resource in a weblog's uploads area.
     */
    private File getRealFile(Weblog weblog, String fileId) throws IOException {

        // make sure uploads area exists for this weblog
        File weblogDir = new File(this.storageDir + weblog.getHandle());
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
            throw new IOException("Invalid path [" + filePath + "], " + "cannot read from path.");
        }

        // make sure someone isn't trying to sneak outside the uploads dir
        if (!file.getCanonicalPath().startsWith(
                weblogDir.getCanonicalPath())) {
            throw new IllegalArgumentException("Invalid path " + filePath + "], "
                    + "access attempt outside defined uploads dir.");
        }

        return file;
    }

}
