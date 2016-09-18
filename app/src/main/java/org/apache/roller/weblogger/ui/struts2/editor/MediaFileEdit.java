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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaDirectory;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds or edits a media file.
 */
public class MediaFileEdit extends UIAction {

    private static Logger log = LoggerFactory.getLogger(MediaFileEdit.class);
    private MediaFile bean = new MediaFile();
    private MediaDirectory directory;
    private String mediaFileId;
    private List<MediaDirectory> allDirectories;

    private MediaFileManager mediaFileManager;

    public void setMediaFileManager(MediaFileManager mediaFileManager) {
        this.mediaFileManager = mediaFileManager;
    }

    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    // an array of files uploaded by the user, if applicable
    private File uploadedFile = null;

    // content type for upload file
    private String uploadedFileContentType = null;

    // an array of filenames for uploaded files
    private String uploadedFileFileName = null;

    private String directoryId = null;

    public MediaFileEdit() {
        this.desiredMenu = "editor";
    }

    private boolean isAdd() {
        return actionName.equals("mediaFileAdd");
    }

    @Override
    public GlobalRole getRequiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole getRequiredWeblogRole() {
        return WeblogRole.POST;
    }

    /**
     * Prepares action class
     */
    public void prepare() {
        allDirectories = mediaFileManager.getMediaDirectories(getActionWeblog());
        if (!StringUtils.isEmpty(directoryId)) {
            setDirectory(mediaFileManager.getMediaDirectory(directoryId));
        } else {
            throw new IllegalStateException("Directory not provided.");
        }
        directoryId = getDirectory().getId();
        bean.setDirectoryId(getDirectory().getId());
        persistenceStrategy.flush();
    }

