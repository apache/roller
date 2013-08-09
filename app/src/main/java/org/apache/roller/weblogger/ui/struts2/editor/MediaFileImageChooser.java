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
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.FileIOException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileComparator;
import org.apache.roller.weblogger.pojos.MediaFileComparator.MediaFileComparatorType;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileDirectoryComparator;
import org.apache.roller.weblogger.pojos.MediaFileDirectoryComparator.DirectoryComparatorType;
import org.apache.roller.weblogger.ui.struts2.util.KeyValueObject;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Browse media files action.
 */
@SuppressWarnings("serial")
public class MediaFileImageChooser extends MediaFileBase {
    private static Log log = LogFactory.getLog(MediaFileImageChooser.class);

    private String directoryId;
    private String directoryPath;

    private List<MediaFile>          childFiles;
    private MediaFileDirectory       currentDirectory;
    private List<MediaFileDirectory> childDirectories;

    public MediaFileImageChooser() {
        this.actionName = "mediaFileImageChooser";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFileImaegChooser.title";
    }

    /**
     * Prepares view action
     */
    public void myPrepare() {
        refreshAllDirectories();
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

            // only add image files
            for (MediaFile mf : directory.getMediaFiles()) {
                if (mf.isImageFile()) {
                    this.childFiles.add(mf);
                }
            }

            Collections.sort(this.childDirectories,
                    new MediaFileDirectoryComparator(DirectoryComparatorType.NAME));
            Collections.sort(this.childFiles,
                    new MediaFileComparator(MediaFileComparatorType.NAME));

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
     * @return the directoryId
     */
    public String getDirectoryId() {
        return directoryId;
    }

    /**
     * @param directoryId the directoryId to set
     */
    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    /**
     * @return the directoryPath
     */
    public String getDirectoryPath() {
        return directoryPath;
    }

    /**
     * @param directoryPath the directoryPath to set
     */
    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * @return the childFiles
     */
    public List<MediaFile> getChildFiles() {
        return childFiles;
    }

    /**
     * @param childFiles the childFiles to set
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
     * @param currentDirectory the currentDirectory to set
     */
    public void setCurrentDirectory(MediaFileDirectory currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    /**
     * @return the childDirectories
     */
    public List<MediaFileDirectory> getChildDirectories() {
        return childDirectories;
    }

    /**
     * @param childDirectories the childDirectories to set
     */
    public void setChildDirectories(List<MediaFileDirectory> childDirectories) {
        this.childDirectories = childDirectories;
    }
}
