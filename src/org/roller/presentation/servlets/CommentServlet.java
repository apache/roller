package org.roller.presentation.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.IndexManager;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.velocity.CommentAuthenticator;
import org.roller.presentation.weblog.formbeans.CommentFormEx;
import org.roller.util.SpamChecker;
import org.roller.util.MailUtil;
import org.roller.util.StringUtils;
import org.roller.presentation.*;
import org.roller.presentation.cache.CacheManager;
import org.roller.presentation.velocity.DefaultCommentAuthenticator;
import org.roller.util.Utilities;

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
 * @author Allen Gilliland
 *
 * @web.servlet name="CommentServlet"
 * @web.servlet-mapping url-pattern="/comment/*"
 */
public class CommentServlet extends HttpServlet {
    
    private static Log mLogger = LogFactory.getLog(CommentServlet.class);
    
    private static final String EMAIL_ADDR_REGEXP = "^.*@.*[.].{2,}$";
    
    private ResourceBundle bundle =
        ResourceBundle.getBundle("ApplicationResources");
    
    private CommentAuthenticator authenticator = null;
    
    
    /** 
     * Initialization.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // lookup the authenticator we are going to use and instantiate it
        try {
            String name = RollerConfig.getProperty("comment.authenticator.classname");
            
            Class clazz = Class.forName(name);
            this.authenticator = (CommentAuthenticator) clazz.newInstance();
            
        } catch(Exception e) {
            mLogger.error(e);
            this.authenticator = new DefaultCommentAuthenticator();
        }

    }
    
    
    /**
     * Handle incoming http GET requests.
     *
     * The CommentServlet is not meant to handle GET requests, so we just
     * redirect these request to the root of the webapp.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        
        // we should never get any GET requests, but just in case
        response.sendRedirect(request.getContextPath());
    }
    
    
    /**
     * Service incoming POST requests.
     *
     * Here we handle incoming comment postings.  We will collect the data,
     * validate it, and save it.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        boolean preview = false;
        String error = null;
        String message = null;
        String entry_permalink = request.getContextPath();
        
        String method = request.getParameter("method");
        if(method == null)
            method = "post";
        else if (method.equals("preview"))
            preview = true;
        
        // parse request and validate
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        HttpSession session = request.getSession();
        
        // make sure we know the entry this comment is going to
        WeblogEntryData entry = rreq.getWeblogEntry();
        if (entry == null || entry.getId() == null) {
            session.setAttribute(RollerSession.ERROR_MESSAGE, "Cannot post comment to null entry");
            RequestDispatcher dispatcher = 
                request.getRequestDispatcher(entry_permalink);
            dispatcher.forward(request, response);
            return;
        }
            
        try {
            // we know what our weblog entry is, so setup our permalink url
            entry_permalink = entry.getPermaLink();
            
            mLogger.debug("Doing comment posting for entry = "+entry_permalink);
            
            // check if we even allow comments
            if(!RollerRuntimeConfig.getBooleanProperty("users.comments.enabled"))
                throw new Exception("Comments are disabled for this site.");
            
            if (!entry.getWebsite().getAllowComments().booleanValue() ||
                    !entry.getCommentsStillAllowed())
                throw new Exception("Comments not allowed on this entry");
            
            WebsiteData website = entry.getWebsite();
            
            // Construct our Comment object from the submitted data
            WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
            CommentFormEx cf = new CommentFormEx();
            CommentData comment = new CommentData();
            RequestUtils.populate(cf, request);
            cf.copyTo(comment, request.getLocale());
            
            comment.setWeblogEntry(entry);
            comment.setRemoteHost(request.getRemoteHost());
            comment.setPostTime(new java.sql.Timestamp(System.currentTimeMillis()));
            
            cf.setWeblogEntry(entry);
            cf.setPostTime(new java.sql.Timestamp(System.currentTimeMillis()));
            
            request.setAttribute("commentForm", cf);
            request.setAttribute("blogEntry", entry);
            
            if (preview) {
                message = "This is a comment preview only";
                
                // If comment contains blacklisted text, warn commenter
                SpamChecker checker = new SpamChecker();
                if (checker.checkComment(comment)) {
                   error = bundle.getString("commentServlet.previewMarkedAsSpam");
                   mLogger.debug("Comment marked as spam"); 
                }
                request.setAttribute("previewComments", "dummy");
                mLogger.debug("Comment is a preview");
                
            } else {
                if (this.authenticator.authenticate(comment, request)) {
                    mLogger.debug("Comment passed authentication");
                    
                    // If comment contains blacklisted text, mark as spam
                    SpamChecker checker = new SpamChecker();
                    if (checker.checkComment(comment)) {
                       comment.setSpam(Boolean.TRUE);
                       error = bundle.getString("commentServlet.commentMarkedAsSpam");
                       mLogger.debug("Comment marked as spam"); 
                    }
                     
                    // If comment moderation is on, set comment as pending
                    if (website.getCommentModerationRequired()) {
                        comment.setPending(Boolean.TRUE);   
                        comment.setApproved(Boolean.FALSE);
                        message = bundle.getString("commentServlet.submittedToModerator");
                    } else { 
                        comment.setPending(Boolean.FALSE);   
                        comment.setApproved(Boolean.TRUE);
                    }
                    
                    mgr.saveComment(comment);
                    RollerFactory.getRoller().flush();
                    reindexEntry(entry);
                    
                    // Clear all caches associated with comment
                    CacheManager.invalidate(comment);
                    
                    // Send email notifications
                    RollerContext rc = RollerContext.getRollerContext();                                
                    String rootURL = rc.getAbsoluteContextUrl(request);
                    if (rootURL == null || rootURL.trim().length()==0) {
                        rootURL = RequestUtils.serverURL(request) + request.getContextPath();
                    }            
                    sendEmailNotification(comment, rootURL);
                    
                } else {
                    error = bundle.getString("error.commentAuthFailed");
                    mLogger.debug("Comment failed authentication");
                }
            }
        } catch (RollerException re) {
            mLogger.error("ERROR posting comment", re);
            error = re.getMessage();
        } catch (Exception e) {
            error = e.getMessage();
        }
        
        // the work has been done, now send the user back to the entry page
        if (error != null)
            session.setAttribute(RollerSession.ERROR_MESSAGE, error);
        if (message != null)
            session.setAttribute(RollerSession.STATUS_MESSAGE, message);
        
        if(error == null && !preview) {
            entry_permalink = request.getContextPath()+entry_permalink;            
            mLogger.debug("comment complete, redirecting to "+entry_permalink);
            response.sendRedirect(entry_permalink);
        } else {
            mLogger.debug("more work needed, forwarding to "+entry_permalink);
            RequestDispatcher dispatcher = 
                request.getRequestDispatcher(entry_permalink);
            dispatcher.forward(request, response);
        }
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
     * Send email notification of comment.
     *
     * TODO: Make the addressing options configurable on a per-website basis.
     */
    public static void sendEmailNotification(CommentData cd, String rootURL) {
        
        // Send commment notifications in locale of server
        ResourceBundle resources = ResourceBundle.getBundle("ApplicationResources");

        WeblogEntryData entry = cd.getWeblogEntry();
        WebsiteData site = entry.getWebsite();
        UserData user = entry.getCreator();
        
        // Send e-mail to owner and subscribed users (if enabled)
        boolean notify = RollerRuntimeConfig.getBooleanProperty("users.comments.emailnotify");
        if (notify && site.getEmailComments().booleanValue()) {
            mLogger.debug("Comment notification enabled ... preparing email");
            
            // Determine message and addressing options from init parameters
            boolean separateMessages =
                    RollerConfig.getBooleanProperty("comment.notification.separateOwnerMessage");
            boolean hideCommenterAddrs =
                    RollerConfig.getBooleanProperty("comment.notification.hideCommenterAddresses");
            
            //------------------------------------------
            // --- Determine the "from" address
            // --- Use either the site configured from address or the user's address
            
            String from =
                    (StringUtils.isEmpty(site.getEmailFromAddress()))
                    ? user.getEmailAddress()
                    : site.getEmailFromAddress();
            
            //------------------------------------------
            // --- Build list of email addresses to send notification to
            
            List comments = null;
            try {
                WeblogManager wMgr = RollerFactory.getRoller().getWeblogManager();
                // get only approved, non spam comments
                comments = entry.getComments(true, true); 
            } catch(RollerException re) {
                // should never happen
                comments = new ArrayList();
            }
            
            // Get all the subscribers to this comment thread
            Set subscribers = new TreeSet();
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
            
            // Form array of commenter addrs
            String[] commenterAddrs = (String[])subscribers.toArray(new String[0]);
            
            //------------------------------------------
            // --- Form the messages to be sent -
            // For simplicity we always build separate owner and commenter messages even if sending a single one
            
            // Determine with mime type to use for e-mail
            StringBuffer msg = new StringBuffer();
            StringBuffer ownermsg = new StringBuffer();
            boolean escapeHtml = RollerRuntimeConfig.getBooleanProperty("users.comments.escapehtml");
            
            if (!escapeHtml) {
                msg.append("<html><body style=\"background: white; ");
                msg.append(" color: black; font-size: 12px\">");
            }
            
            if (!StringUtils.isEmpty(cd.getName())) {
                msg.append(cd.getName() + " "
                        + resources.getString("email.comment.wrote")+": ");
            } else {
                msg.append(resources.getString("email.comment.anonymous")+": ");
            }
            
            msg.append((escapeHtml) ? "\n\n" : "<br /><br />");
                        
            msg.append((escapeHtml) ? Utilities.escapeHTML(cd.getContent()) 
                : Utilities.transformToHTMLSubset(Utilities.escapeHTML(cd.getContent())));
            
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
            
            ownermsg.append(msg);
            
            // add link to weblog edit page so user can login to manage comments
            ownermsg.append((escapeHtml) ? "\n\n----\n" :
                "<br /><br /><hr /><span style=\"font-size: 11px\">");
            ownermsg.append("Link to comment management page:");
            ownermsg.append((escapeHtml) ? "\n" : "<br />");
            
            StringBuffer deleteURL = new StringBuffer(rootURL);
            deleteURL.append("/editor/commentManagement.do?method=query&entryid=" + entry.getId());
            
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
                    (StringUtils.equals(cd.getEmail(), user.getEmailAddress()))) {
                subject= "RE: "+resources.getString("email.comment.title")+": ";
            } else {
                subject = resources.getString("email.comment.title") + ": ";
            }
            subject += entry.getTitle();
            
            //------------------------------------------
            // --- Send message to email recipients
            try {
                javax.naming.Context ctx = (javax.naming.Context)
                new InitialContext().lookup("java:comp/env");
                Session session = (Session)ctx.lookup("mail/Session");
                boolean isHtml = !escapeHtml;
                if (separateMessages) {
                    // Send separate messages to owner and commenters
                    sendMessage(session, from,
                            new String[]{user.getEmailAddress()}, null, null, subject, ownermsg.toString(), isHtml);
                            if (commenterAddrs.length > 0) {
                                // If hiding commenter addrs, they go in Bcc: otherwise in the To: of the second message
                                String[] to = hideCommenterAddrs ? null : commenterAddrs;
                                String[] bcc = hideCommenterAddrs ? commenterAddrs : null;
                                sendMessage(session, from, to, null, bcc, subject, msg.toString(), isHtml);
                                
                            }
                } else {
                    // Single message.  User in To: header, commenters in either cc or bcc depending on hiding option
                    String[] cc = hideCommenterAddrs ? null : commenterAddrs;
                    String[] bcc = hideCommenterAddrs ? commenterAddrs : null;
                    sendMessage(session, from, new String[]{user.getEmailAddress()}, cc, bcc, subject,
                            ownermsg.toString(), isHtml);
                }
            } catch (javax.naming.NamingException ne) {
                mLogger.error("Unable to lookup mail session.  Check configuration.  NamingException: " + ne.getMessage());
            } catch (Exception e) {
                mLogger.warn("Exception sending comment mail: " + e.getMessage());
                // This will log the stack trace if debug is enabled
                if (mLogger.isDebugEnabled()) {
                    mLogger.debug(e);
                }
            }
            
            mLogger.debug("Done sending email message");
            
        } // if email enabled
    }
    
    /**
     * Send message to author of approved comment
     *
     * TODO: Make the addressing options configurable on a per-website basis.
     */
    public static void sendEmailApprovalNotification(CommentData cd, String rootURL) {
        
        // Send commment notifications in locale of server
        ResourceBundle resources = ResourceBundle.getBundle("ApplicationResources");
        
        WeblogEntryData entry = cd.getWeblogEntry();
        WebsiteData site = entry.getWebsite();
        UserData user = entry.getCreator();
            
        // Only send email if email notificaiton is enabled
        boolean notify = RollerRuntimeConfig.getBooleanProperty("users.comments.emailnotify");
        if (notify && site.getEmailComments().booleanValue()) {
            mLogger.debug("Comment notification enabled ... preparing email");
            

                                
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

            // Build link back to comment
            StringBuffer commentURL = new StringBuffer(rootURL);
            commentURL.append(entry.getPermaLink());
            commentURL.append("#comments");
            msg.append(commentURL.toString());
            
            //------------------------------------------
            // --- Send message to author of approved comment
            try {
                javax.naming.Context ctx = (javax.naming.Context)
                new InitialContext().lookup("java:comp/env");
                Session session = (Session)ctx.lookup("mail/Session");
                String[] cc = null;
                String[] bcc = null;
                sendMessage(session, from, 
                    new String[] {cd.getEmail()}, 
                    null, // cc
                    null, // bcc
                    subject, msg.toString(), false);
            } catch (javax.naming.NamingException ne) {
                mLogger.error("Unable to lookup mail session.  Check configuration.  NamingException: " + ne.getMessage());
            } catch (Exception e) {
                mLogger.warn("Exception sending comment mail: " + e.getMessage());
                // This will log the stack trace if debug is enabled
                if (mLogger.isDebugEnabled()) {
                    mLogger.debug(e);
                }
            }
            
            mLogger.debug("Done sending email message");
            
        } // if email enabled
    }
    
    
    /*
     * This is somewhat ridiculous, but avoids duplicating a bunch of logic 
     * in the already messy sendEmailNotification.
     */
    static void sendMessage(Session session, String from, String[] to, String[] cc, String[] bcc, String subject,
            String msg, boolean isHtml) throws MessagingException {
        if (isHtml)
            MailUtil.sendHTMLMessage(session, from, to, cc, bcc, subject, msg);
        else
            MailUtil.sendTextMessage(session, from, to, cc, bcc, subject, msg);
    }
    
}

