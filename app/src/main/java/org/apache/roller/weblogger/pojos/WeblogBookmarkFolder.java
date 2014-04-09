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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.util.UUIDGenerator;


/**
 * <p>Folder that holds Bookmarks. A Roller Weblog has one or more Folders
 * with one folder permanently labeled default.</p>
 */
public class WeblogBookmarkFolder implements Serializable, Comparable<WeblogBookmarkFolder> {
    
    public static final long serialVersionUID = -6272468884763861944L;

    // attributes
    private String id = UUIDGenerator.generateUUID();
    private String name = null;

    // associations
    private Weblog weblog = null;
    private List<WeblogBookmark> bookmarks = new ArrayList<WeblogBookmark>();
    
    
    public WeblogBookmarkFolder() {
    }
    
    public WeblogBookmarkFolder(
            String name,
            Weblog weblog) {
        
        this.name = name;
        this.weblog = weblog;
        weblog.addBookmarkFolder(this);
    }
    
        
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getName());
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
                .append(getName(), o.getName())
                .append(getWeblog(), o.getWeblog())
                .isEquals();
        }
        
        return false;
    }    
    
    
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getName())
            .append(getWeblog())
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
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the weblog which owns this folder.
     */
    public Weblog getWeblog() {
        return weblog;
    }
    
    public void setWeblog( Weblog website ) {
        this.weblog = website;
    }

    /**
     * Get bookmarks contained in this folder.
     */
    public List<WeblogBookmark> getBookmarks() {
        return this.bookmarks;
    }
    
    // this is private to force the use of add/remove bookmark methods.
    private void setBookmarks(List<WeblogBookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    /** 
     * Add a bookmark to this folder.
     */
    public void addBookmark(WeblogBookmark bookmark) {
        for (WeblogBookmark bookmarkItem : bookmarks) {
            if (bookmarkItem.getId().equals(bookmark.getId())) {
                // already in bookmark list
                return;
            }
        }
        bookmark.setFolder(this);
        getBookmarks().add(bookmark);
    }

    public boolean hasBookmarkOfName(String bookmarkName) {
        for (WeblogBookmark bookmark : bookmarks) {
            if (bookmark.getName().equals(bookmarkName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     */
    public List<WeblogBookmark> retrieveBookmarks() throws WebloggerException {
        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        return bmgr.getBookmarks(this);
    }

    // convenience method for updating the folder name
    public void updateName(String newName) throws WebloggerException {
        setName(newName);
        WebloggerFactory.getWeblogger().getBookmarkManager().saveFolder(this);
    }
    
}
