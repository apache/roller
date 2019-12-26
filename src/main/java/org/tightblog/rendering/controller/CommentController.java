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
package org.tightblog.rendering.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.service.CommentSpamChecker;
import org.tightblog.service.EmailService;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.ApprovalStatus;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.domain.WebloggerProperties.CommentPolicy;
import org.tightblog.domain.WebloggerProperties.SpamPolicy;
import org.tightblog.rendering.service.CommentAuthenticator;
import org.tightblog.domain.WeblogEntryComment.SpamCheckResult;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.Utilities;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Handles all incoming weblog entry comment posts.
 * <p>
 * We validate each incoming comment based on various comment settings and if
 * all checks are passed then the comment is saved.
 * <p>
 * Incoming comments are tested by a customizable list of validators.  If they
 * are found to be spam, then they are marked as spam (to be later evaluated by
 * the blogger on the Comments tab) and hidden from view.
 * <p>
 * If email notification is turned on, each new non-spam comment will result in
 * an email sent to the blog owner and all who have commented on the same post.
 */
@RestController("RenderingCommentController")
// how @RequestMapping is combined at the class- and method-levels: http://stackoverflow.com/q/22702568
@RequestMapping(path = CommentController.PATH)
public class CommentController extends AbstractController {

    private static Logger log = LoggerFactory.getLogger(CommentController.class);

    public static final String PATH = "/tb-ui/rendering/comment";

    private static final String EMAIL_ADDR_REGEXP = "^.*@.*[.].{2,}$";

    private WeblogDao weblogDao;

    private UserDao userDao;
    private LuceneIndexer luceneIndexer;
    private WeblogEntryManager weblogEntryManager;
    private UserManager userManager;
    private EmailService emailService;
    private MessageSource messages;
    private PageModel pageModel;
    private WebloggerPropertiesDao webloggerPropertiesDao;
    private CommentSpamChecker commentSpamChecker;

    private EntityManager entityManager;

    @PersistenceContext
    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public CommentController(WeblogDao weblogDao, UserDao userDao, LuceneIndexer luceneIndexer,
                             WeblogEntryManager weblogEntryManager, UserManager userManager,
                             EmailService emailService,
                             MessageSource messages, PageModel pageModel,
                             CommentSpamChecker commentSpamChecker,
                             WebloggerPropertiesDao webloggerPropertiesDao) {
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.weblogDao = weblogDao;
        this.userDao = userDao;
        this.luceneIndexer = luceneIndexer;
        this.weblogEntryManager = weblogEntryManager;
        this.userManager = userManager;
        this.pageModel = pageModel;
        this.emailService = emailService;
        this.messages = messages;
        this.commentSpamChecker = commentSpamChecker;
    }

    @Autowired(required = false)
    private CommentAuthenticator commentAuthenticator;

    void setCommentAuthenticator(CommentAuthenticator commentAuthenticator) {
        this.commentAuthenticator = commentAuthenticator;
    }

