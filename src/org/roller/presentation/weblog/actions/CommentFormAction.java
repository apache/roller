package org.roller.presentation.weblog.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.RequestUtils;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.PageData;
import org.roller.pojos.RollerConfig;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.LanguageUtil;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.pagecache.PageCache;
import org.roller.presentation.weblog.formbeans.CommentFormEx;
import org.roller.presentation.weblog.search.IndexManager;
import org.roller.presentation.weblog.search.operations.AddEntryOperation;
import org.roller.presentation.weblog.search.operations.RemoveEntryOperation;
import org.roller.util.CommentSpamChecker;
import org.roller.util.MailUtil;
import org.roller.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/////////////////////////////////////////////////////////////////////////////
/**
 * Comment form action supports displaying, posting, previewing, and
 * deleting comments.
 *
 * @struts.action name="commentFormEx" path="/comment"
 *  scope="session" parameter="method"
 */
public class CommentFormAction extends DispatchAction
{
    private static final String COMMENT_SPAM_MSG =
                    "Your comment has been recognized as " +
                    "<a href='http://www.jayallen.org/projects/mt-blacklist/'>" +
                    "Comment Spam</a> and rejected.";
	private static Log mLogger =
        LogFactory.getFactory().getInstance(CommentFormAction.class);

    //------------------------------------------------------------------------

    /**
     * Done.
     */
    public ActionForward done(ActionMapping mapping, ActionForm actionForm,
            HttpServletRequest req, HttpServletResponse res)
    throws Exception
    {
        return mapping.findForward("editWeblog");
    }

    //-----------------------------------------------------------------------
    /**
     * load the comments for weblogEntryId into request attribute
     **/
	private void loadComments(
		HttpServletRequest request,
		String weblogEntryId,
		WeblogManager mgr,
        boolean noSpam)
		throws RollerException
	{
		List comments = mgr.getComments( weblogEntryId, noSpam );
		request.setAttribute("blogComments", comments);
	}

