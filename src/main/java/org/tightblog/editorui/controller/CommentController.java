package org.tightblog.editorui.controller;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.tightblog.config.DynamicProperties;
import org.tightblog.editorui.model.CommentData;
import org.tightblog.service.EmailService;
import org.tightblog.service.URLService;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.CommentSearchCriteria;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.ApprovalStatus;
import org.tightblog.domain.WeblogRole;
import org.tightblog.dao.UserDao;
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
import javax.servlet.http.HttpServletResponse;
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
    private UserManager userManager;
    private UserDao userDao;
    private WeblogEntryManager weblogEntryManager;
    private LuceneIndexer luceneIndexer;
    private URLService urlService;
    private EmailService emailService;
    private MessageSource messages;
    private DynamicProperties dp;

    @Autowired
    public CommentController(WeblogDao weblogDao, UserManager userManager, UserDao userDao,
                             WeblogEntryManager weblogEntryManager, DynamicProperties dp,
                             LuceneIndexer luceneIndexer, URLService urlService, EmailService emailService,
                             MessageSource messages, WebloggerPropertiesDao webloggerPropertiesDao,
                             WeblogEntryDao weblogEntryDao,
                             WeblogEntryCommentDao weblogEntryCommentDao) {
        this.weblogDao = weblogDao;
        this.weblogEntryDao = weblogEntryDao;
        this.weblogEntryCommentDao = weblogEntryCommentDao;
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.userManager = userManager;
        this.userDao = userDao;
        this.weblogEntryManager = weblogEntryManager;
        this.luceneIndexer = luceneIndexer;
        this.urlService = urlService;
        this.emailService = emailService;
        this.messages = messages;
        this.dp = dp;
    }

    @PostMapping(value = "/{weblogId}/page/{page}")
    public CommentData getWeblogComments(@PathVariable String weblogId, @PathVariable int page,
                                         @RequestParam(required = false) String entryId,
                                         @RequestBody CommentSearchCriteria criteria,
                                         Principal principal, HttpServletResponse response) {

        Weblog weblog = weblogDao.findById(weblogId).orElse(null);
        if (weblog != null && userManager.checkWeblogRole(principal.getName(), weblog, WeblogRole.POST)) {

            CommentData data = new CommentData();

            criteria.setWeblog(weblog);
            if (entryId != null) {
                criteria.setEntry(weblogEntryDao.findByIdOrNull(entryId));
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
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
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

    @DeleteMapping(value = "/{id}")
    public void deleteComment(@PathVariable String id, Principal p, HttpServletResponse response) {

        WeblogEntryComment itemToRemove = weblogEntryCommentDao.findByIdOrNull(id);
        if (itemToRemove != null) {
            Weblog weblog = itemToRemove.getWeblogEntry().getWeblog();
            if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.POST)) {
                weblogEntryManager.removeComment(itemToRemove);
                luceneIndexer.updateIndex(itemToRemove.getWeblogEntry(), false);
                dp.updateLastSitewideChange();
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @PostMapping(value = "/{id}/approve")
    public void approveComment(@PathVariable String id, Principal p, HttpServletResponse response) {
        changeApprovalStatus(id, p, response, WeblogEntryComment.ApprovalStatus.APPROVED);
    }

    @PostMapping(value = "/{id}/hide")
    public void hideComment(@PathVariable String id, Principal p, HttpServletResponse response) {
        changeApprovalStatus(id, p, response, WeblogEntryComment.ApprovalStatus.DISAPPROVED);
    }

    private void changeApprovalStatus(@PathVariable String id, Principal p, HttpServletResponse response,
                               WeblogEntryComment.ApprovalStatus newStatus) {

        WeblogEntryComment comment = weblogEntryCommentDao.findByIdOrNull(id);
        if (comment != null) {
            Weblog weblog = comment.getWeblogEntry().getWeblog();
            if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.POST)) {
                WeblogEntryComment.ApprovalStatus oldStatus = comment.getStatus();
                comment.setStatus(newStatus);
                // send approval notification only first time, not after any subsequent hide and approves.
                if ((oldStatus == ApprovalStatus.PENDING || oldStatus == ApprovalStatus.SPAM) &&
                        newStatus == ApprovalStatus.APPROVED) {
                    emailService.sendYourCommentWasApprovedNotifications(Collections.singletonList(comment));
                }
                boolean needRefresh = ApprovalStatus.APPROVED.equals(oldStatus) ^ ApprovalStatus.APPROVED.equals(newStatus);
                weblogEntryManager.saveComment(comment, needRefresh);
                luceneIndexer.updateIndex(comment.getWeblogEntry(), false);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @PutMapping(value = "/{id}/content")
    public WeblogEntryComment updateComment(@PathVariable String id, Principal p, HttpServletRequest request,
                                     HttpServletResponse response) throws IOException {
        WeblogEntryComment wec = weblogEntryCommentDao.findByIdOrNull(id);
        if (wec == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            // need post permission to edit comments
            User authenticatedUser = userDao.findEnabledByUserName(p.getName());
            Weblog weblog = wec.getWeblogEntry().getWeblog();
            if (userManager.checkWeblogRole(authenticatedUser, weblog, WeblogRole.POST)) {
                String content = Utilities.apiValueToFormSubmissionValue(request.getInputStream());

                // Validate content
                HTMLSanitizer.Level sanitizerLevel = webloggerPropertiesDao.findOrNull().getCommentHtmlPolicy();
                Whitelist commentHTMLWhitelist = sanitizerLevel.getWhitelist();

                wec.setContent(Jsoup.clean(content, commentHTMLWhitelist));

                // don't update the posttime when updating the comment
                wec.setPostTime(wec.getPostTime());
                weblogEntryManager.saveComment(wec, true);
                return wec;
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        }
        return null;
    }
}
