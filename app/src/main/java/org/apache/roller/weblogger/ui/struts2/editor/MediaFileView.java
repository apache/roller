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
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.ui.struts2.pagers.MediaFilePager;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * View media files.
 */
@SuppressWarnings("serial")
public class MediaFileView extends MediaFileBase {

    private static Log log = LogFactory.getLog(MediaFileView.class);

    private String directoryId;
    private String directoryName;
    private String sortBy;
    private String newDirectoryName;

    private List<MediaFile> childFiles;
    private MediaFileDirectory currentDirectory;

    // Search criteria - drop-down for file type
    private static List<Pair<String, String>> FILE_TYPES = null;

    // Search criteria - drop-down for size filter
    private static List<Pair<String, String>> SIZE_FILTER_TYPES = null;

    // Search criteria - drop-down for size unit
    private static List<Pair<String, String>> SIZE_UNITS = null;

    // Sort options for search results.
    private static List<Pair<String, String>> SORT_OPTIONS = null;

    // Pager for displaying search results.
    private MediaFilePager pager;

    // Path of new directory to be created.
    private String newDirectoryPath;

    // a new directory the user wishes to view
    private String viewDirectoryId = null;

    private MediaFileSearchBean bean = new MediaFileSearchBean();

    public MediaFileView() {
        this.actionName = "mediaFileView";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFileView.title";
    }

    /**
     * Prepares view action
     */
    public void myPrepare() {

        if (SIZE_FILTER_TYPES == null) {

            SIZE_FILTER_TYPES = Arrays.asList(
                    Pair.of("mediaFileView.gt", getText("mediaFileView.gt")),
                    Pair.of("mediaFileView.ge", getText("mediaFileView.ge")),
                    Pair.of("mediaFileView.eq", getText("mediaFileView.eq")),
                    Pair.of("mediaFileView.le", getText("mediaFileView.le")),
                    Pair.of("mediaFileView.lt", getText("mediaFileView.lt")));

            FILE_TYPES = Arrays.asList(
                    Pair.of("mediaFileView.any", getText("mediaFileView.any")),
                    Pair.of("mediaFileView.others", getText("mediaFileView.others")),
                    Pair.of("mediaFileView.image", getText("mediaFileView.image")),
                    Pair.of("mediaFileView.video", getText("mediaFileView.video")),
                    Pair.of("mediaFileView.audio", getText("mediaFileView.audio")));

            SIZE_UNITS = Arrays.asList(
                    Pair.of("mediaFileView.bytes", getText("mediaFileView.bytes")),
                    Pair.of("mediaFileView.kb", getText("mediaFileView.kb")),
                    Pair.of("mediaFileView.mb", getText("mediaFileView.mb")));

            SORT_OPTIONS = Arrays.asList(
                    Pair.of("name", getText("generic.name")),
                    Pair.of("date_uploaded", getText("mediaFileView.date")),
                    Pair.of("type", getText("mediaFileView.type")));
        }

        refreshAllDirectories();
    }

