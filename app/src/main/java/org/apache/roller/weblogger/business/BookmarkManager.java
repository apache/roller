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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.business;

import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Interface to Bookmark Management. Provides methods for retrieving, storing,
 * moving, removing and querying for folders and bookmarks.
 */
public interface BookmarkManager {
    
    
    /**
     * Save a Bookmark.
     *
     * @param bookmark The bookmark to be saved.
     * @throws WebloggerException If there is a problem.
     */
    void saveBookmark(WeblogBookmark bookmark) throws WebloggerException;
    
    
    /**
     * Remove a Bookmark.
     *
     * @param bookmark The bookmark to be removed.
     * @throws WebloggerException If there is a problem.
     */
    void removeBookmark(WeblogBookmark bookmark) throws WebloggerException;
    
    
    /** 
     * Lookup a Bookmark by ID.
     *
     * @param id The id of the bookmark to lookup.
     * @return BookmarkData The bookmark, or null if not found.
     * @throws WebloggerException If there is a problem.
     */
    WeblogBookmark getBookmark(String id) throws WebloggerException;
    
    
    /** 
     * Lookup all Bookmarks for a website, optionally search recursively.
     *
     * @param folder The folder to get the bookmarks from.
     * @return List The list of bookmarks found.
     * @throws WebloggerException If there is a problem.
     */
    List<WeblogBookmark> getBookmarks(Weblog weblog)
            throws WebloggerException;
    

    /**
     * Release all resources associated with Roller session.
     */
    void release();
    
}
