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
package org.tightblog.rendering.processors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.tightblog.business.MailManager;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.search.IndexManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.rendering.comment.CommentAuthenticator;
import org.tightblog.rendering.comment.CommentValidator;
import org.tightblog.rendering.comment.CommentValidator.ValidationResult;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.I18nMessages;
import org.tightblog.util.Utilities;
import org.tightblog.rendering.cache.CacheManager;
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

    public void setWeblogPageRequestCreator(WeblogPageRequest.Creator creator) {
        this.weblogPageRequestCreator = creator;
    }

    @Autowired
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Autowired(required = false)
    private CommentAuthenticator commentAuthenticator = null;

    public void setCommentAuthenticator(CommentAuthenticator commentAuthenticator) {
        this.commentAuthenticator = commentAuthenticator;
    }

    @Autowired
    private IndexManager indexManager;

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Autowired
    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    @Autowired
    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    @Resource(name = "commentValidatorList")
    private List<CommentValidator> commentValidators;

    public void setCommentValidators(List<CommentValidator> commentValidators) {
        this.commentValidators = commentValidators;
    }

    /**
     * Here we handle incoming comment postings.
     */
    @RequestMapping(path = "/**", method = RequestMethod.POST)
    public void postComment(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        WebloggerProperties props = persistenceStrategy.getWebloggerProperties();

        WebloggerProperties.CommentPolicy commentOption = props.getCommentPolicy();

        if (WebloggerProperties.CommentPolicy.NONE.equals(commentOption)) {
            log.info("Getting comment post even though commenting is disabled -- returning 403");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String dispatchUrl;

        Weblog weblog;
        WeblogEntry entry;
        boolean nonSpamCommentApprovalRequired;

        WeblogPageRequest commentRequest = weblogPageRequestCreator.create(request);

        // check weblog and entry exist
        weblog = commentRequest.getWeblog();
        if (weblog == null) {
            log.info("Commenter attempted to leave comment for weblog with unknown handle: {}, returning 404",
                    commentRequest.getWeblogHandle());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        entry = weblogEntryManager.getWeblogEntryByAnchor(weblog, commentRequest.getWeblogAnchor());
        if (entry == null) {
            log.info("Commenter attempted to leave comment for weblog {}'s entry with unknown anchor: {}, returning 404",
                    commentRequest.getWeblogHandle(), commentRequest.getWeblogAnchor());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // We're forwarding to the PageProcessor to re-render the blog entry with the comment processing results
        dispatchUrl = PageProcessor.PATH + "/" + weblog.getHandle();
        dispatchUrl += "/entry/" + Utilities.encode(commentRequest.getWeblogAnchor());

        WeblogEntryComment incomingComment = createCommentFromRequest(request, entry, props.getCommentHtmlPolicy());
        log.debug("Incoming comment: {}", incomingComment.toString());

        // check comment for invalid input
        // Commenter can either be previewing or submitting the comment
        // test #1: ensure method being read properly
        String method = request.getParameter("method");
        incomingComment.setPreview(method != null && method.equals("preview"));

        String errorProperty;
        String[] errorValues = new String[1];

        // test #2
        if (!entry.getCommentsStillAllowed() || !entry.isPublished()) {
            errorProperty = "comments.disabled";
        } else if (!incomingComment.isPreview() && commentAuthenticator != null && !commentAuthenticator.authenticate(request)) {
            // test #3
            errorValues[0] = request.getParameter("answer");
            errorProperty = "error.commentAuthFailed";
        } else {
            errorProperty = validateComment(incomingComment);
        }

        I18nMessages messageUtils = I18nMessages.getMessages(commentRequest.getLocaleInstance());

        // return error due to bad input
        // test #4
        if (errorProperty != null) {
            incomingComment.setError(true);
            incomingComment.setMessage(messageUtils.getString(errorProperty, errorValues));
            request.setAttribute("commentForm", incomingComment);
            RequestDispatcher dispatcher = request.getRequestDispatcher(dispatchUrl);
            dispatcher.forward(request, response);
            return;
        }

        // At this stage, comment structurally good, only question is whether it is spam.
        // those with at least the POST role for the weblog don't need to have their comments moderated.
        boolean ownComment = false;
        String maybeUser = commentRequest.getAuthenticatedUser();
        if (maybeUser != null) {
            ownComment = userManager.checkWeblogRole(maybeUser, commentRequest.getWeblogHandle(), WeblogRole.POST);
        }

        String message = null;
        Map<String, List<String>> messages = new HashMap<>();
        // test #5: check even a spammy comment still approved if user is logged in.
        // test #6: not so if person has just edit draft.
        ValidationResult valResult = ownComment ? ValidationResult.NOT_SPAM : runSpamCheckers(incomingComment, messages);

        if (!incomingComment.isPreview()) {

            // test #7: determine nonSpamCommentApprovalRequired calculated properly
            nonSpamCommentApprovalRequired = WebloggerProperties.CommentPolicy.MUSTMODERATE.equals(commentOption) ||
                    WebloggerProperties.CommentPolicy.MUSTMODERATE.equals(weblog.getAllowComments());

            if (valResult == ValidationResult.NOT_SPAM) {
                if (!ownComment && nonSpamCommentApprovalRequired) {
                    // Valid comments go into moderation if required
                    incomingComment.setStatus(WeblogEntryComment.ApprovalStatus.PENDING);
                    message = messageUtils.getString("commentServlet.submittedToModerator");
                } else {
                    // else they're approved
                    incomingComment.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
                    message = messageUtils.getString("commentServlet.commentAccepted");
                }
            } else {
                // Invalid comments are marked as spam, but just a moderation message sent to spammer
                // Informing the spammer the reasons for its detection encourages the spammer to just modify
                // the spam message so it will pass through; also indicating that the message is subject
                // to moderation (and sure refusal) discourages future spamming attempts.
                incomingComment.setStatus(WeblogEntryComment.ApprovalStatus.SPAM);
                message = messageUtils.getString("commentServlet.submittedToModerator");

                // log specific error messages if they exist
                // test: capture logging, make sure correct.
                log.debug("Comment marked as spam, reasons: ");
                if (messages.size() > 0) {
                    for (Map.Entry<String, List<String>> item : messages.entrySet()) {
                        if (item.getValue() != null) {
                            log.debug(messageUtils.getString(item.getKey(), item.getValue() != null ?
                                    item.getValue().toArray() : null));
                        } else {
                            log.debug(messageUtils.getString(item.getKey()));
                        }
                    }
                } else {
                    log.debug("None available.");
                }
            }

            // Akismet validator can be configured to return -1 for blatant spam, if so configured, don't save in queue.
            if (!ValidationResult.BLATANT_SPAM.equals(valResult) && (!WeblogEntryComment.ApprovalStatus.SPAM.equals(incomingComment.getStatus()) || !props.isAutodeleteSpam())) {

                boolean noModerationNeeded = ownComment ||
                        (!nonSpamCommentApprovalRequired && !WeblogEntryComment.ApprovalStatus.SPAM.equals(incomingComment.getStatus()));
                // test: need to test saveComment called.
                weblogEntryManager.saveComment(incomingComment, noModerationNeeded);
                persistenceStrategy.flush();

                // need to test these methods called
                if (noModerationNeeded) {
                    mailManager.sendNewPublishedCommentNotification(incomingComment);
                } else {
                    mailManager.sendPendingCommentNotice(incomingComment, messages);
                }

                // only re-index/invalidate the cache if comment isn't moderated
                if (!nonSpamCommentApprovalRequired) {

                    // if published, index the entry
                    if (entry.isPublished() && indexManager.isIndexComments()) {
                        indexManager.updateIndex(entry, false);
                    }

                    // Clear all caches associated with comment
                    cacheManager.invalidate(incomingComment);
                }

                // comment was successful, clear the comment form
                incomingComment = new WeblogEntryComment();
                incomingComment.initializeFormFields();
            }
        }

        // the work has been done, now send the user back to the entry page
        if (message != null) {
            incomingComment.setMessage(message);
        }
        // for subsequent processing by PageProcessor, which will put in into PageModel
        // so the templates can work with the comments.
        request.setAttribute("commentForm", incomingComment);

        // off to PageProcessor's POST handling.
        log.debug("comment processed, forwarding to {}", dispatchUrl);
        RequestDispatcher dispatcher = request.getRequestDispatcher(dispatchUrl);
        dispatcher.forward(request, response);
    }

    String validateComment(WeblogEntryComment incomingComment) {
        String errorProperty = null;

        if (StringUtils.isBlank(incomingComment.getContent())) {
            errorProperty = "error.commentPostContentMissing";
        } else if (StringUtils.isBlank(incomingComment.getName())) {
            errorProperty = "error.commentPostNameMissing";
        } else if (StringUtils.isEmpty(incomingComment.getEmail()) || !incomingComment.getEmail().matches(EMAIL_ADDR_REGEXP)) {
            errorProperty = "error.commentPostFailedEmailAddress";
        } else if (StringUtils.isNotEmpty(incomingComment.getUrl()) &&
                !new UrlValidator(new String[]{"http", "https"}).isValid(incomingComment.getUrl())) {
            errorProperty = "error.commentPostFailedURL";
        }

        return errorProperty;
    }

    WeblogEntryComment createCommentFromRequest(HttpServletRequest request, WeblogEntry entry,
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
        comment.setWeblogEntry(entry);
        comment.setRemoteHost(request.getRemoteHost());
        comment.setPostTime(Instant.now());

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
        Whitelist commentHTMLWhitelist = sanitizerLevel.getWhitelist();

        // Need to insert paragraph breaks in case commenter didn't do so.
        String commentTemp = Utilities.insertLineBreaksIfMissing(comment.getContent());

        // Remove HTML tags outside those permitted by the TightBlog admin
        comment.setContent(Jsoup.clean(commentTemp, commentHTMLWhitelist));

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

    ValidationResult runSpamCheckers(WeblogEntryComment comment, Map<String, List<String>> messages) {
        boolean spamDetected = false;

        ValidationResult singleResponse;
        if (commentValidators.size() > 0) {
            for (CommentValidator val : commentValidators) {
                log.debug("Invoking comment validator {}", val.getClass().getName());
                singleResponse = val.validate(comment, messages);
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
