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
package org.apache.roller.weblogger.ui.rendering.processors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.pojos.WebloggerProperties;
import org.apache.roller.weblogger.ui.rendering.comment.CommentAuthenticator;
import org.apache.roller.weblogger.ui.rendering.comment.CommentValidator;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogEntryRequest;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.CacheManager;
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
 * Incoming comments are tested against any site and/or weblog-defined blacklist values. If they
 * are found to be spam, then they are marked as spam and hidden from view.
 * <p>
 * If email notification is turned on, each new comment will result in an email
 * sent to the blog owner and all who have commented on the same post.
 */
@RestController
// how @RequestMapping is combined at the class- and method-levels: http://stackoverflow.com/q/22702568
@RequestMapping(path = "/tb-ui/rendering/comment")
public class CommentProcessor extends AbstractProcessor {

    private static Logger log = LoggerFactory.getLogger(CommentProcessor.class);

    public static final String PATH = "/tb-ui/rendering/comment";

    private static final String EMAIL_ADDR_REGEXP = "^.*@.*[.].{2,}$";

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

        // are we doing a preview? or a post?
        String method = request.getParameter("method");
        final boolean preview;
        if (method != null && method.equals("preview")) {
            preview = true;
            log.debug("Handling comment preview post");
        } else {
            preview = false;
            log.debug("Handling regular comment post");
        }

        WeblogEntryRequest commentRequest;

