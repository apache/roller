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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.parameter.StrutsParameter;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * The blogroll page: a weblog's blogrolls (bookmark folders) and the links in
 * one of them. Links can be reordered, moved to another blogroll and deleted;
 * a blogroll other than the default one can be deleted.
 */
public class Bookmarks extends UIAction {

    private static final Log log = LogFactory.getLog(Bookmarks.class);

    /** The blogroll that themes show; it can't be renamed or deleted. */
    public static final String DEFAULT_FOLDER = "default";

    // the id of folder being viewed
    private String folderId = null;

    // the folder being viewed
    private WeblogBookmarkFolder folder = null;

    // the links to move or delete
    private String[] selectedBookmarks = null;

    // the folder to move links to
    private String targetFolderId = null;

    // the links of the folder, in their new order
    private List<String> bookmarkIds = new ArrayList<>();

    // all folders of the weblog: default first, then by name
    private List<WeblogBookmarkFolder> allFolders = Collections.emptyList();

    public Bookmarks() {
        this.actionName = "bookmarks";
        this.desiredMenu = "editor";
        this.pageTitle = "bookmarksForm.rootTitle";
    }

    /**
     * Loads the requested blogroll. Without one, opens the default blogroll if
     * it has links, otherwise the first blogroll that has links, so the page
     * doesn't open on an empty list while the links are elsewhere.
     */
    @Override
    public void myPrepare() {
        try {
            BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            List<WeblogBookmarkFolder> folders = new ArrayList<>(bmgr.getAllFolders(getActionWeblog()));
            folders.sort(Comparator
                    .comparing((WeblogBookmarkFolder f) -> !DEFAULT_FOLDER.equals(f.getName()))
                    .thenComparing(f -> StringUtils.defaultString(f.getName()), String.CASE_INSENSITIVE_ORDER));
            allFolders = folders;

            WeblogBookmarkFolder requested = StringUtils.isEmpty(getFolderId())
                    ? null : bmgr.getFolderById(getActionWeblog(), getFolderId());
            if (requested == null) {
                requested = folders.stream()
                        .filter(f -> !f.getBookmarks().isEmpty())
                        .findFirst()
                        .orElse(folders.isEmpty() ? null : folders.get(0));
            }
            setFolder(requested);
        } catch (WebloggerException ex) {
            log.error("Error looking up folder", ex);
        }
    }

    /**
     * Shows the blogroll.
     */
    @SkipValidation
    @Override
    public String execute() {
        return LIST;
    }

    /**
     * Deletes the selected links.
     */
    public String delete() {
        List<WeblogBookmark> links = selectedLinks();
        if (links == null) {
            addError("bookmarksForm.error.notFound");
            return execute();
        }
        try {
            BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            for (WeblogBookmark link : links) {
                folder.getBookmarks().remove(link);
                bmgr.removeBookmark(link);
            }
            WebloggerFactory.getWeblogger().flush();
            CacheManager.invalidate(getActionWeblog());
            addMessage(links.size() == 1 ? "bookmarksForm.deletedOne" : "bookmarksForm.deletedMany",
                    String.valueOf(links.size()));
        } catch (WebloggerException ex) {
            log.error("Error deleting links", ex);
            addError("generic.error.check.logs");
        }
        return execute();
    }

    /**
     * Deletes the blogroll being viewed and its links, then shows the default blogroll.
     */
    public String deleteFolder() {
        if (folder == null) {
            addError("folderForm.notFound");
            return execute();
        }
        if (isDefaultFolder()) {
            addError("bookmarksForm.error.deleteDefault");
            return execute();
        }
        try {
            BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            String name = folder.getName();
            bmgr.removeFolder(folder);
            WebloggerFactory.getWeblogger().flush();
            CacheManager.invalidate(getActionWeblog());
            addMessage("bookmarksForm.folderDeleted", name);

            allFolders = new ArrayList<>(allFolders);
            allFolders.remove(folder);
            setFolder(bmgr.getDefaultFolder(getActionWeblog()));
        } catch (WebloggerException ex) {
            log.error("Error deleting folder", ex);
            addError("generic.error.check.logs");
        }
        return execute();
    }

    @SkipValidation
    public String folderCreated() {
        // reached by a redirect from FolderEdit after it creates a folder, and
        // from BookmarksImport after it imports links into a new one
        if (folder != null) {
            addMessage("folderForm.created", folder.getName());
        }
        return execute();
    }