    /**
     * Validates media file to be added.
     */
    private void myValidate() {
        if (StringUtils.isEmpty(bean.getName())) {
            addError("MediaFile.error.nameNull");
        } else {
            if (isAdd()) {
                // make sure uploads are enabled
                if (!propertiesManager.getBooleanProperty("uploads.enabled")) {
                    addError("error.upload.disabled");
                }
                if (uploadedFile == null || !uploadedFile.exists()) {
                    addError("error.upload.nofile");
                }
            } else {
                MediaFile fileWithSameName = getDirectory().getMediaFile(getBean().getName());
                if (fileWithSameName != null && !fileWithSameName.getId().equals(getMediaFileId())) {
                    addError("MediaFile.error.duplicateName", getBean().getName());
                }
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
        if (!isAdd()) {
            try {
                MediaFile mediaFile = mediaFileManager.getMediaFile(getMediaFileId());
                bean.setId(mediaFile.getId());
                bean.setName(mediaFile.getName());
                bean.setAltText(mediaFile.getAltText());
                bean.setTitleText(mediaFile.getTitleText());
                bean.setAnchor(mediaFile.getAnchor());
                bean.setNotes(mediaFile.getNotes());
                bean.setDirectory(mediaFile.getDirectory());
                bean.setDirectoryId(mediaFile.getDirectory().getId());
                bean.setWidth(mediaFile.getWidth());
                bean.setHeight(mediaFile.getHeight());
                bean.setLength(mediaFile.getLength());
                bean.setContentType(mediaFile.getContentType());
            } catch (Exception e) {
                log.error("Error uploading file {}", bean.getName(), e);
                addError("uploadFiles.error.upload", bean.getName());
            }
        }
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
            try {
                if (isAdd()) {
                    MediaFile mediaFile = new MediaFile();
                    mediaFile.setName(bean.getName().trim());
                    mediaFile.setAltText(bean.getAltText().trim());
                    mediaFile.setTitleText(bean.getTitleText().trim());
                    mediaFile.setAnchor(bean.getAnchor().trim());
                    mediaFile.setNotes(bean.getNotes().trim());
                    mediaFile.setCreator(getAuthenticatedUser());
                    String fileName = getUploadedFileFileName();

                    // make sure fileName is valid
                    if (fileName.indexOf('/') != -1 || fileName.indexOf('\\') != -1 ||
                            fileName.contains("..") || fileName.indexOf('\000') != -1) {
                        addError("uploadFiles.error.badPath", fileName);
                    } else {
                        mediaFile.setName(fileName);
                        mediaFile.setDirectory(getDirectory());
                        mediaFile.setLength(this.uploadedFile.length());
                        mediaFile.setInputStream(new FileInputStream(this.uploadedFile));
                        mediaFile.setContentType(this.uploadedFileContentType);

                        // in some cases Struts2 is not able to guess the content
                        // type correctly and assigns the default, which is
                        // octet-stream. So in cases where we see octet-stream
                        // we double check and see if we can guess the content
                        // type via the Java MIME type facilities.
                        mediaFile.setContentType(this.uploadedFileContentType);
                        if (mediaFile.getContentType() == null ||
                                mediaFile.getContentType().endsWith("/octet-stream")) {
                            String ctype = Utilities.getContentTypeFromFileName(mediaFile.getName());
                            if (null != ctype) {
                                mediaFile.setContentType(ctype);
                            }
                        }

                        Map<String, List<String>> errors = new HashMap<>();
                        mediaFileManager.createMediaFile(getActionWeblog(), mediaFile, errors);
                        for (Map.Entry<String, List<String>> error : errors.entrySet()) {
                            addError(error.getKey(), error.getValue());
                        }

                        persistenceStrategy.flush();
                        // below should not be necessary as createMediaFile refreshes the directory's
                        // file listing but caching of directory's old file listing occurring somehow.
                        mediaFile.getDirectory().getMediaFiles().add(mediaFile);
                    }

                    if (!this.errorsExist()) {
                        addMessage("uploadFiles.uploadedFiles");
                        addMessage("uploadFiles.uploadedFile", mediaFile.getName());
                        this.pageTitle = "mediaFileAddSuccess.title";
                        return SUCCESS;
                    }
                } else {
                    MediaFile mediaFile = mediaFileManager.getMediaFile(getMediaFileId());
                    mediaFile.setName(bean.getName());
                    mediaFile.setAltText(bean.getAltText());
                    mediaFile.setTitleText(bean.getTitleText());
                    mediaFile.setAnchor(bean.getAnchor());
                    mediaFile.setNotes(bean.getNotes());

                    if (uploadedFile != null) {
                        mediaFile.setLength(this.uploadedFile.length());
                        mediaFile.setContentType(this.uploadedFileContentType);
                        mediaFile.setCreator(getAuthenticatedUser());
                        mediaFileManager.updateMediaFile(getActionWeblog(), mediaFile,
                                new FileInputStream(this.uploadedFile));
                    } else {
                        mediaFileManager.updateMediaFile(getActionWeblog(), mediaFile);
                    }

                    // Move file
                    if (!getBean().getDirectoryId().equals(mediaFile.getDirectory().getId())) {
                        log.debug("Processing move of {}", mediaFile.getId());
                        MediaDirectory targetDirectory = mediaFileManager.getMediaDirectory(getBean().getDirectoryId());
                        mediaFileManager.moveMediaFile(mediaFile, targetDirectory);
                    }

                    persistenceStrategy.flush();

                    addMessage("mediaFile.update.success");
                    return SUCCESS;
                }
            } catch (Exception e) {
                log.error("Error uploading file {}", bean.getName(), e);
                addError("mediaFileAdd.errorUploading", bean.getName());
            }
        }
        return INPUT;
    }

    public MediaFile getBean() {
        return bean;
    }

    public void setBean(MediaFile b) {
        this.bean = b;
    }

    public MediaDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(MediaDirectory directory) {
        this.directory = directory;
    }

    public File getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(File uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getUploadedFileContentType() {
        return uploadedFileContentType;
    }

    public void setUploadedFileContentType(String uploadedFileContentType) {
        this.uploadedFileContentType = uploadedFileContentType;
    }

    public String getUploadedFileFileName() {
        return uploadedFileFileName;
    }

    public void setUploadedFileFileName(String uploadedFileFileName) {
        this.uploadedFileFileName = uploadedFileFileName;
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public List<MediaDirectory> getAllDirectories() {
        return allDirectories;
    }

    public String getMediaFileId() {
        return mediaFileId;
    }

    public void setMediaFileId(String mediaFileId) {
        this.mediaFileId = mediaFileId;
    }

}
