/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.ui.restapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogBookmark;
import org.tightblog.domain.WeblogRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.repository.BlogrollLinkRepository;
import org.tightblog.repository.WeblogRepository;

import javax.persistence.EntityNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manage a blog's blogroll
 */
@RestController
public class BlogrollController {

    private static Logger log = LoggerFactory.getLogger(BlogrollController.class);

    private WeblogRepository weblogRepository;
    private BlogrollLinkRepository blogrollLinkRepository;
    private WeblogManager weblogManager;
    private UserManager userManager;

    @Autowired
    public BlogrollController(WeblogRepository weblogRepository, BlogrollLinkRepository blogrollLinkRepository,
                              WeblogManager weblogManager, UserManager userManager) {
        this.weblogRepository = weblogRepository;
        this.blogrollLinkRepository = blogrollLinkRepository;
        this.weblogManager = weblogManager;
        this.userManager = userManager;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{id}/bookmarks")
    public List<WeblogBookmark> getWeblogBookmarks(@PathVariable String id, HttpServletResponse response) {
        Weblog weblog = weblogRepository.findById(id).orElse(null);
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

    private void deleteBookmark(String id, Principal p) {
        WeblogBookmark itemToRemove = blogrollLinkRepository.findById(id).orElse(null);
        if (itemToRemove != null) {
            Weblog weblog = itemToRemove.getWeblog();
            if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                weblog.getBookmarks().remove(itemToRemove);
                weblogManager.saveWeblog(weblog);
            } else {
                log.warn("Effort to delete bookmark {} by user {} failed, insufficent access rights",
                        itemToRemove, p.getName());
            }
        } else {
            log.warn("Effort to delete bookmark {} by user {} failed, item could not be found",
                    id, p.getName());
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/bookmarks/delete")
    public void deleteBookmarks(@RequestBody List<String> bookmarkIds, Principal p,
                                HttpServletResponse response) throws ServletException {

        if (bookmarkIds != null && bookmarkIds.size() > 0) {
            for (String bookmarkId : bookmarkIds) {
                try {
                    deleteBookmark(bookmarkId, p);
                } catch (Exception e) {
                    String message = String.format("Error while user %s deleting bookmark %s: %s",
                            p.getName(), bookmarkId, e.getMessage());
                    throw new ServletException(message);
                }
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @PutMapping(value = "/tb-ui/authoring/rest/bookmark/{id}")
    public void updateBookmark(@PathVariable String id, @RequestBody WeblogBookmark newData, Principal p,
                               HttpServletResponse response) throws ServletException {
        try {
            WeblogBookmark bookmark = blogrollLinkRepository.getOne(id);
            Weblog weblog = bookmark.getWeblog();
            if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                WeblogBookmark bookmarkFromWeblog = weblog.getBookmarks().stream()
                        .filter(wb -> wb.getId().equals(bookmark.getId())).findFirst().orElse(null);
                if (bookmarkFromWeblog != null) {
                    bookmarkFromWeblog.setName(newData.getName());
                    bookmarkFromWeblog.setUrl(newData.getUrl());
                    bookmarkFromWeblog.setDescription(newData.getDescription());
                    weblogManager.saveWeblog(weblog);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    // should never happen
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (EntityNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @PutMapping(value = "/tb-ui/authoring/rest/bookmarks")
    public void addBookmark(@RequestParam(name = "weblogId") String weblogId, @RequestBody WeblogBookmark newData, Principal p,
                            HttpServletResponse response) throws ServletException {
        try {
            Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
            if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                WeblogBookmark bookmark = new WeblogBookmark(weblog, newData.getName(),
                        newData.getUrl(), newData.getDescription());
                weblog.addBookmark(bookmark);
                weblogManager.saveWeblog(weblog);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }
}
