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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * List bookmarks and folders and allow for moving them around and deleting
 * them.
 */
public class Bookmarks extends UIAction {

	private static Log log = LogFactory.getLog(Bookmarks.class);

	// the id of folder being viewed
	private String folderId = null;

	// the folder being viewed
	private WeblogBookmarkFolder folder = null;

	// the list of bookmarks to move or delete
	private String[] selectedBookmarks = null;

	// the target folder to move items to
	private String targetFolderId = null;

    // a new folder the user wishes to view
    private String viewFolderId = null;

	// all folders from the action weblog
	private List<WeblogBookmarkFolder> allFolders = Collections.emptyList();

	public Bookmarks() {
		this.actionName = "bookmarks";
		this.desiredMenu = "editor";
		this.pageTitle = "bookmarksForm.rootTitle";
	}

	// admin perms required
	public List<String> requiredWeblogPermissionActions() {
		return Collections.singletonList(WeblogPermission.ADMIN);
	}

	public void myPrepare() {
		try {
			BookmarkManager bmgr = WebloggerFactory.getWeblogger()
					.getBookmarkManager();
			if (!StringUtils.isEmpty(getFolderId())) {
				setFolder(bmgr.getFolder(getFolderId()));
			} else {
				setFolder(bmgr.getDefaultFolder(getActionWeblog()));
                setFolderId(getFolder().getId());
			}
		} catch (WebloggerException ex) {
			log.error("Error looking up folder", ex);
		}
	}

	/**
	 * Present the bookmarks available in the folder specified by the request.
	 */
	public String execute() {

		// build list of folders that the user can navigate to
        List<WeblogBookmarkFolder> newFolders = new ArrayList<WeblogBookmarkFolder>();

		try {
			// Build list of all folders, except for current one
			BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
			List<WeblogBookmarkFolder> folders = bmgr.getAllFolders(getActionWeblog());
			log.info("*** Folder ID is: " + getFolderId());
            for (WeblogBookmarkFolder fd : folders) {
				if (!fd.getId().equals(getFolderId())) {
					newFolders.add(fd);
				}
			}

		} catch (WebloggerException ex) {
			log.error("Error building folders list", ex);
			addError("Error building folders list");
		}

		if (newFolders.size() > 0) {
			setAllFolders(newFolders);
		}

		return LIST;
	}

	/**
	 * Delete folders and bookmarks.
	 */
	public String delete() {

		BookmarkManager bmgr = WebloggerFactory.getWeblogger()
				.getBookmarkManager();

		log.debug("Deleting selected bookmarks.");

		try {
			WeblogBookmark bookmark;
			String bookmarks[] = getSelectedBookmarks();
			if (null != bookmarks && bookmarks.length > 0) {
				if (log.isDebugEnabled()) {
                    log.debug("Processing delete of " + bookmarks.length
                            + " bookmarks.");
                }
				for (int j = 0; j < bookmarks.length; j++) {
					if (log.isDebugEnabled()) {
						log.debug("Deleting bookmark - " + bookmarks[j]);
                    }
					bookmark = bmgr.getBookmark(bookmarks[j]);
					if (bookmark != null) {
						bmgr.removeBookmark(bookmark);
					}

				}
			}

			// flush changes
			WebloggerFactory.getWeblogger().flush();

			// notify caches
			CacheManager.invalidate(getActionWeblog());

		} catch (WebloggerException ex) {
			log.error("Error doing bookmark deletes", ex);
			addError("Error doing bookmark deletes");
		}

		return execute();
	}

    /**
     * View the contents of another bookmark folder.
     */
    public String view() {
        try {
            BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            if (!StringUtils.isEmpty(viewFolderId)) {
                setFolder(bmgr.getFolder(viewFolderId));
                setFolderId(viewFolderId);
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up folder", ex);
        }
        return execute();
    }

	/**
	 * Move bookmarks to a new folder.
	 */
	public String move() {

		try {
			BookmarkManager bmgr = WebloggerFactory.getWeblogger()
					.getBookmarkManager();

			if (log.isDebugEnabled()) {
                log.debug("Moving bookmarks to folder - "
                        + getTargetFolderId());
            }

			// Move bookmarks to new parent folder.
            WeblogBookmarkFolder parent = bmgr.getFolder(getTargetFolderId());
			String bookmarks[] = getSelectedBookmarks();
			if (null != bookmarks && bookmarks.length > 0) {
				for (int j = 0; j < bookmarks.length; j++) {
					// maybe we should be using folder.addBookmark()?
					WeblogBookmark bd = bmgr.getBookmark(bookmarks[j]);
					bd.setFolder(parent);
					bmgr.saveBookmark(bd);
				}
			}

			// flush changes
			WebloggerFactory.getWeblogger().flush();

			// notify caches
			CacheManager.invalidate(getActionWeblog());

		} catch (WebloggerException e) {
			log.error("Error doing bookmark move", e);
			addError("bookmarksForm.error.move");
		}

		return execute();
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
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

	public List<WeblogBookmarkFolder> getAllFolders() {
		return allFolders;
	}

	public void setAllFolders(List<WeblogBookmarkFolder> allFolders) {
		this.allFolders = allFolders;
	}

	public WeblogBookmarkFolder getFolder() {
		return folder;
	}

	public void setFolder(WeblogBookmarkFolder folder) {
		this.folder = folder;
	}

    public String getViewFolderId() {
        return viewFolderId;
    }

    public void setViewFolderId(String viewFolderId) {
        this.viewFolderId = viewFolderId;
    }
}