    /**
     * Here we handle incoming comment postings.
     */
    @PostMapping(path = "/{weblogHandle}/entry/{anchor}")
    void postComment(HttpServletRequest request, HttpServletResponse response,
                     @PathVariable String weblogHandle, @PathVariable String anchor,
                     Principal principal)
            throws IOException, ServletException {

        WebloggerProperties props = webloggerPropertiesDao.findOrNull();
        WebloggerProperties.CommentPolicy commentOption = props.getCommentPolicy();

        if (WebloggerProperties.CommentPolicy.NONE.equals(commentOption)) {
            log.info("Getting comment post even though commenting is disabled -- returning 404");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        WeblogPageRequest incomingRequest = new WeblogPageRequest(weblogHandle, principal, pageModel);
        incomingRequest.setWeblogEntryAnchor(anchor);

        Weblog weblog = weblogDao.findByHandleAndVisibleTrue(incomingRequest.getWeblogHandle());
        if (weblog == null) {
            log.info("Commenter attempted to leave comment for weblog with unknown handle: {}, returning 404",
                    incomingRequest.getWeblogHandle());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            incomingRequest.setWeblog(weblog);
        }

        WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog, incomingRequest.getWeblogEntryAnchor());
        if (entry == null || !entry.isPublished()) {
            log.info("Commenter attempted to leave comment for weblog {}'s entry with unknown or " +
                            "unpublished anchor: {}, returning 404",
                    incomingRequest.getWeblogHandle(), incomingRequest.getWeblogEntryAnchor());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            incomingRequest.setWeblogEntry(entry);
        }

        if (incomingRequest.getAuthenticatedUser() != null) {
            incomingRequest.setBlogger(userDao.findEnabledByUserName(incomingRequest.getAuthenticatedUser()));
        }

        WeblogEntryComment incomingComment = createCommentFromRequest(request, incomingRequest, props.getCommentHtmlPolicy());

        log.debug("Incoming comment: {}", incomingComment.toString());

        // First check comment for valid and authorized input
        String errorProperty;
        String errorValue = null;

        if (!weblogEntryManager.canSubmitNewComments(incomingRequest.getWeblogEntry())) {
            errorProperty = "comments.disabled";
        } else if (commentAuthenticator != null && incomingRequest.getBlogger() == null
                && !commentAuthenticator.authenticate(request)) {
            errorValue = request.getParameter("answer");
            errorProperty = "error.commentAuthFailed";
        } else {
            errorProperty = validateComment(incomingComment);
        }

        if (errorProperty != null) {
            // return error due to bad/missing input
            incomingComment.setStatus(ApprovalStatus.INVALID);
            incomingComment.setSubmitResponseMessage(messages.getMessage(errorProperty, new Object[]{errorValue},
                    request.getLocale()));
        } else {
            // Otherwise next check if comment needs moderation (approval)
            boolean ownComment = userManager.checkWeblogRole(incomingRequest.getBlogger(), weblog,
                    WeblogRole.POST);

            boolean skipModeration = ownComment ||
                    (CommentPolicy.MODERATE_NONAUTH.equals(commentOption) && incomingRequest.getAuthenticatedUser() != null);

            String commentStatusKey;

            // most restrictive policy between weblog and global in effect
            SpamPolicy spamPolicy = Stream.of(incomingComment.getWeblog().getSpamPolicy(), props.getSpamPolicy())
                    .max(Comparator.comparing(Enum::ordinal))
                    .orElse(SpamPolicy.MARK_SPAM);
            Map<String, List<String>> spamEvaluations = new HashMap<>();

            if (skipModeration) {
                // else they're approved
                incomingComment.setStatus(ApprovalStatus.APPROVED);
                commentStatusKey = "commentServlet.commentAccepted";
            } else {
                incomingComment.setStatus(ApprovalStatus.PENDING);
                commentStatusKey = "commentServlet.submittedToModerator";

                if (spamPolicy.getLevel() > SpamPolicy.DONT_CHECK.getLevel()) {
                    SpamCheckResult valResult = commentSpamChecker.evaluate(incomingComment, spamEvaluations);

                    if (valResult == SpamCheckResult.SPAM) {
                        incomingComment.setStatus(ApprovalStatus.SPAM);
                    }
                }
            }

            incomingComment.setSubmitResponseMessage(messages.getMessage(commentStatusKey, null,
                    request.getLocale()));

            ApprovalStatus status = incomingComment.getStatus();

            // Don't save spam if blog configured to ignore it
            if (!ApprovalStatus.SPAM.equals(status) || !SpamPolicy.JUST_DELETE.equals(spamPolicy)) {

                weblogEntryManager.saveComment(incomingComment, ApprovalStatus.APPROVED.equals(status));

                // now email and index
                if (ApprovalStatus.APPROVED.equals(status)) {
                    emailService.sendNewPublishedCommentNotification(incomingComment);

                    if (luceneIndexer.isIndexComments()) {
                        luceneIndexer.updateIndex(incomingRequest.getWeblogEntry(), false);
                    }
                } else if (!ApprovalStatus.SPAM.equals(status) || spamPolicy.getLevel() < SpamPolicy.NO_EMAIL.getLevel()) {
                    emailService.sendPendingCommentNotice(incomingComment, spamEvaluations);
                }
            } else {
                log.info("Incoming comment from {} ({}) for blog {} judged to be spam, deleted per blog's spam policy",
                        incomingComment.getName(), incomingComment.getEmail(), incomingComment.getWeblog().getHandle());
            }

            // detach comment object in case of comment approvals, allows for comment to appear in comment list
            // (as a new JPA managed copy) and also provide status feedback (detached copy) on the comment entry form.
            entityManager.detach(incomingComment);
            // clear comment fields while retaining status field for rendering
            incomingComment.initializeFormFields();
        }

        // now send the user back to the weblog entry page via PageProcessor
        String dispatchUrl = PageController.PATH + "/" + weblog.getHandle() + "/entry/"
                + Utilities.encode(incomingRequest.getWeblogEntry().getAnchor());

        log.debug("comment processed, forwarding to {}", dispatchUrl);
        // add comment so PageProcessor can place it in the PageModel for rendering
        request.setAttribute("commentForm", incomingComment);
        RequestDispatcher dispatcher = request.getRequestDispatcher(dispatchUrl);
        dispatcher.forward(request, response);
    }

