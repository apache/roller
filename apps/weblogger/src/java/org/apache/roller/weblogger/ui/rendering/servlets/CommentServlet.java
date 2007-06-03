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
 */

package org.apache.roller.weblogger.ui.rendering.servlets;  

import java.io.IOException;
import java.sql.Timestamp;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.config.RollerRuntimeConfig;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.util.CommentAuthenticator;
import org.apache.roller.weblogger.ui.rendering.util.CommentValidationManager;
import org.apache.roller.weblogger.ui.rendering.util.DefaultCommentAuthenticator;
import org.apache.roller.weblogger.ui.rendering.util.WeblogCommentRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogEntryCommentForm;
import org.apache.roller.weblogger.util.GenericThrottle;
import org.apache.roller.weblogger.util.IPBanList;
import org.apache.roller.weblogger.util.MailUtil;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.URLUtilities;
import org.apache.roller.weblogger.util.cache.CacheManager;


/**
 * The CommentServlet handles all incoming weblog entry comment posts.
 *
 * We validate each incoming comment based on various comment settings and
 * if all checks are passed then the comment is saved.
 *
 * Incoming comments are tested against the MT Blacklist. If they are found
 * to be spam, then they are marked as spam and hidden from view.
 *
 * If email notification is turned on, each new comment will result in an
 * email sent to the blog owner and all who have commented on the same post.
 */
public class CommentServlet extends HttpServlet {
    
    private static Log log = LogFactory.getLog(CommentServlet.class);
    
