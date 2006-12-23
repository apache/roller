
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
package org.apache.roller.business.datamapper;

import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.hibernate.HibernatePersistenceStrategy;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.Utilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;

/*
 * DatamapperBookmarkManagerImpl.java
 *
 * Created on May 31, 2006, 3:49 PM
 *
 */
public class DatamapperBookmarkManagerImpl implements BookmarkManager {
    
    private DatamapperPersistenceStrategy strategy;
    
    /**
     * The logger instance for this class.
     */
    private static Log log = LogFactory
            .getFactory().getInstance(DatamapperBookmarkManagerImpl.class);

    /** Creates a new instance of DatamapperBookmarkManagerImpl */
    public DatamapperBookmarkManagerImpl 
            (DatamapperPersistenceStrategy strategy) {
        log.debug("Instantiating Datamapper Bookmark Manager");

        this.strategy = strategy;
    }

    public void saveBookmark(BookmarkData bookmark) throws RollerException {
        this.strategy.store(bookmark);
        
        // update weblog last modified date (date is updated by saveWebsite())
        RollerFactory.getRoller().getUserManager().
            saveWebsite(bookmark.getWebsite());
    }

    public BookmarkData getBookmark(String id) throws RollerException {
        return (BookmarkData) strategy.load(BookmarkData.class, id);
    }

    public void removeBookmark(BookmarkData bookmark) throws RollerException {
        //Now remove it from database
        this.strategy.remove(bookmark);
        //Remove the bookmark from its parent folder
        bookmark.getFolder().getBookmarks().remove(bookmark);
        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager()
                .saveWebsite(bookmark.getWebsite());
    }

    public void saveFolder(FolderData folder) throws RollerException {
        
        if(folder.getId() == null && isDuplicateFolderName(folder)) {
            throw new RollerException("Duplicate folder name");
        }

        this.strategy.store(folder);

        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().
            saveWebsite(folder.getWebsite());
    }

    public void removeFolder(FolderData folder) throws RollerException {
        this.strategy.remove(folder);

        // update weblog last modified date.  date updated by saveWebsite()
        RollerFactory.getRoller().getUserManager().
            saveWebsite(folder.getWebsite());
    }
    
    public void moveFolder(FolderData srcFolder, FolderData destFolder)
            throws RollerException {
        
        // TODO: this check should be made before calling this method?
        if (destFolder.descendentOf(srcFolder)) {
            throw new RollerException(
                    "ERROR cannot move parent folder into it's own child");
        }
        
        log.debug("Moving folder " + srcFolder.getPath() + " under " +
            destFolder.getPath());
        
        srcFolder.setParent(destFolder);
        if("/".equals(destFolder.getPath())) {
            srcFolder.setPath("/"+srcFolder.getName());
        } else {
            srcFolder.setPath(destFolder.getPath() + "/" + srcFolder.getName());
        }
        saveFolder(srcFolder);
        
        // the main work to be done for a category move is to update the 
        // path attribute of the category and all descendent categories
        updatePathTree(srcFolder);
    }    

    // updates the paths of all descendents of the given folder
    private void updatePathTree(FolderData folder) throws RollerException {
        
        log.debug("Updating path tree for folder "+folder.getPath());
        
        FolderData childFolder = null;
        Iterator childFolders = folder.getFolders().iterator();
        while(childFolders.hasNext()) {
            childFolder = (FolderData) childFolders.next();
            
            log.debug("OLD child folder path was "+childFolder.getPath());
            
            // update path and save
            if("/".equals(folder.getPath())) {
                childFolder.setPath("/" + childFolder.getName());
            } else {
                childFolder.setPath(folder.getPath() + "/" + 
                    childFolder.getName());
            }
            saveFolder(childFolder);
            
            log.debug("NEW child folder path is "+ childFolder.getPath());
            
            // then make recursive call to update this folders children
            updatePathTree(childFolder);
        }
    }

    
    /**
     * Retrieve folder and lazy-load it's sub-folders and bookmarks.
     */
    public FolderData getFolder(String id) throws RollerException {
        return (FolderData) strategy.load(FolderData.class, id);
    }

    
    public void importBookmarks(
            WebsiteData website, String folderName, String opml)
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
                        getRootFolder(website), 
                        folderName, folderName, website);
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
    // NOTE: this method does not commit any changes; 
    // that is done by importBookmarks()
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
            // Currently bookmarks must have at least a name and 
            // HTML url to be stored. Previous logic was
            // trying to skip invalid ones, but was letting ones 
            // with an xml url and no html url through
            // which could result in a db exception.
            // TODO: Consider providing error feedback instead of 
            // silently skipping the invalid bookmarks here.
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


    public FolderData getFolder(WebsiteData website, String path)
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
            DatamapperQuery query = strategy.newQuery(
                BookmarkData.class, "FolderData.getByWebsite&Path");
            query.setUnique();
            return (FolderData) query.execute(
                new Object[]{website, folderPath});
        }
    }

    /**
     * @see org.apache.roller.model.BookmarkManager#retrieveBookmarks(
     *      org.apache.roller.pojos.FolderData, boolean)
     */
    public List getBookmarks(FolderData folder, boolean subfolders) 
            throws RollerException {
        DatamapperQuery query = null;
        List results = null;

        if(!subfolders) {
            // if no subfolders then this is an equals query
            query = strategy.newQuery(
                BookmarkData.class, "BoomarkData.getByFolder");
            results = (List) query.execute(folder);
        } else {
            // if we are doing subfolders then do a case sensitive
            // query using folder path
            query = strategy.newQuery(BookmarkData.class, 
                "BoomarkData.getByFolder.pathLike&Folder.website");
            results = (List) query.execute(
                new Object[] {folder.getPath(), folder.getWebsite()});
        }
            
        return results;
    }

    public FolderData getRootFolder(WebsiteData website)
            throws RollerException {
        if (website == null)
            throw new RollerException("website is null");
        
        return (FolderData) strategy.newQuery(FolderData.class, 
                "FolderData.getByWebsite&ParentNull").
                setUnique().execute(website);
    }

    public List getAllFolders(WebsiteData website)
            throws RollerException {
        if (website == null)
            throw new RollerException("Website is null");
        
        return (List) strategy.newQuery(FolderData.class, 
            "FolderData.getByWebsite").execute(website);
    }

    
    /**
     * make sure the given folder doesn't already exist.
     */
    private boolean isDuplicateFolderName(FolderData folder) 
        throws RollerException {

        // ensure that no sibling categories share the same name
        FolderData parent = folder.getParent();
        if (null != parent) {
            return (getFolder(folder.getWebsite(), folder.getPath()) != null);
        }
        
        return false;
    }


    public void release() {}
    
}
