
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

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.BookmarkManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.Assoc;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.WebsiteData;

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
    private static Log logger = LogFactory
            .getFactory().getInstance(DatamapperBookmarkManagerImpl.class);

    /** Creates a new instance of DatamapperBookmarkManagerImpl */
    public DatamapperBookmarkManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public void saveBookmark(BookmarkData bookmark)
            throws RollerException {
        this.strategy.store(bookmark);
        
        // update weblog last modified date (date is updated by saveWebsite())
        RollerFactory.getRoller().getUserManager()
            .saveWebsite(bookmark.getWebsite());
    }

    public void removeBookmark(BookmarkData bookmark)
            throws RollerException {
        strategy.remove(bookmark);
    }

    public BookmarkData getBookmark(String id)
            throws RollerException {
        return (BookmarkData) strategy.load(BookmarkData.class, id);
    }

    public List getBookmarks(FolderData data, boolean subfolders)
            throws RollerException {
        // todo
        return null;
    }

    public void saveFolder(FolderData folder) 
            throws RollerException {
        strategy.store(folder);
    }

    public void removeFolder(FolderData folder) 
            throws RollerException {
        strategy.remove(folder);
    }

    public FolderData getFolder(String id) 
            throws RollerException {
        return (FolderData) strategy.load(FolderData.class, id);
    }

    public List getAllFolders(WebsiteData wd) 
            throws RollerException {
        // todo
        return null;
    }

    public FolderData getRootFolder(WebsiteData website) 
            throws RollerException {
        // todo
        return null;
    }

    public FolderData getFolder(WebsiteData website, String folderPath) 
            throws RollerException {
        // todo
        return null;
    }

    public String getPath(FolderData folder) 
            throws RollerException {
        // todo
        return null;
    }

    public FolderData getFolderByPath(
            WebsiteData wd, FolderData folder, String string) 
            throws RollerException {
        // todo
        return null;
    }

    public boolean isFolderInUse(FolderData folder)
            throws RollerException {
        // todo
        return true;
    }

    public boolean isDuplicateFolderName(FolderData data)
            throws RollerException {
        // todo
        return true;
    }

    public boolean isDescendentOf(FolderData data, FolderData ancestor)
            throws RollerException {
        // todo
        return true;
    }

    public Assoc getFolderParentAssoc(FolderData data)
            throws RollerException {
        // todo
        return null;
    }

    public List getFolderChildAssocs(FolderData data)
            throws RollerException {
        // todo
        return null;
    }

    public List getAllFolderDecscendentAssocs(FolderData data)
            throws RollerException {
        // todo
        return null;
    }

    public List getFolderAncestorAssocs(FolderData data)
            throws RollerException {
        // todo
        return null;
    }

    public void importBookmarks(WebsiteData site, String folder, String opml)
            throws RollerException {
    }

    public void moveFolderContents(FolderData src, FolderData dest)
            throws RollerException {
    }

    public void removeFolderContents(FolderData src)
            throws RollerException {
    }

    public void release() {
    }
    
}
