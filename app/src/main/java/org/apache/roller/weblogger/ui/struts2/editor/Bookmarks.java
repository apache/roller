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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
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
	public List<String> requiredWeblogPermissionActions() {
		return Collections.singletonList(WeblogPermission.ADMIN);
	}

	public void myPrepare() {
		try {
			BookmarkManager bmgr = WebloggerFactory.getWeblogger()
					.getBookmarkManager();
			if (!StringUtils.isEmpty(getFolderId())
					&& !"/".equals(getFolderId())) {
				setFolder(bmgr.getFolder(getFolderId()));
			} else {
				setFolder(bmgr.getRootFolder(getActionWeblog()));
			}
		} catch (WebloggerException ex) {
			log.error("Error looking up folder", ex);
		}
	}

	/**
	 * Present the bookmarks and subfolders available in the folder specified by
	 * the request.
	 */
	public String execute() {

		// build list of folders for display
		TreeSet newFolders = new TreeSet(new FolderPathComparator());

		try {
			// Build list of all folders, except for current one, sorted by
			// path.
			BookmarkManager bmgr = WebloggerFactory.getWeblogger()
					.getBookmarkManager();
			List<WeblogBookmarkFolder> folders = bmgr
					.getAllFolders(getActionWeblog());
			for (WeblogBookmarkFolder fd : folders) {
				if (getFolderId() == null && fd.getParent() == null) {
					// Root folder so do not show the root /
				} else if (!fd.getId().equals(getFolderId())) {
					newFolders.add(fd);
				}
			}

			// build folder path
			WeblogBookmarkFolder parent = getFolder().getParent();
			if (parent != null) {
				List inFolderPath = new LinkedList();
				inFolderPath.add(0, getFolder());
				while (parent != null) {
					inFolderPath.add(0, parent);
					parent = parent.getParent();
				}
				setFolderPath(inFolderPath);
			}
		} catch (WebloggerException ex) {
			log.error("Error building folders list", ex);
			// TODO: i18n
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

		log.debug("Deleting selected folders and bookmarks.");

		try {
			String folders[] = getSelectedFolders();
			if (null != folders && folders.length > 0) {
				if (log.isDebugEnabled()) {
                    log.debug("Processing delete of " + folders.length
                            + " folders.");
                }
				for (int i = 0; i < folders.length; i++) {
					if (log.isDebugEnabled()) {
                        log.debug("Deleting folder - " + folders[i]);
                    }
					WeblogBookmarkFolder fd = bmgr.getFolder(folders[i]);
					if (fd != null) {
						bmgr.removeFolder(fd); // removes child folders and
						// bookmarks too
					}
				}
			}

			WeblogBookmark bookmark = null;
			String bookmarks[] = getSelectedBookmarks();
			if (null != bookmarks && bookmarks.length > 0) {
				if (log.isDebugEnabled()) {
                    log.debug("Processing delete of " + bookmarks.length
                            + " bookmarks.");
                }
				for (int j = 0; j < bookmarks.length; j++) {
					if (log.isDebugEnabled())
						log.debug("Deleting bookmark - " + bookmarks[j]);
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
			BookmarkManager bmgr = WebloggerFactory.getWeblogger()
					.getBookmarkManager();

			if (log.isDebugEnabled()) {
                log.debug("Moving folders and bookmarks to folder - "
                        + getTargetFolderId());
            }

			// Move folders to new parent folder.
			String folders[] = getSelectedFolders();
			WeblogBookmarkFolder parent = bmgr.getFolder(getTargetFolderId());
			if (null != folders && folders.length > 0) {
				for (int i = 0; i < folders.length; i++) {
					WeblogBookmarkFolder fd = bmgr.getFolder(folders[i]);

					// Don't move folder into itself.
					if (!fd.getId().equals(parent.getId())
							&& !parent.descendentOf(fd)) {
						bmgr.moveFolder(fd, parent);
					} else {
						addMessage("bookmarksForm.warn.notMoving", fd.getName());
					}
				}
			}

			// Move bookmarks to new parent folder.
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
			log.error("Error doing folder/bookmark move", e);
			addError("bookmarksForm.error.move");
		}

		return execute();
	}

	private static final class FolderPathComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			WeblogBookmarkFolder f1 = (WeblogBookmarkFolder) o1;
			WeblogBookmarkFolder f2 = (WeblogBookmarkFolder) o2;
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

	public WeblogBookmarkFolder getFolder() {
		return folder;
	}

	public void setFolder(WeblogBookmarkFolder folder) {
		this.folder = folder;
	}

	public List getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(List folderPath) {
		this.folderPath = folderPath;
	}

}
