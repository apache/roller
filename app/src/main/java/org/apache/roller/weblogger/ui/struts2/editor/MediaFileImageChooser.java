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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileDirectoryComparator;
import org.apache.roller.weblogger.pojos.MediaFileDirectoryComparator.DirectoryComparatorType;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.pagers.MediaFilePager;
import org.apache.roller.weblogger.ui.struts2.util.KeyValueObject;
import org.apache.roller.weblogger.util.URLUtilities;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Browse media files action.
 */
@SuppressWarnings("serial")
// TODO: make this work @AllowedMethods({"execute"})
public class MediaFileImageChooser extends MediaFileBase {
    private static Log log = LogFactory.getLog(MediaFileImageChooser.class);

    private String directoryId;
    private String directoryName;

    private List<MediaFile> childFiles;
    private MediaFileDirectory currentDirectory;

    // name search and page; the chooser always shows one folder, newest first
    private MediaFileSearchBean bean = new MediaFileSearchBean();
    private MediaFilePager pager;

    public MediaFileImageChooser() {
        this.actionName = "mediaFileImageChooser";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFileImaegChooser.title";
    }

    /**
     * Prepares view action
     */
    @Override
    public void myPrepare() {
        refreshAllDirectories();
    }

    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.EDIT_DRAFT);
    }

    /**
     * Fetches and displays list of media file for the given directory. The
     * directory could be chosen by ID or path.
     * 
     * @return String The result of the action.
     */
    /**
     * Shows a page of a folder's files, newest first, optionally only those
     * whose name starts with the search text. Same query as the library.
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
                directory = manager.getMediaFileDirectoryByName(getActionWeblog(), this.directoryName);
            }
            if (directory == null) {
                directory = manager.getDefaultMediaFileDirectory(getActionWeblog());
            }
            this.currentDirectory = directory;
            this.directoryId = directory.getId();

            bean.setSortBy(MediaFileSearchBean.SORT_NEWEST);
            bean.setScope(MediaFileSearchBean.SCOPE_FOLDER);
            MediaFileFilter filter = new MediaFileFilter();
            bean.copyTo(filter);
            filter.setDirectory(directory);

            long totalCount = manager.getMediaFileCount(getActionWeblog(), filter);
            this.childFiles = new ArrayList<>(manager.searchMediaFiles(getActionWeblog(), filter));

            Map<String, String> params = new LinkedHashMap<>();
            params.put("weblog", getActionWeblog().getHandle());
            params.put("directoryId", directoryId);
            params.putAll(bean.toParams());
            String baseUrl = WebloggerRuntimeConfig.getRelativeContextURL()
                    + "/roller-ui/authoring/overlay/mediaFileImageChooser.rol?"
                    + params.entrySet().stream()
                        .map(e -> e.getKey() + "=" + URLUtilities.encode(e.getValue()))
                        .collect(Collectors.joining("&"));
            this.pager = new MediaFilePager(baseUrl, bean.getPage(), childFiles, totalCount,
                    MediaFileSearchBean.PAGE_SIZE);

            List<MediaFileDirectory> sortedDirList = new ArrayList<>(
                    manager.getMediaFileDirectories(getActionWeblog()));
            sortedDirList.sort(new MediaFileDirectoryComparator(DirectoryComparatorType.NAME));
            setAllDirectories(sortedDirList);

        } catch (Exception ex) {
            log.error("Error viewing media file directory ", ex);
            addError("MediaFile.error.view");
        }
        return SUCCESS;
    }

    public MediaFilePager getPager() {
        return pager;
    }

    @StrutsParameter(depth = 1)
    public MediaFileSearchBean getBean() {
        return bean;
    }

    public void setBean(MediaFileSearchBean bean) {
        this.bean = bean;
    }

    public List<KeyValueObject> getCurrentDirectoryHierarchy() {
        List<KeyValueObject> directoryHierarchy = new ArrayList<>();

        String fullPath = "/" + this.currentDirectory.getName();
        if (fullPath.length() > 1) {
            String[] directoryNames = fullPath.substring(1).split("/");
            String dirPath = "";
            for (String directoryName : directoryNames) {
                dirPath = dirPath + "/" + directoryName;
                directoryHierarchy.add(new KeyValueObject(dirPath,
                        directoryName));
            }
        }
        return directoryHierarchy;
    }

    /**
     * @return the directoryId
     */
    public String getDirectoryId() {
        return directoryId;
    }

    /**
     * @param directoryId
     *            the directoryId to set
     */
    @StrutsParameter
    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    /**
     * @return the directory name
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

    /**
     * @return the childFiles
     */
    public List<MediaFile> getChildFiles() {
        return childFiles;
    }

    /**
     * @param childFiles
     *            the childFiles to set
     */
    public void setChildFiles(List<MediaFile> childFiles) {
        this.childFiles = childFiles;
    }

    /**
     * @return the currentDirectory
     */
    public MediaFileDirectory getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * @param currentDirectory
     *            the currentDirectory to set
     */
    public void setCurrentDirectory(MediaFileDirectory currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

}
