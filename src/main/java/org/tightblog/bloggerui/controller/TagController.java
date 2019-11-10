/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.bloggerui.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.tightblog.bloggerui.model.WeblogTagSummaryData;
import org.tightblog.service.URLService;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntryTagAggregate;
import org.tightblog.domain.WeblogRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.dao.WeblogDao;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/tb-ui/authoring/rest/tags")
public class TagController {

    private WeblogDao weblogDao;
    private UserManager userManager;
    private WeblogManager weblogManager;
    private URLService urlService;

    @Autowired
    public TagController(WeblogDao weblogDao, UserManager userManager, WeblogManager weblogManager,
                         URLService urlService) {
        this.weblogDao = weblogDao;
        this.userManager = userManager;
        this.weblogManager = weblogManager;
        this.urlService = urlService;
    }

    private static Logger log = LoggerFactory.getLogger(TagController.class);

    // number of entries to show per page
    private static final int ITEMS_PER_PAGE = 30;

    @GetMapping(value = "/{weblogId}/page/{page}")
    public WeblogTagSummaryData getTags(@PathVariable String weblogId, @PathVariable int page,
                                        Principal principal, HttpServletResponse response) {

        Weblog weblog = weblogDao.findById(weblogId).orElse(null);
        if (weblog != null && userManager.checkWeblogRole(principal.getName(), weblog, WeblogRole.OWNER)) {

            WeblogTagSummaryData data = new WeblogTagSummaryData();

            List<WeblogEntryTagAggregate> rawEntries = weblogManager.getTags(weblog, "count", null,
                    page * ITEMS_PER_PAGE, ITEMS_PER_PAGE + 1);

            data.getTags().addAll(rawEntries.stream().peek(re -> re.setWeblog(null))
                    .peek(re -> re.setViewUrl(
                            urlService.getWeblogCollectionURL(weblog, null, null, re.getName(), 0)))
                    .collect(Collectors.toList()));

            if (rawEntries.size() > ITEMS_PER_PAGE) {
                data.getTags().remove(data.getTags().size() - 1);
                data.setHasMore(true);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            return data;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @PostMapping(value = "/weblog/{weblogId}/delete")
    public void deleteTags(@PathVariable String weblogId, @RequestBody List<String> tagNames, Principal p,
                           HttpServletResponse response) {
        if (tagNames != null && tagNames.size() > 0) {
            Weblog weblog = weblogDao.findById(weblogId).orElse(null);
            if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.POST)) {
                for (String tagName : tagNames) {
                    try {
                        weblogManager.removeTag(weblog, tagName);
                    } catch (Exception e) {
                        log.warn("Error removing tagName {} from weblog {}: {}", tagName, weblog.getHandle(), e.getMessage());
                        throw e;
                    }
                }
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @PostMapping(value = "/weblog/{weblogId}/add/currenttag/{currentTagName}/newtag/{newTagName}")
    public Map<String, Integer> addTag(@PathVariable String weblogId, @PathVariable String currentTagName,
                                       @PathVariable String newTagName, Principal p, HttpServletResponse response) {
        return changeTags(weblogId, currentTagName, newTagName, p, response, true);
    }

    @PostMapping(value = "/weblog/{weblogId}/replace/currenttag/{currentTagName}/newtag/{newTagName}")
    public Map<String, Integer> replaceTag(@PathVariable String weblogId, @PathVariable String currentTagName,
                                       @PathVariable String newTagName, Principal p, HttpServletResponse response) {
        return changeTags(weblogId, currentTagName, newTagName, p, response, false);
    }

    private Map<String, Integer> changeTags(String weblogId, String currentTagName, String newTagName,
                                            Principal p, HttpServletResponse response, boolean isAdd) {

        Weblog weblog = weblogDao.findById(weblogId).orElse(null);
        log.info("For weblog {} {} current tag {} to new tag {}", isAdd ? "adding" : "renaming",
                weblog == null ? "(null)" : weblog.getHandle(), currentTagName, newTagName);

        if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.POST)) {
            Map<String, Integer> results = weblogManager.addTag(weblog, currentTagName, newTagName);
            if (!isAdd) {
                weblogManager.removeTag(weblog, currentTagName);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return results;
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

}
