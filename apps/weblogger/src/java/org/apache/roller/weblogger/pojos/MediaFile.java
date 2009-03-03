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

import org.apache.roller.util.UUIDGenerator;

/**
 * Media file
 *
 * @hibernate.cache usage="read-write"
 * @hibernate.class lazy="true" table="media_file"
 */
public class MediaFile implements ThemeResource {

    // TODO: Ganesh - change class name to WeblogMediaFile?
    // TODO Ganesh - review all class attributes
    // TODO Ganesh - database constraints (not null etc) - revisit.
	
	final String id;
    String name;
    String description;
    String copyrightText;
    Boolean isSharedForGallery;
    long length;
    Timestamp dateUploaded;
    Timestamp lastUpdated;
    MediaFileDirectory directory;
    Set<MediaFileTag> tags;
    String contentType;
    InputStream is;
    FileContent content;
    
    public MediaFile() {
    	this.id = UUIDGenerator.generateUUID();
    }
    
	/**
	 * Name for the media file
	 * 
     * @ejb:persistent-field
     * @hibernate.property column="name" non-null="true" unique="false"
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
     * @ejb:persistent-field
     * @hibernate.property column="description" unique="false"
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
     * @ejb:persistent-field
     * @hibernate.property column="copyright_text" unique="false"
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
     * @hibernate.property column="is_public" non-null="true" unique="false"
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
     * @ejb:persistent-field
     * @hibernate.property column="size_in_bytes" non-null="true" unique="false"
	 */
	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

    /**
     * @ejb:persistent-field
     * @hibernate.property column="date_uploaded" non-null="true" unique="false"
     * @roller.wrapPojoMethod type="simple"
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
     * @ejb:persistent-field
     * @hibernate.property column="last_updated" unique="false"
     * @roller.wrapPojoMethod type="simple"
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

	/*
	 * Set of tags for this media file
	 * 
     * @hibernate.collection-key column="media_file_id"
     * @hibernate.collection-one-to-many class="org.apache.roller.weblogger.pojos.MediaFileTag"
     * @hibernate.set lazy="true" inverse="true" cascade="delete" 
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
     * @ejb:persistent-field
     * @hibernate.property column="content_type" non-null="true" unique="false"
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
     *
     * @ejb:persistent-field
     * @hibernate.id column="id"
     *  generator-class="assigned"  
     */
	public String getId() {
		return id;
	}

	public String getPath() {
		return directory.getPath();
	}

	public ThemeResource[] getChildren() {
		// TODO - Ganesh - To be implemented
		return null;
	}

	public boolean isDirectory() {
		return false;
	}

	public boolean isFile() {
		return true;
	}

	public InputStream getInputStream() {
		if (is != null) {
			return is;
		}
		else if (content != null ){
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
	
	public boolean isImageFile() {
		if (this.contentType == null) return false;
    	String[] allowedImageContentTypes = MediaFileType.IMAGE.getContentTypes(); 
    	for (String imageContentType: allowedImageContentTypes) {
    		if (imageContentType.equals(this.contentType)) {
    			return true;
    		}
    	}
    	return false;
	}
}
