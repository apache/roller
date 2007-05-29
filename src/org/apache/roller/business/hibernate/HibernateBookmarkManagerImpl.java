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

package org.apache.roller.business.hibernate;

import java.io.StringReader;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.WeblogBookmark;
import org.apache.roller.pojos.WeblogBookmarkFolder;
import org.apache.roller.pojos.Weblog;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.util.Utilities;


/**
 * Hibernate implementation of the BookmarkManager.
 */
public class HibernateBookmarkManagerImpl implements BookmarkManager {
    
    static final long serialVersionUID = 5286654557062382772L;
    
    private static Log log = LogFactory.getLog(HibernateBookmarkManagerImpl.class);
    
    private HibernatePersistenceStrategy strategy = null;
    
    
    /**
     * @param pstrategy
     * @param roller
     */
    public HibernateBookmarkManagerImpl(HibernatePersistenceStrategy strat) {
        log.debug("Instantiating Hibernate Bookmark Manager");
        
        this.strategy = strat;
    }
    
    
    public void saveBookmark(WeblogBookmark bookmark) throws RollerException {
        this.strategy.store(bookmark);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(bookmark.getWebsite());
    }
    
    
    public WeblogBookmark getBookmark(String id) throws RollerException {
        WeblogBookmark bd = (WeblogBookmark)
        strategy.load(id,WeblogBookmark.class);
        // TODO: huh?  why do we do this?
        if (bd != null) bd.setBookmarkManager(this);
        return bd;
    }
    
    
    public void removeBookmark(WeblogBookmark bookmark) throws RollerException {
        //Remove the bookmark from its parent folder
        bookmark.getFolder().getBookmarks().remove(bookmark);
        //Now remove it from database
        this.strategy.remove(bookmark);
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager()
                .saveWebsite(bookmark.getWebsite());
    }
    
    
    public void saveFolder(WeblogBookmarkFolder folder) throws RollerException {
        
        WeblogBookmarkFolder existingFolder = getFolder(folder.getId());        
        if(existingFolder == null && isDuplicateFolderName(folder)) {
            throw new RollerException("Duplicate folder name");
        }
        
        this.strategy.store(folder);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(folder.getWebsite());
    }
    
    
    public void removeFolder(WeblogBookmarkFolder folder) throws RollerException {
        
        this.strategy.remove(folder);
        
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().saveWebsite(folder.getWebsite());
    }
    
    
    public void moveFolder(WeblogBookmarkFolder srcFolder,

    WeblogBookmarkFolder destFolder
)
            throws RollerException {
        
        // TODO: this check should be made before calling this method?
        if (destFolder.descendentOf(srcFolder)) {
            throw new RollerException(
                    "ERROR cannot move parent folder into it's own child");
        }
        
        log.debug("Moving folder "+srcFolder.getPath()+" under "+destFolder.getPath());
        
        srcFolder.setParent(destFolder);
        if("/".equals(destFolder.getPath())) {
            srcFolder.setPath("/"+srcFolder.getName());
        } else {
            srcFolder.setPath(destFolder.getPath() + "/" + srcFolder.getName());
        }
        saveFolder(srcFolder);
        
        // the main work to be done for a category move is to update the 
        // path attribute of the category and all descendent categories
        WeblogBookmarkFolder.updatePathTree(srcFolder);
    }
    
    
    /**
     * Retrieve folder and lazy-load it's sub-folders and bookmarks.
     */
    public WeblogBookmarkFolder getFolder(String id) throws RollerException {
        return (WeblogBookmarkFolder)strategy.load(id,WeblogBookmarkFolder.class);
    }
    
    
    public void importBookmarks(Weblog website, String folderName, String opml)
            throws RollerException {
        
        String msg = "importBookmarks";
        try {
            // Build JDOC document OPML string
            SAXBuilder builder = new SAXBuilder();
            StringReader reader = new StringReader( opml );
            Document doc = builder.build( reader );
            
            WeblogBookmarkFolder newFolder = getFolder(website, folderName);
            if (newFolder == null) {
                newFolder = new WeblogBookmarkFolder(
                        getRootFolder(website), folderName, folderName, website);
                this.strategy.store(newFolder);
            }
            
            // Iterate through children of OPML body, importing each
            Element body = doc.getRootElement().getChild("body");
            Iterator iter = body.getChildren().iterator();
            while (iter.hasNext()) {
                Element elem = (Element)iter.next();
                importOpmlElement( website, elem, newFolder );
            }
            
        } catch (Exception ex) {
            throw new RollerException(ex);
        }
    }
    