        try {
            commentRequest = new WeblogEntryRequest(request);

            // check weblog specified by comment request
            weblog = commentRequest.getWeblog();
            if (weblog == null) {
                throw new IllegalArgumentException("unable to lookup weblog: " + commentRequest.getWeblogHandle());
            }

            // lookup entry specified by comment request
            entry = weblogEntryManager.getWeblogEntryByAnchor(weblog, commentRequest.getWeblogAnchor());
            if (entry == null) {
                throw new IllegalArgumentException("unable to lookup entry: " + commentRequest.getWeblogAnchor());
            }

        } catch (Exception e) {
            // some kind of error parsing the request or looking up weblog
            log.debug("error creating page request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        nonSpamCommentApprovalRequired = WebloggerProperties.CommentPolicy.MUSTMODERATE.equals(commentOption) ||
                WebloggerProperties.CommentPolicy.MUSTMODERATE.equals(weblog.getAllowComments());

        // we know what the weblog entry is, so setup our urls
        dispatchUrl = PageProcessor.PATH + "/" + weblog.getHandle();
        dispatchUrl += "/entry/" + Utilities.encode(commentRequest.getWeblogAnchor());

        /*
         * parse request parameters
         *
         * the params we currently care about are:
         *   name - comment author
         *   email - comment email
         *   url - comment referring url
         *   content - comment contents
         *   notify - if commenter wants to receive notifications
         */
        WeblogEntryComment commentForm = new WeblogEntryComment();
        commentForm.setNotify(request.getParameter("notify") != null);
        commentForm.setName(Utilities.removeHTML(request.getParameter("name")));
        commentForm.setEmail(Utilities.removeHTML(request.getParameter("email")));
        commentForm.setWeblogEntry(entry);
        commentForm.setRemoteHost(request.getRemoteHost());
        commentForm.setPostTime(Instant.now());

        // Validate url
        commentForm.setUrl(Utilities.removeHTML(request.getParameter("url")));
        String urlCheck = commentForm.getUrl();
        if (StringUtils.isNotEmpty(urlCheck)) {
            urlCheck = urlCheck.trim().toLowerCase();
            if (!urlCheck.startsWith("http://") && !urlCheck.startsWith("https://")) {
                urlCheck = "http://" + urlCheck;
            }
        }
        commentForm.setUrl(urlCheck);

        // Validate content
        commentForm.setContent(StringUtils.left(request.getParameter("content"), 2000));
        HTMLSanitizer.Level sanitizerLevel = props.getCommentHtmlPolicy();
        Whitelist commentHTMLWhitelist = sanitizerLevel.getWhitelist();

        // Need to insert paragraphs breaks in case commenter didn't do so.
        String commentTemp = Utilities.insertLineBreaksIfMissing(commentForm.getContent());

        // JSoup pulls out disallowable tags, normally good but HTMLSanitizer.Level.LIMITED is so
        // limited good to give commenter opportunity to fix it in case too much removed.
        boolean noDisallowedTags = true;
        if (sanitizerLevel == HTMLSanitizer.Level.LIMITED) {
            noDisallowedTags = Jsoup.isValid(commentTemp, commentHTMLWhitelist);
        }

        if (noDisallowedTags) {
            commentForm.setContent(Jsoup.clean(commentTemp, commentHTMLWhitelist));
        }

        if (log.isDebugEnabled()) {
            log.debug("Doing comment posting for entry = {}", urlStrategy.getWeblogEntryURL(entry.getWeblog(),
                    entry.getAnchor(), true));
            log.debug("name = " + commentForm.getName());
            log.debug("email = " + commentForm.getEmail());
            log.debug("url = " + commentForm.getUrl());
            log.debug("content = " + commentForm.getContent());
            log.debug("notify = " + commentForm.getNotify());
        }

        String error = null;
        I18nMessages messageUtils = I18nMessages.getMessages(commentRequest.getLocaleInstance());

        // validate comment
        // this checks site-wide settings, weblog settings, and entry settings
        if (!entry.getCommentsStillAllowed() || !entry.isPublished()) {
            error = messageUtils.getString("comments.disabled");
        } else if (StringUtils.isBlank(commentForm.getContent())) {
            error = messageUtils.getString("macro.weblog.commentwarning");
            log.debug("Comment field is blank");
        } else if (StringUtils.isBlank(commentForm.getName())) {
            error = messageUtils.getString("error.commentPostNameMissing");
            log.debug("Name field is blank");
        } else if (StringUtils.isEmpty(commentForm.getEmail()) || !commentForm.getEmail().matches(EMAIL_ADDR_REGEXP)) {
            error = messageUtils.getString("error.commentPostFailedEmailAddress");
            log.debug("Email Address is invalid: {}", commentForm.getEmail());
            // if there is an URL it must be valid
        } else if (StringUtils.isNotEmpty(commentForm.getUrl()) &&
                !new UrlValidator(new String[]{"http", "https"}).isValid(commentForm.getUrl())) {
            error = messageUtils.getString("error.commentPostFailedURL");
            log.debug("URL is invalid: {}", commentForm.getUrl());
            // if this is a real comment post then authenticate request
        } else if (!preview && commentAuthenticator != null && !commentAuthenticator.authenticate(request)) {
            String[] msg = {request.getParameter("answer")};
            error = messageUtils.getString("error.commentAuthFailed", msg);
            log.debug("Comment failed authentication");
        } else if (!noDisallowedTags) {
            error = messageUtils.getString("error.commentPostDisallowedTags");
        }

        // bail now if we have already found an error
        if (error != null) {
            commentForm.setError(true);
            commentForm.setMessage(error);
            request.setAttribute("commentForm", commentForm);
            RequestDispatcher dispatcher = request.getRequestDispatcher(dispatchUrl);
            dispatcher.forward(request, response);
            return;
        }

        // those with at least the POST role for the weblog don't need to have their comments moderated.
        boolean ownComment = false;
        String maybeUser = commentRequest.getAuthenticatedUser();
        if (maybeUser != null) {
            ownComment = userManager.checkWeblogRole(maybeUser, commentRequest.getWeblogHandle(), WeblogRole.POST);
        }

        String message = null;
        Map<String, List<String>> messages = new HashMap<>();
        int validationScore = ownComment ? 100 : validateComment(commentForm, messages);
        log.debug("Comment Validation score: {}", validationScore);

        if (preview) {
            commentForm.setPreview(true);
        } else {
            if (validationScore == Utilities.PERCENT_100) {
                if (!ownComment && nonSpamCommentApprovalRequired) {
                    // Valid comments go into moderation if required
                    commentForm.setStatus(ApprovalStatus.PENDING);
                    message = messageUtils.getString("commentServlet.submittedToModerator");
                } else {
                    // else they're approved
                    commentForm.setStatus(ApprovalStatus.APPROVED);
                    message = messageUtils.getString("commentServlet.commentAccepted");
                }
            } else {
                // Invalid comments are marked as spam, but just a moderation message sent to spammer
                // Informing the spammer the reasons for its detection encourages the spammer to just modify
                // the spam message so it will pass through; also indicating that the message is subject
                // to moderation (and sure refusal) discourages future spamming attempts.
                commentForm.setStatus(ApprovalStatus.SPAM);
                message = messageUtils.getString("commentServlet.submittedToModerator");

                // log specific error messages if they exist
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
            if (validationScore >= 0 && (!ApprovalStatus.SPAM.equals(commentForm.getStatus()) || !props.isAutodeleteSpam())) {

                boolean noModerationNeeded = ownComment ||
                        (!nonSpamCommentApprovalRequired && !ApprovalStatus.SPAM.equals(commentForm.getStatus()));
                weblogEntryManager.saveComment(commentForm, noModerationNeeded);
                persistenceStrategy.flush();

                if (noModerationNeeded) {
                    mailManager.sendNewCommentNotification(commentForm);
                } else {
                    mailManager.sendPendingCommentNotice(commentForm, messages);
                }

                // only re-index/invalidate the cache if comment isn't moderated
                if (!nonSpamCommentApprovalRequired) {

                    // if published, index the entry
                    if (entry.isPublished() && indexManager.isIndexComments()) {
                        indexManager.addEntryReIndexOperation(entry);
                    }

                    // Clear all caches associated with comment
                    cacheManager.invalidate(commentForm);
                }

                // comment was successful, clear the comment form
                commentForm = new WeblogEntryComment();
                commentForm.initializeFormFields();
            }
        }

        // the work has been done, now send the user back to the entry page
        if (message != null) {
            commentForm.setMessage(message);
        }
        // for subsequent processing by PageProcessor, which will put in into PageModel
        // so the templates can work with the comments.
        request.setAttribute("commentForm", commentForm);

        // off to PageProcessor's POST handling.
        log.debug("comment processed, forwarding to {}", dispatchUrl);
        RequestDispatcher dispatcher = request.getRequestDispatcher(dispatchUrl);
        dispatcher.forward(request, response);
    }

    /**
     * Used for generating the html used for comment authentication.  This is done
     * outside of the normal rendering process so that we can cache full pages and
     * still set the comment authentication section dynamically.
     */
    @RequestMapping(value = "/authform", method = RequestMethod.GET)
    public void generateAuthForm(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html; charset=utf-8");

        // Convince proxies and browsers not to cache this.
        response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Expires", "-1");

        PrintWriter out = response.getWriter();
        out.println(commentAuthenticator == null ? "" : commentAuthenticator.getHtml(request));
    }

    protected int validateComment(WeblogEntryComment comment, Map<String, List<String>> messages) {
        int total = 0;
        int singleResponse;
        if (commentValidators.size() > 0) {
            for (CommentValidator val : commentValidators) {
                log.debug("Invoking comment validator {}", val.getName());
                singleResponse = val.validate(comment, messages);
                if (singleResponse == -1) { // blatant spam
                    return -1;
                }
                total += singleResponse;
            }
            total = total / commentValidators.size();
        } else {
            // When no validators: consider all comments valid
            total = Utilities.PERCENT_100;
        }
        return total;
    }
}
