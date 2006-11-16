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
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.WebsiteData;


/**
 * Interface to Bookmark Management. Provides methods for retrieving, storing,
 * moving, removing and querying for folders and bookmarks.
 */
public interface BookmarkManager {
    
    
    /**
     * Save a Folder.  
     * 
     * Also saves any bookmarks in the folder.  This method should enforce the 
     * fact that a weblog cannot have 2 folders with the same path.
     *
     * @param folder The folder to be saved.
     * @throws RollerException If there is a problem.
     */
    public void saveFolder(FolderData folder) throws RollerException;
    
    
    /**
     * Remove a Folder.  
     * 
     * Also removes any subfolders and bookmarks.
     *
     * @param folder The folder to be removed.
     * @throws RollerException If there is a problem.
     */
    public void removeFolder(FolderData folder) throws RollerException;
    
    
    /**
     * Lookup a folder by ID.
     *
     * @param id The id of the folder to lookup.
     * @returns FolderData The folder, or null if not found.
     * @throws RollerException If there is a problem.
     */
    public FolderData getFolder(String id) throws RollerException;
    
    
    /** 
     * Get all folders for a weblog.
     *
     * @param weblog The weblog we want the folders from.
     * @returns List The list of FolderData objects from the weblog.
     * @throws RollerException If there is a problem.
     */
    public List getAllFolders(WebsiteData weblog) throws RollerException;
    
    
    /** 
     * Get root folder for a weblog.  
     * All weblogs should have only 1 root folder.
     *
     * @param weblog The weblog we want the root folder from.
     * @returns FolderData The root folder, or null if not found.
     * @throws RollerException If there is a problem.
     */
    public FolderData getRootFolder(WebsiteData weblog) throws RollerException;
    
    
    /** 
     * Get a folder from a weblog based on its path.
     *
     * @param weblog The weblog we want the folder from.
     * @param path The full path of the folder.
     * @returns FolderData The folder from the given path, or null if not found.
     * @throws RollerException If there is a problem.
     */
    public FolderData getFolder(WebsiteData weblog, String path)
            throws RollerException;
    
    
    /**
     * Save a Bookmark.
     *
     * @param bookmark The bookmark to be saved.
     * @throws RollerException If there is a problem.
     */
    public void saveBookmark(BookmarkData bookmark) throws RollerException;
    
    
    /**
     * Remove a Bookmark.
     *
     * @param bookmark The bookmark to be removed.
     * @throws RollerException If there is a problem.
     */
    public void removeBookmark(BookmarkData bookmark) throws RollerException;
    
    
    /** 
     * Lookup a Bookmark by ID.
     *
     * @param id The id of the bookmark to lookup.
     * @returns BookmarkData The bookmark, or null if not found.
     * @throws RollerException If there is a problem.
     */
    public BookmarkData getBookmark(String id) throws RollerException;
    
    
    /** 
     * Lookup all Bookmarks in a folder, optionally search recursively.
     *
     * @param folder The folder to get the bookmarks from.
     * @param recurse True if bookmarks should be included.
     * @returns List The list of bookmarks found.
     * @throws RollerException If there is a problem.
     */
    public List getBookmarks(FolderData folder, boolean recurse)
            throws RollerException;
    
    
    /** 
     * Import bookmarks and folders from OPML string into the specified folder.
     *
     * @param weblog The weblog to import the OPML into.
     * @param folder The NEW folder name to import the OPML into.
     * @param opml OPML data to be imported.
     */
    public void importBookmarks(WebsiteData weblog, String folder, String opml)
            throws RollerException;
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}