    /**
     * Moves the selected links to the end of another blogroll.
     */
    public String move() {
        try {
            BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            WeblogBookmarkFolder target = StringUtils.isEmpty(getTargetFolderId())
                    ? null : bmgr.getFolderById(getActionWeblog(), getTargetFolderId());
            List<WeblogBookmark> links = selectedLinks();
            if (target == null || links == null || target.equals(folder)) {
                addError("bookmarksForm.error.move");
                return execute();
            }
            for (WeblogBookmark link : links) {
                folder.getBookmarks().remove(link);
                target.addBookmark(link);
                link.setFolder(target);
                // recalculated by saveBookmark: after the target's last link
                link.setPriority(null);
                bmgr.saveBookmark(link);
            }
            WebloggerFactory.getWeblogger().flush();
            CacheManager.invalidate(getActionWeblog());
            addMessage(links.size() == 1 ? "bookmarksForm.movedOne" : "bookmarksForm.movedMany",
                    List.of(String.valueOf(links.size()), target.getName()));
        } catch (WebloggerException e) {
            log.error("Error moving links", e);
            addError("bookmarksForm.error.move");
        }
        return execute();
    }

    /**
     * Saves a new order for the links of the blogroll. The posted ids must be
     * exactly the blogroll's links.
     */
    public String reorder() {
        if (folder == null) {
            addError("folderForm.notFound");
            return execute();
        }
        List<WeblogBookmark> links = folder.getBookmarks();
        Set<String> ids = new HashSet<>(bookmarkIds);
        if (ids.size() != bookmarkIds.size() || ids.size() != links.size()
                || links.stream().anyMatch(link -> !ids.contains(link.getId()))) {
            addError("bookmarksForm.error.reorder");
            return execute();
        }
        try {
            BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            for (WeblogBookmark link : links) {
                link.setPriority(bookmarkIds.indexOf(link.getId()));
                bmgr.saveBookmark(link);
            }
            links.sort(Comparator.comparing(WeblogBookmark::getPriority));
            WebloggerFactory.getWeblogger().flush();
            CacheManager.invalidate(getActionWeblog());
            addMessage("bookmarksForm.orderSaved");
        } catch (WebloggerException ex) {
            log.error("Error reordering links", ex);
            addError("generic.error.check.logs");
        }
        return execute();
    }

    /**
     * The selected links, or null if any of them is not in the blogroll being viewed.
     */
    private List<WeblogBookmark> selectedLinks() {
        if (folder == null || selectedBookmarks == null || selectedBookmarks.length == 0) {
            return null;
        }
        List<WeblogBookmark> links = new ArrayList<>();
        for (String id : selectedBookmarks) {
            WeblogBookmark link = folder.getBookmarks().stream()
                    .filter(b -> b.getId().equals(id))
                    .findFirst().orElse(null);
            if (link == null) {
                return null;
            }
            links.add(link);
        }
        return links;
    }

    /**
     * The links of the blogroll being viewed, as a plain list: Struts doesn't
     * allow the page to call methods on the persistence provider's list.
     */
    public List<WeblogBookmark> getLinks() {
        return folder == null ? Collections.emptyList() : new ArrayList<>(folder.getBookmarks());
    }

    public int linkCount(WeblogBookmarkFolder f) {
        return f == null ? 0 : f.getBookmarks().size();
    }

    /**
     * A link's name or description as plain text. They are stored sanitized,
     * so may hold markup and entities; the page shows them as themes do.
     */
    public String plainText(String text) {
        return text == null ? "" : Utilities.unescapeHTML(Utilities.removeHTML(text, false));
    }

    public boolean isDefaultFolder() {
        return folder != null && DEFAULT_FOLDER.equals(folder.getName());
    }

    /** The blogrolls the selected links can be moved to: all but the one being viewed. */
    public List<WeblogBookmarkFolder> getMoveTargets() {
        return allFolders.stream().filter(f -> !f.equals(folder)).toList();
    }

    public String getFolderId() {
        return folderId;
    }

    @StrutsParameter
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String[] getSelectedBookmarks() {
        return selectedBookmarks;
    }

    @StrutsParameter
    public void setSelectedBookmarks(String[] bookmarks) {
        this.selectedBookmarks = bookmarks;
    }

    public String getTargetFolderId() {
        return targetFolderId;
    }

    @StrutsParameter
    public void setTargetFolderId(String targetFolderId) {
        this.targetFolderId = targetFolderId;
    }

    public List<String> getBookmarkIds() {
        return bookmarkIds;
    }

    @StrutsParameter
    public void setBookmarkIds(List<String> bookmarkIds) {
        this.bookmarkIds = bookmarkIds == null ? new ArrayList<>() : bookmarkIds;
    }

    public List<WeblogBookmarkFolder> getAllFolders() {
        return allFolders;
    }

    public WeblogBookmarkFolder getFolder() {
        return folder;
    }

    private void setFolder(WeblogBookmarkFolder folder) {
        this.folder = folder;
        if (folder != null) {
            this.folderId = folder.getId();
        }
    }
}
