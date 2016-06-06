/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.MediaDirectory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Select a media file from the blog entry edit screen.
 */
public class MediaFileImageChooser extends UIAction {
    private static Logger log = LoggerFactory.getLogger(MediaFileImageChooser.class);

    private String directoryId;

    private MediaDirectory currentDirectory;

    private List<MediaDirectory> otherDirectories;
    private List<MediaFile> childFiles;

    private MediaFileManager mediaFileManager;

    public void setMediaFileManager(MediaFileManager mediaFileManager) {
        this.mediaFileManager = mediaFileManager;
    }

    public MediaFileImageChooser() {
        actionName = "mediaFileImageChooser";
        desiredMenu = "editor";
        pageTitle = "mediaFileImageChooser.title";
        requiredGlobalRole = GlobalRole.BLOGGER;
        requiredWeblogRole = WeblogRole.POST;
    }

    /**
     * Fetches and displays list of media files for the given directory as well as other
     * directories the user can switch to.
     */
    @SkipValidation
    public String execute() {
        try {
            if (this.directoryId != null) {
                currentDirectory = mediaFileManager.getMediaDirectory(this.directoryId);
            } else {
                currentDirectory = mediaFileManager.getDefaultMediaDirectory(getActionWeblog());
                this.directoryId = currentDirectory.getId();
            }

            this.childFiles = new ArrayList<>();

            this.childFiles.addAll(currentDirectory.getMediaFiles().stream().collect(Collectors.toList()));

            Collections.sort(this.childFiles, MediaFile.NameComparator);

            // List of other directories user can switch to
            otherDirectories = mediaFileManager.getMediaDirectories(getActionWeblog());
            otherDirectories.remove(currentDirectory);

            return SUCCESS;
        } catch (Exception e) {
            log.error("Error viewing media file directory ", e);
            addError("MediaFile.error.view");
        }
        return SUCCESS;
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public List<MediaFile> getChildFiles() {
        return childFiles;
    }

    public MediaDirectory getCurrentDirectory() {
        return currentDirectory;
    }

    public List<MediaDirectory> getOtherDirectories() {
        return otherDirectories;
    }
}
