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

package org.apache.roller.business;

import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.Assoc;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.WebsiteData;


/**
 * Interface to Bookmark Management. Provides methods for retrieving, storing,
 * moving, removing and querying for folders and bookmarks.
 */
public interface BookmarkManager {
    
    
    public void saveBookmark(BookmarkData bookmark) throws RollerException;
    
    
    /** 
     * Delete bookmark. 
     */
    public void removeBookmark(BookmarkData bookmark) throws RollerException;
    
    /** 
     * Retrieve bookmark by ID, a persistent instance. 
     */
    public BookmarkData getBookmark(String id) throws RollerException;
    
    
    /**
     * @param data
     * @param subfolders
     * @return
     */
    public List getBookmarks(FolderData data, boolean subfolders)
            throws RollerException;
    
    
    public void saveFolder(FolderData folder) throws RollerException;
    
    
    public void removeFolder(FolderData folder) throws RollerException;
    
    
    /** 
     * Retrieve folder by ID, a persistent instance. 
     */
    public FolderData getFolder(String id) throws RollerException;
    
    
    /** 
     * Get all folders for a website.
     *
     * @param website Website.
     */
    public List getAllFolders(WebsiteData wd) throws RollerException;
    
    
    /** 
     * Get top level folders for a website.
     *
     * @param website Website.
     */
    public FolderData getRootFolder(WebsiteData website) throws RollerException;
    
    
    /** 
     * Get folder specified by website and folderPath.
     *
     * @param website Website of folder.
     * @param folderPath Path of folder, relative to folder root.
     */
    public FolderData getFolder(WebsiteData website, String folderPath)
            throws RollerException;
    
    /**
     * Get absolute path to folder, appropriate for use by getFolderByPath().
     *
     * @param folder Folder.
     * @return Forward slash separated path string.
     */
    public String getPath(FolderData folder) throws RollerException;
    
    
    /**
     * Get subfolder by path relative to specified folder.
     *
     * @param folder  Root of path or null to start at top of folder tree.
     * @param path    Path of folder to be located.
     * @param website Website of folders.
     * @return FolderData specified by path or null if not found.
     */
    public FolderData getFolderByPath(WebsiteData wd, FolderData folder, String string)
            throws RollerException;
    
    
    /**
     * Determine if folder is in use.  A folder is <em>in use</em> if it contains any bookmarks
     * or has any children.
     *
     * @param folder
     * @return true if the folder contains bookmarks or has children, false otherwise.
     * @throws RollerException
     */
    public boolean isFolderInUse(FolderData folder) throws RollerException;
    
    
    /**
     * Check duplicate folder name.
     */
    public boolean isDuplicateFolderName(FolderData data) throws RollerException;
    
    
    /**
     * Determines if folder is descendent of folder.
     */
    public boolean isDescendentOf(FolderData data, FolderData ancestor) throws RollerException;
    
    
    /**
     */
    public Assoc getFolderParentAssoc(FolderData data) throws RollerException;
    
    /**
     */
    public List getFolderChildAssocs(FolderData data) throws RollerException;
    
    /**
     */
    public List getAllFolderDecscendentAssocs(FolderData data) throws RollerException;
    
    /**
     */
    public List getFolderAncestorAssocs(FolderData data) throws RollerException;
    
    
    /** 
     * Import bookmarks from OPML string into specified folder.
     *
     * @param site Website.
     * @param folder Name of folder to hold bookmarks.
     * @param opml OPML data to be imported.
     */
    public void importBookmarks(WebsiteData site, String folder, String opml)
            throws RollerException;
    
    
    /** 
     * Move contents of folder to another folder.
     *
     * @param src Source folder.
     * @param dest Destination folder.
     */
    public void moveFolderContents(FolderData src, FolderData dest)
            throws RollerException;
    
    
    /**
     * Delete contents of specified folder.
     */
    public void removeFolderContents(FolderData src) throws RollerException;
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}

