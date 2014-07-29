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
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileDirectoryComparator;
import org.apache.roller.weblogger.pojos.MediaFileDirectoryComparator.DirectoryComparatorType;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Base class for all actions related to media files.
 * 
 */
@SuppressWarnings("serial")
public class MediaFileBase extends UIAction {

    private static Log log = LogFactory.getLog(MediaFileBase.class);
    private String[] selectedMediaFiles;
    private String[] selectedMediaFileDirectories;
    private String selectedDirectory;
    private String mediaFileId;
    private List<MediaFileDirectory> allDirectories;
    private boolean overlayMode;

    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.POST);
    }

    /**
     * Deletes media file
     */
    protected void doDeleteMediaFile() {

        try {
            log.debug("Processing delete of file id - " + this.mediaFileId);
            MediaFileManager manager = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();
            MediaFile mediaFile = manager.getMediaFile(this.mediaFileId);
            manager.removeMediaFile(getActionWeblog(), mediaFile);
            // flush changes
            WebloggerFactory.getWeblogger().flush();
            WebloggerFactory.getWeblogger().release();
            addMessage("mediaFile.delete.success");
        } catch (WebloggerException e) {
            log.error("Error deleting media file", e);
            addError("mediaFile.delete.error", this.mediaFileId);
        }
    }

    /**
     * Shares media file for public gallery
     */
    protected void doIncludeMediaFileInGallery() {

        try {
            log.debug("Processing include-in-gallery of file id - "
                    + this.mediaFileId);
            MediaFileManager manager = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();
            MediaFile mediaFile = manager.getMediaFile(this.mediaFileId);
            mediaFile.setSharedForGallery(true);
            manager.updateMediaFile(getActionWeblog(), mediaFile);
            // flush changes
            WebloggerFactory.getWeblogger().flush();
            addMessage("mediaFile.includeInGallery.success");
        } catch (WebloggerException e) {
            log.error("Error including media file in gallery", e);
            addError("mediaFile.includeInGallery.error", this.mediaFileId);
        }
    }

    /**
     * Delete selected media files.
     */
    protected void doDeleteSelected() {
        String[] fileIds = getSelectedMediaFiles();
        String[] dirIds = getSelectedMediaFileDirectories();
        try {
            MediaFileManager manager = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();

            if (fileIds != null && fileIds.length > 0) {
                log.debug("Processing delete of " + fileIds.length
                        + " media files.");
                for (String fileId : fileIds) {
                    log.debug("Deleting media file - " + fileId);
                    MediaFile mediaFile = manager.getMediaFile(fileId);
                    if (mediaFile != null) {
                        manager.removeMediaFile(getActionWeblog(), mediaFile);
                    }
                }
            }

            WebloggerFactory.getWeblogger().getWeblogManager()
                    .saveWeblog(this.getActionWeblog());

            // flush changes
            WebloggerFactory.getWeblogger().flush();
            WebloggerFactory.getWeblogger().release();
            addMessage("mediaFile.delete.success");

        } catch (WebloggerException e) {
            log.error("Error deleting selected media files", e);
            addError("mediaFile.delete.error");
        }
    }

    /**
     * Move selected media files to a directory.
     */
    protected void doMoveSelected() {
        String[] fileIds = getSelectedMediaFiles();
        String[] dirIds = getSelectedMediaFileDirectories();
        try {
            int movedFiles = 0;
            MediaFileManager manager = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();

            if (fileIds != null && fileIds.length > 0) {
                log.debug("Processing move of " + fileIds.length
                        + " media files.");
                MediaFileDirectory targetDirectory = manager
                        .getMediaFileDirectory(this.selectedDirectory);
                for (String fileId : fileIds) {
                    log.debug("Moving media file - " + fileId
                            + " to directory - " + this.selectedDirectory);
                    MediaFile mediaFile = manager.getMediaFile(fileId);
                    if (mediaFile != null && !mediaFile.getDirectory().getId().equals(targetDirectory.getId())) {
                        manager.moveMediaFile(mediaFile, targetDirectory);
                        movedFiles++;
                    }
                }
            }

            // flush changes
            WebloggerFactory.getWeblogger().flush();
            WebloggerFactory.getWeblogger().release();
            if (movedFiles > 0) {
                addMessage("mediaFile.move.success");
            }

        } catch (WebloggerException e) {
            log.error("Error moving selected media files", e);
            addError("mediaFile.move.errors");
        }
    }

    /**
     * Refresh the list of directories.
     */
    protected void refreshAllDirectories() {
        try {
            MediaFileManager mgr = WebloggerFactory.getWeblogger()
                    .getMediaFileManager();
            List<MediaFileDirectory> directories = mgr.getMediaFileDirectories(getActionWeblog());
            List<MediaFileDirectory> sortedDirList = new ArrayList<MediaFileDirectory>();
            sortedDirList.addAll(directories);
            Collections.sort(sortedDirList, new MediaFileDirectoryComparator(
                    DirectoryComparatorType.NAME));
            setAllDirectories(sortedDirList);
        } catch (WebloggerException ex) {
            log.error("Error looking up media file directories", ex);
        }
    }

    public String[] getSelectedMediaFiles() {
        return selectedMediaFiles;
    }

    public void setSelectedMediaFiles(String[] selectedMediaFiles) {
        this.selectedMediaFiles = selectedMediaFiles;
    }

    public String getSelectedDirectory() {
        return selectedDirectory;
    }

    public void setSelectedDirectory(String selectedDirectory) {
        this.selectedDirectory = selectedDirectory;
    }

    public List<MediaFileDirectory> getAllDirectories() {
        return allDirectories;
    }

    public void setAllDirectories(List<MediaFileDirectory> allDirectories) {
        this.allDirectories = allDirectories;
    }

    public String getMediaFileId() {
        return mediaFileId;
    }

    public void setMediaFileId(String mediaFileId) {
        this.mediaFileId = mediaFileId;
    }

    public boolean isOverlayMode() {
        return overlayMode;

    }

    public void setOverlayMode(boolean mode) {
        this.overlayMode = mode;
    }

    /**
     * @return the selectedMediaFileDirectories
     */
    public String[] getSelectedMediaFileDirectories() {
        return selectedMediaFileDirectories;
    }

    /**
     * @param selectedMediaFileDirectories
     *            the selectedMediaFileDirectories to set
     */
    public void setSelectedMediaFileDirectories(
            String[] selectedMediaFileDirectories) {
        this.selectedMediaFileDirectories = selectedMediaFileDirectories;
    }
}
