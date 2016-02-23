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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.plugins.comments.CommentAuthenticator;
import org.apache.roller.weblogger.ui.rendering.plugins.comments.CommentValidationManager;
import org.apache.roller.weblogger.ui.rendering.plugins.comments.CommentValidator;
import org.apache.roller.weblogger.ui.rendering.util.WeblogCommentRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogEntryCommentForm;
import org.apache.roller.weblogger.util.GenericThrottle;
import org.apache.roller.weblogger.util.IPBanList;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.RollerMessages.RollerMessage;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles all incoming weblog entry comment posts.
 *
 * We validate each incoming comment based on various comment settings and if
 * all checks are passed then the comment is saved.
 *
 * Incoming comments are tested against any site and/or weblog-defined blacklist values. If they
 * are found to be spam, then they are marked as spam and hidden from view.
 *
 * If email notification is turned on, each new comment will result in an email
 * sent to the blog owner and all who have commented on the same post.
 */
@RestController
@RequestMapping(path="/tb-ui/rendering/comment/**")
public class CommentProcessor {

    private static Log log = LogFactory.getLog(CommentProcessor.class);

    public static final String PATH = "/tb-ui/rendering/comment";

    private CommentValidationManager commentValidationManager = null;

    @Autowired(required=false)
    private GenericThrottle commentThrottle = null;

