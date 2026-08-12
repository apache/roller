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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FileIOException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.apache.struts2.interceptor.validation.SkipValidation;

import java.util.List;

/**
 * Edits metadata for a media file.
 */
@SuppressWarnings("serial")
// TODO: make this work @AllowedMethods({"execute","save"})
public class MediaFileEdit extends MediaFileBase implements UploadedFilesAware {

    private static Log log = LogFactory.getLog(MediaFileEdit.class);
    private MediaFileBean bean = new MediaFileBean();
    private MediaFileDirectory directory;

    // replacement file uploaded by the user, if applicable, injected
    // by the actionFileUpload interceptor
    private UploadedFile uploadedFile = null;

    public MediaFileEdit() {
        this.actionName = "mediaFileEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFile.edit.title";
    }

    /**
     * Prepares edit action.
     */
    @Override
    public void myPrepare() {
        refreshAllDirectories();
        try {
            MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
            if (!StringUtils.isEmpty(bean.getDirectoryId())) {
                setDirectory(mgr.getMediaFileDirectory(bean.getDirectoryId()));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up media file directory", ex);
        }

    }

    @Override
    public void withUploadedFiles(List<UploadedFile> uploadedFiles) {
        this.uploadedFile = uploadedFiles.isEmpty() ? null : uploadedFiles.get(0);
    }

    /**
     * Validates media file metadata to be updated.
     */
    public void myValidate() {
        MediaFile fileWithSameName = getDirectory().getMediaFile(getBean().getName());
        if (fileWithSameName != null && !fileWithSameName.getId().equals(getMediaFileId())) {
            addError("MediaFile.error.duplicateName", getBean().getName());
        }
    }

    /**
     * Show form for adding a new media file.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    @Override
    public String execute() {
        MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
        try {
            MediaFile mediaFile = manager.getMediaFile(getMediaFileId());
            this.bean.copyFrom(mediaFile);

        } catch (FileIOException ex) {
            addError("uploadFiles.error.upload", bean.getName());

        } catch (Exception e) {
            log.error("Error uploading file " + bean.getName(), e);
            addError("uploadFiles.error.upload", bean.getName());
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
            MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
            try {
                MediaFile mediaFile = manager.getMediaFile(getMediaFileId());
                bean.copyTo(mediaFile);

                if (uploadedFile != null
                        && uploadedFile.getContent() instanceof File uploadFile) {
                    mediaFile.setLength(uploadFile.length());
                    mediaFile.setContentType(uploadedFile.getContentType());
                    manager.updateMediaFile(getActionWeblog(), mediaFile,
                            new FileInputStream(uploadFile));
                } else {
                    manager.updateMediaFile(getActionWeblog(), mediaFile);
                }

                // Move file
                if (!getBean().getDirectoryId().equals(
                        mediaFile.getDirectory().getId())) {
                    log.debug("Processing move of " + mediaFile.getId());
                    setSelectedMediaFiles(new String[] { mediaFile.getId() });
                    setSelectedDirectory(getBean().getDirectoryId());
                    doMoveSelected();
                }

                WebloggerFactory.getWeblogger().flush();

                addMessage("mediaFile.update.success");
                return SUCCESS;

            } catch (FileIOException ex) {
                addError("uploadFiles.error.upload", bean.getName());

            } catch (Exception e) {
                log.error("Error uploading file " + bean.getName(), e);
                addError("uploadFiles.error.upload", bean.getName());
            }

        }
        return INPUT;
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

}
