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
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.ui.struts2.pagers.MediaFilePager;
import org.apache.roller.weblogger.ui.struts2.util.KeyValueObject;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * View media files.
 */
@SuppressWarnings("serial")
public class MediaFileView extends MediaFileBase {

    private static Log log = LogFactory.getLog(MediaFileView.class);

    private String directoryId;
    private String directoryPath;
    private String sortBy;
    private String newDirectoryName;
    
    private List<MediaFile>          childFiles;
    private MediaFileDirectory       currentDirectory;
    private List<MediaFileDirectory> childDirectories;
    

    // Search criteria - drop-down for file type
    private static List<KeyValueObject> FILE_TYPES = null;

    // Search criteria - drop-down for size filter
    private static List<KeyValueObject> SIZE_FILTER_TYPES = null;

    // Search criteria - drop-down for size unit
    private static List<KeyValueObject> SIZE_UNITS = null;

    // Sort options for search results.
    private static List<KeyValueObject> SORT_OPTIONS = null;

    // Pager for displaying search results.
    private MediaFilePager pager;

    // Path of new directory to be created.
    private String newDirectoryPath;

    private MediaFileSearchBean bean = new MediaFileSearchBean();


    public MediaFileView() {
        this.actionName = "mediaFileView";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFileView.title";

        if (SIZE_FILTER_TYPES == null) {

            SIZE_FILTER_TYPES = Arrays.asList(
                new KeyValueObject("mediaFileView.gt", getText("mediaFileView.gt")),
                new KeyValueObject("mediaFileView.ge", getText("mediaFileView.ge")),
                new KeyValueObject("mediaFileView.eq", getText("mediaFileView.eq")),
                new KeyValueObject("mediaFileView.le", getText("mediaFileView.le")),
                new KeyValueObject("mediaFileView.lt", getText("mediaFileView.lt")));

            FILE_TYPES = Arrays.asList(
                new KeyValueObject("mediaFileView.any",   getText("mediaFileView.any")),
                new KeyValueObject("mediaFileView.others",   getText("mediaFileView.others")),
                new KeyValueObject("mediaFileView.image", getText("mediaFileView.image")),
                new KeyValueObject("mediaFileView.video", getText("mediaFileView.video")),
                new KeyValueObject("mediaFileView.audio", getText("mediaFileView.audio")));

            SIZE_UNITS = Arrays.asList(
                new KeyValueObject("mediaFileView.bytes", getText("mediaFileView.bytes")),
                new KeyValueObject("mediaFileView.kb",    getText("mediaFileView.kb")),
                new KeyValueObject("mediaFileView.mb",    getText("mediaFileView.mb")));

            SORT_OPTIONS = Arrays.asList(
                new KeyValueObject("mediaFileView.name", getText("mediaFileView.name")),
                new KeyValueObject("mediaFileView.date", getText("mediaFileView.date")),
                new KeyValueObject("mediaFileView.type", getText("mediaFileView.type")));
        }
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
            if (StringUtils.isNotEmpty(this.directoryId)) {
                directory = manager.getMediaFileDirectory(this.directoryId);

            } else if (StringUtils.isNotEmpty(this.directoryPath)) {
                directory = manager.getMediaFileDirectoryByPath(getActionWeblog(), this.directoryPath);

            } else {
                directory = manager.getMediaFileRootDirectory(getActionWeblog());
            }
            this.directoryId = directory.getId();
            this.directoryPath = directory.getPath();

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
     * Save a media file.
     *
     * @return String The result of the action.
     */
    public String search() {

        boolean valSuccess = myValidate();

        if (valSuccess) {
            MediaFileFilter filter = new MediaFileFilter();
            bean.copyTo(filter);
            MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
            try {
                List<MediaFile> rawResults = manager.searchMediaFiles(getActionWeblog(), filter);
                boolean hasMore = false;
                List<MediaFile> results = new ArrayList<MediaFile>();
                results.addAll(rawResults);
                if (results.size() > MediaFileSearchBean.PAGE_SIZE) {
                    results.remove(results.size() - 1);
                    hasMore = true;
                }

                this.pager = new MediaFilePager(bean.getPageNum(), results, hasMore);
                
            } catch (Exception e) {
                log.error("Error applying search criteria", e);
                // TODO: i18n
                addError("Error applying search criteria");
            }

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
        String dpath = "";
        if (fullPath.length() > 1) {
            String[] directoryNames = fullPath.substring(1).split("/");
            for (String directoryName : directoryNames) {
                dpath = dpath + "/" + directoryName;
                directoryHierarchy.add(new KeyValueObject(dpath, directoryName));
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

    /**
     * Validates search input
     */
    public boolean myValidate() {
        if (StringUtils.isEmpty(bean.getName()) && StringUtils.isEmpty(bean.getTags()) && StringUtils.isEmpty(bean.getType()) && bean.getSize() == 0) {
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

    public List<KeyValueObject> getFileTypes() {
        return FILE_TYPES;
    }

    public List<KeyValueObject> getSizeFilterTypes() {
        return SIZE_FILTER_TYPES;
    }

    public List<KeyValueObject> getSizeUnits() {
        return SIZE_UNITS;
    }

    public List<KeyValueObject> getSortOptions() {
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
}
