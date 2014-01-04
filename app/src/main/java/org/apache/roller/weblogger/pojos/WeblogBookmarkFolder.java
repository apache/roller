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

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.util.UUIDGenerator;


/**
 * <p>Folder that holds Bookmarks and other Folders. A Roller Website has a
 * set of Folders (there is no one root folder) and each Folder may contain
 * Folders or Bookmarks. Don't construct one of these yourself, instead use
 * the create method in your BookmarkManager implementation.</p>
 */
public class WeblogBookmarkFolder implements Serializable, Comparable<WeblogBookmarkFolder> {
    
    public static final long serialVersionUID = -6272468884763861944L;
    
    private static Log log = LogFactory.getLog(WeblogBookmarkFolder.class);
    
    
    // attributes
    private String id = UUIDGenerator.generateUUID();
    private String name = null;
    private String description = null;
    private String path = null;
    
    // associations
    private Weblog website = null;
    private WeblogBookmarkFolder parentFolder = null;
    private Set<WeblogBookmarkFolder> childFolders = new TreeSet<WeblogBookmarkFolder>();
    private Set<WeblogBookmark> bookmarks = new TreeSet<WeblogBookmark>();
    
    
    public WeblogBookmarkFolder() {
    }
    
    public WeblogBookmarkFolder(
            WeblogBookmarkFolder parent,
            String name,
            String desc,
            Weblog website) {
        
        this.name = name;
        this.description = desc;
        
        this.website = website;
        this.parentFolder = parent;
        
        // calculate path
        if(parent == null) {
            this.path = "/";
        } else if("/".equals(parent.getPath())) {
            this.path = "/"+name;
        } else {
            this.path = parent.getPath() + "/" + name;
        }
    }
    
        
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getPath());
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        
        if (other == null) {
            return false;
        }
        
        if (other instanceof WeblogBookmarkFolder) {
            WeblogBookmarkFolder o = (WeblogBookmarkFolder) other;
            return new EqualsBuilder()
                .append(getPath(), o.getPath()) 
                //.append(getWebsite(), o.getWebsite()) 
                .isEquals();
        }
        
        return false;
    }    
    
    
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getPath())
            //.append(getWebsite())
            .toHashCode();
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(WeblogBookmarkFolder other) {
        return getName().compareTo(other.getName());
    }
    
    
    /**
     * Database surrogate key.
     */
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) {
            return;
        }
        this.id = id;
    }
    
    
    /**
     * The short name for this folder.
     *
     * @struts.validator type="required" msgkey="errors.required"
     * @struts.validator type="mask" msgkey="errors.noslashes"
     * @struts.validator-var name="mask" value="${noslashes}"
     * @struts.validator-args arg0resource="folderForm.name"
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    /**
     * A full description for this folder.
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The full path to this folder in the hierarchy.
     */
    public String getPath() {
        return this.path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the weblog which owns this folder.
     */
    public Weblog getWebsite() {
        return website;
    }
    
    public void setWebsite( Weblog website ) {
        this.website = website;
    }

    /**
     * Return parent folder, or null if folder is root of hierarchy.
     */
    public WeblogBookmarkFolder getParent() {
        return this.parentFolder;
    }
    
    public void setParent(WeblogBookmarkFolder parent) {
        this.parentFolder = parent;
    }
    
    
    /**
     * Get child folders of this folder.
     */
    public Set<WeblogBookmarkFolder> getFolders() {
        return this.childFolders;
    }
    
    private void setFolders(Set<WeblogBookmarkFolder> folders) {
        this.childFolders = folders;
    }
    
    
    /**
     * Get bookmarks contained in this folder.
     */
    public Set<WeblogBookmark> getBookmarks() {
        return this.bookmarks;
    }
    
    // this is private to force the use of add/remove bookmark methods.
    private void setBookmarks(Set<WeblogBookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }
    
    
    /**
     * Add a folder as a child of this folder.
     */
    public void addFolder(WeblogBookmarkFolder folder) {
        
        // make sure folder is not null
        if(folder == null || folder.getName() == null) {
            throw new IllegalArgumentException("Folder cannot be null and must have a valid name");
        }
        
        // make sure we don't already have a folder with that name
        if(this.hasFolder(folder.getName())) {
            throw new IllegalArgumentException("Duplicate folder name '"+folder.getName()+"'");
        }
        
        // set ourselves as the parent of the folder
        folder.setParent(this);
        
        // add it to our list of child folder
        getFolders().add(folder);
    }
    
    
    /** 
     * Add a bookmark to this folder.
     */
    public void addBookmark(WeblogBookmark bookmark) throws WebloggerException {
        bookmark.setFolder(this);
        getBookmarks().add(bookmark);
    }
    
    
    /**
     * @param subfolders
     */
    public List retrieveBookmarks(boolean subfolders) throws WebloggerException {
        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        return bmgr.getBookmarks(this, subfolders);
    }
    
    
    /**
     * Does this folder have a child folder with the specified name?
     *
     * @param name The name of the folder to check for.
     * @return boolean true if child folder exists, false otherwise.
     */
    public boolean hasFolder(String name) {
        for (WeblogBookmarkFolder folder : this.getFolders()) {
            if(name.equals(folder.getName())) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Is this folder a descendent of the other folder?
     */
    public boolean descendentOf(WeblogBookmarkFolder ancestor) {
        
        // if this is a root node then we can't be a descendent
        if(getParent() == null) {
            return false;
        } else {
            // if our path starts with our parents path then we are a descendent
            return getPath().startsWith(ancestor.getPath());
        }
    }
    
    
    // convenience method for updating the folder name, which triggers a path tree rebuild
    public void updateName(String newName) throws WebloggerException {
        
        // update name
        setName(newName);
        
        // calculate path
        if(getParent() == null) {
            setPath("/");
        } else if("/".equals(getParent().getPath())) {
            setPath("/"+getName());
        } else {
            setPath(getParent().getPath() + "/" + getName());
        }
        
        // update path tree for all children
        updatePathTree(this);
    }
    
    
    // update the path tree for a given folder
    public static void updatePathTree(WeblogBookmarkFolder folder) 
            throws WebloggerException {
        
        log.debug("Updating path tree for folder "+folder.getPath());
        
        for (WeblogBookmarkFolder childFolder : folder.getFolders()) {

            log.debug("OLD child folder path was "+childFolder.getPath());
            
            // update path and save
            if("/".equals(folder.getPath())) {
                childFolder.setPath("/" + childFolder.getName());
            } else {
                childFolder.setPath(folder.getPath() + "/" + childFolder.getName());
            }
            WebloggerFactory.getWeblogger().getBookmarkManager().saveFolder(childFolder);
            
            log.debug("NEW child folder path is "+ childFolder.getPath());
            
            // then make recursive call to update this folders children
            updatePathTree(childFolder);
        }
    }
    
}
