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

package org.apache.roller.weblogger.ui.restapi;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.util.cache.CacheManager;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * List bookmarks and allow for moving them around and deleting them.
 */
@RestController
public class BlogrollController {

    public BlogrollController() {
    }

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

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{id}/bookmarks", method = RequestMethod.GET)
    public List<WeblogBookmark> getWeblogBookmarks(@PathVariable String id, HttpServletResponse response) {
        Weblog weblog = weblogManager.getWeblog(id);
        if (weblog != null) {
            return weblog.getBookmarks()
                    .stream()
                    .peek(bkmk -> bkmk.setWeblog(null))
                    .collect(Collectors.toList());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/bookmark/{id}", method = RequestMethod.DELETE)
    public void deleteBookmark(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            WeblogBookmark itemToRemove = weblogManager.getBookmark(id);
            if (itemToRemove != null) {
                Weblog weblog = itemToRemove.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

                    weblogManager.removeBookmark(itemToRemove);
                    persistenceStrategy.flush();
                    cacheManager.invalidate(itemToRemove);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/bookmark/{id}", method = RequestMethod.PUT)
    public void updateBookmark(@PathVariable String id, @RequestBody WeblogBookmark newData, Principal p,
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
                        persistenceStrategy.flush();
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
    public void addBookmark(@RequestParam(name="weblogId") String weblogId, @RequestBody WeblogBookmark newData, Principal p,
                            HttpServletResponse response) throws ServletException {
        try {
            Weblog weblog = weblogManager.getWeblog(weblogId);
            if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                WeblogBookmark bookmark = new WeblogBookmark(weblog, newData.getName(),
                        newData.getUrl(), newData.getDescription());
                try {
                    weblogManager.saveBookmark(bookmark);
                    persistenceStrategy.flush();
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
}
