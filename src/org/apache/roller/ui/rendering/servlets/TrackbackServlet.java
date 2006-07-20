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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.rendering.util.WeblogTrackbackRequest;
import org.apache.roller.util.LinkbackExtractor;
import org.apache.roller.util.SpamChecker;
import org.apache.roller.util.URLUtilities;
import org.apache.roller.util.cache.CacheManager;
import org.apache.struts.util.RequestUtils;


/**
 * Roller's Trackback server implementation. POSTing to this Servlet will add a
 * Trackback to a Weblog Entry. For more info on Trackback, read the spec:
 * <a href="http://www.movabletype.org/docs/mttrackback.html>MT Trackback</a>.
 *
 * @web.servlet name="TrackbackServlet"
 * @web.servlet-mapping url-pattern="/roller-ui/rendering/trackback/*"
 */
public class TrackbackServlet extends HttpServlet { 
    
    private static Log logger = LogFactory.getLog(TrackbackServlet.class);
    
    
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
        
        WebsiteData weblog = null;
        WeblogEntryData entry = null;
        
        WeblogTrackbackRequest trackbackRequest = null;
        if(!RollerRuntimeConfig.getBooleanProperty("users.trackbacks.enabled")) {
            error = "Trackbacks are disabled for this site";
        } else {
            
            try {
                trackbackRequest = new WeblogTrackbackRequest(request);
                
                if((trackbackRequest.getTitle() == null) ||
                        "".equals(trackbackRequest.getTitle())) {
                    trackbackRequest.setTitle(trackbackRequest.getUrl());
                }
                
                if(trackbackRequest.getExcerpt() == null) {
                    trackbackRequest.setExcerpt("");
                } else if(trackbackRequest.getExcerpt().length() >= 255) {
                    trackbackRequest.setExcerpt(trackbackRequest.getExcerpt().substring(0, 252)+"...");
                }
                
                // lookup weblog specified by comment request
                UserManager uMgr = RollerFactory.getRoller().getUserManager();
                weblog = uMgr.getWebsiteByHandle(trackbackRequest.getWeblogHandle());
                
                if(weblog == null) {
                    throw new RollerException("unable to lookup weblog: "+
                            trackbackRequest.getWeblogHandle());
                }
                
                // lookup entry specified by comment request
                WeblogManager weblogMgr = RollerFactory.getRoller().getWeblogManager();
                entry = weblogMgr.getWeblogEntryByAnchor(weblog, trackbackRequest.getWeblogAnchor());
                
                if(entry == null) {
                    throw new RollerException("unable to lookup entry: "+
                            trackbackRequest.getWeblogAnchor());
                }
                
            } catch (Exception e) {
                // some kind of error parsing the request or looking up weblog
                logger.debug("error creating page request", e);
                error = e.getMessage();
            }
        }
        
        if(error != null) {
            pw.println(this.getErrorResponse(error));
            return;
        }
        
        try {
            boolean siteAllows = weblog.getAllowComments().booleanValue();
            boolean verified = true;
            
            if (entry!=null && siteAllows && entry.getCommentsStillAllowed()) {
                
                // Track trackbacks as comments
                CommentData comment = new CommentData();
                comment.setContent("[Trackback] "+trackbackRequest.getExcerpt());
                comment.setName(trackbackRequest.getBlogName());
                comment.setUrl(trackbackRequest.getUrl());
                comment.setWeblogEntry(entry);
                comment.setNotify(Boolean.FALSE);
                comment.setPostTime(new Timestamp(new Date().getTime()));
                
                // If comment contains blacklisted text, mark as spam
                SpamChecker checker = new SpamChecker();
                if (checker.checkTrackback(comment)) {
                    comment.setSpam(Boolean.TRUE);
                    logger.debug("Trackback blacklisted: "+comment.getUrl());
                    error = "REJECTED: trackback contains spam words";
                }
                // Else, if trackback verification is on...
                else if (RollerRuntimeConfig.getBooleanProperty(
                        "site.trackbackVerification.enabled")) {
                    
                    // ...ensure trackbacker actually links to us
                    LinkbackExtractor linkback = new LinkbackExtractor(
                            comment.getUrl(), URLUtilities.getWeblogEntryURL(weblog, null, entry.getAnchor(), true));
                    if (linkback.getExcerpt() == null) {
                        comment.setPending(Boolean.TRUE);
                        comment.setApproved(Boolean.FALSE);
                        verified = false;
                        // if we can't verify trackback, then reject it
                        error = "REJECTED: trackback failed verification";
                        logger.debug("Trackback failed verification: "+comment.getUrl());
                    }
                }
                
                if (error == null) {
                    // If comment moderation is on, set comment as pending
                    if (verified && weblog.getCommentModerationRequired()) {
                        comment.setPending(Boolean.TRUE);
                        comment.setApproved(Boolean.FALSE);
                    } else if (verified) {
                        comment.setPending(Boolean.FALSE);
                        comment.setApproved(Boolean.TRUE);
                    }
                    
                    // save, commit, send response
                    WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
                    mgr.saveComment(comment);
                    RollerFactory.getRoller().flush();
                    
                    // Clear all caches associated with comment
                    CacheManager.invalidate(comment);
                    
                    // Send email notifications
                    String rootURL = RollerRuntimeConfig.getAbsoluteContextURL();
                    if (rootURL == null || rootURL.trim().length()==0) {
                        rootURL = RequestUtils.serverURL(request) + request.getContextPath();
                    }
                    CommentServlet.sendEmailNotification(comment, rootURL);
                    
                    if(comment.getPending().booleanValue()) {
                        pw.println(this.getSuccessResponse("Trackback submitted to moderator"));
                    } else {
                        pw.println(this.getSuccessResponse("Trackback accepted"));
                    }
                }
                
            } else if (entry!=null) {
                error = "Comments and Trackbacks are disabled for the entry you specified.";
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