    //-----------------------------------------------------------------------
    /**
     * Load comments and blog entry objects and forward to comment display
     * edit page.
     */
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("comment.page");
        ActionErrors errors = new ActionErrors();
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        try
        {
            WeblogEntryData wd = rreq.getWeblogEntry();
            if (wd == null || wd.getId() == null)
            {
              // this is likely to be a fraudulent attempt
              throw new NullPointerException("Unable to find WeblogEntry for "+
                    request.getParameter(RollerRequest.WEBLOGENTRYID_KEY));
            }
            request.setAttribute("blogEntry", wd);

            CommentFormEx cf = (CommentFormEx)actionForm;
            cf.setWeblogEntry( wd );

            WeblogManager mgr = rreq.getRoller().getWeblogManager();
            loadComments(request, wd.getId(), mgr, true); // no spam
        }
        catch (Exception e)
        {
            forward = mapping.findForward("error");

            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.edit.comment", e.toString()));
            saveErrors(request,errors);

            if (e instanceof NullPointerException)
            {
                // from a fraudulent attempt - lower logging level
                mLogger.warn(getResources(request).getMessage("error.edit.comment")
                    + e.toString(),e);
            }
            else
            {
                mLogger.error(getResources(request).getMessage("error.edit.comment")
                    + e.toString(),e);
            }
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    /**
     * Saves comment and forward to the comment-edit page.
     */
    public ActionForward update(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = null; // we'll compute this later

        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        ActionErrors errors = new ActionErrors();

        try
        {
            // Get weblog entry object, put in page context
            WeblogEntryData wd = rreq.getWeblogEntry();
            if (wd == null || wd.getId() == null)
            {
                throw new NullPointerException(
                    "Unable to find WeblogEntry for "+
                    request.getParameter(RollerRequest.WEBLOGENTRYID_KEY));
            }
            if ( !wd.getWebsite().getAllowComments().booleanValue() ) {
                throw new ServletException("ERROR comments disabled by user");
            }
            request.setAttribute("blogEntry", wd);

            // get the User to which the blog belongs
            UserData user = rreq.getUser();

            // Save comment
            WeblogManager mgr = rreq.getRoller().getWeblogManager();
            CommentFormEx cf = (CommentFormEx)actionForm;
            CommentData cd = new CommentData();
            cf.copyTo(cd, request.getLocale());
            cd.setWeblogEntry(wd);
            cd.setRemoteHost(request.getRemoteHost());

            cd.setPostTime(new java.sql.Timestamp( System.currentTimeMillis()));
            if (cd.getSpam() == null) 
            {
            	cd.setSpam(Boolean.FALSE);
            }
            cd.save();
            rreq.getRoller().commit();

            // check for spam
            testCommentSpam(cd, request);
            if (cd.getSpam().booleanValue())
            {
                errors.add(ActionErrors.GLOBAL_ERROR,
                           new ActionError("error.update.comment", COMMENT_SPAM_MSG));
                saveErrors(request,errors);                
            }
            else
            {
                reindexEntry(wd);
            }

			// Refresh user's entries in page cache
			PageCache.removeFromCache( request, user );

			// Put array of comments in context
			List comments = mgr.getComments( wd.getId() );
			request.setAttribute("blogComments", comments);

			// Remove the obsolete form bean
			request.removeAttribute(mapping.getAttribute());

			// MR: Added functionality to e-mail comments
            sendEmailNotification(request, rreq, wd, cd, user, comments);

            if ( request.getAttribute("updateFromPage")!=null )
            {
                forward = updateFromPageForward(request, rreq, wd, user);
            }
            else
            {
                forward = mapping.findForward("comment.page");
            }

            // Put array of comments in context
            loadComments(request, wd.getId(), mgr, true); // no spam

            cf.reset(mapping,request);
        }
        catch (Exception e)
        {
            forward = mapping.findForward("error");

            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.update.comment", e.toString()));
            saveErrors(request,errors);

            mLogger.error(getResources(request)
                .getMessage("error.update.comment") + e.toString(),e);
        }
        return forward;
    }

    /**
     * Re-index the WeblogEntry so that the new comment gets indexed
     * along with it.
     * 
     * @param wd
     */
    private void reindexEntry(WeblogEntryData entry)
    {
        IndexManager manager =
            RollerContext.getRollerContext(
                RollerContext.getServletContext()).getIndexManager();
        
        // remove entry before (re)adding it, or in case it isn't Published
        RemoveEntryOperation removeOp = new RemoveEntryOperation(entry);
        manager.executeIndexOperationNow(removeOp); 
        
        // if published, index the entry
        if (entry.getPublishEntry() == Boolean.TRUE) 
        {
            AddEntryOperation addEntry = new AddEntryOperation(entry);
            manager.scheduleIndexOperation(addEntry);
        }
    }

    private void sendEmailNotification(
        HttpServletRequest request,
        RollerRequest rreq,
        WeblogEntryData wd,
        CommentData cd,
        UserData user,
        List comments)
        throws MalformedURLException
    {
        RollerContext rc = RollerContext.getRollerContext( request );
        RollerConfig rollerConfig = rc.getRollerConfig();
        
		MessageResources resources = getResources(request);
		Locale viewLocale = LanguageUtil.getViewLocale(request);
        
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
            mLogger.error("Couldn't get UserManager from RollerContext", re.getRootCause());
        }
        
        // Send e-mail to owner (if enabled) and subscribed users
        if ( rollerConfig.getEmailComments().booleanValue() &&
                site.getEmailComments().booleanValue() )
        {
            
            String defaultFromEmail = (StringUtils.isEmpty(site.getEmailFromAddress())) ? 
                                       user.getEmailAddress() : site.getEmailFromAddress();
            
        	String from = (StringUtils.isEmpty(cd.getEmail()))
        					? defaultFromEmail : cd.getEmail();

        	// get all the subscribers to this comment thread
        	ArrayList subscribers = new ArrayList();
        	for (Iterator it = comments.iterator(); it.hasNext();) 
        	{
        		CommentData comment = (CommentData) it.next();
        		// only add the subscriber if they don't already exist
        		if (   ! StringUtils.isEmpty(comment.getEmail())
        			&& ! subscribers.contains(comment.getEmail())
        			&& ! StringUtils.equals(comment.getEmail(), 
        					                user.getEmailAddress())) 
        		{
        			subscribers.add(comment.getEmail());
        		}
        	}

        	// There's got to be an easier way, but I'm in a hurry ;-)
        	String[] cc = new String[subscribers.size()];
        	for (int i = 0; i < subscribers.size(); i++)
        	{
        		cc[i] = (String) subscribers.get(i);
        	}

        	StringBuffer msg = new StringBuffer();

            // determine with mime type to use for e-mail
        	boolean escapeHtml = rollerConfig.getEscapeCommentHtml().booleanValue();
        	if (!escapeHtml)
        	{
        		msg.append("<html><body style=\"background: white; ");
        		msg.append(" color: black; font-size: 12px\">");
        	}

			if (!StringUtils.isEmpty(cd.getName()))
			{
				msg.append(cd.getName() + " " + resources.getMessage(viewLocale, "email.comment.wrote") + ": ");
			}
			else
			{
				msg.append(resources.getMessage(viewLocale, "email.comment.anonymous") + ": ");
			}

        	msg.append((escapeHtml) ? "\n\n" : "<br /><br />");
        	msg.append(cd.getContent());
            msg.append((escapeHtml) ? "\n\n----\n" :
        		"<br /><br /><hr /><span style=\"font-size: 11px\">");
			msg.append(resources.getMessage(viewLocale, "email.comment.respond") + ": ");
        	msg.append((escapeHtml) ? "\n" : "<br />");

        	// build link back to comment
        	StringBuffer commentURL = new StringBuffer();
        	commentURL.append(RequestUtils.serverURL(request));
        	commentURL.append(request.getContextPath()+"/page/");
        	commentURL.append(rreq.getUser().getUserName());
        	PageData page = rreq.getPage();
        	if (page == null)
        	{
        		commentURL.append("?anchor=");
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
        		msg.append("<a href=\"" + commentURL + "\">" + commentURL + "</a></span>");
        		msg.append("</body></html>");
        	}

        	String subject = null;

			if ( (subscribers.size() > 1) ||
				 (StringUtils.equals(cd.getEmail(), user.getEmailAddress()) ))
			{
				subject = "RE: " + resources.getMessage(viewLocale, "email.comment.title") + ": ";
			}
			else
			{
				subject = resources.getMessage(viewLocale, "email.comment.title") + ": ";
			}

        	subject += wd.getTitle();

        	try
            {
        		Context ctx = (Context)
                new InitialContext().lookup("java:comp/env");
        		Session session = (Session) ctx.lookup("mail/Session");
        		if (escapeHtml)
                {
                    // signifies plain text
                    // from, to, subject, content
        			MailUtil.sendTextMessage(session, from,
        			  user.getEmailAddress(), cc,
        		      subject, msg.toString());
        		}
                else
                {
        			MailUtil.sendHTMLMessage(session, from,
        			  user.getEmailAddress(), cc,
        			  subject, msg.toString());
        		}
        	}
            catch (NamingException ne)
            {
        		log.error("NamingException: " + ne.getMessage());
        		log.warn("Looking up mail session in JNDI failed!");
        	}
            catch (MessagingException me)
            {
        		log.error("MessagingException: " + me.getMessage());
        		log.warn("Sending comments e-mail failed!");
        	}

        }

    }

