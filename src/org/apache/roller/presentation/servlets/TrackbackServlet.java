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
/*
 * Created on Apr 13, 2003
 */
package org.apache.roller.presentation.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.util.SpamChecker;

import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.presentation.RollerContext;
import org.apache.roller.presentation.RollerRequest;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.util.LinkbackExtractor;


/**
 * Roller's Trackback server implementation. POSTing to this Servlet will add a
 * Trackback to a Weblog Entrty. For more info on Trackback, read the spec:
 * <a href="http://www.movabletype.org/docs/mttrackback.html>MT Trackback</a>.
 *
 * @web.servlet name="TrackbackServlet"
 * @web.servlet-mapping url-pattern="/trackback/*"
 *
 * @author David M Johnson
 */
public class TrackbackServlet extends HttpServlet { 
    
    private static Log logger = 
        LogFactory.getFactory().getInstance(TrackbackServlet.class);
        
    /** Request parameter to indicate a trackback "tb" */
    //private static final String TRACKBACK_PARAM = "tb";
    
    /** Request parameter for the trackback "title" */
    private static final String TRACKBACK_TITLE_PARAM = "title";
    
    /** Request parameter for the trackback "excerpt" */
    private static final String TRACKBACK_EXCERPT_PARAM = "excerpt";
    
    /** Request parameter for the trackback "url" */
    private static final String TRACKBACK_URL_PARAM = "url";
    
    /** Request parameter for the trackback "blog_name" */
    private static final String TRACKBACK_BLOG_NAME_PARAM = "blog_name";
    
    /** Key under which the trackback return code will be placed
     * (example: on the request for the JSPDispatcher) */
    public static final String TRACKBACK_RETURN_CODE =
            "BLOJSOM_TRACKBACK_RETURN_CODE";
    
    /** Key under which the trackback error message will be placed
     * (example: on the request for the JSPDispatcher) */
    public static final String TRACKBACK_MESSAGE =
            "BLOJSOM_TRACKBACK_MESSAGE";
    
    /** Trackback success page */
    //private static final String TRACKBACK_SUCCESS_PAGE = "trackback-success";
    
    /** Trackback failure page */
    //private static final String TRACKBACK_FAILURE_PAGE = "trackback-failure";
    
    /**
     * Constructor.
     */
    public TrackbackServlet() {
        super();
    }
    
    /**
     * POSTing to this Servlet will add a Trackback to a Weblog Entrty.
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        doPost(req,res);
    }
    
    /**
     * POSTing to this Servlet will add a Trackback to a Weblog Entrty.
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        
        try {
            // insure that incoming data is parsed as UTF-8
            req.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ServletException("Can't set incoming encoding to UTF-8");
        }
        
        String url = req.getParameter(TRACKBACK_URL_PARAM);
        String title = req.getParameter(TRACKBACK_TITLE_PARAM);
        String excerpt = req.getParameter(TRACKBACK_EXCERPT_PARAM);
        String blogName = req.getParameter(TRACKBACK_BLOG_NAME_PARAM);
        
        if ((title == null) || "".equals(title)) {
            title = url;
        }
        
        if (excerpt == null) {
            excerpt = "";
        } else {
            if (excerpt.length() >= 255) {
                excerpt = excerpt.substring(0, 252);
                excerpt += "...";
            }
        }
        
        String error = null;
        boolean verified = true;
        PrintWriter pw = new PrintWriter(res.getOutputStream());
        try {
            if(!RollerRuntimeConfig.getBooleanProperty("users.trackbacks.enabled")) {
                error = "Trackbacks are disabled for this site";
            } 
            else if (title==null || url==null || excerpt==null || blogName==null) {
                error = "title, url, excerpt, and blog_name not specified.";
            } 
            else {                
                RollerRequest rreq = RollerRequest.getRollerRequest(req);
                WeblogEntryData entry = rreq.getWeblogEntry();
                WebsiteData website = entry.getWebsite();
                boolean siteAllows = website.getAllowComments().booleanValue();
                
                if (entry!=null && siteAllows && entry.getCommentsStillAllowed()) {
                    
                    // Track trackbacks as comments
                    CommentData comment = new CommentData();
                    comment.setContent("[Trackback] "+excerpt);
                    comment.setName(blogName);
                    comment.setUrl(url);
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
                        RollerContext rctx= RollerContext.getRollerContext();
                        String absurl = rctx.getAbsoluteContextUrl();
                        LinkbackExtractor linkback = new LinkbackExtractor(
                            comment.getUrl(), absurl + entry.getPermaLink());
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
                        if (verified && website.getCommentModerationRequired()) {
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
                        RollerContext rc = RollerContext.getRollerContext();                                
                        String rootURL = rc.getAbsoluteContextUrl(req);
                        if (rootURL == null || rootURL.trim().length()==0) {
                            rootURL = RequestUtils.serverURL(req) + req.getContextPath();
                        } 
                        CommentServlet.sendEmailNotification(comment, rootURL);

                        pw.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
                        pw.println("<response>");
                        pw.println("<error>0</error>");
                        if (comment.getPending().booleanValue()) {
                            pw.println("<message>Trackback sumitted to moderation</message>");
                        } else {
                            pw.println("<message>Trackback accepted</message>");
                        }
                        pw.println("</response>");
                        pw.flush();
                    }
                    
                } else if (entry!=null) {
                    error = "Comments and Trackbacks are disabled for the entry you specified.";
                } else {
                    error = "Entry not specified.";
                }
            }
            
        } catch (Exception e) {
            error = e.getMessage();
            if ( error == null ) {
                error = e.getClass().getName();
            }
        }
        
        if ( error!= null ) {
            pw.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
            pw.println("<response>");
            pw.println("<error>1</error>");
            pw.println("<message>ERROR: "+error+"</message>");
            pw.println("</response>");
            pw.flush();
        }
        res.flushBuffer();
        
        // TODO : FindBugs thinks 'pw' should close
    }
}