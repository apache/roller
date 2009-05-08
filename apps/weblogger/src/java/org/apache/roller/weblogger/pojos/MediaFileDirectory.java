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

import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * Represents a Media file directory.
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
     */
	public String getId() {
		return id;
	}

    /**
     * A short name for this folder.
     *
     */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    /**
     * A full description for this folder.
     *
     */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    /**
     * Return parent folder, or null if folder is root of hierarchy.
     *
     */
	public MediaFileDirectory getParent() {
		return parent;
	}

	public void setParent(MediaFileDirectory parent) {
		this.parent = parent;
	}

    /**
     * Get the weblog which owns this folder.
     *
     */
	public Weblog getWeblog() {
		return weblog;
	}

	public void setWeblog(Weblog weblog) {
		this.weblog = weblog;
	}

    /**
     * The full path to this folder in the hierarchy.
     *
     */
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
    /**
     * The collection of files in this directory
     * 
     */
	public Set<MediaFile> getMediaFiles() {
		return mediaFiles;
	}

	public void setMediaFiles(Set<MediaFile> mediaFiles) {
		this.mediaFiles = mediaFiles;
	}

	
    /**
     * Get child folders of this folder.
     *
     */
    public Set<MediaFileDirectory> getChildDirectories() {
        return this.childDirectories;
    }
    
    public void setChildDirectories(Set<MediaFileDirectory> folders) {
        this.childDirectories = folders;
    }
    
    /**
     * Indicates whether this directory contains the specified file.
     * 
     * @param name file name
     * @return true if the file is present in the directory, false otherwise.
     */
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
    
    /**
     * Returns file with the given name, if present in this directory
     * 
     * @param name file name
     * @return media file object
     */
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
    
    /**
     * Indicates whether this directory contains the specified sub-directory.
     * 
     * @param name directory name
     * @return true if the sub-directory is present, false otherwise.
     */
    public boolean hasDirectory(String name) {
    	Set<MediaFileDirectory> dirSet = this.getChildDirectories();
    	for (MediaFileDirectory directory: dirSet) {
    		if (directory.getName().equals(name)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Creates a new sub-directory
     * 
     * @param name new directory name
     * @return reference to the newly created directory.
     */
    public MediaFileDirectory createNewDirectory(String name) {
    	MediaFileDirectory newDirectory = new MediaFileDirectory(this, name, "", this.getWeblog());
    	this.getChildDirectories().add(newDirectory);
    	return newDirectory;
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
