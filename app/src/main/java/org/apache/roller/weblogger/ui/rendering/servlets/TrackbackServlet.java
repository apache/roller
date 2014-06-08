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
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.plugins.comments.CommentValidationManager;
import org.apache.roller.weblogger.ui.rendering.plugins.comments.TrackbackLinkbackCommentValidator;
import org.apache.roller.weblogger.ui.rendering.util.WeblogTrackbackRequest;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.MailUtil;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.cache.CacheManager;


/**
 * Roller's Trackback server implementation. POSTing to this Servlet will add a
 * Trackback to a Weblog Entry. For more info on Trackback, read the spec:
 * <a href="http://www.movabletype.org/documentation/trackback/specification.html">MT Trackback</a>.
 */
public class TrackbackServlet extends HttpServlet { 
    
    private static Log logger = LogFactory.getLog(TrackbackServlet.class);
    
    private CommentValidationManager commentValidationManager = null;
    

    public void init(ServletConfig config) throws ServletException {
        commentValidationManager = new CommentValidationManager();
        
        // add trackback verification validator just for trackbacks
        commentValidationManager.addCommentValidator(new TrackbackLinkbackCommentValidator());
    }
    
    
    /**
     * Handle incoming http GET requests.
     *
     * The TrackbackServlet does not support GET requests, it's a 404.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    
    
    /**
     * Service incoming POST requests.
     *
     * Here we handle incoming trackback posts.
     */
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
                } else if (trackbackRequest.getExcerpt().length() >= RollerConstants.TEXTWIDTH_255) {
                    trackbackRequest.setExcerpt(trackbackRequest.getExcerpt().substring(0,
                            RollerConstants.TEXTWIDTH_255 - 3)+"...");
                }
                
                // lookup weblog specified by comment request
                weblog = WebloggerFactory.getWeblogger().getWeblogManager()
                        .getWeblogByHandle(trackbackRequest.getWeblogHandle());
                
                if (weblog == null) {
                    throw new WebloggerException("unable to lookup weblog: "+
                            trackbackRequest.getWeblogHandle());
                }
                
                // lookup entry specified by comment request
                WeblogEntryManager weblogMgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                entry = weblogMgr.getWeblogEntryByAnchor(weblog, trackbackRequest.getWeblogAnchor());
                
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
                
                if (validationScore == RollerConstants.PERCENT_100 && weblog.getCommentModerationRequired()) {
                    // Valid comments go into moderation if required
                    comment.setStatus(WeblogEntryComment.PENDING);
                } else if (validationScore == RollerConstants.PERCENT_100) {
                    // else they're approved
                    comment.setStatus(WeblogEntryComment.APPROVED);
                } else {
                    // Invalid comments are marked as spam
                    comment.setStatus(WeblogEntryComment.SPAM);
                }
                
                // save, commit, send response
                if(!WeblogEntryComment.SPAM.equals(comment.getStatus()) ||
                        !WebloggerRuntimeConfig.getBooleanProperty("trackbacks.ignoreSpam.enabled")) {
                    
                    WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                    mgr.saveComment(comment);
                    WebloggerFactory.getWeblogger().flush();
                    
                    // only invalidate the cache if comment isn't moderated
                    if(!weblog.getCommentModerationRequired()) {
                        // Clear all caches associated with comment
                        CacheManager.invalidate(comment);
                    }
                    
                    // Send email notifications
                    MailUtil.sendEmailNotification(comment, messages, 
                            I18nMessages.getMessages(trackbackRequest.getLocaleInstance()),
                            validationScore == RollerConstants.PERCENT_100);
                    
                    if(WeblogEntryComment.PENDING.equals(comment.getStatus())) {
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
        
        StringBuilder output = new StringBuilder();
        
        output.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
        output.append("<response>");
        output.append("<error>0</error>");
        output.append("<message>");
        output.append(message);
        output.append("</message>");
        output.append("</response>");
            
        return output.toString();
    }
    
    
    private String getErrorResponse(String message) {
        
        StringBuilder output = new StringBuilder();
        
        output.append("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
        output.append("<response>");
        output.append("<error>1</error>");
        output.append("<message>ERROR: ");
        output.append(message);
        output.append("</message>");
        output.append("</response>");
            
        return output.toString();
    }
    
}
