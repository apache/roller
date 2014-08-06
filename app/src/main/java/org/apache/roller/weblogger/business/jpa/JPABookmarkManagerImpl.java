
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
package org.apache.roller.weblogger.business.jpa;

import java.io.StringReader;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.Weblog;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/*
 * JPABookmarkManagerImpl.java
 *
 * Created on May 31, 2006, 3:49 PM
 *
 */
@com.google.inject.Singleton
public class JPABookmarkManagerImpl implements BookmarkManager {
    
    private final Weblogger roller;
    private final JPAPersistenceStrategy strategy;
    
    
    /**
     * The logger instance for this class.
     */
    private static Log log = LogFactory
            .getFactory().getInstance(JPABookmarkManagerImpl.class);

    /**
     * Creates a new instance of JPABookmarkManagerImpl
     */
   @com.google.inject.Inject
   protected JPABookmarkManagerImpl(Weblogger roller, JPAPersistenceStrategy strategy) {
        log.debug("Instantiating JPA Bookmark Manager");
        this.roller = roller;
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
        roller.getWeblogManager().saveWeblog(bookmark.getWebsite());
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
        roller.getWeblogManager().saveWeblog(weblog);
    }

    public void saveFolder(WeblogBookmarkFolder folder) throws WebloggerException {

        // If new folder make sure name is unique
        if ((folder.getId() == null || this.getFolder(folder.getId()) == null) && isDuplicateFolderName(folder)) {
            throw new WebloggerException("Duplicate folder name");
        }

        this.strategy.store(folder);

        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(folder.getWeblog());
    }

    public void removeFolder(WeblogBookmarkFolder folder) throws WebloggerException {
        Weblog weblog = folder.getWeblog();
        weblog.getBookmarkFolders().remove(folder);
        this.strategy.remove(folder);

        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);
    }

    /**
     * Retrieve folder and lazy-load its bookmarks.
     */
    public WeblogBookmarkFolder getFolder(String id) throws WebloggerException {
        return (WeblogBookmarkFolder) strategy.load(WeblogBookmarkFolder.class, id);
    }

    
    public void importBookmarks(
            Weblog website, String folderName, String opml)
            throws WebloggerException {

        try {
            // Build JDOC document OPML string
            SAXBuilder builder = new SAXBuilder();
            StringReader reader = new StringReader( opml );
            Document doc = builder.build( reader );

            WeblogBookmarkFolder newFolder = getFolder(website, folderName);
            if (newFolder == null) {
                newFolder = new WeblogBookmarkFolder(
                        folderName, website);
                this.strategy.store(newFolder);
            }

            // Iterate through children of OPML body, importing each
            Element body = doc.getRootElement().getChild("body");
            for (Object elem : body.getChildren()) {
                importOpmlElement((Element) elem, newFolder );
            }
        } catch (Exception ex) {
            throw new WebloggerException(ex);
        }
    }

    // convenience method used when importing bookmarks
    // NOTE: this method does not commit any changes; 
    // that is done higher up in execution chain
    private void importOpmlElement(
            Element elem, WeblogBookmarkFolder folder)
            throws WebloggerException {
        String text = elem.getAttributeValue("text");
        String title = elem.getAttributeValue("title");
        String desc = elem.getAttributeValue("description");
        String url = elem.getAttributeValue("url");
        String xmlUrl = elem.getAttributeValue("xmlUrl");
        String htmlUrl = elem.getAttributeValue("htmlUrl");

        title =   null!=title ? title : text;
        desc =    null!=desc ? desc : title;
        xmlUrl =  null!=xmlUrl ? xmlUrl : url;
        url =     null!=htmlUrl ? htmlUrl : url;
        
        // better to truncate imported OPML fields than to fail import or drop whole bookmark
        int maxLength = RollerConstants.TEXTWIDTH_255;

        if (title != null && title.length() > maxLength) {
            title = title.substring(0,  maxLength);
        }
        if (desc != null && desc.length() > maxLength) {
            desc = desc.substring(0, maxLength);
        }
        if (url != null && url.length() > maxLength) {
            url = url.substring(0, maxLength);
        }
        if (xmlUrl != null && xmlUrl.length() > maxLength) {
            xmlUrl = xmlUrl.substring(0, maxLength);
        }

        if (elem.getChildren().size()==0) {
            // Leaf element.  Store a bookmark
            // Currently bookmarks must have at least a name and 
            // HTML url to be stored. Previous logic was
            // trying to skip invalid ones, but was letting ones 
            // with an xml url and no html url through
            // which could result in a db exception.
            if (null != title && null != url) {
                WeblogBookmark bd = new WeblogBookmark(folder,
                        title,
                        desc,
                        url,
                        xmlUrl,
                        null);
                folder.addBookmark(bd);
                this.strategy.store(bd);
            }
        } else {
            // Import suboutline's children into folder
            for (Object subelem : elem.getChildren("outline")) {
                importOpmlElement((Element) subelem, folder );
            }
        }
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
