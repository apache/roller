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

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.RollbackException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * List bookmarks and allow for moving them around and deleting them.
 */
@RestController
public class Bookmarks extends UIAction {

    private static Logger log = LoggerFactory.getLogger(MediaFileView.class);

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy = null;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // the weblog being viewed
    private Weblog weblogObj = null;

    // the list of bookmarks to move or delete
    private String[] selectedBookmarks = null;

    public Bookmarks() {
        this.actionName = "bookmarks";
        this.desiredMenu = "editor";
        this.pageTitle = "bookmarksForm.rootTitle";
    }

    @Override
    public GlobalRole getRequiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole getRequiredWeblogRole() {
        return WeblogRole.OWNER;
    }

    public void prepare() {
        setWeblogObj(getActionWeblog());
    }

    /**
     * Present the weblog's bookmarks
     */
    public String execute() {
        return LIST;
    }

    /**
     * Delete bookmarks.
     */
    public String delete() {
        WeblogBookmark bookmark;
        String bookmarks[] = getSelectedBookmarks();
        if (null != bookmarks && bookmarks.length > 0) {
            log.debug("Processing delete of {} bookmarks.", bookmarks.length);
            for (String bookmarkName : bookmarks) {
                log.debug("Deleting bookmark {}", bookmarkName);
                bookmark = weblogManager.getBookmark(bookmarkName);
                if (bookmark != null) {
                    weblogManager.removeBookmark(bookmark);
                }
            }
        }
        persistenceStrategy.flush();
        return execute();
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/bookmark/{id}", method = RequestMethod.PUT)
    public void updateBookmark(@PathVariable String id, @RequestBody BookmarkData newData, Principal p,
                               HttpServletResponse response) throws ServletException {
        try {
            WeblogBookmark bookmark = weblogManager.getBookmark(id);
            if (bookmark != null) {
                Weblog weblog = bookmark.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                    bookmark.setName(newData.getName());
                    bookmark.setUrl(newData.getUrl());
                    bookmark.setDescription(newData.getDescription());
                    try {
                        weblogManager.saveBookmark(bookmark);
                        WebloggerFactory.flush();
                    } catch (RollbackException e) {
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        return;
                    }
                    cacheManager.invalidate(bookmark);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/bookmarks", method = RequestMethod.PUT)
    public void addBookmark(@RequestParam(name="weblog") String weblogHandle, @RequestBody BookmarkData newData, Principal p,
                            HttpServletResponse response) throws ServletException {
        try {
            if (userManager.checkWeblogRole(p.getName(), weblogHandle, WeblogRole.OWNER)) {
                Weblog weblog = weblogManager.getWeblogByHandle(weblogHandle);
                WeblogBookmark bookmark = new WeblogBookmark(weblog, newData.getName(),
                        newData.getUrl(), newData.getDescription());
                try {
                    weblogManager.saveBookmark(bookmark);
                    WebloggerFactory.flush();
                } catch (RollbackException e) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
                persistenceStrategy.refresh(weblog);
                cacheManager.invalidate(bookmark);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private static class BookmarkData {
        public BookmarkData() {
        }

        private String id;
        private String name;
        private String description;
        private String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }


    public String[] getSelectedBookmarks() {
        return selectedBookmarks;
    }

    public void setSelectedBookmarks(String[] bookmarks) {
        this.selectedBookmarks = bookmarks;
    }

    public Weblog getWeblogObj() {
        return weblogObj;
    }

    public void setWeblogObj(Weblog weblogObj) {
        this.weblogObj = weblogObj;
    }
}
