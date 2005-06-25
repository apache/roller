package org.roller.presentation.velocity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.IndexManager;
import org.roller.model.Roller;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.PageData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.LanguageUtil;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.presentation.weblog.formbeans.CommentFormEx;
import org.roller.util.CommentSpamChecker;
import org.roller.util.MailUtil;
import org.roller.util.StringUtils;

/**
 * Extend PageServlet to do special handling needed to support viewing and 
 * posting of comments. Handles in-page comments and popup-style comments.
 * <p />
 * This servlet overrides the VelocityServlet's handleRequest method so that 
 * the correct comments page template can be loaded for popup comments.
 * If this servlet is called with the request paramteter 'popup' 
 * defined, then it will load the user's _popupcomments page and if no such
 * page is found it will use /popupcomments.vm which looks just like the old
 * pre-0.9.9 popup comments page. 
 * <p />
 * This servlet also overrides doPost() to handle review and posting of new
 * comments. If the request paramter 'method' is set to 'preview' then the 
 * posted comment will be previewed, otherwise if it will be posted.
 * <p />
 * Incoming comments are tested against the MT Blacklist. If they are found
 * to be spam, then they are marked as spam and hidden from view.
 * <p />
 * If email notification is turned on, each new comment will result in an 
 * email sent to the blog owner and all who have commented on the same post.
 *
 * @web.servlet name="CommentServlet" 
 * @web.servlet-mapping url-pattern="/comments/*"
 * @web.servlet-init-param name="org.apache.velocity.properties" 
 * 		                  value="/WEB-INF/velocity.properties"
 *
 * @author Dave Johnson 
 */
public class CommentServlet extends PageServlet 
{
    private static final String COMMENT_SPAM_MSG = 
              "Your comment has been recognized as "
            + "<a href='http://www.jayallen.org/projects/mt-blacklist/'>"
            + "Comment Spam</a> and rejected.";
    private transient ResourceBundle bundle = 
        ResourceBundle.getBundle("ApplicationResources");
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(CommentServlet.class);
    
