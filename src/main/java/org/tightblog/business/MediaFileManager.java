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
package org.tightblog.business;

import org.tightblog.pojos.MediaDirectory;
import org.tightblog.pojos.MediaFile;
import org.tightblog.pojos.Weblog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface to media file management.
 */
public interface MediaFileManager {

    int MAX_THUMBNAIL_WIDTH = 120;
    int MAX_THUMBNAIL_HEIGHT = 120;

    /**
     * Update metadata for a media file and content.
     * @param mf - Media File to update
     * @param is - if non-null, file's contents will be replaced with the contents of this InputStream.
     * @param errors object to receive message bundle keys and argument values or null if not desired to receive them
     */
    void storeMediaFile(MediaFile mf, InputStream is, Map<String, List<String>> errors) throws IOException;

    /**
     * Get media file metadata by file id, null if does not exist
     */
    MediaFile getMediaFile(String id);

    /**
     * Get media file metadata optionally including the actual content
     * @return MediaFile object or null if unavailable/inaccessible.
     */
    MediaFile getMediaFile(String id, boolean includeContent);

    /**
     * Create a media file directory with a given name.
     */
    MediaDirectory createMediaDirectory(Weblog weblog, String name);

    /**
     * Get media file directory by id
     */
    MediaDirectory getMediaDirectory(String id);

    /**
     * Get the list of media file directories for the given weblog.
     */
    List<MediaDirectory> getMediaDirectories(Weblog weblog);

    /**
     * Move a set of media files to a new directory.
     */
    void moveMediaFiles(Collection<MediaFile> mediaFiles, MediaDirectory directory);

    /**
     * Move one media file to a new directory.
     */
    void moveMediaFile(MediaFile mediaFile, MediaDirectory directory);

    /**
     * Remove all media content (files and directories) associated with a weblog.
     */
    void removeAllFiles(Weblog weblog);

    /**
     * Delete a directory and all of its associated file contents
     */
    void removeAllFiles(MediaDirectory mediaFileDir);

    /**
     * Delete a media file and its associated file contents
     */
    void removeMediaFile(Weblog weblog, MediaFile mediaFile);
}
