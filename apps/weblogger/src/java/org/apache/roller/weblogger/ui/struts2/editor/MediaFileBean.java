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

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileTag;

/**
 * Bean for managing media file.
 */
public class MediaFileBean {

    String id;
    String name;
    String description;
    String copyrightText;
    String tags;
    String directoryId;
    boolean isSharedForGallery;

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

        Set<MediaFileTag> tagsSet = new HashSet<MediaFileTag>();
        for (String tag : this.tags.split(" ")) {
            tagsSet.add(new MediaFileTag(tag, dataHolder));
        }
        dataHolder.setTags(tagsSet);
        dataHolder.setSharedForGallery(this.isSharedForGallery);
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
            StringBuffer tagDisplayBuffer = new StringBuffer();
            for (MediaFileTag tag : dataHolder.getTags()) {
                tagDisplayBuffer.append(tag.getName());
                tagDisplayBuffer.append(" ");
            }
            tagDisplayBuffer.deleteCharAt(tagDisplayBuffer.length() - 1);
            this.setTags(tagDisplayBuffer.toString());
        }

        this.setSharedForGallery(dataHolder.isSharedForGallery());
        this.setDirectoryId(dataHolder.getDirectory().getId());
    }
}