    // convenience method used when importing bookmarks
    // NOTE: this method does not commit any changes, that is done by importBookmarks()
    private void importOpmlElement(
            
            Weblog website, Element elem,WeblogBookmarkFolder parent)
            throws RollerException {
        String text = elem.getAttributeValue("text");
        String title = elem.getAttributeValue("title");
        String desc = elem.getAttributeValue("description");
        String url = elem.getAttributeValue("url");
        //String type = elem.getAttributeValue("type");
        String xmlUrl = elem.getAttributeValue("xmlUrl");
        String htmlUrl = elem.getAttributeValue("htmlUrl");
        
        title =   null!=title ? title : text;
        desc =    null!=desc ? desc : title;
        xmlUrl =  null!=xmlUrl ? xmlUrl : url;
        url =     null!=htmlUrl ? htmlUrl : url;
        
        if (elem.getChildren().size()==0) {
            // Leaf element.  Store a bookmark
            // Currently bookmarks must have at least a name and HTML url to be stored. Previous logic was
            // trying to skip invalid ones, but was letting ones with an xml url and no html url through
            // which could result in a db exception.
            // TODO: Consider providing error feedback instead of silently skipping the invalid bookmarks here.
            if (null != title && null != url) {
                WeblogBookmark bd = new WeblogBookmark(parent,
                        title,
                        desc,
                        url,
                        xmlUrl,
                        new Integer(0),
                        new Integer(100),
                        null);
                parent.addBookmark(bd);
                // TODO: maybe this should be saving the folder?
                this.strategy.store(bd);
            }
        } else {
            // Store a folder
            WeblogBookmarkFolder fd = new WeblogBookmarkFolder(
                    parent,
                    title,
                    desc,
                    parent.getWebsite());
            this.strategy.store(fd);
            
            // Import folder's children
            Iterator iter = elem.getChildren("outline").iterator();
            while ( iter.hasNext() ) {
                Element subelem = (Element)iter.next();
                importOpmlElement( website, subelem, fd  );
            }
        }
    }
    
    
    public WeblogBookmarkFolder getFolder(Weblog website, String path)
            throws RollerException {
        
        if (path == null || path.trim().equals("/")) {
            return getRootFolder(website);
        } else {
            String folderPath = path;
            
            // all folder paths must begin with a '/'
            if(!folderPath.startsWith("/")) {
                folderPath = "/"+folderPath;
            }
            
            // now just do simple lookup by path
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            
            Criteria criteria = session.createCriteria(WeblogBookmarkFolder.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("path", folderPath));
            
            return (WeblogBookmarkFolder) criteria.uniqueResult();
        }
    }
    
    
    /**
     * @see org.apache.roller.model.BookmarkManager#retrieveBookmarks(
     *      org.apache.roller.pojos.FolderData, boolean)
     */
    public List getBookmarks(WeblogBookmarkFolder folder, boolean subfolders)
            throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogBookmark.class);
            
            if(!subfolders) {
                // if no subfolders then this is an equals query
                criteria.add(Expression.eq("folder", folder));
            } else {
                // if we are doing subfolders then do a case sensitive
                // query using folder path
                criteria.createAlias("folder", "fd");
                criteria.add(Expression.like("fd.path", folder.getPath()+"%"));
                criteria.add(Expression.eq("fd.website", folder.getWebsite()));
            }
            
            return criteria.list();
            
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    public WeblogBookmarkFolder getRootFolder(Weblog website) throws RollerException {
        
        if (website == null)
            throw new RollerException("website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogBookmarkFolder.class);
            
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.isNull("parent"));
            criteria.setMaxResults(1);
            
            return (WeblogBookmarkFolder) criteria.uniqueResult();
            
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public List getAllFolders(Weblog website) throws RollerException {
        if (website == null)
            throw new RollerException("Website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogBookmarkFolder.class);
            criteria.add(Expression.eq("website", website));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        
    }
    
    
    /**
     * make sure the given folder doesn't already exist.
     */
    private boolean isDuplicateFolderName(WeblogBookmarkFolder folder) throws RollerException {
        
        // ensure that no sibling categories share the same name
        WeblogBookmarkFolder parent = folder.getParent();
        if (null != parent) {
            return (getFolder(folder.getWebsite(), folder.getPath()) != null);
        }
        
        return false;
    }
    
    
    public void release() {}
    
}