    String validateComment(WeblogEntryComment incomingComment) {
        String errorProperty = null;

        if (StringUtils.isBlank(incomingComment.getContent())) {
            errorProperty = "error.commentPostContentMissing";
        } else if (StringUtils.isBlank(incomingComment.getName())) {
            errorProperty = "error.commentPostNameMissing";
        } else if (StringUtils.isEmpty(incomingComment.getEmail())
                || !incomingComment.getEmail().matches(EMAIL_ADDR_REGEXP)) {
            errorProperty = "error.commentPostFailedEmailAddress";
        } else if (StringUtils.isNotEmpty(incomingComment.getUrl()) &&
                !new UrlValidator(new String[]{"http", "https"}).isValid(incomingComment.getUrl())) {
            errorProperty = "error.commentPostFailedURL";
        }

        return errorProperty;
    }

    WeblogEntryComment createCommentFromRequest(HttpServletRequest request, WeblogPageRequest pageRequest,
                                                HTMLSanitizer.Level sanitizerLevel) {

        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setNotify(request.getParameter("notify") != null);
        comment.setName(Utilities.removeHTML(request.getParameter("name")));
        comment.setEmail(Utilities.removeHTML(request.getParameter("email")));
        comment.setWeblogEntry(pageRequest.getWeblogEntry());
        comment.setRemoteHost(request.getRemoteHost());
        comment.setPostTime(Instant.now());
        comment.setBlogger(pageRequest.getBlogger());
        comment.setWeblog(pageRequest.getWeblog());

        // Validate url
        comment.setUrl(Utilities.removeHTML(request.getParameter("url")));
        String urlCheck = comment.getUrl();
        if (StringUtils.isNotBlank(urlCheck)) {
            urlCheck = urlCheck.trim().toLowerCase();
            if (!urlCheck.startsWith("http://") && !urlCheck.startsWith("https://")) {
                urlCheck = "https://" + urlCheck;
            }
            comment.setUrl(urlCheck);
        }

        // Validate content
        String rawComment = request.getParameter("content");

        if (StringUtils.isNotBlank(rawComment)) {
            comment.setContent(StringUtils.left(rawComment, 2000));

            Whitelist commentHTMLWhitelist = sanitizerLevel.getWhitelist();

            // Need to insert paragraph breaks in case commenter didn't do so.
            String commentTemp = Utilities.insertLineBreaksIfMissing(comment.getContent());

            // Remove HTML tags outside those permitted by the TightBlog admin
            comment.setContent(Jsoup.clean(commentTemp, commentHTMLWhitelist));
        }

        return comment;
    }

    /**
     * Used for generating the html used for comment authentication.  This is done
     * outside of the normal rendering process so that we can cache full pages and
     * still set the comment authentication section dynamically.
     */
    @GetMapping(value = "/authform")
    ResponseEntity<Resource> generateAuthForm(HttpServletRequest request) {
        byte[] html = (commentAuthenticator == null ? "" : commentAuthenticator.getHtml(request))
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                // https://www.baeldung.com/spring-security-cache-control-headers
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.TEXT_HTML)
                .contentLength(html.length)
                .body(new ByteArrayResource(html));
    }
}
