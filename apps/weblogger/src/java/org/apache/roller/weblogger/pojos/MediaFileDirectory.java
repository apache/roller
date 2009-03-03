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

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * Media file directory.
 * 
 */
public class MediaFileDirectory {
	
	String id;
	String name;
	String description;
	MediaFileDirectory parent;
	Weblog weblog;
	String path;
	Set<MediaFileDirectory> childDirectories;
	Set<MediaFile> mediaFiles;
	
	public MediaFileDirectory() {
    	
    }

	public MediaFileDirectory(
            MediaFileDirectory parent,
            String name,
            String desc,
            Weblog weblog) {
        
    	this.id = UUIDGenerator.generateUUID();
    	this.name = name;
        this.description = desc;
        
        this.weblog = weblog;
        this.parent = parent;
        
        // calculate path
        if(parent == null) {
            this.path = "/";
        } else if("/".equals(parent.getPath())) {
            this.path = "/"+name;
        } else {
            this.path = parent.getPath() + "/" + name;
        }
    }

    /**
     * Database surrogate key.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @ejb:persistent-field
     *
     * @hibernate.id column="id"
     *     generator-class="assigned"  
     */
	public String getId() {
		return id;
	}

    /**
     * A short name for this folder.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @ejb:persistent-field
     *
     * @hibernate.property column="name" non-null="true" unique="false"
     */
	public String getName() {
		return name;
	}

	/**
     * @ejb:persistent-field
	 */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * A full description for this folder.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @ejb:persistent-field
     *
     * @hibernate.property column="description" unique="false"
     */
	public String getDescription() {
		return description;
	}

	/**
     * @ejb:persistent-field
	 */
	public void setDescription(String description) {
		this.description = description;
	}

    /**
     * Return parent folder, or null if folder is root of hierarchy.
     *
     * @roller.wrapPojoMethod type="pojo"
     *
     * @ejb:persistent-field
     *
     * @hibernate.many-to-one column="parentid" cascade="none" not-null="false"
     */
	public MediaFileDirectory getParent() {
		return parent;
	}

	/**
     * @ejb:persistent-field
	 */
	public void setParent(MediaFileDirectory parent) {
		this.parent = parent;
	}

    /**
     * Get the weblog which owns this folder.
     *
     * @roller.wrapPojoMethod type="pojo"
     *
     * @ejb:persistent-field
     *
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
	public Weblog getWeblog() {
		return weblog;
	}

	/**
     * @ejb:persistent-field
	 */
	public void setWeblog(Weblog weblog) {
		this.weblog = weblog;
	}

    /**
     * The full path to this folder in the hierarchy.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @ejb:persistent-field
     *
     * @hibernate.property column="path" non-null="true" unique="false"
     */
	public String getPath() {
		return path;
	}

	/**
     * @ejb:persistent-field
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
	
    /**
     * Get child folders of this folder.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.weblogger.pojos.WeblogBookmarkFolder"
     *
     * @hibernate.set lazy="true" inverse="true" cascade="delete" 
     * @hibernate.collection-key column="parentid"
     * @hibernate.collection-one-to-many class="org.apache.roller.weblogger.pojos.WeblogBookmarkFolder"
     */
    public Set<MediaFileDirectory> getChildDirectories() {
        return this.childDirectories;
    }
    
    public void setChildDirectories(Set<MediaFileDirectory> folders) {
        this.childDirectories = folders;
    }
    
    public boolean hasMediaFile(String name) {
    	Set<MediaFile> fileSet = this.getMediaFiles();
    	if (fileSet == null) 
    		return false;
    	for (MediaFile mediaFile: fileSet) {
    		if (mediaFile.getName().equals(name)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public MediaFile getMediaFile(String name) {
    	Set<MediaFile> fileSet = this.getMediaFiles();
    	if (fileSet == null) 
    		return null;
    	for (MediaFile mediaFile: fileSet) {
    		if (mediaFile.getName().equals(name)) {
    			return mediaFile;
    		}
    	}
    	return null;
    }
    
    public boolean hasDirectory(String name) {
    	Set<MediaFileDirectory> dirSet = this.getChildDirectories();
    	for (MediaFileDirectory directory: dirSet) {
    		if (directory.getName().equals(name)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public MediaFileDirectory createNewDirectory(String name) {
    	MediaFileDirectory newDirectory = new MediaFileDirectory(this, name, "", this.getWeblog());
    	this.getChildDirectories().add(newDirectory);
    	return newDirectory;
    }
    
    
    public Set<MediaFile> getMediaFiles() {
		return mediaFiles;
	}

	public void setMediaFiles(Set<MediaFile> mediaFiles) {
		this.mediaFiles = mediaFiles;
	}

	@Override
	public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof MediaFileDirectory != true) return false;
        MediaFileDirectory o = (MediaFileDirectory)other;
        return new EqualsBuilder()
            .append(getId(), o.getId()) 
            .append(getName(), o.getName()) 
            .append(getDescription(), o.getDescription()) 
            .append(getPath(), o.getPath()) 
            .isEquals();
	}
    

}
