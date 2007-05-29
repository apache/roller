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

package org.apache.roller.ui.rendering.servlets;

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
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.WeblogEntryComment;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.Weblog;
import org.apache.roller.ui.rendering.util.CommentValidationManager;
import org.apache.roller.ui.rendering.util.TrackbackLinkbackCommentValidator;
import org.apache.roller.ui.rendering.util.WeblogTrackbackRequest;
import org.apache.roller.util.I18nMessages;
import org.apache.roller.util.RollerMessages;
import org.apache.roller.util.cache.CacheManager;


/**
 * Roller's Trackback server implementation. POSTing to this Servlet will add a
 * Trackback to a Weblog Entry. For more info on Trackback, read the spec:
 * <a href="http://www.movabletype.org/docs/mttrackback.html">MT Trackback</a>.
 *
 * @web.servlet name="TrackbackServlet"
 * @web.servlet-mapping url-pattern="/roller-ui/rendering/trackback/*"
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
        WeblogEntryData entry = null;
        
        RollerMessages messages = new RollerMessages();
        
        WeblogTrackbackRequest trackbackRequest = null;
        if (!RollerRuntimeConfig.getBooleanProperty("users.trackbacks.enabled")) {
            // TODO: i18n
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
                } else if (trackbackRequest.getExcerpt().length() >= 255) {
                    trackbackRequest.setExcerpt(trackbackRequest.getExcerpt().substring(0, 252)+"...");
                }
                
                // lookup weblog specified by comment request
                UserManager uMgr = RollerFactory.getRoller().getUserManager();
                weblog = uMgr.getWebsiteByHandle(trackbackRequest.getWeblogHandle());
                
                if (weblog == null) {
                    throw new RollerException("unable to lookup weblog: "+
                            trackbackRequest.getWeblogHandle());
                }
                
                // lookup entry specified by comment request
                WeblogManager weblogMgr = RollerFactory.getRoller().getWeblogManager();
                entry = weblogMgr.getWeblogEntryByAnchor(weblog, trackbackRequest.getWeblogAnchor());
                
                if (entry == null) {
                    throw new RollerException("unable to lookup entry: "+
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
                
                if (validationScore == 100 && weblog.getCommentModerationRequired()) {
                    // Valid comments go into moderation if required
                    comment.setStatus(WeblogEntryComment.PENDING);
                } else if (validationScore == 100) {
                    // else they're approved
                    comment.setStatus(WeblogEntryComment.APPROVED);
                } else {
                    // Invalid comments are marked as spam
                    comment.setStatus(WeblogEntryComment.SPAM);
                }
                
                // save, commit, send response
                if(!WeblogEntryComment.SPAM.equals(comment.getStatus()) ||
                        !RollerRuntimeConfig.getBooleanProperty("trackbacks.ignoreSpam.enabled")) {
                    
                    WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
                    mgr.saveComment(comment);
                    RollerFactory.getRoller().flush();
                    
                    // only invalidate the cache if comment isn't moderated
                    if(!weblog.getCommentModerationRequired()) {
                        // Clear all caches associated with comment
                        CacheManager.invalidate(comment);
                    }
                    
                    // Send email notifications
                    String rootURL = RollerRuntimeConfig.getAbsoluteContextURL();
                    CommentServlet.sendEmailNotification(comment, validationScore == 100, messages, rootURL, 
                            I18nMessages.getMessages(trackbackRequest.getLocaleInstance()));
                    
                    if(WeblogEntryComment.PENDING.equals(comment.getStatus())) {
                        pw.println(this.getSuccessResponse("Trackback submitted to moderator"));
                    } else {
                        pw.println(this.getSuccessResponse("Trackback accepted"));
                    }
                }
                
            } else if (entry!=null) {
                // TODO: i18n
                error = "Comments and Trackbacks are disabled for the entry you specified.";
            } else {
                // TODO: i18n
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
        
        StringBuffer output = new StringBuffer();
        
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
        
        StringBuffer output = new StringBuffer();
        
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