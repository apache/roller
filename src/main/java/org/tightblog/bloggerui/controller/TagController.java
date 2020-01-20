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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.tightblog.bloggerui.model.WeblogTagSummaryData;
import org.tightblog.service.URLService;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntryTagAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.dao.WeblogDao;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/tb-ui/authoring/rest/tags")
public class TagController {

    private WeblogDao weblogDao;
    private WeblogManager weblogManager;
    private URLService urlService;

    @Autowired
    public TagController(WeblogDao weblogDao, WeblogManager weblogManager, URLService urlService) {
        this.weblogDao = weblogDao;
        this.weblogManager = weblogManager;
        this.urlService = urlService;
    }

    private static Logger log = LoggerFactory.getLogger(TagController.class);

    // number of entries to show per page
    private static final int ITEMS_PER_PAGE = 30;

    @GetMapping(value = "/{weblogId}/page/{page}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public WeblogTagSummaryData getTags(@PathVariable String weblogId, @PathVariable int page, Principal p) {

        Weblog weblog = weblogDao.getOne(weblogId);

        List<WeblogEntryTagAggregate> rawEntries = weblogManager.getTags(weblog, "count", null,
                page * ITEMS_PER_PAGE, ITEMS_PER_PAGE + 1);

        WeblogTagSummaryData data = new WeblogTagSummaryData();
        data.getTags().addAll(rawEntries.stream()
                .peek(re -> re.setWeblog(null))
                .peek(re -> re.setViewUrl(
                        urlService.getWeblogCollectionURL(weblog, null, null, re.getName(), 0)))
                .collect(Collectors.toList()));

        if (rawEntries.size() > ITEMS_PER_PAGE) {
            data.getTags().remove(data.getTags().size() - 1);
            data.setHasMore(true);
        }

        return data;
    }

    @PostMapping(value = "/weblog/{weblogId}/add/currenttag/{currentTagName}/newtag/{newTagName}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public Map<String, Integer> addTag(@PathVariable String weblogId, @PathVariable String currentTagName,
                                       @PathVariable String newTagName, Principal p) {
        return changeTags(weblogId, currentTagName, newTagName, true);
    }

    @PostMapping(value = "/weblog/{weblogId}/replace/currenttag/{currentTagName}/newtag/{newTagName}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public Map<String, Integer> replaceTag(@PathVariable String weblogId, @PathVariable String currentTagName,
                                       @PathVariable String newTagName, Principal p) {
        return changeTags(weblogId, currentTagName, newTagName, false);
    }

    private Map<String, Integer> changeTags(String weblogId, String currentTagName, String newTagName, boolean isAdd) {

        Weblog weblog = weblogDao.getOne(weblogId);
        log.info("For weblog {} {} current tag {} to new tag {}", weblog.getHandle(), isAdd ? "adding" : "renaming",
                currentTagName, newTagName);

        Map<String, Integer> results = weblogManager.addTag(weblog, currentTagName, newTagName);
        if (!isAdd) {
            weblogManager.removeTag(weblog, currentTagName);
        }
        return results;
    }

    @PostMapping(value = "/weblog/{weblogId}/delete")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public void deleteTags(@PathVariable String weblogId, @RequestBody List<String> tagNames, Principal p) {
        Weblog weblog = weblogDao.getOne(weblogId);
        for (String tagName : tagNames) {
            weblogManager.removeTag(weblog, tagName);
        }
    }
}