    //-----------------------------------------------------------------------
    /**
     * Override VelocityServlet so we can pick the right page and stick
     * the right stuff into the VelocityContext before page execution.
     */
    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context ctx ) throws Exception
    {
        Template template = null;
        if (request.getParameter("popup") == null)
        {
            // Request does not specify popup, so normal return
            template = super.handleRequest(request, response, ctx); 
        }
        else
        {
            PageContext pageContext =
                JspFactory.getDefaultFactory().getPageContext(
                    this, request, response,"", true, 8192, true);
            RollerRequest rreq= RollerRequest.getRollerRequest(pageContext);
            UserManager userMgr = rreq.getRoller().getUserManager();
            WebsiteData website = rreq.getWebsite();
                
            // Request specifies popup
            PageData page = null;
            Exception pageException = null;
            try 
            {
                // Does user have a popupcomments page?
                page = userMgr.getPageByName(website, "_popupcomments");
            }
            catch(Exception e )
            {
               pageException = e;
               response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            if (pageException != null)
            {
                mLogger.error("EXCEPTION: in RollerServlet", pageException);
                request.setAttribute("DisplayException", pageException);
            }
            // User doesn't have one so return the default
            if (page == null) 
            {
                page = new PageData("/popupcomments.vm", website, "Comments", 
                    "Comments", "dummy_link", "dummy_template", new Date());
            }
            rreq.setPage(page);
            template = prepareForPageExecution(ctx, rreq, response, page);
        }
        return template;
    }
    
    //-----------------------------------------------------------------------
    /**
     * Handle POST from comment form, then hand off to super for page rendering.
     */
    public void doPost(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        if (request.getParameter("method") != null 
            && request.getParameter("method").equals("preview"))
        {
            doPreviewPost(request, response);
            return;
        }

        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        HttpSession session = request.getSession();
        try
        {
            // Get weblog entry object, put in page context
            WeblogEntryData wd = rreq.getWeblogEntry();
            if (wd == null || wd.getId() == null)
            {
                throw new RollerException(
                    "Unable to find WeblogEntry for "
                    + request.getParameter(RollerRequest.WEBLOGENTRYID_KEY));
            }
            if (   !wd.getWebsite().getAllowComments().booleanValue()
                || !wd.getCommentsStillAllowed())
            {
                throw new RollerException("ERROR comments not allowed");
            }
            
            request.setAttribute("blogEntry", wd);

            // get the User to which the blog belongs
            UserData user = wd.getWebsite().getUser();
            
            // TODO: A hack to be replaced by Object.canEdit()
            request.setAttribute(RollerRequest.OWNING_USER, user);

            // Save comment
            WeblogManager mgr = rreq.getRoller().getWeblogManager();
            CommentFormEx cf = new CommentFormEx();
            CommentData cd = new CommentData();
            RequestUtils.populate(cf, request);
            cf.copyTo(cd, request.getLocale());
            cd.setWeblogEntry(wd);
            cd.setRemoteHost(request.getRemoteHost());
            cd.setPostTime(new java.sql.Timestamp(System.currentTimeMillis()));
            
            if (!testCommentSpam(cd, request)) 
            {
                if (RollerContext.getCommentAuthenticator().authenticate(cd, request))
                {
                    cd.save();
                    rreq.getRoller().commit();
                    reindexEntry(rreq.getRoller(), wd);

                    // Refresh user's entries in page cache
                    PageCacheFilter.removeFromCache(request, user);

                    // Put array of comments in context
                    List comments = mgr.getComments(wd.getId());
                    request.setAttribute("blogComments", comments);

                    // MR: Added functionality to e-mail comments
                    sendEmailNotification(request, rreq, wd, cd, user,comments);
                    
                    super.doPost(request, response);
                    return;
                }
                else
                {
                    request.getSession().setAttribute(
                        RollerSession.ERROR_MESSAGE, 
                        bundle.getString("error.commentAuthFailed"));
                }
            }
            doPreviewPost(request, response);
        }
        catch (Exception e)
        {
            mLogger.error("ERROR posting comment", e);
            // Noted: this never gets back to the user.  Not sure why it is being set.
            session.setAttribute(RollerSession.ERROR_MESSAGE, e.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Load comment and blog entry objects and forward to either the popup
     * or the in-page comment page for comment preview.
     */
    public void doPreviewPost(
        HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        try
        {
            WeblogEntryData wd = rreq.getWeblogEntry();
            if (wd == null || wd.getId() == null)
            {
                throw new RollerException(
                   "Unable to find WeblogEntry for "
                   + request.getParameter(RollerRequest.WEBLOGENTRYID_KEY));
            }
            request.setAttribute("blogEntry", wd);

            // TODO: A hack to be replaced by Object.canEdit()
            request.setAttribute(RollerRequest.OWNING_USER, wd.getWebsite().getUser());

            CommentFormEx cf = new CommentFormEx();
            RequestUtils.populate(cf, request);
            cf.setWeblogEntry(wd);
            cf.setPostTime(new java.sql.Timestamp(System.currentTimeMillis()));
            request.setAttribute("commentForm", cf);
            request.setAttribute("previewComments","dummy");
        }
        catch (Exception e)
        {
            // TODO: error message for browser and log
            mLogger.error(e);
        }
        super.doPost(request, response);
    }

    /**
     * Re-index the WeblogEntry so that the new comment gets indexed.
     * @param entry
     */
    private void reindexEntry(Roller roller, WeblogEntryData entry) throws RollerException
    {
        IndexManager manager = roller.getIndexManager();

        // remove entry before (re)adding it, or in case it isn't Published
        manager.removeEntryIndexOperation(entry);

        // if published, index the entry
        if (entry.getPublishEntry() == Boolean.TRUE)
        {
            manager.addEntryIndexOperation(entry);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Test CommentData to see if it is spam, if it is set it's spam property
     * to true and put a RollerSession.ERROR_MESSAGE message in the session.
     * @param cd CommentData to be tested.
     */
    private boolean testCommentSpam(CommentData cd, HttpServletRequest req)
    {
        boolean ret = false;
        CommentSpamChecker checker = new CommentSpamChecker();
        checker.testComment(cd);
        if (cd.getSpam().booleanValue())
        {
           HttpSession session = req.getSession();
           session.setAttribute(
              RollerSession.ERROR_MESSAGE, COMMENT_SPAM_MSG);
          ret = true;
        }        
        return ret;
    }

    //-----------------------------------------------------------------------

    // Email notification

    // agangolli: Incorporated suggested changes from Ken Blackler, with server-wide configurable options
    // TODO: Make the addressing options configurable on a per-website basis.

    private static final String EMAIL_ADDR_REGEXP = "^.*@.*[.].{2,}$";

    // Servlet init params that control how messages are addressed server-wide.  These default to false for old behavior
    // Controls whether the owner and commenters get separate messages (owner's message contains a link to the entry edit page).
    private static final String SEPARATE_OWNER_MSG_PARAM = CommentServlet.class.getName() + ".separateOwnerMessage";
    // Controls whether the commenters addresses are placed in a Bcc header or a visible address field
    private static final String HIDE_COMMENTER_ADDRESSES_PARAM = CommentServlet.class.getName() + ".hideCommenterAddresses";


    /**
     * Send email notification of comment.
     */
    private void sendEmailNotification(
        HttpServletRequest request,
        RollerRequest rreq, 
        WeblogEntryData wd,
        CommentData cd,
        UserData user, 
        List comments) throws MalformedURLException
    {
        RollerContext rc = RollerContext.getRollerContext(request);
        ResourceBundle resources = ResourceBundle.getBundle(
            "ApplicationResources",LanguageUtil.getViewLocale(request));
        UserManager userMgr = null;
        WebsiteData site = null;
        try
        {
            userMgr = RollerContext.getRoller(request).getUserManager();
            site = userMgr.getWebsite(user.getUserName());
        }
        catch (RollerException re)
        {
            re.printStackTrace();
            mLogger.error(
              "Couldn't get UserManager from RollerContext", re.getRootCause());
        }

        // Send e-mail to owner and subscribed users (if enabled)
        boolean notify = RollerRuntimeConfig.getBooleanProperty("users.comments.emailnotify");
        if (notify && site.getEmailComments().booleanValue())
        {
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
            
            // Get all the subscribers to this comment thread
            Set subscribers = new TreeSet();
            for (Iterator it = comments.iterator(); it.hasNext();)
            {
                CommentData comment = (CommentData) it.next();
                if (!StringUtils.isEmpty(comment.getEmail()))
                {
	                // If user has commented twice, 
	                // count the most recent notify setting
	                if (comment.getNotify().booleanValue())
	                {
	                    // only add those with valid email
	                    if (comment.getEmail().matches(EMAIL_ADDR_REGEXP))
	                    {
	                        subscribers.add(comment.getEmail());
	                    }
	                }
	                else
	                {
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
                        
            if (!escapeHtml)
            {
                msg.append("<html><body style=\"background: white; ");
                msg.append(" color: black; font-size: 12px\">");
            }

            if (!StringUtils.isEmpty(cd.getName()))
            {
                msg.append(cd.getName() + " " 
                           + resources.getString("email.comment.wrote")+": ");
            }
            else
            {
                msg.append(resources.getString("email.comment.anonymous")+": ");
            }

            msg.append((escapeHtml) ? "\n\n" : "<br /><br />");
            msg.append(cd.getContent());
            msg.append((escapeHtml) ? "\n\n----\n"
                    : "<br /><br /><hr /><span style=\"font-size: 11px\">");
            msg.append(resources.getString("email.comment.respond") + ": ");
            msg.append((escapeHtml) ? "\n" : "<br />");

            String rootURL = rc.getAbsoluteContextUrl(request);
            if (rootURL == null || rootURL.trim().length()==0)
            {
            	rootURL = RequestUtils.serverURL(request) + request.getContextPath();
            }

            // Build link back to comment
            
            StringBuffer commentURL = new StringBuffer(rootURL);
            commentURL.append("/comments/");
            commentURL.append(rreq.getUser().getUserName());
            
            PageData page = rreq.getPage();
            if (page == null)
            {
                commentURL.append("?entry=");
            }
            else
            {
                commentURL.append("/").append(page.getLink()).append("/");
            }

            commentURL.append(wd.getAnchor());
	
            if (escapeHtml) 
            {
                msg.append(commentURL.toString());
            } 
            else 
            {
             msg.append("<a href=\""+commentURL+"\">"+commentURL+"</a></span>");
            }
            
            ownermsg.append(msg);

             // add link to weblog edit page so user can login to manage comments
            ownermsg.append((escapeHtml) ? "\n\n----\n" :
                    "<br /><br /><hr /><span style=\"font-size: 11px\">");
            ownermsg.append("Link to comment management page:");            
            ownermsg.append((escapeHtml) ? "\n" : "<br />");
            
            StringBuffer deleteURL = new StringBuffer(rootURL);
			deleteURL.append("/editor/weblog.do?method=edit&entryid="+wd.getId());            
            
            if (escapeHtml) 
            {
                 ownermsg.append(deleteURL.toString());
            } 
            else 
            {
                 ownermsg.append(
                  "<a href=\"" + deleteURL + "\">" + deleteURL + "</a></span>");
                 msg.append("</Body></html>");
                 ownermsg.append("</Body></html>");
            }
            
            String subject = null;
            if ((subscribers.size() > 1) || 
                (StringUtils.equals(cd.getEmail(), user.getEmailAddress())))
            {
                subject= "RE: "+resources.getString("email.comment.title")+": ";
            }
            else
            {
                subject = resources.getString("email.comment.title") + ": ";
            }
            subject += wd.getTitle();

            //------------------------------------------
            // --- Send message to email recipients
            try
            {
                javax.naming.Context ctx = (javax.naming.Context)
                    new InitialContext().lookup("java:comp/env");
                Session session = (Session)ctx.lookup("mail/Session");
                boolean isHtml = !escapeHtml;
                if (separateMessages)
                {
                    // Send separate messages to owner and commenters
                    sendMessage(session, from,
                        new String[]{user.getEmailAddress()}, null, null, subject, ownermsg.toString(), isHtml);
                    if (commenterAddrs.length > 0)
                    {
                        // If hiding commenter addrs, they go in Bcc: otherwise in the To: of the second message
                        String[] to = hideCommenterAddrs ? null : commenterAddrs;
                        String[] bcc = hideCommenterAddrs ? commenterAddrs : null;
                        sendMessage(session, from, to, null, bcc, subject, msg.toString(), isHtml);

                    }
                }
                else
                {
                    // Single message.  User in To: header, commenters in either cc or bcc depending on hiding option
                    String[] cc = hideCommenterAddrs ? null : commenterAddrs;
                    String[] bcc = hideCommenterAddrs ? commenterAddrs : null;
                    sendMessage(session, from, new String[]{user.getEmailAddress()}, cc, bcc, subject,
                        ownermsg.toString(), isHtml);
                }
            }
            catch (javax.naming.NamingException ne)
            {
                mLogger.error("Unable to lookup mail session.  Check configuration.  NamingException: " + ne.getMessage());
            }
            catch (Exception e)
            {
                mLogger.warn("Exception sending comment mail: " + e.getMessage());
                // This will log the stack trace if debug is enabled
                if (mLogger.isDebugEnabled())
                {
                    mLogger.debug(e);
                }
            }

        } // if email enabled
    }

    // This is somewhat ridiculous, but avoids duplicating a bunch of logic in the already messy sendEmailNotification
    private void sendMessage(Session session, String from, String[] to, String[] cc, String[] bcc, String subject,
                             String msg, boolean isHtml) throws MessagingException
    {
        if (isHtml)
            MailUtil.sendHTMLMessage(session, from, to, cc, bcc, subject, msg);
        else
            MailUtil.sendTextMessage(session, from, to, cc, bcc, subject, msg);
    }

    /* old method not used anymore -- Allen G
    private boolean getBooleanContextParam(String paramName, boolean defaultValue) {
        String paramValue = getServletContext().getInitParameter(paramName);
        if (paramValue == null) return defaultValue;
        return Boolean.valueOf(paramValue).booleanValue();
    }
    */
}