    /**
     * Create a new directory by name. All folders placed at the root.
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
                MediaFileManager manager = WebloggerFactory.getWeblogger()
                        .getMediaFileManager();

                if (!getActionWeblog().hasMediaFileDirectory(
                        this.newDirectoryName)) {
                    // Create
                    MediaFileDirectory dir = manager.createMediaFileDirectory(
                            getActionWeblog(), this.newDirectoryName);
                    // flush changes
                    WebloggerFactory.getWeblogger().flush();
                    addMessage("mediaFile.directoryCreate.success",
                            this.newDirectoryName);

                    // Switch to folder
                    setDirectoryId(dir.getId());

                    dirCreated = true;
                } else {
                    // already exists
                    addMessage("mediaFile.directoryCreate.error.exists",
                            this.newDirectoryName);
                }

            } catch (WebloggerException e) {
                log.error("Error creating new directory", e);
                addError("Error creating new directory");
            }
        }

        if (dirCreated) {
            // Refresh list of directories so the newly created directory is
            // included.
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
     * Fetches and displays list of media files for the given directory. The
     * directory could be chosen by ID or path.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
        MediaFileManager manager = WebloggerFactory.getWeblogger()
                .getMediaFileManager();
        try {
            MediaFileDirectory directory;
            if (StringUtils.isNotEmpty(this.directoryId)) {
                directory = manager.getMediaFileDirectory(this.directoryId);

            } else if (StringUtils.isNotEmpty(this.directoryName)) {
                directory = manager.getMediaFileDirectoryByName(
                        getActionWeblog(), this.directoryName);

            } else {
                directory = manager
                        .getDefaultMediaFileDirectory(getActionWeblog());
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

            // set current directory if valid
            if (directory != null) {
                setViewDirectoryId(directory.getId());
            }

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
            MediaFileManager manager = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();
            if (!StringUtils.isEmpty(viewDirectoryId)) {
                setDirectoryId(viewDirectoryId);
                setCurrentDirectory(manager
                        .getMediaFileDirectory(viewDirectoryId));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up directory", ex);
        }
        return execute();
    }

    /**
     * Save a media file.
     * 
     * @return String The result of the action.
     */
    public String search() {

        boolean valSuccess = myValidate();

        if (valSuccess) {
            MediaFileFilter filter = new MediaFileFilter();
            bean.copyTo(filter);
            MediaFileManager manager = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();
            try {
                List<MediaFile> rawResults = manager.searchMediaFiles(
                        getActionWeblog(), filter);
                boolean hasMore = false;
                List<MediaFile> results = new ArrayList<MediaFile>();
                results.addAll(rawResults);
                if (results.size() > MediaFileSearchBean.PAGE_SIZE) {
                    results.remove(results.size() - 1);
                    hasMore = true;
                }

                this.pager = new MediaFilePager(bean.getPageNum(), results,
                        hasMore);

            } catch (Exception e) {
                log.error("Error applying search criteria", e);
                addError("Error applying search criteria - check Roller logs");
            }

        }

        return SUCCESS;
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
     * Delete folder
     */
    public String deleteFolder() {

        try {
            MediaFileManager manager = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();
            if (directoryId != null) {
                log.debug("Deleting media file folder - " + directoryId + " ("
                        + directoryName + ")");
                MediaFileDirectory mediaFileDir = manager
                        .getMediaFileDirectory(directoryId);
                manager.removeMediaFileDirectory(mediaFileDir);
                refreshAllDirectories();
                WebloggerFactory.getWeblogger().getWeblogManager()
                        .saveWeblog(this.getActionWeblog());

                // flush changes
                WebloggerFactory.getWeblogger().flush();
                WebloggerFactory.getWeblogger().release();
                addMessage("mediaFile.deleteFolder.success");

                // notify caches
                CacheManager.invalidate(getActionWeblog());

                // re-route to default folder
                mediaFileDir = manager
                        .getDefaultMediaFileDirectory(getActionWeblog());
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

    public MediaFileDirectory getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(MediaFileDirectory currentDirectory) {
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

    /**
     * Validates search input
     */
    public boolean myValidate() {
        if (StringUtils.isEmpty(bean.getName())
                && StringUtils.isEmpty(bean.getTags())
                && StringUtils.isEmpty(bean.getType()) && bean.getSize() == 0) {
            addError("MediaFile.error.search.empty");
            return false;
        }
        return true;
    }

    public MediaFileSearchBean getBean() {
        return bean;
    }

    public void setBean(MediaFileSearchBean b) {
        this.bean = b;
    }

    public List<Pair<String, String>> getFileTypes() {
        return FILE_TYPES;
    }

    public List<Pair<String, String>> getSizeFilterTypes() {
        return SIZE_FILTER_TYPES;
    }

    public List<Pair<String, String>> getSizeUnits() {
        return SIZE_UNITS;
    }

    public List<Pair<String, String>> getSortOptions() {
        return SORT_OPTIONS;
    }

    public MediaFilePager getPager() {
        return pager;
    }

    public void setPager(MediaFilePager pager) {
        this.pager = pager;
    }

    public String getNewDirectoryPath() {
        return newDirectoryPath;
    }

    public void setNewDirectoryPath(String newDirectoryPath) {
        this.newDirectoryPath = newDirectoryPath;
    }

    public String getViewDirectoryId() {
        return viewDirectoryId;
    }

    public void setViewDirectoryId(String viewDirectoryId) {
        this.viewDirectoryId = viewDirectoryId;
    }

}
