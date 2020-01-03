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
package org.tightblog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotBlank;

import org.springframework.http.MediaType;
import org.tightblog.util.Utilities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.File;
import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a media file
 */
@Entity
@Table(name = "media_file")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaFile implements Comparable<MediaFile>, WeblogOwned {

    public static final MediaType THUMBNAIL_CONTENT_TYPE = MediaType.IMAGE_PNG;
    public static final int MAX_THUMBNAIL_WIDTH = 120;
    public static final int MAX_THUMBNAIL_HEIGHT = 120;

    private String id = Utilities.generateUUID();
    private int hashCode;

    @NotBlank(message = "{mediaFile.error.nameNull}")
    private String name;
    private String altText;
    private String titleText;
    private String anchor;
    private String notes;
    private long length;
    private int width = -1;
    private int height = -1;
    private int thumbnailHeight = -1;
    private int thumbnailWidth = -1;
    private String contentType;
    private Instant dateUploaded = Instant.now();
    private Instant lastUpdated = Instant.now();
    private User creator;

    private MediaDirectory directory;

    private File content;
    private File thumbnail;

    private String permalink;
    private String thumbnailURL;

    public MediaFile() {
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * notes for media file
     */
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Column(name = "alt_attr")
    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    @Column(name = "title_attr")
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
     */
    @Column(name = "size_in_bytes")
    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Column(name = "date_uploaded", nullable = false)
    public Instant getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Instant dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    @Column(name = "last_updated")
    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant time) {
        this.lastUpdated = time;
    }

    @ManyToOne
    @JoinColumn(name = "directoryid", nullable = false)
    public MediaDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(MediaDirectory dir) {
        this.directory = dir;
    }

    @Column(name = "content_type", nullable = false)
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

    @Transient
    public File getContent() {
        return content;
    }

    public void setContent(File content) {
        this.content = content;
    }

    public void setThumbnail(File thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Transient
    public File getThumbnail() {
        return thumbnail;
    }

    /**
     * Indicates whether this is an image file.
     */
    @Transient
    public boolean isImageFile() {
        return contentType != null && contentType.toLowerCase().startsWith("image/");
    }

    /**
     * Returns permalink URL for this media file resource.
     */
    @Transient
    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    /**
     * Returns thumbnail URL for this media file resource. Resulting URL will be
     * a 404 if media file is not an image.
     */
    @Transient
    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    @ManyToOne
    @JoinColumn(name = "creatorid", nullable = false)
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
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
        // image determines thumbnail size
        int newWidth = getWidth();
        int newHeight = getHeight();

        if (getWidth() > getHeight()) {
            if (getWidth() > MAX_THUMBNAIL_WIDTH) {
                newHeight = (int) ((float) getHeight() * ((float) MAX_THUMBNAIL_WIDTH / (float) getWidth()));
                newWidth = MAX_THUMBNAIL_WIDTH;
            }

        } else {
            if (getHeight() > MAX_THUMBNAIL_HEIGHT) {
                newWidth = (int) ((float) getWidth() * ((float) MAX_THUMBNAIL_HEIGHT / (float) getHeight()));
                newHeight = MAX_THUMBNAIL_HEIGHT;
            }
        }
        thumbnailHeight = newHeight;
        thumbnailWidth = newWidth;
    }

    @Override
    public String toString() {
        return "MediaFile: id=" + id + ", name=" + name + ", directory=" + directory;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof MediaFile && Objects.equals(id, ((MediaFile) other).id));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(id);
        }
        return hashCode;
    }

    private static final Comparator<MediaFile> COMPARATOR =
            Comparator.comparing(MediaFile::getDirectory)
                    .thenComparing(MediaFile::getName);

    @Override
    public int compareTo(MediaFile o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    @JsonIgnore
    public Weblog getWeblog() {
        return directory.getWeblog();
    }
}
