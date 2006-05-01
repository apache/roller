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
/*
 * Created on Jun 18, 2004
 */
package org.roller.business.hibernate;

import java.io.IOException;
import java.io.StringReader;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.roller.RollerException;
import org.roller.pojos.Assoc;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderAssoc;
import org.roller.pojos.FolderData;
import org.roller.pojos.WebsiteData;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.roller.model.BookmarkManager;
import org.roller.util.Utilities;


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
    
    
    public void saveBookmark(BookmarkData bookmark) throws RollerException {
        this.strategy.store(bookmark);
    }
    
    
    public BookmarkData getBookmark(String id) throws RollerException {
        BookmarkData bd = (BookmarkData)
        strategy.load(id, BookmarkData.class);
        // TODO: huh?  why do we do this?
        if (bd != null) bd.setBookmarkManager(this);
        return bd;
    }
    
    
    public void removeBookmark(BookmarkData bookmark) throws RollerException {
        this.strategy.remove(bookmark);
    }
    
    //------------------------------------------------------------ Folder CRUD
    
    
    public void saveFolder(FolderData folder) throws RollerException {
        
        if(isDuplicateFolderName(folder)) {
            throw new RollerException("Duplicate folder name");
        }
        
        this.strategy.store(folder);
    }
    
    
    public void removeFolder(FolderData folder) throws RollerException {
        
        this.strategy.remove(folder);
    }
    
    
    /**
     * Retrieve folder and lazy-load it's sub-folders and bookmarks.
     */
    public FolderData getFolder(String id) throws RollerException {
        return (FolderData)strategy.load(id, FolderData.class);
    }
    
    //------------------------------------------------------------ Operations
    
    public void importBookmarks(WebsiteData website, String folderName, String opml)
            throws RollerException {
        
        String msg = "importBookmarks";
        try {
            // Build JDOC document OPML string
            SAXBuilder builder = new SAXBuilder();
            StringReader reader = new StringReader( opml );
            Document doc = builder.build( reader );
            
            FolderData newFolder = getFolder(website, folderName);
            if (newFolder == null) {
                newFolder = new FolderData(
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
            WebsiteData website, Element elem, FolderData parent)
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
                BookmarkData bd = new BookmarkData(parent,
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
            FolderData fd = new FolderData(
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
    
    //----------------------------------------------------------------
    public void moveFolderContents(FolderData src, FolderData dest)
            throws RollerException {
        
        if (dest.descendentOf(src)) {
            throw new RollerException(
                    "ERROR cannot move parent folder into it's own child");
        }
        
        try {
            // Add to destination folder
            LinkedList deleteList = new LinkedList();
            Iterator srcBookmarks = src.getBookmarks().iterator();
            while (srcBookmarks.hasNext()) {
                BookmarkData bd = (BookmarkData)srcBookmarks.next();
                deleteList.add(bd);
                
                BookmarkData movedBd = new BookmarkData();
                movedBd.setData(bd);
                movedBd.setId(null);
                
                dest.addBookmark(movedBd);
                this.strategy.store(movedBd);
            }
            
            // Remove from source folder
            Iterator deleteIter = deleteList.iterator();
            while (deleteIter.hasNext()) {
                BookmarkData bd = (BookmarkData)deleteIter.next();
                src.removeBookmark(bd);
                // TODO: this won't conflict with the bookmark we store above right?
                this.strategy.remove(bd);
            }
            
        } catch (Exception ex) {
            throw new RollerException(ex);
        }
    }
    
    //----------------------------------------------------------------
    public void removeFolderContents(FolderData src) throws RollerException {
        
        // just go through the folder and remove each bookmark
        Iterator srcBookmarks = src.getBookmarks().iterator();
        while (srcBookmarks.hasNext()) {
            BookmarkData bd = (BookmarkData)srcBookmarks.next();
            this.strategy.remove(bd);
        }
    }
    
    //---------------------------------------------------------------- Queries
    
    public FolderData getFolder(WebsiteData website, String folderPath)
    throws RollerException {
        return getFolderByPath(website, null, folderPath);
    }
    
    public String getPath(FolderData folder) throws RollerException {
        if (null == folder.getParent()) {
            return "/";
        } else {
            String parentPath = getPath(folder.getParent());
            parentPath = "/".equals(parentPath) ? "" : parentPath;
            return parentPath + "/" + folder.getName();
        }
    }
    
    public FolderData getFolderByPath(
            WebsiteData website, FolderData folder, String path)
            throws RollerException {
        final Iterator folders;
        final String[] pathArray = Utilities.stringToStringArray(path, "/");
        
        if (folder == null && (null == path || "".equals(path.trim()))) {
            throw new RollerException("Bad arguments.");
        }
        
        if (path.trim().equals("/")) {
            return getRootFolder(website);
        } else if (folder == null || path.trim().startsWith("/")) {
            folders = getRootFolder(website).getFolders().iterator();
        } else {
            folders = folder.getFolders().iterator();
        }
        
        while (folders.hasNext()) {
            FolderData possibleMatch = (FolderData)folders.next();
            if (possibleMatch.getName().equals(pathArray[0])) {
                if (pathArray.length == 1) {
                    return possibleMatch;
                } else {
                    String[] subpath = new String[pathArray.length - 1];
                    System.arraycopy(pathArray, 1, subpath, 0, subpath.length);
                    
                    String pathString= Utilities.stringArrayToString(subpath,"/");
                    return getFolderByPath(website, possibleMatch, pathString);
                }
            }
        }
        
        // The folder did not match and neither did any subfolders
        return null;
    }
    
    //----------------------------------------------- FolderAssoc CRUD
    
    
    public FolderAssoc retrieveFolderAssoc(String id) throws RollerException {
        return (FolderAssoc)strategy.load(id, FolderAssoc.class);
    }
    
    public void release() {}
    
    /**
     * @see org.roller.model.BookmarkManager#retrieveBookmarks(
     *      org.roller.pojos.FolderData, boolean)
     */
    public List getBookmarks(FolderData folder, boolean subfolders)
    throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            List bookmarks = new LinkedList();
            if (subfolders) {
                // get bookmarks in subfolders
                Criteria assocsQuery = session
                        .createCriteria(FolderAssoc.class);
                assocsQuery.add(Expression.eq("ancestorFolder", folder));
                Iterator assocs = assocsQuery.list().iterator();
                while (assocs.hasNext()) {
                    FolderAssoc assoc = (FolderAssoc) assocs.next();
                    Criteria bookmarksQuery = session
                            .createCriteria(BookmarkData.class);
                    bookmarksQuery.add(Expression.eq("folder", assoc
                            .getFolder()));
                    Iterator bookmarkIter = bookmarksQuery.list().iterator();
                    while (bookmarkIter.hasNext()) {
                        BookmarkData entry = (BookmarkData) bookmarkIter.next();
                        bookmarks.add(entry);
                    }
                }
            }
            
            // get bookmarks in folder
            Criteria bookmarksQuery = session
                    .createCriteria(BookmarkData.class);
            bookmarksQuery.add(Expression.eq("folder", folder));
            Iterator bookmarkIter = bookmarksQuery.list().iterator();
            while (bookmarkIter.hasNext()) {
                BookmarkData bookmark = (BookmarkData) bookmarkIter.next();
                bookmarks.add(bookmark);
            }
            return bookmarks;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public FolderData getRootFolder(WebsiteData website) throws RollerException {
        if (website == null)
            throw new RollerException("website is null");
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.createAlias("folder", "f");
            criteria.add(Expression.eq("f.website", website));
            criteria.add(Expression.isNull("ancestorFolder"));
            criteria.add(Expression.eq("relation", FolderAssoc.PARENT));
            List results = criteria.list();
            if (results.size() > 1) {
                // Should not have more than one root
                throw new RollerException(
                        "More than one root folder found for website "
                        + website.getId());
            } else if (results.size() == 1) {
                // Return root
                return ((FolderAssoc) results.get(0)).getFolder();
            }
            return null;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public List getAllFolders(WebsiteData website) throws RollerException {
        if (website == null)
            throw new RollerException("Website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(FolderData.class);
            criteria.add(Expression.eq("website", website));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        
    }
    
    /**
     * @see org.roller.model.BookmarkManager#isDuplicateFolderName(org.roller.pojos.FolderData)
     */
    public boolean isDuplicateFolderName(FolderData folder) throws RollerException {
        // ensure that no sibling folders share the same name
        boolean isNewFolder = (folder.getId() == null);
        FolderData parent =
                isNewFolder ? (FolderData)folder.getNewParent() : folder.getParent();
        
        if (null != parent) {
            List sameNames;
            try {
                Session session = ((HibernatePersistenceStrategy) strategy).getSession();
                Criteria criteria = session.createCriteria(FolderAssoc.class);
                criteria.createAlias("folder", "f");
                criteria.add(Expression.eq("f.name", folder.getName()));
                criteria.add(Expression.eq("ancestorFolder", parent));
                criteria.add(Expression.eq("relation", Assoc.PARENT));
                sameNames = criteria.list();
            } catch (HibernateException e) {
                throw new RollerException(e);
            }
            // If we got some matches
            if (sameNames.size() > 0) {
                // if we're saving a new folder, any matches are dups
                if (isNewFolder) return true;
                // otherwise it's a dup it isn't the same one (one match with the same id).
                if (!(sameNames.size() == 1 && folder.getId().equals(((FolderAssoc)sameNames.get(0)).getFolder().getId())))
                    return true;
            }
        }
        return false;
    }
    
    /**
     * @see org.roller.model.BookmarkManager#getFolderParentAssoc(
     * org.roller.pojos.FolderData)
     */
    public Assoc getFolderParentAssoc(FolderData folder) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.add(Expression.eq("folder", folder));
            criteria.add(Expression.eq("relation", Assoc.PARENT));
            List parents = criteria.list();
            if (parents.size() > 1) {
                throw new RollerException("ERROR: more than one parent");
            } else if (parents.size() == 1) {
                return (Assoc) parents.get(0);
            } else {
                return null;
            }
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    /**
     * @see org.roller.model.BookmarkManager#getFolderChildAssocs(
     * org.roller.pojos.FolderData)
     */
    public List getFolderChildAssocs(FolderData folder) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.add(Expression.eq("ancestorFolder", folder));
            criteria.add(Expression.eq("relation", Assoc.PARENT));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    /**
     * @see org.roller.model.BookmarkManager#getAllFolderDecscendentAssocs(
     * org.roller.pojos.FolderData)
     */
    public List getAllFolderDecscendentAssocs(FolderData folder) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.add(Expression.eq("ancestorFolder", folder));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    /**
     * @see org.roller.model.BookmarkManager#getFolderAncestorAssocs(
     * org.roller.pojos.FolderData)
     */
    public List getFolderAncestorAssocs(FolderData folder) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.add(Expression.eq("folder", folder));
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    /**
     * @see org.roller.model.BookmarkManager#isFolderInUse(org.roller.pojos.FolderData)
     */
    public boolean isFolderInUse(FolderData folder) throws RollerException {
        try {
            // We consider a folder to be "in use" if it contains any bookmarks or has
            // any children.
            
            // We first determine the number of bookmark entries.
            // NOTE: This seems to be an attempt to optimize, rather than just use getBookmarks(),
            // but I'm not sure that this optimization is really worthwhile, and it ignores
            // caching in the case that the (lazy) getBookmarks has been done already. --agangolli
            // TODO: condider changing to just use getBookmarks().size()
            
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(BookmarkData.class);
            criteria.add(Expression.eq("folder", folder));
            criteria.setMaxResults(1);
            int entryCount = criteria.list().size();
            
            // Return true if we have bookmarks or (, failing that, then checking) if we have children
            return (entryCount > 0 || folder.getFolders().size() > 0);
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public boolean isDescendentOf(FolderData child, FolderData ancestor)
    throws RollerException {
        boolean ret = false;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.add(Expression.eq("folder", child));
            criteria.add(Expression.eq("ancestorFolder", ancestor));
            ret = criteria.list().size() > 0;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        return ret;
    }
}
