/*
   Copyright 2020 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.tightblog.bloggerui.controller;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.tightblog.config.DynamicProperties;
import org.tightblog.bloggerui.model.CommentData;
import org.tightblog.service.EmailService;
import org.tightblog.service.URLService;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.CommentSearchCriteria;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.ApprovalStatus;
import org.tightblog.dao.WeblogEntryCommentDao;
import org.tightblog.dao.WeblogEntryDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.Utilities;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@EnableConfigurationProperties(DynamicProperties.class)
@RequestMapping(path = "/tb-ui/authoring/rest/comments")
public class CommentController {

    // number of comments to show per page
    private static final int ITEMS_PER_PAGE = 30;

    private WeblogDao weblogDao;
    private WeblogEntryDao weblogEntryDao;
    private WeblogEntryCommentDao weblogEntryCommentDao;
    private WebloggerPropertiesDao webloggerPropertiesDao;
    private WeblogEntryManager weblogEntryManager;
    private LuceneIndexer luceneIndexer;
    private URLService urlService;
    private EmailService emailService;
    private MessageSource messages;
    private DynamicProperties dp;

    @Autowired
    public CommentController(WeblogDao weblogDao, WeblogEntryManager weblogEntryManager, DynamicProperties dp,
                             LuceneIndexer luceneIndexer, URLService urlService, EmailService emailService,
                             MessageSource messages, WebloggerPropertiesDao webloggerPropertiesDao,
                             WeblogEntryDao weblogEntryDao,
                             WeblogEntryCommentDao weblogEntryCommentDao) {
        this.weblogDao = weblogDao;
        this.weblogEntryDao = weblogEntryDao;
        this.weblogEntryCommentDao = weblogEntryCommentDao;
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.weblogEntryManager = weblogEntryManager;
        this.luceneIndexer = luceneIndexer;
        this.urlService = urlService;
        this.emailService = emailService;
        this.messages = messages;
        this.dp = dp;
    }

    @GetMapping(value = "/searchfields")
    public Map<String, String> getCommentSearchFields(Locale locale) {

        Map<String, String> statusOptions = new LinkedHashMap<>();
        statusOptions.put("", messages.getMessage("generic.all", null, locale));
        statusOptions.put("PENDING", messages.getMessage("comments.onlyPending", null, locale));
        statusOptions.put("APPROVED", messages.getMessage("comments.onlyApproved", null, locale));
        statusOptions.put("DISAPPROVED", messages.getMessage("comments.onlyDisapproved", null, locale));
        statusOptions.put("SPAM", messages.getMessage("comments.onlySpam", null, locale));
        return statusOptions;
    }

    @PostMapping(value = "/{weblogId}/page/{page}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.Weblog), #weblogId, 'POST')")
    public CommentData getWeblogComments(@PathVariable String weblogId, @PathVariable int page,
                                         @RequestParam(required = false) String entryId,
                                         @RequestBody CommentSearchCriteria criteria, Principal p) {

        Weblog weblog = weblogDao.getOne(weblogId);
        CommentData data = new CommentData();

        criteria.setWeblog(weblog);
        if (entryId != null) {
            criteria.setEntry(weblogEntryDao.getOne(entryId));
            data.setEntryTitle(criteria.getEntry().getTitle());
        }
        criteria.setOffset(page * ITEMS_PER_PAGE);
        criteria.setMaxResults(ITEMS_PER_PAGE + 1);

        List<WeblogEntryComment> rawComments = weblogEntryManager.getComments(criteria);
        data.setComments(rawComments.stream()
                .peek(c -> c.getWeblogEntry().setPermalink(
                        urlService.getWeblogEntryURL(c.getWeblogEntry())))
                .collect(Collectors.toList()));

        if (rawComments.size() > ITEMS_PER_PAGE) {
            data.getComments().remove(data.getComments().size() - 1);
            data.setHasMore(true);
        }

        return data;
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogEntryComment), #id,  'POST')")
    public void deleteComment(@PathVariable String id, Principal p) {

        WeblogEntryComment itemToRemove = weblogEntryCommentDao.getOne(id);
        weblogEntryManager.removeComment(itemToRemove);
        luceneIndexer.updateIndex(itemToRemove.getWeblogEntry(), false);
        dp.updateLastSitewideChange();
    }

    @PostMapping(value = "/{id}/approve")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogEntryComment), #id, 'POST')")
    public void approveComment(@PathVariable String id, Principal p) {
        changeApprovalStatus(id, ApprovalStatus.APPROVED);
    }

    @PostMapping(value = "/{id}/hide")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogEntryComment), #id, 'POST')")
    public void hideComment(@PathVariable String id, Principal p) {
        changeApprovalStatus(id, ApprovalStatus.DISAPPROVED);
    }

    private void changeApprovalStatus(@PathVariable String id, ApprovalStatus newStatus) {

        WeblogEntryComment comment = weblogEntryCommentDao.getOne(id);
        ApprovalStatus oldStatus = comment.getStatus();
        comment.setStatus(newStatus);
        // send approval notification only first time, not after any subsequent hide and approves.
        if ((oldStatus == ApprovalStatus.PENDING || oldStatus == ApprovalStatus.SPAM) &&
                newStatus == ApprovalStatus.APPROVED) {
            emailService.sendYourCommentWasApprovedNotifications(Collections.singletonList(comment));
        }
        boolean needRefresh = ApprovalStatus.APPROVED.equals(oldStatus) ^ ApprovalStatus.APPROVED.equals(newStatus);
        weblogEntryManager.saveComment(comment, needRefresh);
        luceneIndexer.updateIndex(comment.getWeblogEntry(), false);
    }

    @PutMapping(value = "/{id}/content")
    @PreAuthorize("@securityService.hasAccess(#p.name, T(org.tightblog.domain.WeblogEntryComment), #id, 'POST')")
    public WeblogEntryComment updateComment(@PathVariable String id, Principal p, HttpServletRequest request)
            throws IOException {

        WeblogEntryComment wec = weblogEntryCommentDao.getOne(id);
        String content = Utilities.apiValueToFormSubmissionValue(request.getInputStream());

        // Validate content
        HTMLSanitizer.Level sanitizerLevel = webloggerPropertiesDao.findOrNull().getCommentHtmlPolicy();
        Whitelist commentHTMLWhitelist = sanitizerLevel.getWhitelist();
        wec.setContent(Jsoup.clean(content, commentHTMLWhitelist));

        weblogEntryManager.saveComment(wec, true);
        return wec;
    }
}
