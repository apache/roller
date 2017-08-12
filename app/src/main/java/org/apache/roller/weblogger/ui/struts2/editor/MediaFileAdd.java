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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.RollerMessages.RollerMessage;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Adds a new media file.
 */
@SuppressWarnings("serial")
@AllowedMethods({"execute","save"})
public class MediaFileAdd extends MediaFileBase {

    private static Log log = LogFactory.getLog(MediaFileAdd.class);
    private MediaFileBean bean = new MediaFileBean();
    private MediaFileDirectory directory;

    // an array of files uploaded by the user, if applicable
    private File[] uploadedFiles = null;

    // an array of content types for upload files
    private String[] uploadedFilesContentType = null;

    // an array of filenames for uploaded files
    private String[] uploadedFilesFileName = null;

    private List<MediaFile> newImages = new ArrayList<MediaFile>();

    private List<MediaFile> newFiles = new ArrayList<MediaFile>();

    private String directoryName = null;

    public MediaFileAdd() {
        this.actionName = "mediaFileAdd";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFileAdd.title";
    }

    /**
     * Prepares action class
     */
    public void myPrepare() {
        log.debug("Into myprepare");
        refreshAllDirectories();
        try {
            MediaFileManager mgr = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();
            if (!StringUtils.isEmpty(bean.getDirectoryId())) {
                setDirectory(mgr.getMediaFileDirectory(bean.getDirectoryId()));

            } else if (StringUtils.isNotEmpty(directoryName)) {
                setDirectory(mgr.getMediaFileDirectoryByName(getActionWeblog(),
                        directoryName));

            } else {
                MediaFileDirectory root = mgr
                        .getDefaultMediaFileDirectory(getActionWeblog());
                if (root == null) {
                    root = mgr.createDefaultMediaFileDirectory(getActionWeblog());
                }
                setDirectory(root);
            }
            directoryName = getDirectory().getName();
            bean.setDirectoryId(getDirectory().getId());

        } catch (WebloggerException ex) {
            log.error("Error looking up media file directory", ex);
        } finally {
            // flush
            try {
                WebloggerFactory.getWeblogger().flush();
            } catch (WebloggerException e) {
                // ignored
            }
        }
    }

    /**
     * Show form for adding a new media file.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
        return INPUT;
    }

    /**
     * Save a media file.
     * 
     * @return String The result of the action.
     */
    public String save() {

        myValidate();

        if (!hasActionErrors()) {

            MediaFileManager manager = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();

            RollerMessages errors = new RollerMessages();
            List<MediaFile> uploaded = new ArrayList();
            File[] uploads = getUploadedFiles();

            if (uploads != null && uploads.length > 0) {

                // loop over uploaded files and try saving them
                for (int i = 0; i < uploads.length; i++) {

                    // skip null files
                    if (uploads[i] == null || !uploads[i].exists()) {
                        continue;
                    }

                    try {
                        MediaFile mediaFile = new MediaFile();
                        bean.copyTo(mediaFile);

                        String fileName = getUploadedFilesFileName()[i];
                        int terminated = fileName.indexOf('\000');
                        if (terminated != -1) {
                            // disallow sneaky null terminated strings
                            fileName = fileName.substring(0, terminated).trim();
                        }

                        // make sure fileName is valid
                        if (fileName.indexOf('/') != -1
                                || fileName.indexOf('\\') != -1
                                || fileName.contains("..")) {
                            addError("uploadFiles.error.badPath", fileName);
                            continue;
                        }

                        mediaFile.setName(fileName);
                        mediaFile.setDirectory(getDirectory());
                        mediaFile.setWeblog(getActionWeblog());
                        mediaFile.setLength(this.uploadedFiles[i].length());
                        mediaFile.setInputStream(new FileInputStream(
                                this.uploadedFiles[i]));
                        mediaFile
                                .setContentType(this.uploadedFilesContentType[i]);

                        // insome cases Struts2 is not able to guess the content
                        // type correctly and assigns the default, which is
                        // octet-stream. So in cases where we see octet-stream
                        // we double check and see if we can guess the content
                        // type via the Java MIME type facilities.
                        mediaFile
                                .setContentType(this.uploadedFilesContentType[i]);
                        if (mediaFile.getContentType() == null
                                || mediaFile.getContentType().endsWith(
                                        "/octet-stream")) {

                            String ctype = Utilities
                                    .getContentTypeFromFileName(mediaFile
                                            .getName());
                            if (null != ctype) {
                                mediaFile.setContentType(ctype);
                            }
                        }

                        manager.createMediaFile(getActionWeblog(), mediaFile,
                                errors);
                        WebloggerFactory.getWeblogger().flush();

                        if (mediaFile.isImageFile()) {
                            newImages.add(mediaFile);
                        } else {
                            newFiles.add(mediaFile);
                        }

                        uploaded.add(mediaFile);

                    } catch (Exception e) {
                        log.error("Error uploading media file", e);
                        addError("mediaFileAdd.errorUploading", bean.getName());
                    }
                }

                for (Iterator it = errors.getErrors(); it.hasNext();) {
                    RollerMessage msg = (RollerMessage) it.next();
                    addError(msg.getKey(), Arrays.asList(msg.getArgs()));
                }

                if (uploaded.size() > 0 && !this.errorsExist()) {
                    addMessage("uploadFiles.uploadedFiles");
                    for (MediaFile upload : uploaded) {
                        addMessage("uploadFiles.uploadedFile",
                                upload.getPermalink());
                    }

                } else {
                    return INPUT;
                }

                this.pageTitle = "mediaFileAddSuccess.title";
                return SUCCESS;
            }
        }
        return INPUT;
    }

    /**
     * Validates media file to be added.
     */
    public void myValidate() {

        //
        // TODO: don't allow upload if user is over quota
        //

        // make sure uploads are enabled
        if (!WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")) {
            addError("error.upload.disabled");
        }
    }
    
    public MediaFileBean getBean() {
        return bean;
    }

    public void setBean(MediaFileBean b) {
        this.bean = b;
    }

    public MediaFileDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(MediaFileDirectory directory) {
        this.directory = directory;
    }

    public File[] getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(File[] uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public String[] getUploadedFilesContentType() {
        return uploadedFilesContentType;
    }

    public void setUploadedFilesContentType(String[] uploadedFilesContentType) {
        this.uploadedFilesContentType = uploadedFilesContentType;
    }

    public String[] getUploadedFilesFileName() {
        return uploadedFilesFileName;
    }

    public void setUploadedFilesFileName(String[] uploadedFilesFileName) {
        this.uploadedFilesFileName = uploadedFilesFileName;
    }

    /**
     * @return the newImages
     */
    public List<MediaFile> getNewImages() {
        return newImages;
    }

    /**
     * @param newImages
     *            the newImages to set
     */
    public void setNewImages(List<MediaFile> newImages) {
        this.newImages = newImages;
    }

    /**
     * @return the newFiles
     */
    public List<MediaFile> getNewFiles() {
        return newFiles;
    }

    /**
     * @param newFiles
     *            the newFiles to set
     */
    public void setNewFiles(List<MediaFile> newFiles) {
        this.newFiles = newFiles;
    }

    /**
     * @return the directoryName
     */
    public String getDirectoryName() {
        return directoryName;
    }

    /**
     * @param directoryName
     *            the directoryName to set
     */
    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }
}
