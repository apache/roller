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
package org.tightblog.bloggerui.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.tightblog.dao.BlogrollLinkDao;
import org.tightblog.dao.WeblogDao;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class BlogrollController {
    private static Logger log = LoggerFactory.getLogger(BlogrollController.class);

    private WeblogDao weblogDao;
    private BlogrollLinkDao blogrollLinkDao;
    private WeblogManager weblogManager;
    private UserManager userManager;

    @Autowired
    public BlogrollController(WeblogDao weblogDao, BlogrollLinkDao blogrollLinkDao,
                              WeblogManager weblogManager, UserManager userManager) {
        this.weblogDao = weblogDao;
        this.blogrollLinkDao = blogrollLinkDao;
        this.weblogManager = weblogManager;
        this.userManager = userManager;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{id}/bookmarks")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #id, 'OWNER')")
    public List<WeblogBookmark> getBookmarks(@PathVariable String id, Principal p) {

        return weblogDao.getOne(id).getBookmarks()
                .stream()
                .peek(bkmk -> bkmk.setWeblog(null))
                .collect(Collectors.toList());
    }

    @PutMapping(value = "/tb-ui/authoring/rest/bookmarks")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'OWNER')")
    public void addBookmark(@RequestParam(name = "weblogId") String weblogId, @RequestBody WeblogBookmark newData,
                            Principal p) {

        Weblog weblog = weblogDao.getOne(weblogId);
        WeblogBookmark bookmark = new WeblogBookmark(weblog, newData.getName(),
                newData.getUrl(), newData.getDescription());
        weblog.addBookmark(bookmark);
        weblogManager.saveWeblog(weblog, true);
    }

    @PutMapping(value = "/tb-ui/authoring/rest/bookmark/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogBookmark), #id, 'OWNER')")
    public void updateBookmark(@PathVariable String id, @RequestBody WeblogBookmark newData, Principal p) {

        WeblogBookmark bookmark = blogrollLinkDao.getOne(id);
        bookmark.setName(newData.getName());
        bookmark.setUrl(newData.getUrl());
        bookmark.setDescription(newData.getDescription());
        weblogManager.saveWeblog(bookmark.getWeblog(), true);
    }

    private void deleteBookmark(String id, Principal p) {
        WeblogBookmark itemToRemove = blogrollLinkDao.findById(id).orElse(null);
        if (itemToRemove != null) {
            Weblog weblog = itemToRemove.getWeblog();
            if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.OWNER)) {
                weblog.getBookmarks().remove(itemToRemove);
                weblogManager.saveWeblog(weblog, true);
            } else {
                log.warn("Effort to delete bookmark {} by user {} failed, insufficient access rights",
                        itemToRemove, p.getName());
            }
        } else {
            log.warn("Effort to delete bookmark {} by user {} failed, item could not be found",
                    id, p.getName());
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/bookmarks/delete")
    public void deleteBookmarks(@RequestBody List<String> bookmarkIds, Principal p) {
        if (bookmarkIds != null && bookmarkIds.size() > 0) {
            for (String bookmarkId : bookmarkIds) {
                deleteBookmark(bookmarkId, p);
            }
        }
    }
}
