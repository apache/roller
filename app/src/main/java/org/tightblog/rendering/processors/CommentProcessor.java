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
package org.tightblog.rendering.processors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.context.MessageSource;
import org.tightblog.business.MailManager;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.search.IndexManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WeblogEntryComment.ApprovalStatus;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.pojos.WebloggerProperties.CommentPolicy;
import org.tightblog.rendering.comment.CommentAuthenticator;
import org.tightblog.rendering.comment.CommentValidator;
import org.tightblog.rendering.comment.CommentValidator.ValidationResult;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.Utilities;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@RestController
// how @RequestMapping is combined at the class- and method-levels: http://stackoverflow.com/q/22702568
@RequestMapping(path = "/tb-ui/rendering/comment")
public class CommentProcessor extends AbstractProcessor {

    private static Logger log = LoggerFactory.getLogger(CommentProcessor.class);

    public static final String PATH = "/tb-ui/rendering/comment";

    private static final String EMAIL_ADDR_REGEXP = "^.*@.*[.].{2,}$";

    private WeblogPageRequest.Creator weblogPageRequestCreator;

    public CommentProcessor() {
        this.weblogPageRequestCreator = new WeblogPageRequest.Creator();
    }

    void setWeblogPageRequestCreator(WeblogPageRequest.Creator creator) {
        this.weblogPageRequestCreator = creator;
    }

    @Autowired(required = false)
    private CommentAuthenticator commentAuthenticator;

    void setCommentAuthenticator(CommentAuthenticator commentAuthenticator) {
        this.commentAuthenticator = commentAuthenticator;
    }

    @Autowired
    private IndexManager indexManager;

    void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    @Autowired
    private WeblogManager weblogManager;

    void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Autowired
    private UserManager userManager;

    void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy;

    void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    @Autowired
    private MailManager mailManager;

    void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    @Resource(name = "commentValidatorList")
    private List<CommentValidator> commentValidators;

    void setCommentValidators(List<CommentValidator> commentValidators) {
        this.commentValidators = commentValidators;
    }

