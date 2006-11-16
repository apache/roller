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
    
    static final long serialVersionUID = -6272468884763861944L;
    
    private String id = null;
    private String name = null;
    private String description = null;
    private String path = null;
    
    private WebsiteData website = null;
    private FolderData parentFolder = null;
    private Set childFolders = new TreeSet();
    private Set bookmarks = new TreeSet();
    
    
    /** For use by BookmarkManager implementations only. */
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
    }
    
    public void setData(PersistentObject other) {
        FolderData otherData = (FolderData) other;
        
        this.id = otherData.getId();
        this.name = otherData.getName();
        this.description = otherData.getDescription();
        this.website = otherData.getWebsite();
        this.parentFolder = otherData.getParent();
        this.childFolders = otherData.getFolders();
        this.setBookmarks(otherData.getBookmarks());
    }
    
    
    public String toString() {
        StringBuffer str = new StringBuffer("{");
        str.append(
                "bookmarks=" + bookmarks + " "
                + "id=" + id + " "
                + "name=" + name + " "
                + "description=" + description);
        str.append('}');
        return (str.toString());
    }
    
    public boolean equals(Object pOther) {
        if (pOther instanceof FolderData) {
            FolderData lTest = (FolderData) pOther;
            boolean lEquals = true;
            
//            if (this.bookmarks == null)
//            {
//                lEquals = lEquals && (lTest.bookmarks == null);
//            }
//            else
//            {
//                lEquals = lEquals && this.bookmarks.equals(lTest.bookmarks);
//            }
            
            if (this.id == null) {
                lEquals = lEquals && (lTest.getId() == null);
            } else {
                lEquals = lEquals && this.id.equals(lTest.getId());
            }
            
            if (this.name == null) {
                lEquals = lEquals && (lTest.getName() == null);
            } else {
                lEquals = lEquals && this.name.equals(lTest.getName());
            }
            
            if (this.description == null) {
                lEquals = lEquals && (lTest.getDescription() == null);
            } else {
                lEquals = lEquals &&
                        this.description.equals(lTest.getDescription());
            }
            
            if (this.website == null) {
                lEquals = lEquals && (lTest.getWebsite() == null);
            } else {
                lEquals = lEquals && this.website.equals(lTest.getWebsite());
            }
            
            return lEquals;
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        int result = 17;
        
        result = (37 * result) +
                ((this.id != null) ? this.id.hashCode() : 0);
        result = (37 * result) +
                ((this.name != null) ? this.name.hashCode() : 0);
        result = (37 * result) +
                ((this.description != null) ? this.description.hashCode() : 0);
        result = (37 * result) +
                ((this.website != null) ? this.website.hashCode() : 0);
        
        return result;
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        FolderData other = (FolderData)o;
        return getName().compareTo(other.getName());
    }
    
    
    /**
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
     * Description
     *
     * @roller.wrapPojoMethod type="simple"
     *
     * @hibernate.property column="description" non-null="true" unique="false"
     */
    public String getDescription() {
        return this.description;
    }
    
    /** @ejb:persistent-field */
    public void setDescription(String description) {
        this.description = description;
    }
    
    
    /**
     * Get path to this bookmark folder.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getPath() throws RollerException {
        
        if (null == path) {
            if (getParent() == null) {
                return "/";
            } else {
                String parentPath = getParent().getPath();
                parentPath = "/".equals(parentPath) ? "" : parentPath;
                return parentPath + "/" + this.name;
            }
        }
        
        return path;
    }
    /** TODO: fix formbean generation so this is not needed. */
    public void setPath(String string) {}
    
    
    /**
     * @roller.wrapPojoMethod type="pojo"
     *
     * @ejb:persistent-field
     *
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public WebsiteData getWebsite() {
        return website;
    }
    
    /** @ejb:persistent-field */
    public void setWebsite( WebsiteData website ) {
        this.website = website;
    }
    
    
    /**
     * Return parent category, or null if category is root of hierarchy.
     *
     * @roller.wrapPojoMethod type="pojo"
     *
     * @hibernate.many-to-one column="parentid" cascade="none" not-null="false"
     */
    public FolderData getParent() {
        return this.parentFolder;
    }
    
    /** Set parent category, database will be updated when object is saved. */
    public void setParent(FolderData parent) {
        this.parentFolder = parent;
    }
    
    
    /**
     * Query to get child categories of this category.
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
    
    /** Set parent category, database will be updated when object is saved. */
    private void setFolders(Set folders) {
        this.childFolders = folders;
    }
    
    
    /**
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.BookmarkData"
     *
     * @hibernate.set lazy="true" order-by="name" inverse="true" cascade="all-delete-orphan"
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
    
    
    /** Store bookmark and add to folder */
    public void addBookmark(BookmarkData bookmark) throws RollerException {
        bookmark.setFolder(this);
        getBookmarks().add(bookmark);
    }
    
    /** Remove boomkark from folder */
    public void removeBookmark(BookmarkData bookmark) {
        getBookmarks().remove(bookmark);
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
     * @roller.wrapPojoMethod type="simple"
     */
    public boolean descendentOf(FolderData ancestor) {
        
        // if this is root then we can't be a descendent
        if(getParent() == null) {
            return false;
        } else {
            // if ancestor is our parent then we are a descendent
            if(getParent().equals(ancestor)) {
                return true;
            } else {
                // see if our parent is a descendent
                return getParent().descendentOf(ancestor);
            }
        }
    }
    
}
