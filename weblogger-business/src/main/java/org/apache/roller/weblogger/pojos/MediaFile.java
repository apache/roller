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
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;

/**
 * Represents a media file
 *
 */
public class MediaFile {

    private static Log log =
            LogFactory.getFactory().getInstance(MediaFile.class);

    private String id;

    private String      name;
    private String      description;
    private String      copyrightText;
    private Boolean     isSharedForGallery = Boolean.FALSE;
    private long        length;
    private int         width = -1;
    private int         height = -1;
    private int         thumbnailHeight = -1;
    private int         thumbnailWidth = -1;
    private String      contentType;
    private String      originalPath;
    private Timestamp   dateUploaded;
    private Timestamp   lastUpdated;
    private String      creatorUserName;
    private Weblog      weblog;

    private InputStream is;

    private MediaFileDirectory directory;
    private Set<MediaFileTag>  tags;

    private FileContent content;
    private FileContent thumbnail;

    
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
     */
    public String getPermalink() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getMediaFileURL(
                this.weblog, this.getId(), true);
    }

    /**
     * Returns thumbnail URL for this media file resource.
     * Resulting URL will be a 404 if media file is not an image.
     */
    public String getThumbnailURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getMediaFileThumbnailURL(
                this.weblog, this.getId(), true);
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

    /**
     * @return the weblog
     */
    public Weblog getWeblog() {
        return weblog;
    }

    /**
     * @param weblog the weblog to set
     */
    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns input stream for the underlying thumbnail file in the file system.
     * @return
     */
    public InputStream getThumbnailInputStream() {
        if (thumbnail != null) {
            return thumbnail.getInputStream();
        }
        return null;
    }

    public void setThumbnailContent(FileContent thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     * @return the thumbnailHeight
     */
    public int getThumbnailHeight() {
        if (isImageFile() && (thumbnailWidth == -1 || thumbnailHeight == -1)) {
            figureThumbnailSize();
        }
        return thumbnailHeight;
    }

    /**
     * @return the thumbnailWidth
     */
    public int getThumbnailWidth() {
        if (isImageFile() && (thumbnailWidth == -1 || thumbnailHeight == -1)) {
            figureThumbnailSize();
        }
        return thumbnailWidth;
    }

    private void figureThumbnailSize() {
        // image determine thumbnail size
        int newWidth = getWidth();
        int newHeight = getHeight();

        if (getWidth() > getHeight()) {
            if (getWidth() > MediaFileManager.MAX_WIDTH) {
                newHeight = (int)((float)getHeight() * ((float)MediaFileManager.MAX_WIDTH / (float)getWidth()));
                newWidth = MediaFileManager.MAX_WIDTH;
            }

        } else {
            if (getHeight() > MediaFileManager.MAX_HEIGHT) {
                newWidth = (int)((float)getWidth() * ((float)MediaFileManager.MAX_HEIGHT / (float)getHeight()));
                newHeight = MediaFileManager.MAX_HEIGHT;
            }
        }
        thumbnailHeight = newHeight;
        thumbnailWidth = newWidth;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
