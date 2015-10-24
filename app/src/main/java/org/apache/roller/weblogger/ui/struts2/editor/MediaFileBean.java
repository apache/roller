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
package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.MediaFile;

/**
 * Bean for managing media file.
 */
public class MediaFileBean {

    private String id;
    private String name;
    private String altText;
    private String titleText;
    private String anchor;
    private String notes;
    private String contentType;
    private String directoryId;
    private String permalink;
    private String thumbnailURL;
    private boolean isImage;
    private int width;
    private int height;
    private long length;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

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
     * Copies the contents of this bean to a media file object
     * 
     */
    public void copyTo(MediaFile dataHolder) throws WebloggerException {
        dataHolder.setName(this.name);
        dataHolder.setAltText(this.altText);
        dataHolder.setTitleText(this.titleText);
        dataHolder.setAnchor(this.anchor);
        dataHolder.setNotes(this.notes);
    }

    /**
     * Populates this bean from a media file object.
     * 
     */
    public void copyFrom(MediaFile dataHolder) {
        this.setId(dataHolder.getId());
        this.setName(dataHolder.getName());
        this.setAltText(dataHolder.getAltText());
        this.setTitleText(dataHolder.getTitleText());
        this.setAnchor(dataHolder.getAnchor());
        this.setNotes(dataHolder.getNotes());
        this.setDirectoryId(dataHolder.getDirectory().getId());
        this.setPermalink(dataHolder.getPermalink());
        this.setThumbnailURL(dataHolder.getThumbnailURL());
        this.setIsImage(dataHolder.isImageFile());
        this.setWidth(dataHolder.getWidth());
        this.setHeight(dataHolder.getHeight());
        this.setLength(dataHolder.getLength());
        this.setContentType(dataHolder.getContentType());
    }

    /**
     * @return the permalink
     */
    public String getPermalink() {
        return permalink;
    }

    /**
     * @param permalink
     *            the permalink to set
     */
    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    /**
     * @return the isImage
     */
    public boolean isIsImage() {
        return isImage;
    }

    /**
     * @param isImage
     *            the isImage to set
     */
    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }

    /**
     * @return the thumbnailURL
     */
    public String getThumbnailURL() {
        return thumbnailURL;
    }

    /**
     * @param thumbnailURL
     *            the thumbnailURL to set
     */
    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width
     *            the width to set
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
     * @param height
     *            the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the length
     */
    public long getLength() {
        return length;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType
     *            the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
