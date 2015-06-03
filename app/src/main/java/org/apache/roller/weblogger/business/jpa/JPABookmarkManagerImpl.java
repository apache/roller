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
package org.apache.roller.weblogger.business.jpa;

import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.Weblog;

/*
 * JPABookmarkManagerImpl.java
 *
 * Created on May 31, 2006, 3:49 PM
 *
 */
public class JPABookmarkManagerImpl implements BookmarkManager {
    
    private final WeblogManager weblogManager;
    private final JPAPersistenceStrategy strategy;
    
    
    /**
     * The logger instance for this class.
     */
    private static Log log = LogFactory
            .getFactory().getInstance(JPABookmarkManagerImpl.class);

    /**
     * Creates a new instance of JPABookmarkManagerImpl
     */
   protected JPABookmarkManagerImpl(WeblogManager wm, JPAPersistenceStrategy strategy) {
        log.debug("Instantiating JPA Bookmark Manager");
        this.weblogManager = wm;
        this.strategy = strategy;
    }

   
    public void saveBookmark(WeblogBookmark bookmark) throws WebloggerException {
        boolean exists = getBookmark(bookmark.getId()) != null;
        if (!exists) {
            // New object make sure that relationship is set on managed copy of other side
            bookmark.getFolder().addBookmark(bookmark);
        }
        // set ranking (order of appearance) of bookmark
        if (bookmark.getPriority() == null) {
            bookmark.calculatePriority();
        }

        this.strategy.store(bookmark);

        // update weblog last modified date (date is updated by saveWebsite())
        weblogManager.saveWeblog(bookmark.getWebsite());
    }

    public WeblogBookmark getBookmark(String id) throws WebloggerException {
        return (WeblogBookmark) strategy.load(WeblogBookmark.class, id);
    }

    public void removeBookmark(WeblogBookmark bookmark) throws WebloggerException {
        Weblog weblog = bookmark.getWebsite();
        
        //Remove the bookmark from its parent folder
        bookmark.getFolder().getBookmarks().remove(bookmark);
        
        // Now remove it from database
        this.strategy.remove(bookmark);
        
        // update weblog last modified date.  date updated by saveWebsite()
        weblogManager.saveWeblog(weblog);
    }

    public void saveFolder(WeblogBookmarkFolder folder) throws WebloggerException {

        // If new folder make sure name is unique
        if ((folder.getId() == null || this.getFolder(folder.getId()) == null) && isDuplicateFolderName(folder)) {
            throw new WebloggerException("Duplicate folder name");
        }

        this.strategy.store(folder);

        // update weblog last modified date.  date updated by saveWeblog()
        weblogManager.saveWeblog(folder.getWeblog());
    }

    public void removeFolder(WeblogBookmarkFolder folder) throws WebloggerException {
        Weblog weblog = folder.getWeblog();
        weblog.getBookmarkFolders().remove(folder);
        this.strategy.remove(folder);

        // update weblog last modified date.  date updated by saveWeblog()
        weblogManager.saveWeblog(weblog);
    }

    /**
     * Retrieve folder and lazy-load its bookmarks.
     */
    public WeblogBookmarkFolder getFolder(String id) throws WebloggerException {
        return (WeblogBookmarkFolder) strategy.load(WeblogBookmarkFolder.class, id);
    }

    /**
     * @see org.apache.roller.weblogger.business.BookmarkManager#getBookmarks(
     *      org.apache.roller.weblogger.pojos.WeblogBookmarkFolder)
     */
    public List<WeblogBookmark> getBookmarks(WeblogBookmarkFolder folder)
            throws WebloggerException {
        TypedQuery<WeblogBookmark> query;
        List<WeblogBookmark> results;

        query = strategy.getNamedQuery("BookmarkData.getByFolder", WeblogBookmark.class);
        query.setParameter(1, folder);
        results = query.getResultList();

        return results;
    }

    public WeblogBookmarkFolder getFolder(Weblog website, String name)
            throws WebloggerException {

        // Do simple lookup by name
        TypedQuery<WeblogBookmarkFolder> query = strategy.getNamedQuery("WeblogBookmarkFolder.getByWebsite&Name",
                WeblogBookmarkFolder.class);
        query.setParameter(1, website);
        query.setParameter(2, name);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public WeblogBookmarkFolder getDefaultFolder(Weblog weblog)
            throws WebloggerException {

        if (weblog == null) {
            throw new WebloggerException("weblog is null");
        }
        
        TypedQuery<WeblogBookmarkFolder> q = strategy.getNamedQuery("WeblogBookmarkFolder.getByWebsite&Name",
                WeblogBookmarkFolder.class);
        q.setParameter(1, weblog);
        q.setParameter(2, "default");
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<WeblogBookmarkFolder> getAllFolders(Weblog website)
            throws WebloggerException {
        if (website == null) {
            throw new WebloggerException("Website is null");
        }
        
        TypedQuery<WeblogBookmarkFolder> q = strategy.getNamedQuery("WeblogBookmarkFolder.getByWebsite",
                WeblogBookmarkFolder.class);
        q.setParameter(1, website);
        return q.getResultList();
    }

    
    /**
     * make sure the given folder doesn't already exist.
     */
    private boolean isDuplicateFolderName(WeblogBookmarkFolder folder) 
        throws WebloggerException {

        // ensure that no sibling folders share the same name
        return getFolder(folder.getWeblog(), folder.getName()) != null;
    }


    public void release() {}
    
}
