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
import org.apache.roller.weblogger.pojos.Weblog;

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
        boolean newBookmark = getBookmark(bookmark.getId()) == null;

        // set ranking (order of appearance) of bookmark
        if (bookmark.getPriority() == null) {
            bookmark.calculatePriority();
        }

        this.strategy.store(bookmark);

        if (newBookmark) {
            // New object make sure that relationship is set on managed copy of other side
            bookmark.getWeblog().getBookmarks().add(bookmark);
        }

        // update weblog last modified date (date is updated by saveWebsite())
        weblogManager.saveWeblog(bookmark.getWeblog());
    }

    public WeblogBookmark getBookmark(String id) throws WebloggerException {
        return (WeblogBookmark) strategy.load(WeblogBookmark.class, id);
    }

    public void removeBookmark(WeblogBookmark bookmark) throws WebloggerException {
        Weblog weblog = bookmark.getWeblog();
        
        //Remove the bookmark from its weblog
        bookmark.getWeblog().getBookmarks().remove(bookmark);
        
        // Now remove it from database
        this.strategy.remove(bookmark);
        
        // update weblog last modified date.  date updated by saveWebsite()
        weblogManager.saveWeblog(weblog);
    }

    /**
     * @see org.apache.roller.weblogger.business.BookmarkManager#getBookmarks(
     *      org.apache.roller.weblogger.pojos.Weblog)
     */
    public List<WeblogBookmark> getBookmarks(Weblog weblog)
            throws WebloggerException {
        TypedQuery<WeblogBookmark> query;
        List<WeblogBookmark> results;

        query = strategy.getNamedQuery("Bookmark.getByWeblog", WeblogBookmark.class);
        query.setParameter(1, weblog);
        results = query.getResultList();

        return results;
    }

    public void release() {}
    
}
