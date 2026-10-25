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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.RollerMessages.RollerMessage;
import org.apache.roller.weblogger.util.MediaTypePolicy;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Adds a new media file.
 */
@SuppressWarnings("serial")
// TODO: make this work @AllowedMethods({"execute","save"})
public class MediaFileAdd extends MediaFileBase implements UploadedFilesAware {

    private static final Log log = LogFactory.getLog(MediaFileAdd.class);
    private MediaFileBean bean = new MediaFileBean();
    private MediaFileDirectory directory;

    // files uploaded by the user, injected by the actionFileUpload interceptor;
    // deliberately not exposed via a getter/setter pair so the params
    // interceptor can't collide with the form field of the same name
    private List<UploadedFile> uploadedFiles = null;

    private List<MediaFile> newImages = new ArrayList<>();

    private List<MediaFile> newFiles = new ArrayList<>();

    // the outcome for each file in the upload, in the order they were sent
    private final List<UploadResult> uploadResults = new ArrayList<>();

    private String directoryName = null;

    public MediaFileAdd() {
        this.actionName = "mediaFileAdd";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFileAdd.title";
    }

    /**
     * Prepares action class
     */
    @Override
    public void myPrepare() {
        log.debug("Into myprepare");
        refreshAllDirectories();
        try {
            MediaFileManager mgr = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();
            if (!StringUtils.isEmpty(bean.getDirectoryId())) {
                setDirectory(mgr.getMediaFileDirectory(getActionWeblog(), bean.getDirectoryId()));

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
            if (getDirectory() == null) {
                addError("mediaFile.directory.notFound");
                return;
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
    @Override
    public String execute() {
        return INPUT;
    }

    @Override
    public void withUploadedFiles(List<UploadedFile> uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
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

            if (uploadedFiles != null && !uploadedFiles.isEmpty()) {

                // loop over uploaded files and try saving each one; a file that
                // fails does not stop the others
                for (UploadedFile upload : uploadedFiles) {

                    // skip null files
                    if (upload == null
                            || !(upload.getContent() instanceof File uploadFile)
                            || !uploadFile.exists()) {
                        continue;
                    }

                    String fileName = upload.getOriginalName();
                    int terminated = fileName.indexOf('\000');
                    if (terminated != -1) {
                        // disallow sneaky null terminated strings
                        fileName = fileName.substring(0, terminated).trim();
                    }

                    // make sure fileName is valid
                    if (fileName.indexOf('/') != -1
                            || fileName.indexOf('\\') != -1
                            || fileName.contains("..")) {
                        failed(fileName, getText("uploadFiles.error.badPath",
                                Collections.singletonList(fileName)));
                        continue;
                    }

                    RollerMessages errors = new RollerMessages();
                    try {
                        MediaFile mediaFile = new MediaFile();
                        bean.copyTo(mediaFile);

                        mediaFile.setName(fileName);
                        mediaFile.setDirectory(getDirectory());
                        mediaFile.setWeblog(getActionWeblog());
                        mediaFile.setLength(uploadFile.length());
                        mediaFile.setInputStream(new FileInputStream(uploadFile));
                        // The type the browser put on the part describes what
                        // the sender meant to send. It is taken as a hint and
                        // the stored type is worked out from the file name.
                        String declaredType = MediaTypePolicy.normalizeType(
                                upload.getContentType());
                        if (!WebloggerFactory.getWeblogger().getFileContentManager().canSave(
                                getActionWeblog(), fileName, declaredType,
                                uploadFile.length(), errors)) {
                            failed(fileName, messageText(errors));
                            continue;
                        }
                        mediaFile.setContentType(MediaTypePolicy.storedTypeFor(
                                mediaFile.getName(), declaredType,
                                RollerContext.getServletContext()::getMimeType));

                        manager.createMediaFile(getActionWeblog(), mediaFile, errors);
                        if (errors.getErrorCount() > 0) {
                            failed(fileName, messageText(errors));
                            continue;
                        }
                        WebloggerFactory.getWeblogger().flush();

                        if (mediaFile.isImageFile()) {
                            newImages.add(mediaFile);
                        } else {
                            newFiles.add(mediaFile);
                        }
                        uploadResults.add(UploadResult.uploaded(mediaFile));

                    } catch (Exception e) {
                        log.error("Error uploading media file " + fileName, e);
                        failed(fileName, getText("mediaFileAdd.errorUploading",
                                Collections.singletonList(fileName)));
                    }
                }

                if (newImages.isEmpty() && newFiles.isEmpty()) {
                    // nothing was saved; the errors are on the upload form
                    return INPUT;
                }

                // Files that were saved are listed on the success page, with
                // any that failed, so a partial upload is never hidden.
                this.pageTitle = "mediaFileAddSuccess.title";
                return SUCCESS;

            } else {
                addError("error.upload.file");
            }
        }
        return INPUT;
    }

    /**
     * Record a file that was not saved, and report it as an action error that
     * names the file. The reason is message text with escaped arguments.
     */
    private void failed(String fileName, String reason) {
        uploadResults.add(UploadResult.failed(fileName, reason));
        addActionError(StringEscapeUtils.escapeHtml4(fileName) + ": " + reason);
    }

    private String messageText(RollerMessages messages) {
        List<String> texts = new ArrayList<>();
        for (Iterator<RollerMessage> it = messages.getErrors(); it.hasNext();) {
            RollerMessage msg = it.next();
            texts.add(msg.getArgs() == null ? getText(msg.getKey())
                    : getText(msg.getKey(), Arrays.asList(msg.getArgs())));
        }
        return String.join(" ", texts);
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
    
    @StrutsParameter(depth = 1)
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

    public List<UploadResult> getUploadResults() {
        return uploadResults;
    }

    public int getFailedCount() {
        return (int) uploadResults.stream().filter(r -> !r.isUploaded()).count();
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
    @StrutsParameter
    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    /** What happened to one file in an upload. */
    public static final class UploadResult {
        private final String fileName;
        private final MediaFile mediaFile;
        private final String error;

        private UploadResult(String fileName, MediaFile mediaFile, String error) {
            this.fileName = fileName;
            this.mediaFile = mediaFile;
            this.error = error;
        }

        static UploadResult uploaded(MediaFile mediaFile) {
            return new UploadResult(mediaFile.getName(), mediaFile, null);
        }

        static UploadResult failed(String fileName, String error) {
            return new UploadResult(fileName, null, error);
        }

        public String getFileName() {
            return fileName;
        }

        /** The saved file, or null if it failed. */
        public MediaFile getMediaFile() {
            return mediaFile;
        }

        /** Why the file failed, as escaped message HTML; null if it was saved. */
        public String getError() {
            return error;
        }

        public boolean isUploaded() {
            return mediaFile != null;
        }
    }
}