    //-----------------------------------------------------------------------
    /**
     * Get the "forward" necessary to display the
     * "single entry" view.
     */
	private ActionForward updateFromPageForward(
		HttpServletRequest request,
		RollerRequest rreq,
		WeblogEntryData wd,
		UserData user)
	{
		ActionForward forward;
		PageData page = rreq.getPage();
		if (page == null)
		{
		    try
		    {
		        page = rreq.getRoller().getUserManager().
		            retrievePage(wd.getWebsite().getDefaultPageId());
		    }
		    catch (Exception ignore)
		    {
		    }
		}
		StringBuffer sb = new StringBuffer();
		sb.append("/page/");
		sb.append(user.getUserName());
		if (page == null)
		{
		    sb.append("?anchor=");
		}
		else
		{
		    request.setAttribute(RollerRequest.PAGEID_KEY, page.getId());
		    sb.append("/").append(page.getLink()).append("/");
		}
		sb.append(wd.getAnchor());
		forward = new ActionForward(sb.toString());
		return forward;
	}

    //-----------------------------------------------------------------------
    /**
	 * @param cf
	 * @return
	 */
	private void testCommentSpam(CommentData cd, HttpServletRequest req)
	{
        new CommentSpamChecker().testComment(cd,
            RollerContext.getRollerContext(req).getThreadManager());
	}

	//-----------------------------------------------------------------------
    /**
     * Saves comment and forward to the comment-edit page.
     */
    public ActionForward updateFromPage(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        request.setAttribute("updateFromPage","true");
        return update(mapping,actionForm,request,response);
    }

    //-----------------------------------------------------------------------
    /**
     * Load comment and blog entry objects and forward to comment preview page.
     */
    public ActionForward preview(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = null; // we'll compute this later

        ActionErrors errors = new ActionErrors();
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        try
        {
            WeblogEntryData wd = rreq.getWeblogEntry();
            if (wd == null || wd.getId() == null)
            {
                throw new NullPointerException(
                    "Unable to find WeblogEntry for " +
                    request.getParameter(RollerRequest.WEBLOGENTRYID_KEY));
            }
            request.setAttribute("blogEntry", wd);

            CommentFormEx cf = (CommentFormEx)actionForm;
            cf.setWeblogEntry( wd );
            cf.setPostTime(new java.sql.Timestamp(System.currentTimeMillis()));
            request.setAttribute("commentForm", cf);

            UserData user = rreq.getUser();

            if ( request.getAttribute("previewFromPage")!=null )
            {
                StringBuffer sb = new StringBuffer();
                sb.append("/page/");
                sb.append(user.getUserName());
                sb.append("?anchor=");
                sb.append(wd.getAnchor());
                forward = new ActionForward(sb.toString());
            }
            else
            {
                forward = mapping.findForward("comment.preview");
            }
        }
        catch (Exception e)
        {
            forward = mapping.findForward("error");

            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.preview.comment", e.toString()));
            saveErrors(request,errors);

            mLogger.error(
                getResources(request).getMessage("error.preview.comment"),e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    /**
     * Load comment and blog entry objects and forward to comment preview page.
     */
    public ActionForward previewFromPage(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        request.setAttribute("previewFromPage","true");
        return preview(mapping,actionForm,request,response);
    }

}

