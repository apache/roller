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
package org.apache.roller.weblogger.business;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.RollerMessages;

/**
 * Interface to media file management.
 */
public interface MediaFileManager {

    int MAX_WIDTH = 120;
    int MAX_HEIGHT = 120;

    /**
     * Initialization; deal with upgrade migrations, etc.
     */
    void initialize();

    /**
     * Release all resources associated with Roller session.
     */
    void release();

    /**
     * Create a media file
     */
    void createMediaFile(Weblog weblog, MediaFile mediaFile,
            RollerMessages errors) throws WebloggerException;

    /**
     * Update metadata for a media file
     */
    void updateMediaFile(Weblog weblog, MediaFile mediaFile)
            throws WebloggerException;

    /**
     * Update metadata for a media file and content.
     */
    void updateMediaFile(Weblog website, MediaFile mf, InputStream fis)
            throws WebloggerException;

    /**
     * Get media file metadata by file id
     */
    MediaFile getMediaFile(String id) throws WebloggerException;

    /**
     * Get media file metadata optionally including the actual content
     */
    MediaFile getMediaFile(String id, boolean includeContent)
            throws WebloggerException;

    /**
     * Delete a media file
     */
    void removeMediaFile(Weblog weblog, MediaFile mediaFile)
            throws WebloggerException;

    /**
     * Search for media files based on the filter criteria
     */
    List<MediaFile> searchMediaFiles(Weblog weblog,
            MediaFileFilter filter) throws WebloggerException;

    /**
     * Create root directory for media files in a weblog.
     */
    MediaFileDirectory createRootMediaFileDirectory(Weblog weblog)
            throws WebloggerException;

    /**
     * Create a media file directory with the given name
     */
    MediaFileDirectory createMediaFileDirectory(
            MediaFileDirectory parentDirectory, String newDirName)
            throws WebloggerException;

    /**
     * Create a media file directory
     */
    void createMediaFileDirectory(MediaFileDirectory directory)
            throws WebloggerException;

    /**
     * Create a media file directory given its path
     */
    MediaFileDirectory createMediaFileDirectoryByPath(Weblog weblog,
            String path) throws WebloggerException;

    /**
     * Get media file directory by id
     */
    MediaFileDirectory getMediaFileDirectory(String id)
            throws WebloggerException;

    /**
     * Get media file directory by its path
     */
    MediaFileDirectory getMediaFileDirectoryByPath(Weblog weblog,
            String path) throws WebloggerException;

    /**
     * Get media file by path.
     */
    MediaFile getMediaFileByPath(Weblog weblog, String path)
            throws WebloggerException;

    /**
     * Get media file by the original path by which it was stored. Required for
     * support of old upload file URLs and for theme resources. {@inheritDoc}
     */
    MediaFile getMediaFileByOriginalPath(Weblog weblog, String origpath)
            throws WebloggerException;

    /**
     * Get the list of media file directories for the given weblog.
     */
    List<MediaFileDirectory> getMediaFileDirectories(Weblog weblog)
            throws WebloggerException;

    /**
     * Get the root directory for media files for the given weblog.
     */
    MediaFileDirectory getMediaFileRootDirectory(Weblog weblog)
            throws WebloggerException;

    /**
     * Move a set of media files to a new directory.
     */
    void moveMediaFiles(Collection<MediaFile> mediaFiles,
            MediaFileDirectory directory) throws WebloggerException;

    /**
     * Move one media file to a new directory.
     */
    void moveMediaFile(MediaFile mediaFile, MediaFileDirectory directory)
            throws WebloggerException;

    /**
     * Move a set of media files to a new directory.
     */
    void moveMediaFileDirectories(
            Collection<MediaFileDirectory> mediaFilesDir,
            MediaFileDirectory directory) throws WebloggerException;

    /**
     * Move one media file to a new directory.
     */
    void moveMediaFileDirectory(MediaFileDirectory mediaFileDir,
            MediaFileDirectory directory) throws WebloggerException;

    /**
     * Return recently added media files that are public.
     */
    List<MediaFile> fetchRecentPublicMediaFiles(int length)
            throws WebloggerException;

    /**
     * Remove all media files associated with a weblog.
     * 
     * @param website
     */
    void removeAllFiles(Weblog website) throws WebloggerException;

    /**
     * Remove media file directory
     * 
     * @param mediaFileDir
     */
    void removeMediaFileDirectory(MediaFileDirectory mediaFileDir)
            throws WebloggerException;

    /**
     * Remove tag with given name from given MediaFile
     * 
     * @param name
     *            Name of tag to be removed
     */
    void removeMediaFileTag(String name, MediaFile entry)
            throws WebloggerException;
}