    @Autowired
    private MessageSource messages;

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }

    /**
     * Here we handle incoming comment postings.
     */
    @RequestMapping(path = "/**", method = RequestMethod.POST)
    void postComment(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        WebloggerProperties props = persistenceStrategy.getWebloggerProperties();
        WebloggerProperties.CommentPolicy commentOption = props.getCommentPolicy();

        if (WebloggerProperties.CommentPolicy.NONE.equals(commentOption)) {
            log.info("Getting comment post even though commenting is disabled -- returning 403");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        WeblogPageRequest incomingRequest = weblogPageRequestCreator.create(request);

        Weblog weblog = weblogManager.getWeblogByHandle(incomingRequest.getWeblogHandle(), true);
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
            incomingRequest.setBlogger(userManager.getEnabledUserByUserName(incomingRequest.getAuthenticatedUser()));
        }

        WeblogEntryComment incomingComment = createCommentFromRequest(request, incomingRequest, props.getCommentHtmlPolicy());

        log.debug("Incoming comment: {}", incomingComment.toString());

        // First check comment for valid and authorized input
        String errorProperty;
        String errorValue = null;

        if (!weblogEntryManager.canSubmitNewComments(incomingRequest.getWeblogEntry())) {
            errorProperty = "comments.disabled";
        } else if (!incomingComment.isPreview() && commentAuthenticator != null
                && !commentAuthenticator.authenticate(request)) {
            errorValue = request.getParameter("answer");
            errorProperty = "error.commentAuthFailed";
        } else {
            errorProperty = validateComment(incomingComment);
        }

        if (errorProperty != null) {
            // return error due to bad input
            incomingComment.setStatus(ApprovalStatus.INVALID);
            incomingComment.setSubmitResponseMessage(messages.getMessage(errorProperty, new Object[]{errorValue},
                    request.getLocale()));
        } else if (!incomingComment.isPreview()) {
            // Otherwise next check comment for spam
            boolean ownComment = userManager.checkWeblogRole(incomingRequest.getBlogger(), weblog,
                    WeblogRole.POST);

            boolean commentRequiresApproval = !ownComment && (CommentPolicy.MUSTMODERATE.equals(commentOption) ||
                    CommentPolicy.MUSTMODERATE.equals(weblog.getAllowComments()));

            Map<String, List<String>> spamEvaluations = new HashMap<>();
            ValidationResult valResult = ownComment ? ValidationResult.NOT_SPAM : runSpamCheckers(incomingComment,
                    spamEvaluations);

            String commentStatusKey;

            if (valResult == ValidationResult.NOT_SPAM) {
                if (commentRequiresApproval) {
                    // Valid comments go into moderation if required
                    incomingComment.setStatus(ApprovalStatus.PENDING);
                    commentStatusKey = "commentServlet.submittedToModerator";
                } else {
                    // else they're approved
                    incomingComment.setStatus(ApprovalStatus.APPROVED);
                    commentStatusKey = "commentServlet.commentAccepted";
                }
            } else {
                // Invalid comments are marked as spam, but just a moderation message sent to spammer
                // Informing the spammer the reasons for its detection encourages the spammer to modify
                // the spam message so it will pass through; also indicating that the message is subject
                // to moderation (and sure refusal) discourages future spamming attempts.
                incomingComment.setStatus(ApprovalStatus.SPAM);
                commentStatusKey = "commentServlet.submittedToModerator";
            }

            incomingComment.setSubmitResponseMessage(messages.getMessage(commentStatusKey, null,
                    request.getLocale()));

            // Don't save spam if evaluated as blatant or if blog server configured to ignore all spam.
            if (!ValidationResult.BLATANT_SPAM.equals(valResult) &&
                    (!ApprovalStatus.SPAM.equals(incomingComment.getStatus())
                            || !props.isAutodeleteSpam())) {

                // if spam, requires approval
                commentRequiresApproval |= ApprovalStatus.SPAM.equals(incomingComment.getStatus());

                weblogEntryManager.saveComment(incomingComment, !commentRequiresApproval);
                persistenceStrategy.getWebloggerProperties().setLastWeblogChange(Instant.now());
                persistenceStrategy.store(persistenceStrategy.getWebloggerProperties());
                persistenceStrategy.flush();

                if (commentRequiresApproval) {
                    mailManager.sendPendingCommentNotice(incomingComment, spamEvaluations);
                } else {
                    mailManager.sendNewPublishedCommentNotification(incomingComment);

                    if (indexManager.isIndexComments()) {
                        indexManager.updateIndex(incomingRequest.getWeblogEntry(), false);
                    }
                }
            }

            // detach comment object in case of comment approvals, allows for comment to appear in comment list
            // (as a new JPA managed copy) and also provide status feedback (detached copy) on the comment entry form.
            persistenceStrategy.detach(incomingComment);
            // clear comment fields while retaining status field for rendering
            incomingComment.initializeFormFields();
        }

        // now send the user back to the weblog entry page via PageProcessor
        String dispatchUrl = PageProcessor.PATH + "/" + weblog.getHandle() + "/entry/"
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

        /*
         * Convert request parameters into a WeblogEntryComment object.  Params used:
         *   name - comment author
         *   email - comment email
         *   url - comment referring url
         *   content - comment contents
         *   notify - if commenter wants to receive notifications
         */
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setNotify(request.getParameter("notify") != null);
        comment.setName(Utilities.removeHTML(request.getParameter("name")));
        comment.setEmail(Utilities.removeHTML(request.getParameter("email")));
        comment.setWeblogEntry(pageRequest.getWeblogEntry());
        comment.setRemoteHost(request.getRemoteHost());
        comment.setPostTime(Instant.now());
        comment.setBlogger(pageRequest.getBlogger());

        String previewCheck = request.getParameter("preview");
        comment.setPreview(previewCheck != null && !"false".equalsIgnoreCase(previewCheck));

        // Validate url
        comment.setUrl(Utilities.removeHTML(request.getParameter("url")));
        String urlCheck = comment.getUrl();
        if (StringUtils.isNotEmpty(urlCheck)) {
            urlCheck = urlCheck.trim().toLowerCase();
            if (!urlCheck.startsWith("http://") && !urlCheck.startsWith("https://")) {
                urlCheck = "http://" + urlCheck;
            }
        }
        comment.setUrl(urlCheck);

        // Validate content
        comment.setContent(StringUtils.left(request.getParameter("content"), 2000));

        if (comment.getContent() != null) {
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
    @RequestMapping(value = "/authform", method = RequestMethod.GET)
    void generateAuthForm(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html; charset=utf-8");

        // Convince proxies and browsers not to cache this.
        response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Expires", "-1");

        PrintWriter out = response.getWriter();
        out.println(commentAuthenticator == null ? "" : commentAuthenticator.getHtml(request));
    }

    ValidationResult runSpamCheckers(WeblogEntryComment comment, Map<String, List<String>> validationMessages) {
        boolean spamDetected = false;

        ValidationResult singleResponse;
        if (commentValidators.size() > 0) {
            for (CommentValidator val : commentValidators) {
                log.debug("Invoking comment validator {}", val.getClass().getName());
                singleResponse = val.validate(comment, validationMessages);
                if (ValidationResult.BLATANT_SPAM.equals(singleResponse)) {
                    return ValidationResult.BLATANT_SPAM;
                } else if (ValidationResult.SPAM.equals(singleResponse)) {
                    spamDetected = true;
                }
            }
            return spamDetected ? ValidationResult.SPAM : ValidationResult.NOT_SPAM;
        } else {
            // When no validators: consider all comments valid
            return ValidationResult.NOT_SPAM;
        }
    }
}
