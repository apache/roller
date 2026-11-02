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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FileIOException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.ui.struts2.pagers.MediaFilePager;
import org.apache.roller.weblogger.ui.struts2.util.KeyValueObject;
import org.apache.roller.weblogger.util.URLUtilities;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * The media library: one page of the files in a folder, or of the files that
 * match a search in that folder or in all folders. Browsing and searching are
 * the same query, so both sort and page the same way. The page state travels
 * in GET parameters, so links, the back button and the pager keep it.
 */
@SuppressWarnings("serial")
// TODO: make this work @AllowedMethods({"execute","view","search","delete","deleteSelected","deleteFolder","includeInGallery","moveSelected"})
public class MediaFileView extends MediaFileBase {

    private static Log log = LogFactory.getLog(MediaFileView.class);

    private String directoryId;
    private String directoryName;
    private String newDirectoryName;

    private List<MediaFile> childFiles;
    private MediaFileDirectory currentDirectory;

    // Search criteria - drop-down for file type
    private static List<KeyValueObject> FILE_TYPES = null;

    // Search criteria - drop-down for size filter
    private static List<KeyValueObject> SIZE_FILTER_TYPES = null;

    // Search criteria - drop-down for size unit
    private static List<KeyValueObject> SIZE_UNITS = null;

    // Sort options
    private static List<KeyValueObject> SORT_OPTIONS = null;

    // the page of files shown
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
    @Override
    public void myPrepare() {

        if (SIZE_FILTER_TYPES == null) {

            SIZE_FILTER_TYPES = Arrays.asList(new KeyValueObject(
                    "mediaFileView.gt", getText("mediaFileView.gt")),
                    new KeyValueObject("mediaFileView.ge",
                            getText("mediaFileView.ge")), new KeyValueObject(
                            "mediaFileView.eq", getText("mediaFileView.eq")),
                    new KeyValueObject("mediaFileView.le",
                            getText("mediaFileView.le")), new KeyValueObject(
                            "mediaFileView.lt", getText("mediaFileView.lt")));

            // in the order the type chips show them
            FILE_TYPES = Arrays.asList(
                    new KeyValueObject("mediaFileView.any", getText("mediaFileView.any")),
                    new KeyValueObject("mediaFileView.image", getText("mediaFileView.image")),
                    new KeyValueObject("mediaFileView.video", getText("mediaFileView.video")),
                    new KeyValueObject("mediaFileView.audio", getText("mediaFileView.audio")),
                    new KeyValueObject("mediaFileView.others", getText("mediaFileView.others")));

            SIZE_UNITS = Arrays.asList(new KeyValueObject(
                    "mediaFileView.bytes", getText("mediaFileView.bytes")),
                    new KeyValueObject("mediaFileView.kb",
                            getText("mediaFileView.kb")), new KeyValueObject(
                            "mediaFileView.mb", getText("mediaFileView.mb")));

            SORT_OPTIONS = Arrays.asList(
                    new KeyValueObject(MediaFileSearchBean.SORT_NEWEST, getText("mediaFileView.sortNewest")),
                    new KeyValueObject(MediaFileSearchBean.SORT_OLDEST, getText("mediaFileView.sortOldest")),
                    new KeyValueObject(MediaFileSearchBean.SORT_NAME, getText("generic.name")),
                    new KeyValueObject(MediaFileSearchBean.SORT_TYPE, getText("mediaFileView.type")));
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

                    // Switch to the new, empty folder
                    setDirectoryId(dir.getId());
                    clearSearch();

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
     * Shows a page of the library: the folder chosen by ID or name (the
     * default folder otherwise), filtered by any search criteria.
     *
     * @return String The result of the action.
     */
    @SkipValidation
    @Override
    public String execute() {
        MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
        try {
            MediaFileDirectory directory = null;
            if (StringUtils.isNotEmpty(this.directoryId)) {
                directory = manager.getMediaFileDirectory(getActionWeblog(), this.directoryId);
            } else if (StringUtils.isNotEmpty(this.directoryName)) {
                directory = manager.getMediaFileDirectoryByName(
                        getActionWeblog(), this.directoryName);
            }
            if (directory == null) {
                directory = manager.getDefaultMediaFileDirectory(getActionWeblog());
            }
            this.currentDirectory = directory;
            this.directoryId = directory.getId();
            this.directoryName = directory.getName();
            setViewDirectoryId(directory.getId());

            MediaFileFilter filter = new MediaFileFilter();
            bean.copyTo(filter);
            if (!bean.isSearch() || !bean.isAllFolders()) {
                filter.setDirectory(directory);
            }

            long totalCount = manager.getMediaFileCount(getActionWeblog(), filter);
            if (totalCount > 0 && filter.getStartIndex() >= totalCount) {
                // past the end, e.g. after deleting the last files of the last page
                bean.setPage((int) ((totalCount - 1) / MediaFileSearchBean.PAGE_SIZE));
                bean.copyTo(filter);
            }
            List<MediaFile> files = new ArrayList<>(manager.searchMediaFiles(getActionWeblog(), filter));
            this.childFiles = files;
            this.pager = new MediaFilePager(getPagerBaseURL(), bean.getPage(), files,
                    totalCount, MediaFileSearchBean.PAGE_SIZE);

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
     * Returns directory content in JSON format.
     */
    public String fetchDirectoryContentLight() {
        execute();
        return "success.json";
    }

    /**
     * View the contents of another Media folder.
     */
    public String view() {
        if (!StringUtils.isEmpty(viewDirectoryId)) {
            setDirectoryId(viewDirectoryId);
        }
        return execute();
    }

    /**
     * Search. Kept for old links; the library searches whenever criteria are set.
     */
    @SkipValidation
    public String search() {
        return execute();
    }

    private void clearSearch() {
        MediaFileSearchBean fresh = new MediaFileSearchBean();
        fresh.setSortBy(bean.getSortBy());
        this.bean = fresh;
    }

    /**
     * The library state, as an encoded query string without a leading "?":
     * weblog, folder, criteria, sort and page. Pages linked from the library
     * pass it back so the user returns to the same place.
     */
    public String getLibraryQuery() {
        Map<String, String> params = stateParams();
        if (bean.getPage() > 0) {
            params.put("bean.page", Integer.toString(bean.getPage()));
        }
        return queryString(params);
    }

    private String getPagerBaseURL() {
        return WebloggerRuntimeConfig.getRelativeContextURL()
                + "/roller-ui/authoring/mediaFileView.rol?" + queryString(stateParams());
    }

    private Map<String, String> stateParams() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("weblog", getActionWeblog().getHandle());
        if (directoryId != null) {
            params.put("directoryId", directoryId);
        }
        params.putAll(bean.toParams());
        return params;
    }

    private static String queryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLUtilities.encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    /** True when search criteria are set, rather than browsing a folder. */
    public boolean isSearch() {
        return bean.isSearch();
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
            MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
            if (directoryId != null) {
                log.debug("Deleting media file folder - " + directoryId + " ("
                        + directoryName + ")");
                MediaFileDirectory mediaFileDir = manager
                        .getMediaFileDirectory(getActionWeblog(), directoryId);
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
                clearSearch();
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

    @StrutsParameter
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

    @StrutsParameter
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

    @StrutsParameter
    public void setDirectoryName(String path) {
        this.directoryName = path;
    }

    @StrutsParameter(depth = 1)
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

    public String getViewDirectoryId() {
        return viewDirectoryId;
    }

    @StrutsParameter
    public void setViewDirectoryId(String viewDirectoryId) {
        this.viewDirectoryId = viewDirectoryId;
    }

}