    private CommentAuthenticator     authenticator = null;
    private CommentValidationManager commentValidationManager = null;
    private GenericThrottle          commentThrottle = null;
    
    
    /**
     * Initialization.
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        
        super.init(servletConfig);
        
        log.info("Initializing CommentServlet");
        
        commentValidationManager = new CommentValidationManager();
        
        // lookup the authenticator we are going to use and instantiate it
        try {
            String name = RollerConfig.getProperty("comment.authenticator.classname");
            
            Class clazz = Class.forName(name);
            this.authenticator = (CommentAuthenticator) clazz.newInstance();
            
        } catch(Exception e) {
            log.error(e);
            this.authenticator = new DefaultCommentAuthenticator();
        }
        
        
        // are we doing throttling?
        if(RollerConfig.getBooleanProperty("comment.throttle.enabled")) {
            
            int threshold = 25;
            try {
                threshold = Integer.parseInt(RollerConfig.getProperty("comment.throttle.threshold"));
            } catch(Exception e) {
                log.warn("bad input for config property comment.throttle.threshold", e);
            }
            
            int interval = 60000;
            try {
                interval = Integer.parseInt(RollerConfig.getProperty("comment.throttle.interval"));
                // convert from seconds to milliseconds
                interval = interval * 1000;
            } catch(Exception e) {
                log.warn("bad input for config property comment.throttle.interval", e);
            }
            
            int maxEntries = 250;
            try {
                maxEntries = Integer.parseInt(RollerConfig.getProperty("comment.throttle.maxentries"));
            } catch(Exception e) {
                log.warn("bad input for config property comment.throttle.maxentries", e);
            }
            
            commentThrottle = new GenericThrottle(threshold, interval, maxEntries);
            
            log.info("Comment Throttling ENABLED");
        } else {
            log.info("Comment Throttling DISABLED");
        }
    }
    
    
    /**
     * Handle incoming http GET requests.
     *
     * The CommentServlet does not support GET requests, it's a 404.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    
    
    /**
     * Service incoming POST requests.
     *
     * Here we handle incoming comment postings.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        String error = null;
        String dispatch_url = null;
        
        Weblog weblog = null;
        WeblogEntry entry = null;
        
        String message = null;
        RollerMessages messages = new RollerMessages();
        
        // are we doing a preview?  or a post?
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
        if(commentThrottle != null &&
                commentThrottle.processHit(request.getRemoteAddr())) {
            
            log.debug("ABUSIVE "+request.getRemoteAddr());
            IPBanList.getInstance().addBannedIp(request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        WeblogCommentRequest commentRequest = null;
        try {
            commentRequest = new WeblogCommentRequest(request);
            
            // lookup weblog specified by comment request
            UserManager uMgr = RollerFactory.getRoller().getUserManager();
            weblog = uMgr.getWebsiteByHandle(commentRequest.getWeblogHandle());
            
            if(weblog == null) {
                throw new WebloggerException("unable to lookup weblog: "+
                        commentRequest.getWeblogHandle());
            }
            
            // lookup entry specified by comment request
            entry = commentRequest.getWeblogEntry();
            if(entry == null) {
                throw new WebloggerException("unable to lookup entry: "+
                        commentRequest.getWeblogAnchor());
            }
            
            // we know what the weblog entry is, so setup our urls
            dispatch_url = "/roller-ui/rendering/page/"+weblog.getHandle();
            if(commentRequest.getLocale() != null) {
                dispatch_url += "/"+commentRequest.getLocale();
            }
            dispatch_url += "/entry/"+URLUtilities.encode(commentRequest.getWeblogAnchor());
            
        } catch (Exception e) {
            // some kind of error parsing the request or looking up weblog
            log.debug("error creating page request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        log.debug("Doing comment posting for entry = "+entry.getPermalink());
        
        // collect input from request params and construct new comment object
        // fields: name, email, url, content, notify
        // TODO: data validation on collected comment data
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setName(commentRequest.getName());
        comment.setEmail(commentRequest.getEmail());
        comment.setUrl(commentRequest.getUrl());
        comment.setContent(commentRequest.getContent());
        comment.setNotify(new Boolean(commentRequest.isNotify()));
        comment.setWeblogEntry(entry);
        comment.setRemoteHost(request.getRemoteHost());
        comment.setPostTime(new Timestamp(System.currentTimeMillis()));
        
        WeblogEntryCommentForm cf = new WeblogEntryCommentForm();
        cf.setData(comment);
        if (preview) {
            cf.setPreview(comment);
        }
        
        I18nMessages messageUtils = I18nMessages.getMessages(commentRequest.getLocaleInstance());
        
        // check if comments are allowed for this entry
        // this checks site-wide settings, weblog settings, and entry settings
        if(!entry.getCommentsStillAllowed() || !entry.isPublished()) {
            error = messageUtils.getString("comments.disabled");
            
            // if this is a real comment post then authenticate request
        } else if(!preview && !this.authenticator.authenticate(request)) {
            error = messageUtils.getString("error.commentAuthFailed");
            log.debug("Comment failed authentication");
        }
        
        // bail now if we have already found an error
        if(error != null) {
            cf.setError(error);
            request.setAttribute("commentForm", cf);
            RequestDispatcher dispatcher = request.getRequestDispatcher(dispatch_url);
            dispatcher.forward(request, response);
            return;
        }
        
        int validationScore = commentValidationManager.validateComment(comment, messages);
        log.debug("Comment Validation score: " + validationScore);
        
        if (!preview) {
            
            if (validationScore == 100 && weblog.getCommentModerationRequired()) {
                // Valid comments go into moderation if required
                comment.setStatus(WeblogEntryComment.PENDING);
                message = messageUtils.getString("commentServlet.submittedToModerator");
            } else if (validationScore == 100) {
                // else they're approved
                comment.setStatus(WeblogEntryComment.APPROVED);
            } else {
                // Invalid comments are marked as spam
                comment.setStatus(WeblogEntryComment.SPAM);
                error = messageUtils.getString("commentServlet.commentMarkedAsSpam");
                log.debug("Comment marked as spam");
            }
            
            try {               
                if(!WeblogEntryComment.SPAM.equals(comment.getStatus()) ||
                        !RollerRuntimeConfig.getBooleanProperty("comments.ignoreSpam.enabled")) {
                    
                    WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
                    mgr.saveComment(comment);
                    RollerFactory.getRoller().flush();
                    
                    // Send email notifications only to subscribers if comment is 100% valid
                    boolean notifySubscribers = (validationScore == 100);
                    MailUtil.sendEmailNotification(comment, messages, messageUtils, notifySubscribers);
                    
                    // only re-index/invalidate the cache if comment isn't moderated
                    if(!weblog.getCommentModerationRequired()) {
                        IndexManager manager = RollerFactory.getRoller().getIndexManager();
                        
                        // remove entry before (re)adding it, or in case it isn't Published
                        manager.removeEntryIndexOperation(entry);
                        
                        // if published, index the entry
                        if (entry.isPublished()) {
                            manager.addEntryIndexOperation(entry);
                        }
                        
                        // Clear all caches associated with comment
                        CacheManager.invalidate(comment);
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
        
        log.debug("comment processed, forwarding to "+dispatch_url);
        RequestDispatcher dispatcher =
                request.getRequestDispatcher(dispatch_url);
        dispatcher.forward(request, response);
    }
    
}
