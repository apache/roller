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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileTag;

/**
 * Bean for managing media file.
 */
public class MediaFileBean {

    private String  id;
    private String  name;
    private String  description;
    private String  contentType;
    private String  copyrightText;
    private String  tags;
    private String  directoryId;
    private boolean isSharedForGallery;
    private String  permalink;
    private String  thumbnailURL;
    private boolean isImage;
    private int     width;
    private int     height;
    private long    length;
    private String  originalPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCopyrightText() {
        return copyrightText;
    }

    public void setCopyrightText(String copyrightText) {
        this.copyrightText = copyrightText;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public boolean isSharedForGallery() {
        return isSharedForGallery;
    }

    public void setSharedForGallery(boolean isSharedForGallery) {
        this.isSharedForGallery = isSharedForGallery;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Copies the contents of this bean to a media file object
     * 
     */
    public void copyTo(MediaFile dataHolder) throws WebloggerException {

        dataHolder.setName(this.name);
        dataHolder.setDescription(this.description);
        dataHolder.setCopyrightText(this.copyrightText);

		if (StringUtils.isNotEmpty(tags)) {
			Set<MediaFileTag> tagsSet = new HashSet<MediaFileTag>();
			for (String tag : this.tags.split(" ")) {
				tagsSet.add(new MediaFileTag(tag, dataHolder));
			}
			dataHolder.setTags(tagsSet);
		} else {
			dataHolder.setTags(null);
		}
        dataHolder.setSharedForGallery(this.isSharedForGallery);
        dataHolder.setOriginalPath(this.originalPath);
    }

    /**
     * Populates this bean from a media file object. 
     * 
     */
    public void copyFrom(MediaFile dataHolder) {
        this.setId(dataHolder.getId());
        this.setName(dataHolder.getName());
        this.setDescription(dataHolder.getDescription());
        this.setCopyrightText(dataHolder.getCopyrightText());

        Set<MediaFileTag> tags = dataHolder.getTags();
        if (tags != null && !tags.isEmpty()) {
            StringBuilder tagDisplayBuilder = new StringBuilder();
            for (MediaFileTag tag : dataHolder.getTags()) {
            	if (StringUtils.isNotEmpty(tag.getName())) {
            		tagDisplayBuilder.append(tag.getName());
                    tagDisplayBuilder.append(" ");
				}
            }
			if (tagDisplayBuilder.length() > 0) {
				tagDisplayBuilder.deleteCharAt(tagDisplayBuilder.length() - 1);
			}
            this.setTags(tagDisplayBuilder.toString());
        }

        this.setSharedForGallery(dataHolder.isSharedForGallery());
        this.setDirectoryId(dataHolder.getDirectory().getId());
        this.setPermalink(dataHolder.getPermalink());
        this.setThumbnailURL(dataHolder.getThumbnailURL());
        this.setIsImage(dataHolder.isImageFile());
        this.setWidth(dataHolder.getWidth());
        this.setHeight(dataHolder.getHeight());
        this.setLength(dataHolder.getLength());
        this.setContentType(dataHolder.getContentType());
        this.setOriginalPath(dataHolder.getOriginalPath());
    }

    /**
     * @return the permalink
     */
    public String getPermalink() {
        return permalink;
    }

    /**
     * @param permalink the permalink to set
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
     * @param isImage the isImage to set
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
     * @param thumbnailURL the thumbnailURL to set
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
     * @return the length
     */
    public long getLength() {
        return length;
    }

    /**
     * @param length the length to set
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
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the originalPath
     */
    public String getOriginalPath() {
        return originalPath;
    }

    /**
     * @param originalPath the originalPath to set
     */
    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }
}
