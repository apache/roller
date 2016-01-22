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
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.plugins.comments.CommentValidationManager;
import org.apache.roller.weblogger.ui.rendering.plugins.comments.CommentValidator;
import org.apache.roller.weblogger.ui.rendering.util.WeblogTrackbackRequest;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Receives incoming trackbacks to blogs hosted by the weblogger. POSTs will add a
 * For more info on Trackback see
 * <a href="http://www.movabletype.org/documentation/trackback/specification.html">MT Trackback</a>.
 */
@RestController
@RequestMapping(path="/roller-ui/rendering/trackback/**")
public class IncomingTrackbackProcessor {

    private static Log logger = LogFactory.getLog(IncomingTrackbackProcessor.class);

    public static final String PATH = "/roller-ui/rendering/trackback";

    private CommentValidationManager commentValidationManager = null;

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
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Autowired
    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    @Resource(name="trackbackValidatorList")
    private List<CommentValidator> trackbackValidators;

    public void setTrackbackValidators(List<CommentValidator> trackbackValidators) {
        this.trackbackValidators = trackbackValidators;
    }

    @PostConstruct
    public void init() throws ServletException {
        logger.info("Initializing IncomingTrackbackProcessor");
        commentValidationManager = new CommentValidationManager(trackbackValidators);
    }

    /**
     * Service incoming POST requests.
     *
     * Here we handle incoming trackback posts.
     */
    @RequestMapping(method = RequestMethod.POST)
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String error = null;
        PrintWriter pw = response.getWriter();

        Weblog weblog = null;
        WeblogEntry entry = null;

        RollerMessages messages = new RollerMessages();

        WeblogTrackbackRequest trackbackRequest = null;
        if (!WebloggerRuntimeConfig.getBooleanProperty("users.trackbacks.enabled")) {
            error = "Trackbacks are disabled for this site";
        } else {

            try {
                trackbackRequest = new WeblogTrackbackRequest(request);

                if ((trackbackRequest.getTitle() == null) ||
                        "".equals(trackbackRequest.getTitle())) {
                    trackbackRequest.setTitle(trackbackRequest.getUrl());
                }

                if (trackbackRequest.getExcerpt() == null) {
                    trackbackRequest.setExcerpt("");
                } else if (trackbackRequest.getExcerpt().length() >= WebloggerCommon.TEXTWIDTH_255) {
                    trackbackRequest.setExcerpt(trackbackRequest.getExcerpt().substring(0,
                            WebloggerCommon.TEXTWIDTH_255 - 3)+"...");
                }

                // lookup weblog specified by comment request
                weblog = weblogManager.getWeblogByHandle(trackbackRequest.getWeblogHandle());

                if (weblog == null) {
                    throw new WebloggerException("unable to lookup weblog: "+
                            trackbackRequest.getWeblogHandle());
                }

                // lookup entry specified by comment request
                entry = weblogEntryManager.getWeblogEntryByAnchor(weblog, trackbackRequest.getWeblogAnchor());

                if (entry == null) {
                    throw new WebloggerException("unable to lookup entry: "+
                            trackbackRequest.getWeblogAnchor());
                }

            } catch (Exception e) {
                // some kind of error parsing the request or looking up weblog
                logger.debug("error creating trackback request", e);
                error = e.getMessage();
            }
        }

        if (error != null) {
            pw.println(this.getErrorResponse(error));
            return;
        }

        try {
            // check if trackbacks are allowed for this entry
            // this checks site-wide settings, weblog settings, and entry settings
            if (entry != null && entry.getCommentsStillAllowed() && entry.isPublished()) {

                // Track trackbacks as comments
                WeblogEntryComment comment = new WeblogEntryComment();
                comment.setContent("[Trackback] "+trackbackRequest.getExcerpt());
                comment.setName(trackbackRequest.getBlogName());
                comment.setUrl(trackbackRequest.getUrl());
                comment.setWeblogEntry(entry);
                comment.setRemoteHost(request.getRemoteHost());
                comment.setNotify(Boolean.FALSE);
                comment.setPostTime(new Timestamp(new Date().getTime()));

                // run new trackback through validators
                int validationScore = commentValidationManager.validateComment(comment, messages);
                logger.debug("Comment Validation score: " + validationScore);

                if (validationScore == WebloggerCommon.PERCENT_100 && weblog.getCommentModerationRequired()) {
                    // Valid comments go into moderation if required
                    comment.setStatus(ApprovalStatus.PENDING);
                } else if (validationScore == WebloggerCommon.PERCENT_100) {
                    // else they're approved
                    comment.setStatus(ApprovalStatus.APPROVED);
                } else {
                    // Invalid comments are marked as spam
                    comment.setStatus(ApprovalStatus.SPAM);
                }

                // save, commit, send response
                if (!ApprovalStatus.SPAM.equals(comment.getStatus()) ||
                        !WebloggerRuntimeConfig.getBooleanProperty("trackbacks.ignoreSpam.enabled")) {

                    weblogEntryManager.saveComment(comment);
                    WebloggerFactory.flush();

                    // only invalidate the cache if comment isn't moderated
                    if(!weblog.getCommentModerationRequired()) {
                        // Clear all caches associated with comment
                        cacheManager.invalidate(comment);
                    }

                    // Send email notifications
                    mailManager.sendEmailNotification(comment, messages,
                            I18nMessages.getMessages(trackbackRequest.getLocaleInstance()),
                            validationScore == WebloggerCommon.PERCENT_100);

                    if (ApprovalStatus.PENDING.equals(comment.getStatus())) {
                        pw.println(this.getSuccessResponse("Trackback submitted to moderator"));
                    } else {
                        pw.println(this.getSuccessResponse("Trackback accepted"));
                    }
                }

            } else if (entry!=null) {
                error = "Comments and Trackbacks are disabled for the entry specified.";
            } else {
                error = "Entry not specified.";
            }

        } catch (Exception e) {
            error = e.getMessage();
            if ( error == null ) {
                error = e.getClass().getName();
            }
        }

        if(error!= null) {
            pw.println(this.getErrorResponse(error));
        }

    }

    private String getSuccessResponse(String message) {
        String output = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>";
        output += "<response><error>0</error><message>\"";
        output += message;
        return output;
    }

    private String getErrorResponse(String message) {
        String output = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>";
        output += "<response><error>1</error><message>\"";
        output += "ERROR: " + message;
        output += "</message></response>";
        return output;
    }
}
