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
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.business.search.IndexManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.Weblog;
import org.apache.roller.ui.rendering.model.UtilitiesModel;
import org.apache.roller.ui.rendering.util.CommentAuthenticator;
import org.apache.roller.ui.rendering.util.CommentValidationManager;
import org.apache.roller.ui.rendering.util.DefaultCommentAuthenticator;
import org.apache.roller.ui.rendering.util.WeblogCommentRequest;
import org.apache.roller.ui.rendering.util.WeblogEntryCommentForm;
import org.apache.roller.util.GenericThrottle;
import org.apache.roller.util.IPBanList;
import org.apache.roller.util.MailUtil;
import org.apache.roller.util.I18nMessages;
import org.apache.roller.util.RollerMessages;
import org.apache.roller.util.RollerMessages.RollerMessage;
import org.apache.roller.util.URLUtilities;
import org.apache.roller.util.Utilities;
import org.apache.roller.util.cache.CacheManager;


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
 *
 * @web.servlet name="CommentServlet" load-on-startup="7"
 * @web.servlet-mapping url-pattern="/roller-ui/rendering/comment/*"
 */
public class CommentServlet extends HttpServlet {
    
    private static Log log = LogFactory.getLog(CommentServlet.class);
    
    private static final String EMAIL_ADDR_REGEXP = "^.*@.*[.].{2,}$";
    
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
        WeblogEntryData entry = null;
        
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
                throw new RollerException("unable to lookup weblog: "+
                        commentRequest.getWeblogHandle());
            }
            
            // lookup entry specified by comment request
            entry = commentRequest.getWeblogEntry();
            if(entry == null) {
                throw new RollerException("unable to lookup entry: "+
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
        
        
        log.debug("Doing comment posting for entry = "+entry.getPermaLink());
        
        // collect input from request params and construct new comment object
        // fields: name, email, url, content, notify
        // TODO: data validation on collected comment data
        CommentData comment = new CommentData();
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
                comment.setStatus(CommentData.PENDING);
                message = messageUtils.getString("commentServlet.submittedToModerator");
            } else if (validationScore == 100) {
                // else they're approved
                comment.setStatus(CommentData.APPROVED);
            } else {
                // Invalid comments are marked as spam
                comment.setStatus(CommentData.SPAM);
                error = messageUtils.getString("commentServlet.commentMarkedAsSpam");
                log.debug("Comment marked as spam");
            }
            
            try {               
                if(!CommentData.SPAM.equals(comment.getStatus()) ||
                        !RollerRuntimeConfig.getBooleanProperty("comments.ignoreSpam.enabled")) {
                    
                    WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
                    mgr.saveComment(comment);
                    RollerFactory.getRoller().flush();
                    
                    // Send email notifications, but only to subscribers if comment is 100% valid
                    boolean notifySubscribers = (validationScore == 100);
                    String rootURL = RollerRuntimeConfig.getAbsoluteContextURL();
                    sendEmailNotification(comment, notifySubscribers, messages, rootURL, messageUtils);
                    
                    // only re-index/invalidate the cache if comment isn't moderated
                    if(!weblog.getCommentModerationRequired()) {
                        reindexEntry(entry);
                        
                        // Clear all caches associated with comment
                        CacheManager.invalidate(comment);
                    }
                    
                    // comment was successful, clear the comment form
                    cf = new WeblogEntryCommentForm();
                }
                
            } catch (RollerException re) {
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
    
    
    /**
     * Re-index the WeblogEntry so that the new comment gets indexed.
     */
    private void reindexEntry(WeblogEntryData entry)
    throws RollerException {
        
        IndexManager manager = RollerFactory.getRoller().getIndexManager();
        
        // remove entry before (re)adding it, or in case it isn't Published
        manager.removeEntryIndexOperation(entry);
        
        // if published, index the entry
        if (entry.isPublished()) {
            manager.addEntryIndexOperation(entry);
        }
    }
    
    
    /**
     * Send email notification of new or newly approved comment.
     * TODO: Make the addressing options configurable on a per-website basis.
     *
     * @param commentObject      The new comment
     * @param notifySubscribers  True if subscribers are to be notified
     * @param messages           Messages to be included in e-mail (or null). 
     *                           Errors will be assumed to be "validation errors" 
     *                           and messages will be assumed to be "from the system"
     * @param rootURL            Root URL of the Roller site
     */
    public static void sendEmailNotification(
            CommentData commentObject, boolean notifySubscribers,
            RollerMessages messages, String rootURL, I18nMessages resources) {
        
        WeblogEntryData entry = commentObject.getWeblogEntry();
        Weblog site = entry.getWebsite();
        UserData user = entry.getCreator();
        
        // Send e-mail to owner and subscribed users (if enabled)
        boolean notify = RollerRuntimeConfig.getBooleanProperty("users.comments.emailnotify");
        if (notify && site.getEmailComments().booleanValue()) {
            log.debug("Comment notification enabled ... preparing email");
            
            // Determine message and addressing options from init parameters
            boolean hideCommenterAddrs = RollerConfig.getBooleanProperty(
                    "comment.notification.hideCommenterAddresses");
            
            //------------------------------------------
            // --- Determine the "from" address
            // --- Use either the site configured from address or the user's address
            
            String from =
                    (StringUtils.isEmpty(site.getEmailFromAddress()))
                    ? user.getEmailAddress()
                    : site.getEmailFromAddress();
            
            //------------------------------------------
            // --- Build list of email addresses to send notification to
            
            List comments = entry.getComments(true, true);
            
            Set subscribers = new TreeSet();
            
            // If we are to notify subscribers, then...
            if (notifySubscribers) {
                log.debug("Sending notification email to all subscribers");
                
                // Get all the subscribers to this comment thread
                for (Iterator it = comments.iterator(); it.hasNext();) {
                    CommentData comment = (CommentData) it.next();
                    if (!StringUtils.isEmpty(comment.getEmail())) {
                        // If user has commented twice,
                        // count the most recent notify setting
                        if (comment.getNotify().booleanValue()) {
                            // only add those with valid email
                            if (comment.getEmail().matches(EMAIL_ADDR_REGEXP)) {
                                subscribers.add(comment.getEmail());
                            }
                        } else {
                            // remove user who doesn't want to be notified
                            subscribers.remove(comment.getEmail());
                        }
                    }
                }
            } else {
                log.debug("Sending notification email only to weblog owner");
            }
            
            // Form array of commenter addrs
            String[] commenterAddrs = (String[])subscribers.toArray(new String[0]);
            
            //------------------------------------------
            // --- Form the messages to be sent -
            // Build separate owner and commenter (aka subscriber) messages
            
            // Determine with mime type to use for e-mail
            StringBuffer msg = new StringBuffer();
            StringBuffer ownermsg = new StringBuffer();
            boolean escapeHtml = RollerRuntimeConfig.getBooleanProperty("users.comments.escapehtml");
            
            // first the commenter message
            
            if (!escapeHtml) {
                msg.append("<html><body style=\"background: white; ");
                msg.append(" color: black; font-size: 12px\">");
            }
            
            if (!StringUtils.isEmpty(commentObject.getName())) {
                msg.append(commentObject.getName() + " "
                        + resources.getString("email.comment.wrote")+": ");
            } else {
                msg.append(resources.getString("email.comment.anonymous")+": ");
            }
            
            msg.append((escapeHtml) ? "\n\n" : "<br /><br />");
            
            msg.append((escapeHtml) ? Utilities.escapeHTML(commentObject.getContent())
            : UtilitiesModel.transformToHTMLSubset(Utilities.escapeHTML(commentObject.getContent())));
            
            msg.append((escapeHtml) ? "\n\n----\n"
                    : "<br /><br /><hr /><span style=\"font-size: 11px\">");
            msg.append(resources.getString("email.comment.respond") + ": ");
            msg.append((escapeHtml) ? "\n" : "<br />");
            
            // Build link back to comment
            StringBuffer commentURL = new StringBuffer(rootURL);
            commentURL.append(entry.getPermaLink());
            commentURL.append("#comments");
            
            if (escapeHtml) {
                msg.append(commentURL.toString());
            } else {
                msg.append("<a href=\""+commentURL+"\">"+commentURL+"</a></span>");
            }
            
            // next the owner message
            
            // First, list any messages from the system that were passed in:
            if (messages.getMessageCount() > 0) {
                ownermsg.append((escapeHtml) ? "" : "<p>");
                ownermsg.append(resources.getString("commentServlet.email.thereAreSystemMessages"));
                ownermsg.append((escapeHtml) ? "\n\n" : "</p>");
                ownermsg.append((escapeHtml) ? "" : "<ul>");
            }
            for (Iterator it = messages.getMessages(); it.hasNext();) {
                RollerMessage rollerMessage = (RollerMessage)it.next();
                ownermsg.append((escapeHtml) ? "" : "<li>");
                ownermsg.append(MessageFormat.format(resources.getString(rollerMessage.getKey()), (Object[])rollerMessage.getArgs()) );
                ownermsg.append((escapeHtml) ? "\n\n" : "</li>");
            }
            if (messages.getMessageCount() > 0) {
                ownermsg.append((escapeHtml) ? "\n\n" : "</ul>");
            }
            
            // Next, list any validation error messages that were passed in:
            if (messages.getErrorCount() > 0) {
                ownermsg.append((escapeHtml) ? "" : "<p>");
                ownermsg.append(resources.getString("commentServlet.email.thereAreErrorMessages"));
                ownermsg.append((escapeHtml) ? "\n\n" : "</p>");
                ownermsg.append((escapeHtml) ? "" : "<ul>");
            }
            for (Iterator it = messages.getErrors(); it.hasNext();) {
                RollerMessage rollerMessage = (RollerMessage)it.next();
                ownermsg.append((escapeHtml) ? "" : "<li>");
                ownermsg.append(MessageFormat.format(resources.getString(rollerMessage.getKey()), (Object[])rollerMessage.getArgs()) );
                ownermsg.append((escapeHtml) ? "\n\n" : "</li>");
            }
            if (messages.getErrorCount() > 0) {
                ownermsg.append((escapeHtml) ? "\n\n" : "</ul>");
            }
            
            ownermsg.append(msg);
            
            // add link to weblog edit page so user can login to manage comments
            ownermsg.append((escapeHtml) ? "\n\n----\n" :
                "<br /><br /><hr /><span style=\"font-size: 11px\">");
            ownermsg.append("Link to comment management page:");
            ownermsg.append((escapeHtml) ? "\n" : "<br />");
            
            StringBuffer deleteURL = new StringBuffer(rootURL);
            deleteURL.append("/roller-ui/authoring/comments.rol?entryId=" + entry.getId());
            
            if (escapeHtml) {
                ownermsg.append(deleteURL.toString());
            } else {
                ownermsg.append(
                        "<a href=\"" + deleteURL + "\">" + deleteURL + "</a></span>");
                msg.append("</Body></html>");
                ownermsg.append("</Body></html>");
            }
            
            String subject = null;
            if ((subscribers.size() > 1) ||
                    (StringUtils.equals(commentObject.getEmail(), user.getEmailAddress()))) {
                subject= "RE: "+resources.getString("email.comment.title")+": ";
            } else {
                subject = resources.getString("email.comment.title") + ": ";
            }
            subject += entry.getTitle();
            
            //------------------------------------------
            // --- Send message to email recipients
            try {
                boolean isHtml = !escapeHtml;
                // Send separate messages to owner and commenters
                sendMessage(
                    from, new String[]{user.getEmailAddress()}, null, 
                    null, subject, ownermsg.toString(), isHtml);
                if (notifySubscribers && commenterAddrs.length > 0) {
                    // If hiding commenter addrs, they go in Bcc: otherwise in the To: of the second message
                    String[] to = hideCommenterAddrs ? null : commenterAddrs;
                    String[] bcc = hideCommenterAddrs ? commenterAddrs : null;
                    sendMessage(from, to, null, bcc, subject, msg.toString(), isHtml);

                }
            } catch (Exception e) {
                log.warn("Exception sending comment mail: " + e.getMessage());
                // This will log the stack trace if debug is enabled
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
            }
            
            log.debug("Done sending email message");
            
        } // if email enabled
    }
    
    
    /**
     * Send message to author of approved comment
     *
     * TODO: Make the addressing options configurable on a per-website basis.
     */
    public static void sendEmailApprovalNotification(CommentData cd, String rootURL, I18nMessages resources) {
        
        WeblogEntryData entry = cd.getWeblogEntry();
        Weblog site = entry.getWebsite();
        UserData user = entry.getCreator();
        
        // Only send email if email notificaiton is enabled
        boolean notify = RollerRuntimeConfig.getBooleanProperty("users.comments.emailnotify");
        if (notify && site.getEmailComments().booleanValue()) {
            log.debug("Comment notification enabled ... preparing email");
            
            
            
            //------------------------------------------
            // --- Determine the "from" address
            // --- Use either the site configured from address or the user's address
            
            String from =
                    (StringUtils.isEmpty(site.getEmailFromAddress()))
                    ? user.getEmailAddress()
                    : site.getEmailFromAddress();
            
            //------------------------------------------
            // --- Form the message to be sent -
            
            String subject = resources.getString("email.comment.commentApproved");
            
            StringBuffer msg = new StringBuffer();
            msg.append(resources.getString("email.comment.commentApproved"));
            msg.append("\n\n");
            
            // Build link back to comment
            StringBuffer commentURL = new StringBuffer();
            commentURL.append(entry.getPermalink());
            commentURL.append("#comments");
            msg.append(commentURL.toString());
            
            //------------------------------------------
            // --- Send message to author of approved comment
            try {
                String[] cc = null;
                String[] bcc = null;
                sendMessage(from,
                        new String[] {cd.getEmail()},
                        null, // cc
                        null, // bcc
                        subject, msg.toString(), false);
            } catch (Exception e) {
                log.warn("Exception sending comment mail: " + e.getMessage());
                // This will log the stack trace if debug is enabled
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
            }
            
            log.debug("Done sending email message");
            
        } // if email enabled
    }
    
    
    /*
     * This is somewhat ridiculous, but avoids duplicating a bunch of logic
     * in the already messy sendEmailNotification.
     */
    static void sendMessage(String from, String[] to, String[] cc, String[] bcc, String subject,
            String msg, boolean isHtml) throws MessagingException {
        if (isHtml)
            MailUtil.sendHTMLMessage(from, to, cc, bcc, subject, msg);
        else
            MailUtil.sendTextMessage(from, to, cc, bcc, subject, msg);
    }
    
}
