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

package org.apache.roller.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.roller.RollerException;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.RollerFactory;


/**
 * <p>Folder that holds Bookmarks and other Folders. A Roller Website has a
 * set of Folders (there is no one root folder) and each Folder may contain
 * Folders or Bookmarks. Don't construct one of these yourself, instead use
 * the create method in your BookmarkManager implementation.</p>
 *
 * @struts.form include-all="true"
 *    extends="org.apache.struts.validator.ValidatorForm"
 * @ejb:bean name="FolderData"
 *
 * @hibernate.class lazy="true" table="folder"
 * @hibernate.cache usage="read-write"
 */
public class FolderData extends PersistentObject 
        implements Serializable, Comparable {
    
    public static final long serialVersionUID = -6272468884763861944L;
    
    // attributes
    private String id = null;
    private String name = null;
    private String description = null;
    private String path = null;
    
    // associations
    private WebsiteData website = null;
    private FolderData parentFolder = null;
    private Set childFolders = new TreeSet();
    private Set bookmarks = new TreeSet();
    
    
    public FolderData() {
    }
    
    public FolderData(
            FolderData parent,
            String name,
            String desc,
            WebsiteData website) {
        
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

        this.parentFolder = parent;
        // Relationships need to be maintained from both sides
        if(parent != null) {
            // The following triggers this.hashCode(), which is calculated 
            // based on this.path
            // It needs to happen after this.path is initialized
            parent.childFolders.add(this);
        }

    }
    
    
    public void setData(PersistentObject other) {
        FolderData otherData = (FolderData) other;
        
        this.id = otherData.getId();
        this.name = otherData.getName();
        this.description = otherData.getDescription();
        this.path = otherData.getPath();
        this.website = otherData.getWebsite();
        this.parentFolder = otherData.getParent();
        this.childFolders = otherData.getFolders();
        this.setBookmarks(otherData.getBookmarks());
    }
    
    
    public String toString() {
        
        StringBuffer str = new StringBuffer("{");
        str.append("id=").append(id);
        str.append(",");
        str.append("path=").append(path);
        str.append(",");
        str.append("desc=").append(description);
        str.append("}");
        
        return str.toString();
    }
    
    
    public boolean equals(Object other) {
        
        if (other == null) return false;
        
        if (other instanceof FolderData) {
            
            // NOTE: currently we are implementing equals only using the path
            //   of the folder.  technically the business key should be for
            //   both the weblog & path, but we don't expect to be comparing
            //   folders from 2 different weblogs so this should be fine
            FolderData that = (FolderData) other;
            return this.path.equals(that.getPath());
        }
        
        return false;
    }
    
    
    public int hashCode() {
        // NOTE: just like equals() it's possibly better if this is the combo
        //   of both the path hashCode and the weblog hashCode
        return this.path.hashCode();
    }
    
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        FolderData other = (FolderData)o;
        return getName().compareTo(other.getName());
    }
    
    
    /**
     * Database surrogate key.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @hibernate.id column="id"
     *     generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
    /**
     * The short name for this folder.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @struts.validator type="required" msgkey="errors.required"
     * @struts.validator type="mask" msgkey="errors.noslashes"
     * @struts.validator-var name="mask" value="${noslashes}"
     * @struts.validator-args arg0resource="folderForm.name"
     *
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    /**
     * A full description for this folder.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @hibernate.property column="description" non-null="true" unique="false"
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    
    /**
     * The full path to this folder in the hierarchy.
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @hibernate.property column="path" non-null="true" unique="false"
     */
    public String getPath() {
        return this.path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    
    /**
     * Get the weblog which owns this folder.
     *
     * @roller.wrapPojoMethod type="pojo"
     *
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public WebsiteData getWebsite() {
        return website;
    }
    
    public void setWebsite( WebsiteData website ) {
        this.website = website;
    }
    
    
    /**
     * Return parent folder, or null if folder is root of hierarchy.
     *
     * @roller.wrapPojoMethod type="pojo"
     *
     * @hibernate.many-to-one column="parentid" cascade="none" not-null="false"
     */
    public FolderData getParent() {
        return this.parentFolder;
    }
    
    public void setParent(FolderData parent) {
        this.parentFolder = parent;
    }
    
    
    /**
     * Get child folders of this folder.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.FolderData"
     *
     * @hibernate.set lazy="true" inverse="true" cascade="delete"
     * @hibernate.collection-key column="parentid"
     * @hibernate.collection-one-to-many class="org.apache.roller.pojos.FolderData"
     */
    public Set getFolders() {
        return this.childFolders;
    }
    
    private void setFolders(Set folders) {
        this.childFolders = folders;
    }
    
    
    /**
     * Get bookmarks contained in this folder.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.BookmarkData"
     *
     * @hibernate.set lazy="true" order-by="name" inverse="true" cascade="all"
     * @hibernate.collection-key column="folderid"
     * @hibernate.collection-one-to-many class="org.apache.roller.pojos.BookmarkData"
     */
    public Set getBookmarks() {
        return this.bookmarks;
    }
    
    // this is private to force the use of add/remove bookmark methods.
    private void setBookmarks(Set bookmarks) {
        this.bookmarks = bookmarks;
    }
    
    
    /** 
     * Add a bookmark to this folder.
     */
    public void addBookmark(BookmarkData bookmark) throws RollerException {
        bookmark.setFolder(this);
        getBookmarks().add(bookmark);
    }
    
    /**
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.BookmarkData"
     *
     * @param subfolders
     */
    public List retrieveBookmarks(boolean subfolders) throws RollerException {
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        return bmgr.getBookmarks(this, subfolders);
    }
    
    
    /**
     * Is this folder a descendent of the other folder?
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public boolean descendentOf(FolderData ancestor) {
        
        // if this is a root node then we can't be a descendent
        if(getParent() == null) {
            return false;
        } else {
            // if our path starts with our parents path then we are a descendent
            return this.path.startsWith(ancestor.getPath());
        }
    }
    
}
