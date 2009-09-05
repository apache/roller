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
package org.apache.roller.weblogger.pojos;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.business.WebloggerFactory;

/**
 * Representa a media file
 *
 */
public class MediaFile {

    private static Log log =
            LogFactory.getFactory().getInstance(MediaFile.class);

    final String id;

    private String      name;
    private String      description;
    private String      copyrightText;
    private Boolean     isSharedForGallery;
    long                length;
    private String      contentType;
    private String      originalPath;
    private InputStream is;
    private FileContent content;
    private Timestamp   dateUploaded;
    private Timestamp   lastUpdated;
    private String      creatorUserName;
    private MediaFileDirectory directory;
    private Set<MediaFileTag>  tags;
    
    // TODO: anchor to be populated
    private String      anchor;


    public MediaFile() {
        this.id = UUIDGenerator.generateUUID();
    }

    /**
     * Name for the media file
     *
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Description for media file
     *
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Copyright text for media file
     *
     */
    public String getCopyrightText() {
        return copyrightText;
    }

    public void setCopyrightText(String copyrightText) {
        this.copyrightText = copyrightText;
    }

    /**
     * Is media file shared for gallery
     *
     */
    public Boolean isSharedForGallery() {
        return isSharedForGallery;
    }

    public void setSharedForGallery(Boolean isSharedForGallery) {
        this.isSharedForGallery = isSharedForGallery;
    }

    /**
     * Size of the media file
     *
     */
    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Date uploaded
     * 
     */
    public Timestamp getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Timestamp dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public long getLastModified() {
        return lastUpdated.getTime();
    }

    /**
     * Last updated timestamp
     *
     */
    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp time) {
        this.lastUpdated = time;
    }

    public MediaFileDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(MediaFileDirectory dir) {
        this.directory = dir;
    }

    /**
     * Set of tags for this media file
     *
     */
    public Set<MediaFileTag> getTags() {
        return tags;
    }

    public void setTags(Set<MediaFileTag> tags) {
        this.tags = tags;
    }

    /**
     * Content type of the media file
     *
     */
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Database surrogate key.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getId() {
        return id;
    }

    public String getPath() {
        return directory.getPath();
    }

    /**
     * Returns input stream for the underlying file in the file system.
     * @return
     */
    public InputStream getInputStream() {
        if (is != null) {
            return is;
        } else if (content != null) {
            return content.getInputStream();
        }
        return null;
    }

    public void setInputStream(InputStream is) {
        this.is = is;
    }

    public void setContent(FileContent content) {
        this.content = content;
    }

    /**
     * Indicates whether this is an image file.
     *
     */
    public boolean isImageFile() {
        if (this.contentType == null) {
            return false;
        }
        return (this.contentType.toLowerCase().startsWith(
                MediaFileType.IMAGE.getContentTypePrefix().toLowerCase()));
    }

    /**
     * Returns permalink URL for this media file resource.
     *
     */
    public String getPermalink() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getMediaFileURL(
                this.id, true);
    }

    public String getCreatorUserName() {
        return creatorUserName;
    }

    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
    }

    public User getCreator() {
        try {
            return WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(getCreatorUserName());
        } catch (Exception e) {
            log.error("ERROR fetching user object for username: " + getCreatorUserName(), e);
        }
        return null;
    }

    /**
     * For old migrated files and theme resource files, orignal path of file can never change.
     * @return the originalPath
     */
    public String getOriginalPath() {
        return originalPath;
    }

    /**
     * For old migrated files and theme resource files, orignal path of file can never change.
     * @param originalPath the originalPath to set
     */
    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }
}
