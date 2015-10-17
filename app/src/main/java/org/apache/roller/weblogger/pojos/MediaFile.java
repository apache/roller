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
package org.apache.roller.weblogger.pojos;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Comparator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerUtils;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents a media file
 * 
 */
@Entity
@Table(name="media_file")
@NamedQueries({
        @NamedQuery(name="MediaFile.getByWeblogAndOrigpath",
                query="SELECT f FROM MediaFile f WHERE f.directory.weblog = ?1 AND f.originalPath = ?2")
})
public class MediaFile implements Serializable {

    private static final long serialVersionUID = -6704258422169734004L;

    private static Log log = LogFactory.getFactory().getInstance(
            MediaFile.class);

    private String id = WebloggerUtils.generateUUID();

    private String name;
    private String altText;
    private String titleText;
    private String notes;
    private long length;
    private int width = -1;
    private int height = -1;
    private int thumbnailHeight = -1;
    private int thumbnailWidth = -1;
    private String contentType;
    private String originalPath;
    private Timestamp dateUploaded = new Timestamp(System.currentTimeMillis());
    private Timestamp lastUpdated = new Timestamp(System.currentTimeMillis());
    private String creatorUserName;

    private InputStream is;

    private MediaFileDirectory directory;

    private FileContent content;
    private FileContent thumbnail;

    private String anchor;

    public MediaFile() {
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic(optional=false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * notes for media file
     * 
     */
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Column(name="alt_attr")
    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    @Column(name="title_attr")
    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    /**
     * Size of the media file
     * 
     */
    @Column(name="size_in_bytes")
    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Column(name="date_uploaded", nullable=false)
    public Timestamp getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Timestamp dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    @Transient
    public long getLastModified() {
        return getLastUpdated().getTime();
    }

    @Column(name="last_updated")
    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp time) {
        this.lastUpdated = time;
    }

    @ManyToOne
    @JoinColumn(name="directoryid", nullable=false)
    public MediaFileDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(MediaFileDirectory dir) {
        this.directory = dir;
    }

    @Column(name="content_type", nullable=false)
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Transient
    public String getPath() {
        return getDirectory().getName();
    }

    /**
     * Returns underlying file in the file system.
     * 
     * @return input stream of file
     */
    @Transient
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
    @Transient
    public boolean isImageFile() {
        if (getContentType() == null) {
            return false;
        }
        return (getContentType().toLowerCase().startsWith(MediaFileType.IMAGE
                .getContentTypePrefix().toLowerCase()));
    }

    /**
     * Returns permalink URL for this media file resource.
     */
    @Transient
    public String getPermalink() {
        return WebloggerFactory.getWeblogger().getUrlStrategy()
                .getMediaFileURL(getDirectory().getWeblog(), this.getId(), true);
    }

    /**
     * Returns thumbnail URL for this media file resource. Resulting URL will be
     * a 404 if media file is not an image.
     */
    @Transient
    public String getThumbnailURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy()
                .getMediaFileThumbnailURL(getDirectory().getWeblog(), this.getId(), true);
    }

    @Column(name="creator")
    public String getCreatorUserName() {
        return creatorUserName;
    }

    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
    }

    @Transient
    public User getCreator() {
        try {
            return WebloggerFactory.getWeblogger().getUserManager()
                    .getUserByUserName(getCreatorUserName());
        } catch (Exception e) {
            log.error("ERROR fetching user object for username: "
                    + getCreatorUserName(), e);
        }
        return null;
    }

    /**
     * For old migrated files and theme resource files, original path of file can
     * never change.
     * 
     * @return the originalPath
     */
    @Column(name="origpath")
    public String getOriginalPath() {
        return originalPath;
    }

    /**
     * For old migrated files and theme resource files, orignal path of file can
     * never change.
     * 
     * @param originalPath
     *            the originalPath to set
     */
    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns underlying thumbnail file in the file system.
     *
     * @return input stream of thumbnail
     */
    @Transient
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
    @Transient
    public int getThumbnailHeight() {
        if (isImageFile() && (thumbnailWidth == -1 || thumbnailHeight == -1)) {
            figureThumbnailSize();
        }
        return thumbnailHeight;
    }

    /**
     * @return the thumbnailWidth
     */
    @Transient
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
                newHeight = (int) ((float) getHeight() * ((float) MediaFileManager.MAX_WIDTH / (float) getWidth()));
                newWidth = MediaFileManager.MAX_WIDTH;
            }

        } else {
            if (getHeight() > MediaFileManager.MAX_HEIGHT) {
                newWidth = (int) ((float) getWidth() * ((float) MediaFileManager.MAX_HEIGHT / (float) getHeight()));
                newHeight = MediaFileManager.MAX_HEIGHT;
            }
        }
        thumbnailHeight = newHeight;
        thumbnailWidth = newWidth;
    }

    // ------------------------------------------------------- Good citizenship

    public String toString() {
        return "MediaFile [name=" + getName() + ", directory=" + getDirectory()
                + ", weblog=" + getDirectory().getWeblog() + "]";
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof MediaFile)) {
            return false;
        }
        MediaFile o = (MediaFile) other;
        return new EqualsBuilder().append(getId(), o.getId()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }

    public static Comparator<MediaFile> NameComparator = new Comparator<MediaFile>() {
        public int compare(MediaFile file1, MediaFile file2) {
            return file1.getName().compareTo(file2.getName());
        }
    };

    public static Comparator<MediaFile> ContentTypeComparator = new Comparator<MediaFile>() {
        public int compare(MediaFile file1, MediaFile file2) {
            return file1.getContentType().compareTo(file2.getContentType());
        }
    };

    public static Comparator<MediaFile> DateUploadedComparator = new Comparator<MediaFile>() {
        public int compare(MediaFile file1, MediaFile file2) {
            // Do last uploaded first comparison for date field
            return file2.getDateUploaded().compareTo(file1.getDateUploaded());
        }
    };

}
