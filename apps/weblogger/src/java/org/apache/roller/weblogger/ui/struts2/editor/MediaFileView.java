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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FileIOException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileComparator;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileDirectoryComparator;
import org.apache.roller.weblogger.pojos.MediaFileComparator.MediaFileComparatorType;
import org.apache.roller.weblogger.pojos.MediaFileDirectoryComparator.DirectoryComparatorType;
import org.apache.roller.weblogger.ui.struts2.util.KeyValueObject;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * View media files.
 */
@SuppressWarnings("serial")
public class MediaFileView extends MediaFileBase {

    private static Log log = LogFactory.getLog(MediaFileView.class);
    // Drop-down values for sorting media files.
    private static final List<KeyValueObject> SORT_OPTIONS;

    static {
        SORT_OPTIONS = Arrays.asList(
                new KeyValueObject("", ""),
                new KeyValueObject("name", "Name"),
                new KeyValueObject("date_uploaded", "Date Uploaded"),
                new KeyValueObject("type", "Type"));
    }
    private String directoryId;
    private String directoryPath;
    private String sortBy;
    private String newDirectoryName;
    private List<MediaFile> childFiles;
    private MediaFileDirectory currentDirectory;
    private List<MediaFileDirectory> childDirectories;

    public MediaFileView() {
        this.actionName = "mediaFileView";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFileView.title";
    }

    /**
     * Prepares view action
     */
    public void myPrepare() {
        refreshAllDirectories();
    }

    /**
     * Create a new directory by name under current directory
     */
    public String createNewDirectory() {
        boolean dirCreated = false;
        if (StringUtils.isEmpty(this.newDirectoryName)) {
            addError("mediaFile.error.view.dirNameEmpty");
        } else if (this.newDirectoryName.contains("/")) {
            addError("mediaFile.error.view.dirNameInvalid");
        } else {
            try {
                log.debug("Creating new directory - " + this.newDirectoryName);
                MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
                MediaFileDirectory parentDirectory = manager.getMediaFileDirectory(this.directoryId);
                manager.createMediaFileDirectory(parentDirectory, this.newDirectoryName);
                // flush changes
                WebloggerFactory.getWeblogger().flush();
                addMessage("mediaFile.directoryCreate.success");
                dirCreated = true;
            } catch (WebloggerException e) {
                log.error("Error creating new directory by path", e);
                // TODO: i18n
                addError("Error creating new directory by path");
            }
        }


        if (dirCreated) {
            // Refresh list of directories so the newly created directory is included.
            refreshAllDirectories();
        }
        return execute();

    }

    /**
     * Returns directory content in JSON format.
     */
    public String fetchDirectoryContentLight() {
        execute();
        return "success.json";
    }

    /**
     * Fetches and displays list of media file for the given directory.
     * The directory could be chosen by ID or path.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
        MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
        try {
            MediaFileDirectory directory;
            if (this.directoryId != null) {
                directory = manager.getMediaFileDirectory(this.directoryId);
            } else if (this.directoryPath != null) {
                directory = manager.getMediaFileDirectoryByPath(getActionWeblog(), this.directoryPath);
                this.directoryId = directory.getId();
            } else {
                directory = manager.getMediaFileRootDirectory(getActionWeblog());
                this.directoryId = directory.getId();
            }

            this.childDirectories = new ArrayList<MediaFileDirectory>();
            this.childDirectories.addAll(directory.getChildDirectories());

            this.childFiles = new ArrayList<MediaFile>();
            this.childFiles.addAll(directory.getMediaFiles());

            if ("type".equals(sortBy)) {
                Collections.sort(this.childFiles,
                        new MediaFileComparator(MediaFileComparatorType.TYPE));

            } else if ("date_uploaded".equals(sortBy)) {
                Collections.sort(this.childFiles,
                        new MediaFileComparator(MediaFileComparatorType.DATE_UPLOADED));

            } else { // default to sort by name
                sortBy = "name";
                Collections.sort(this.childDirectories,
                        new MediaFileDirectoryComparator(DirectoryComparatorType.NAME));
                Collections.sort(this.childFiles,
                        new MediaFileComparator(MediaFileComparatorType.NAME));
            }

            this.currentDirectory = directory;

            return SUCCESS;

        } catch (FileIOException ex) {
            log.error("Error viewing media file directory ", ex);
            addError("MediaFile.error.view");
        } catch (Exception e) {
            log.error("Error viewing media file directory ", e);
            addError("MediaFile.error.view");
        }
        return SUCCESS;
    }

    /**
     * Returns the hierarchy of the current directory. This is useful in
     * displaying path information as breadcrumb.
     */
    public List<KeyValueObject> getCurrentDirectoryHierarchy() {
        List<KeyValueObject> directoryHierarchy = new ArrayList<KeyValueObject>();

        directoryHierarchy.add(new KeyValueObject("/", "root"));
        String fullPath = this.currentDirectory.getPath();
        if (fullPath.length() > 1) {
            String[] directoryNames = fullPath.substring(1).split("/");
            String directoryPath = "";
            for (String directoryName : directoryNames) {
                directoryPath = directoryPath + "/" + directoryName;
                directoryHierarchy.add(new KeyValueObject(directoryPath, directoryName));
            }
        }
        return directoryHierarchy;
    }

    /**
     * Delete selected media files.
     * 
     */
    public String deleteSelected() {
        doDeleteSelected();
        return execute();
    }

    /**
     * Delete selected media file
     * 
     */
    public String delete() {
        doDeleteMediaFile();
        return execute();
    }

    /**
     * Include selected media file in gallery
     * 
     */
    public String includeInGallery() {
        doIncludeMediaFileInGallery();
        return execute();
    }

    /**
     * Move selected media files to a different directory
     * 
     */
    public String moveSelected() {
        doMoveSelected();
        return execute();
    }

    public void myValidate() {
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String id) {
        this.directoryId = id;
    }

    public List<MediaFileDirectory> getChildDirectories() {
        return childDirectories;
    }

    public void setChildDirectories(List<MediaFileDirectory> directories) {
        this.childDirectories = directories;
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

    public MediaFileDirectory getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(MediaFileDirectory currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String path) {
        this.directoryPath = path;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public List<KeyValueObject> getSortOptions() {
        return SORT_OPTIONS;
    }
}
