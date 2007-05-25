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

package org.apache.roller.ui.struts2.editor;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.ui.struts2.util.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * List bookmarks and folders and allow for moving them around and deleting them.
 */
public class Bookmarks extends UIAction {
    
    private static Log log = LogFactory.getLog(Bookmarks.class);
    
    // the id of folder being viewed
    private String folderId = null;
    
    // the folder being viewed
    private FolderData folder = null;
    
    // the list of folders to move/delete
    private String[] selectedFolders = null;
    
    // the list of bookmarks to move/delete
    private String[] selectedBookmarks = null;
    
    // the target folder to move items to
    private String targetFolderId = null;
    
    // all folders from the action weblog
    private Set allFolders = Collections.EMPTY_SET;
    
    // path of folders representing selected folders hierarchy
    private List folderPath = Collections.EMPTY_LIST;
    
    
    public Bookmarks() {
        this.actionName = "bookmarks";
        this.desiredMenu = "editor";
        this.pageTitle = "bookmarksForm.rootTitle";
    }
    
    
    // admin perms required
    public short requiredWeblogPermissions() {
        return PermissionsData.ADMIN;
    }
    
    
    public void myPrepare() {
        try {
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
            if(!StringUtils.isEmpty(getFolderId()) && 
                    !"/".equals(getFolderId())) {
                setFolder(bmgr.getFolder(getFolderId()));
            } else {
                setFolder(bmgr.getRootFolder(getActionWeblog()));
            }
        } catch (RollerException ex) {
            log.error("Error looking up folder", ex);
        }
    }
    
    
    /**
     * Present the bookmarks and subfolders available in the folder specified
     * by the request.
     */
    public String execute() {
        
        // build list of folders for display
        TreeSet allFolders = new TreeSet(new FolderPathComparator());
        
        try {
            // Build list of all folders, except for current one, sorted by path.
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
            List<FolderData> folders = bmgr.getAllFolders(getActionWeblog());
            for(FolderData fd : folders) {
                if (!fd.getId().equals(getFolderId())) {
                    allFolders.add(fd);
                }
            }
            
            // build folder path
            FolderData parent = getFolder().getParent();
            if(parent != null) {
                List folderPath = new LinkedList();
                folderPath.add(0, getFolder());
                while (parent != null) {
                    folderPath.add(0, parent);
                    parent = parent.getParent();
                }
                setFolderPath(folderPath);
            }
        } catch (RollerException ex) {
            log.error("Error building folders list", ex);
            // TODO: i18n
            addError("Error building folders list");
        }
        
        if (allFolders.size() > 0) {
            setAllFolders(allFolders);
        }

        return LIST;
    }
    
    
    /**
     * Delete folders and bookmarks.
     */
    public String delete() {
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        log.debug("Deleting selected folders and bookmarks.");
        
        try {
            String folders[] = getSelectedFolders();
            if (null != folders) {
                log.debug("Processing delete of "+folders.length+" folders.");
                for (int i = 0; i < folders.length; i++) {
                    log.debug("Deleting folder - "+folders[i]);
                    FolderData fd = bmgr.getFolder(folders[i]);
                    bmgr.removeFolder(fd); // removes child folders and bookmarks too
                }
            }
            
            BookmarkData bookmark = null;
            String bookmarks[] = getSelectedBookmarks();
            if (null != bookmarks) {
                log.debug("Processing delete of "+bookmarks.length+" bookmarks.");
                for (int j = 0; j < bookmarks.length; j++) {
                    log.debug("Deleting bookmark - "+bookmarks[j]);
                    bookmark = bmgr.getBookmark(bookmarks[j]);
                    bmgr.removeBookmark(bookmark);
                }
            }
            
            // flush changes
            RollerFactory.getRoller().flush();
            
            // notify caches
            CacheManager.invalidate(getActionWeblog());
            
        } catch (RollerException ex) {
            log.error("Error doing folder/bookmark deletes", ex);
            // TODO: i18n
            addError("Error doing folder/bookmark deletes");
        }
        
        return execute();
    }
    
    
    /**
     * Move folders and bookmarks to a new folder.
     */
    public String move() {
        
        try {
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
            
            log.debug("Moving folders and bookmarks to folder - "+getTargetFolderId());
            
            // Move folders to new parent folder.
            String folders[] = getSelectedFolders();
            FolderData parent = bmgr.getFolder(getTargetFolderId());
            if (null != folders) {
                for (int i = 0; i < folders.length; i++) {
                    FolderData fd = bmgr.getFolder(folders[i]);
                    
                    // Don't move folder into itself.
                    if (!fd.getId().equals(parent.getId()) && 
                            !parent.descendentOf(fd)) {
                        bmgr.moveFolder(fd, parent);
                    } else {
                        addMessage("bookmarksForm.warn.notMoving", fd.getName());
                    }
                }
            }
            
            // Move bookmarks to new parent folder.
            String bookmarks[] = getSelectedBookmarks();
            if (null != bookmarks) {
                for (int j = 0; j < bookmarks.length; j++) {
                    // maybe we should be using folder.addBookmark()?
                    BookmarkData bd = bmgr.getBookmark(bookmarks[j]);
                    bd.setFolder(parent);
                    bmgr.saveBookmark(bd);
                }
            }
            
            // flush changes
            RollerFactory.getRoller().flush();
            
            // notify caches
            CacheManager.invalidate(getActionWeblog());
            
        } catch (RollerException e) {
            log.error("Error doing folder/bookmark move", e);
            addError("bookmarksForm.error.move");
        }
        
        return execute();
    }
    
    
    private static final class FolderPathComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            FolderData f1 = (FolderData)o1;
            FolderData f2 = (FolderData)o2;
            return f1.getPath().compareTo(f2.getPath());
        }
    }
    

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String[] getSelectedFolders() {
        return selectedFolders;
    }

    public void setSelectedFolders(String[] folders) {
        this.selectedFolders = folders;
    }

    public String[] getSelectedBookmarks() {
        return selectedBookmarks;
    }

    public void setSelectedBookmarks(String[] bookmarks) {
        this.selectedBookmarks = bookmarks;
    }

    public String getTargetFolderId() {
        return targetFolderId;
    }

    public void setTargetFolderId(String targetFolderId) {
        this.targetFolderId = targetFolderId;
    }

    public Set getAllFolders() {
        return allFolders;
    }

    public void setAllFolders(Set allFolders) {
        this.allFolders = allFolders;
    }

    public FolderData getFolder() {
        return folder;
    }

    public void setFolder(FolderData folder) {
        this.folder = folder;
    }

    public List getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(List folderPath) {
        this.folderPath = folderPath;
    }
    
}
