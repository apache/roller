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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.MediaDirectory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * View media files.
 */
@SuppressWarnings("serial")
public class MediaFileView extends UIAction {

    private static Log log = LogFactory.getLog(MediaFileView.class);

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private JPAPersistenceStrategy strategy = null;

    public void setStrategy(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    private MediaFileManager mediaFileManager;

    public void setMediaFileManager(MediaFileManager mediaFileManager) {
        this.mediaFileManager = mediaFileManager;
    }

    private String directoryId;
    private String directoryName;
    private String sortBy;
    private String newDirectoryName;

    private List<MediaFile> childFiles;
    private MediaDirectory currentDirectory;
    private String selectedDirectory;
    private String[] selectedMediaFiles;

    private String mediaFileId;

    // Sort options for search results.
    private static List<Pair<String, String>> SORT_OPTIONS = null;

    // a new directory the user wishes to view
    private String viewDirectoryId = null;

    public MediaFileView() {
        this.actionName = "mediaFileView";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFileView.title";
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.POST;
    }

    /**
     * Prepares view action
     */
    public void prepare() {
        if (SORT_OPTIONS == null) {
            SORT_OPTIONS = Arrays.asList(
                    Pair.of("name", getText("generic.name")),
                    Pair.of("date_uploaded", getText("mediaFileView.date")),
                    Pair.of("type", getText("mediaFileView.type")));
        }
    }

    /**
     * Create a new directory by name. All folders placed at the root.
     */
    public String createNewDirectory() {
        if (StringUtils.isEmpty(this.newDirectoryName)) {
            addError("mediaFile.error.view.dirNameEmpty");
        } else if (this.newDirectoryName.contains("/")) {
            addError("mediaFile.error.view.dirNameInvalid");
        } else {
            try {
                this.newDirectoryName = this.newDirectoryName.trim();
                log.debug("Creating new directory - " + this.newDirectoryName);
                if (!getActionWeblog().hasMediaDirectory(this.newDirectoryName)) {
                    // Create
                    MediaDirectory dir = mediaFileManager.createMediaDirectory(
                            getActionWeblog(), this.newDirectoryName);
                    // flush changes
                    WebloggerFactory.flush();
                    addMessage("mediaFile.directoryCreate.success",
                            this.newDirectoryName);

                    // Switch to folder
                    setDirectoryId(dir.getId());
                } else {
                    // already exists
                    addError("mediaFile.directoryCreate.error.exists", this.newDirectoryName);
                }

            } catch (WebloggerException e) {
                log.error("Error creating new directory", e);
                addError("Error creating new directory");
            }
        }
        return execute();
    }

    /**
     * Fetches and displays list of media files for the given directory. The
     * directory could be chosen by ID or path.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
        try {
            MediaDirectory directory;
            if (StringUtils.isNotEmpty(this.directoryId)) {
                directory = mediaFileManager.getMediaDirectory(this.directoryId);

            } else if (StringUtils.isNotEmpty(this.directoryName)) {
                directory = mediaFileManager.getMediaDirectoryByName(
                        getActionWeblog(), this.directoryName);
            } else {
                directory = mediaFileManager.getDefaultMediaDirectory(getActionWeblog());
            }
            this.directoryId = directory.getId();
            this.directoryName = directory.getName();

            this.childFiles = new ArrayList<>();
            this.childFiles.addAll(directory.getMediaFiles());

            if ("type".equals(sortBy)) {
                Collections.sort(this.childFiles, MediaFile.ContentTypeComparator);

            } else if ("date_uploaded".equals(sortBy)) {
                Collections.sort(this.childFiles, MediaFile.DateUploadedComparator);

            } else {
                // default to sort by name
                sortBy = "name";
                Collections.sort(this.childFiles, MediaFile.NameComparator);
            }

            this.currentDirectory = directory;
            setViewDirectoryId(directory.getId());

            return SUCCESS;

        } catch (Exception e) {
            log.error("Error viewing media file directory ", e);
            addError("MediaFile.error.view");
        }
        return SUCCESS;
    }

    /**
     * View the contents of another Media folder.
     */
    public String view() {
        try {
            if (!StringUtils.isEmpty(viewDirectoryId)) {
                setDirectoryId(viewDirectoryId);
                setCurrentDirectory(mediaFileManager.getMediaDirectory(viewDirectoryId));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up directory", ex);
        }
        return execute();
    }

    /**
     * Delete selected media files.
     * 
     */
    public String deleteSelected() {
        String[] fileIds = getSelectedMediaFiles();
        try {
            if (fileIds != null && fileIds.length > 0) {
                log.debug("Processing delete of " + fileIds.length
                        + " media files.");
                for (String fileId : fileIds) {
                    log.debug("Deleting media file - " + fileId);
                    MediaFile mediaFile = mediaFileManager.getMediaFile(fileId);
                    if (mediaFile != null) {
                        mediaFileManager.removeMediaFile(getActionWeblog(), mediaFile);
                    }
                }
            }

            weblogManager.saveWeblog(this.getActionWeblog());

            // flush changes
            WebloggerFactory.flush();
            // for some reason need to release to force a refresh of media directory.
            WebloggerFactory.release();
            addMessage("mediaFile.delete.success");

        } catch (WebloggerException e) {
            log.error("Error deleting selected media files", e);
            addError("mediaFile.delete.error");
        }
        return execute();
    }

    /**
     * Delete selected media file
     */
    public String delete() {
        try {
            log.debug("Processing delete of file id - " + getMediaFileId());
            MediaFile mediaFile = mediaFileManager.getMediaFile(getMediaFileId());
            mediaFileManager.removeMediaFile(getActionWeblog(), mediaFile);
            // flush changes
            WebloggerFactory.flush();
            addMessage("mediaFile.delete.success");
        } catch (WebloggerException e) {
            log.error("Error deleting media file", e);
            addError("mediaFile.delete.error", getMediaFileId());
        }
        return execute();
    }

    /**
     * Delete folder
     */
    public String deleteFolder() {

        try {
            if (directoryId != null) {
                log.debug("Deleting media file folder - " + directoryId + " ("
                        + directoryName + ")");
                MediaDirectory mediaFileDir = mediaFileManager.getMediaDirectory(directoryId);
                mediaFileManager.removeMediaDirectory(mediaFileDir);
                weblogManager.saveWeblog(getActionWeblog());
                strategy.flushAndInvalidateWeblog(getActionWeblog());
                addMessage("mediaFile.deleteFolder.success");

                // re-route to default folder
                mediaFileDir = mediaFileManager.getDefaultMediaDirectory(getActionWeblog());
                setDirectoryId(mediaFileDir.getId());
                setDirectoryName(mediaFileDir.getName());
            } else {
                log.error("(System error) No directory ID provided for media file folder delete.");
            }
        } catch (WebloggerException ex) {
            log.error("Error deleting folder", ex);
        }
        return execute();
    }

    /**
     * Move selected media files to a different directory
     * 
     */
    public String moveSelected() {
        String[] fileIds = getSelectedMediaFiles();
        try {
            int movedFiles = 0;

            if (fileIds != null && fileIds.length > 0) {
                log.debug("Processing move of " + fileIds.length
                        + " media files.");
                MediaDirectory targetDirectory = mediaFileManager.getMediaDirectory(getSelectedDirectory());
                for (String fileId : fileIds) {
                    log.debug("Moving media file - " + fileId + " to directory - " + getSelectedDirectory());
                    MediaFile mediaFile = mediaFileManager.getMediaFile(fileId);
                    if (mediaFile != null && !mediaFile.getDirectory().getId().equals(targetDirectory.getId())) {
                        mediaFileManager.moveMediaFile(mediaFile, targetDirectory);
                        movedFiles++;
                    }
                }
            }

            // flush changes
            WebloggerFactory.flush();
            if (movedFiles > 0) {
                addMessage("mediaFile.move.success");
            }

        } catch (WebloggerException e) {
            log.error("Error moving selected media files", e);
            addError("mediaFile.move.errors");
        }
        return execute();
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String id) {
        this.directoryId = id;
    }

    public List<MediaFile> getChildFiles() {
        return childFiles;
    }

    public void setChildFiles(List<MediaFile> files) {
        this.childFiles = files;
    }

    public String getNewDirectoryName() {
        return newDirectoryName;
    }

    public void setNewDirectoryName(String newDirectoryName) {
        this.newDirectoryName = newDirectoryName;
    }

    public MediaDirectory getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(MediaDirectory currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String path) {
        this.directoryName = path;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public List<Pair<String, String>> getSortOptions() {
        return SORT_OPTIONS;
    }

    public String getViewDirectoryId() {
        return viewDirectoryId;
    }

    public void setViewDirectoryId(String viewDirectoryId) {
        this.viewDirectoryId = viewDirectoryId;
    }

    public String getSelectedDirectory() {
        return selectedDirectory;
    }

    public void setSelectedDirectory(String selectedDirectory) {
        this.selectedDirectory = selectedDirectory;
    }

    public String[] getSelectedMediaFiles() {
        return selectedMediaFiles;
    }

    public void setSelectedMediaFiles(String[] selectedMediaFiles) {
        this.selectedMediaFiles = selectedMediaFiles;
    }

    public List<MediaDirectory> getAllDirectories() {
        return getActionWeblog().getMediaDirectories();
    }

    public String getMediaFileId() {
        return mediaFileId;
    }

    public void setMediaFileId(String mediaFileId) {
        this.mediaFileId = mediaFileId;
    }
}