    // See GenericThrottle class for activation information
    public void setCommentThrottle(@Qualifier("commentThrottle") GenericThrottle commentThrottle) {
        this.commentThrottle = commentThrottle;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Autowired
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
    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    @Resource(name="commentValidatorList")
    private List<CommentValidator> commentValidators;

    public void setCommentValidators(List<CommentValidator> commentValidators) {
        this.commentValidators = commentValidators;
    }

    /**
     * Initialization.
     */
    @PostConstruct
    public void init() {
        // instantiate a comment validation manager for comment spam checking
        commentValidationManager = new CommentValidationManager(commentValidators);
    }

    /**
     * Service incoming POST requests.
     *
     * Here we handle incoming comment postings.
     */
    @RequestMapping(method = RequestMethod.POST)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String error = null;
        String dispatch_url;

        Weblog weblog;
        WeblogEntry entry;

        String message = null;
        RollerMessages messages = new RollerMessages();

        // are we doing a preview? or a post?
        String method = request.getParameter("method");
        final boolean preview;
        if (method != null && method.equals("preview")) {
            preview = true;
            messages.addMessage("commentServlet.previewCommentOnly");
            log.debug("Handling comment preview post");
        } else {
            preview = false;
            log.debug("Handling regular comment post");
        }

        // throttling protection against spammers
        if (commentThrottle != null && commentThrottle.processHit(request.getRemoteAddr())) {

            log.debug("ABUSIVE " + request.getRemoteAddr());
            IPBanList.getInstance().addBannedIp(request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        WeblogCommentRequest commentRequest;
        try {
            commentRequest = new WeblogCommentRequest(request);

            // lookup weblog specified by comment request
            weblog = weblogManager.getWeblogByHandle(commentRequest.getWeblogHandle());

            if (weblog == null) {
                throw new WebloggerException("unable to lookup weblog: " + commentRequest.getWeblogHandle());
            }

            // lookup entry specified by comment request
            entry = commentRequest.getWeblogEntry();
            if (entry == null) {
                throw new WebloggerException("unable to lookup entry: " + commentRequest.getWeblogAnchor());
            }

            // we know what the weblog entry is, so setup our urls
            dispatch_url = PageProcessor.PATH + "/" + weblog.getHandle();
            dispatch_url += "/entry/" + Utilities.encode(commentRequest.getWeblogAnchor());

        } catch (Exception e) {
            // some kind of error parsing the request or looking up weblog
            log.debug("error creating page request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        log.debug("Doing comment posting for entry = " + entry.getPermalink());

        // collect input from request params and construct new comment object
        // fields: name, email, url, content, notify
        // TODO: data validation on collected comment data
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setName(commentRequest.getName());
        comment.setEmail(commentRequest.getEmail());

        // Validate url
        if (StringUtils.isNotEmpty(commentRequest.getUrl())) {
            String theUrl = commentRequest.getUrl().trim().toLowerCase();
            StringBuilder url = new StringBuilder();
            if (theUrl.startsWith("http://")) {
                url.append(theUrl);
            } else if (theUrl.startsWith("https://")) {
                url.append(theUrl);
            } else {
                url.append("http://").append(theUrl);
            }
            comment.setUrl(url.toString());
        } else {
            comment.setUrl("");
        }

        comment.setContent(commentRequest.getContent());
        comment.setNotify(commentRequest.isNotify());
        comment.setWeblogEntry(entry);
        comment.setRemoteHost(request.getRemoteHost());
        comment.setPostTime(new Timestamp(System.currentTimeMillis()));

        // set comment content-type depending on if html is allowed
        if (WebloggerRuntimeConfig
                .getBooleanProperty("users.comments.htmlenabled")) {
            comment.setContentType("text/html");
        } else {
            comment.setContentType("text/plain");
        }

        // add all enabled content plugins
        comment.setPlugins(WebloggerRuntimeConfig
                .getProperty("enabled.comment.plugins"));

        WeblogEntryCommentForm cf = new WeblogEntryCommentForm();
        cf.setData(comment);
        if (preview) {
            cf.setPreview(comment);
        }

        I18nMessages messageUtils = I18nMessages.getMessages(commentRequest
                .getLocaleInstance());

        // check if comments are allowed for this entry
        // this checks site-wide settings, weblog settings, and entry settings
        if (!entry.getCommentsStillAllowed() || !entry.isPublished()) {
            error = messageUtils.getString("comments.disabled");

            // Must have an email and also must be valid
        } else if (StringUtils.isEmpty(commentRequest.getEmail())
                || StringUtils.isNotEmpty(commentRequest.getEmail())
                && !Utilities.isValidEmailAddress(commentRequest.getEmail())) {
            error = messageUtils
                    .getString("error.commentPostFailedEmailAddress");
            log.debug("Email Address is invalid : "
                    + commentRequest.getEmail());
            // if there is an URL it must be valid
        } else if (StringUtils.isNotEmpty(comment.getUrl())
                && !new UrlValidator(new String[] { "http", "https" })
                .isValid(comment.getUrl())) {
            error = messageUtils.getString("error.commentPostFailedURL");
            log.debug("URL is invalid : " + comment.getUrl());
            // if this is a real comment post then authenticate request
        } else if (!preview && !this.commentAuthenticator.authenticate(request)) {
            String[] msg = { request.getParameter("answer") };
            error = messageUtils.getString("error.commentAuthFailed", msg);
            log.debug("Comment failed authentication");
        }

        // bail now if we have already found an error
        if (error != null) {
            cf.setError(error);
            request.setAttribute("commentForm", cf);
            RequestDispatcher dispatcher = request.getRequestDispatcher(dispatch_url);
            dispatcher.forward(request, response);
            return;
        }

        int validationScore = commentValidationManager.validateComment(comment, messages);
        log.debug("Comment Validation score: " + validationScore);

        if (!preview) {
            if (validationScore == WebloggerCommon.PERCENT_100
                    && weblog.getCommentModerationRequired()) {
                // Valid comments go into moderation if required
                comment.setStatus(ApprovalStatus.PENDING);
                message = messageUtils.getString("commentServlet.submittedToModerator");
            } else if (validationScore == WebloggerCommon.PERCENT_100) {
                // else they're approved
                comment.setStatus(ApprovalStatus.APPROVED);
                message = messageUtils.getString("commentServlet.commentAccepted");
            } else {
                // Invalid comments are marked as spam
                log.debug("Comment marked as spam");
                comment.setStatus(ApprovalStatus.SPAM);
                error = messageUtils.getString("commentServlet.commentMarkedAsSpam");

                // add specific error messages if they exist
                if (messages.getErrorCount() > 0) {
                    Iterator errors = messages.getErrors();
                    RollerMessage errorKey;

                    StringBuilder buf = new StringBuilder();
                    buf.append("<ul>");
                    while (errors.hasNext()) {
                        errorKey = (RollerMessage) errors.next();

                        buf.append("<li>");
                        if (errorKey.getArgs() != null) {
                            buf.append(messageUtils.getString(errorKey.getKey(), errorKey.getArgs()));
                        } else {
                            buf.append(messageUtils.getString(errorKey.getKey()));
                        }
                        buf.append("</li>");
                    }
                    buf.append("</ul>");

                    error += buf.toString();
                }

            }

            try {
                if (!ApprovalStatus.SPAM.equals(comment.getStatus()) || !WebloggerRuntimeConfig
                        .getBooleanProperty("comments.ignoreSpam.enabled")) {

                    weblogEntryManager.saveComment(comment);
                    WebloggerFactory.flush();

                    // Send email notifications only to subscribers if comment
                    // is 100% valid
                    boolean notifySubscribers = (validationScore == WebloggerCommon.PERCENT_100);
                    mailManager.sendEmailNotification(comment, messages, messageUtils, notifySubscribers);

                    // only re-index/invalidate the cache if comment isn't
                    // moderated
                    if (!weblog.getCommentModerationRequired()) {

                        // remove entry before (re)adding it, or in case it
                        // isn't Published
                        indexManager.removeEntryIndexOperation(entry);

                        // if published, index the entry
                        if (entry.isPublished()) {
                            indexManager.addEntryIndexOperation(entry);
                        }

                        // Clear all caches associated with comment
                        cacheManager.invalidate(comment);
                    }

                    // comment was successful, clear the comment form
                    cf = new WeblogEntryCommentForm();
                }

            } catch (WebloggerException re) {
                log.error("Error saving comment", re);
                error = re.getMessage();
            }
        }

        // the work has been done, now send the user back to the entry page
        if (error != null) {
            cf.setError(error);
        }
        if (message != null) {
            cf.setMessage(message);
        }
        request.setAttribute("commentForm", cf);

        log.debug("comment processed, forwarding to " + dispatch_url);
        RequestDispatcher dispatcher = request.getRequestDispatcher(dispatch_url);
        dispatcher.forward(request, response);
    }

}
