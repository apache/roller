
package org.roller.presentation.weblog.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.RequestUtils;
import org.roller.RollerException;
import org.roller.RollerPermissionsException;
import org.roller.model.IndexManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.RollerSpellCheck;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.roller.presentation.MainPageAction;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.presentation.velocity.PageHelper;
import org.roller.presentation.weblog.formbeans.WeblogEntryFormEx;
import org.roller.util.MailUtil;
import org.roller.util.Utilities;

import com.swabunga.spell.event.SpellCheckEvent;
import org.roller.model.RollerFactory;


/////////////////////////////////////////////////////////////////////////////
/**
 * Supports Weblog Entry form actions edit, remove, update, etc.
 *
 * @struts.action name="weblogEntryFormEx" path="/editor/weblog"
 *     scope="request" parameter="method"
 *  
 * @struts.action-forward name="weblogEdit.page" path=".WeblogEdit"
 * @struts.action-forward name="weblogEntryRemove.page" path=".WeblogEntryRemove"
 */
public final class WeblogEntryFormAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(WeblogEntryFormAction.class);
    
    //-----------------------------------------------------------------------
    /**
     * Allow user to create a new weblog entry.
     */
    public ActionForward create(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("weblogEdit.page");
        try
        {
            RollerSession rses = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (rreq.getWebsite() != null 
                    && rses.isUserAuthorized(rreq.getWebsite()))
            {
                WeblogEntryFormEx form = (WeblogEntryFormEx)actionForm; 
                form.initNew(request, response);
                form.setCreatorId(rses.getAuthenticatedUser().getId());
                form.setWebsiteId(rreq.getWebsite().getId()); 
                
                request.setAttribute("model",
                        new WeblogEntryPageModel(request, response, mapping,
                                (WeblogEntryFormEx)actionForm,
                                WeblogEntryPageModel.EDIT_MODE));
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            request.getSession().getServletContext().log("ERROR",e);
            throw new ServletException(e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    /**
     * Allow user to edit a weblog entry.
     */
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("weblogEdit.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            WeblogEntryData entry = rreq.getWeblogEntry();           
            WeblogEntryFormEx form = (WeblogEntryFormEx)actionForm;
            if (entry == null && form.getId() != null)
            {
                entry = wmgr.retrieveWeblogEntry(form.getId());
            }
            if (entry == null)
            {   
                ResourceBundle resources = ResourceBundle.getBundle(
                    "ApplicationResources", request.getLocale());
                request.setAttribute("javax.servlet.error.message", 
                    resources.getString("weblogEntry.notFound"));  
                forward = mapping.findForward("error");
            }                
            else if (rses.isUserAuthorizedToAuthor(entry.getWebsite()) 
              || (rses.isUserAuthorized(entry.getWebsite()) && entry.isDraft()))
            {
                form.copyFrom(entry, request.getLocale());
                WeblogEntryPageModel pageModel = new WeblogEntryPageModel(
                    request, response, mapping, form, 
                        WeblogEntryPageModel.EDIT_MODE);
                pageModel.setWebsite(entry.getWebsite());
                request.setAttribute("model", pageModel);
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            request.getSession().getServletContext().log("ERROR",e);
            throw new ServletException(e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    public ActionForward preview(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {   
        // we need to save any new entries before Previewing
        WeblogEntryFormEx form = (WeblogEntryFormEx)actionForm;
        if (form.getId() == null) 
        {
            save(mapping, actionForm, request, response);
        }
        return display(WeblogEntryPageModel.PREVIEW_MODE, 
                    mapping, actionForm, request, response);
    }

    //-----------------------------------------------------------------------
    public ActionForward returnToEditMode(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        return display(WeblogEntryPageModel.EDIT_MODE, 
                    mapping, actionForm, request, response);
    }

    //-----------------------------------------------------------------------
    private ActionForward display(
        WeblogEntryPageModel.PageMode mode,
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("weblogEdit.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = 
              RollerSession.getRollerSession(request);
            WeblogEntryPageModel pageModel = new WeblogEntryPageModel(
              request, response, mapping, (WeblogEntryFormEx)actionForm, mode);
            if (rollerSession.isUserAuthorizedToAuthor(
                    pageModel.getWeblogEntry().getWebsite()))
            {
                request.setAttribute("model", pageModel);                   
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            request.getSession().getServletContext().log("ERROR",e);
            throw new ServletException(e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    /**
     * Saves weblog entry and flushes page cache so that new entry will appear 
     * on users weblog page.
     */
    public ActionForward save(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException {
        
        ActionForward forward = mapping.findForward("weblogEdit.page");
        ActionMessages uiMessages = new ActionMessages();
        try {
            WeblogEntryFormEx  form = (WeblogEntryFormEx)actionForm;
            Roller           roller = RollerFactory.getRoller();
            RollerSession      rses = RollerSession.getRollerSession(request);
            UserManager     userMgr = roller.getUserManager();
            WeblogManager weblogMgr = roller.getWeblogManager();
            UserData           ud  = userMgr.retrieveUser(form.getCreatorId());
            WebsiteData       site = userMgr.retrieveWebsite(form.getWebsiteId());
            WeblogEntryData  entry = null;
            
            if ( rses.isUserAuthorizedToAuthor(site) 
                 || (rses.isUserAuthorized(site) 
                    && !form.getStatus().equals(WeblogEntryData.PUBLISHED) )) { 
                
                ActionErrors errors = validateEntry(null, form);
                if (errors.size() > 0) {
                    saveErrors(request, errors);
                    request.setAttribute("model",
                        new WeblogEntryPageModel(request, response, mapping,
                                (WeblogEntryFormEx)actionForm,
                                WeblogEntryPageModel.EDIT_MODE));
                    return forward;                    
                }
                
                if (form.getId() == null || form.getId().trim().length()==0) {
                    entry = new WeblogEntryData();  
                    entry.setCreator(ud);
                    entry.setWebsite( site );
                } else {
                    entry = weblogMgr.retrieveWeblogEntry(form.getId());
                }
                
                mLogger.debug("setting update time now");
                form.setUpdateTime(new Timestamp(new Date().getTime()));
                
                if("PUBLISHED".equals(form.getStatus()) &&
                        "0/0/0".equals(form.getDateString())) {
                    mLogger.debug("setting pubtime now");
                    
                    /* NOTE: the wf.copyTo() method will override this value
                     * based on data submitted with the form if that data is
                     * not null.  check the method to verify.
                     *
                     * this means that setting the pubtime here only takes
                     * effect if the entry is being published for the first
                     * time.
                     */
                    form.setPubTime(form.getUpdateTime());
                }
                
                mLogger.debug("copying submitted form data to entry object");
                form.copyTo(entry, request.getLocale(),request.getParameterMap());

                // Fetch MediaCast content type and length
                mLogger.debug("Checking MediaCast attributes");
                if (!checkMediaCast(entry)) {
                   mLogger.debug("Invalid MediaCast attributes");
                   uiMessages.add(null, 
                     new ActionMessage("weblogEdit.message.mediaCastProblem"));
                }
                else if (mLogger.isDebugEnabled()) {
                   mLogger.debug("Invalid MediaCast attributes");
                }

                // Store value object (creates new or updates existing)
                entry.setUpdateTime(new Timestamp(new Date().getTime()));
                mLogger.debug("Saving entry");
                entry.save();
                RollerFactory.getRoller().commit();

                mLogger.debug("Populating form");
                form.copyFrom(entry, request.getLocale());
                               
                request.setAttribute(
                        RollerRequest.WEBLOGENTRYID_KEY, entry.getId());
                 
                // Reindex entry, flush caches, etc.
                reindexEntry(RollerFactory.getRoller(), entry);
                mLogger.debug("Removing from cache");
                RollerRequest rreq = RollerRequest.getRollerRequest(request);
                PageCacheFilter.removeFromCache(request, entry.getWebsite());
                MainPageAction.flushMainPageCache();

                // Clean up session objects we used
                HttpSession session = request.getSession(true);
                session.removeAttribute("spellCheckEvents");
                session.removeAttribute("entryText");
                
                // Load up request with data for view
                request.setAttribute("model",
                        new WeblogEntryPageModel(request, response, mapping,
                                (WeblogEntryFormEx)actionForm,
                                WeblogEntryPageModel.EDIT_MODE));
                
                if (!rses.isUserAuthorizedToAuthor(site) && 
                        rses.isUserAuthorized(site) && entry.isPending()) {
                    // implies that entry just changed to pending
                    notifyWebsiteAuthorsOfPendingEntry(request, entry);
                    uiMessages.add(null,
                        new ActionMessage("weblogEdit.submitedForReview")); 
                    
                    // so clear entry from editor
                    actionForm = new WeblogEntryFormEx();
                    request.setAttribute(mapping.getName(), actionForm);
                    forward = create(mapping, actionForm, request, response);
                } else {
                    uiMessages.add(null, 
                        new ActionMessage("weblogEdit.changesSaved"));
                }
                saveMessages(request, uiMessages);               
                mLogger.debug("operation complete");
                
            } else {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (RollerPermissionsException e) {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
        return forward;
    }
    
    /**
     * Inform authors and admins of entry's website that entry is pending.
     * @param entry
     * @throws RollerException
     * @throws MalformedURLException
     */
    private void notifyWebsiteAuthorsOfPendingEntry(
            HttpServletRequest request, WeblogEntryData entry) 
    {
        try
        {
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            javax.naming.Context ctx = (javax.naming.Context)
                new InitialContext().lookup("java:comp/env");
            Session mailSession = 
                (Session)ctx.lookup("mail/Session");
            if (mailSession != null)
            {
                String userName = entry.getCreator().getUserName();
                String from = entry.getCreator().getEmailAddress();
                String cc[] = new String[] {from};
                String bcc[] = new String[0];
                String to[];
                String subject;
                String content;
                
                // list of enabled website authors and admins
                ArrayList reviewers = new ArrayList();
                List websiteUsers = 
                        umgr.getUsers(entry.getWebsite(), Boolean.TRUE);
                
                // build list of reviewers (website users with author permission)
                Iterator websiteUserIter = websiteUsers.iterator();
                while (websiteUserIter.hasNext())
                {
                    UserData websiteUser = (UserData)websiteUserIter.next();
                    if (entry.getWebsite().hasUserPermissions(
                            websiteUser, PermissionsData.AUTHOR)
                         && websiteUser.getEmailAddress() != null) 
                    {
                        reviewers.add(websiteUser.getEmailAddress());
                    }
                }
                to = (String[])reviewers.toArray(new String[reviewers.size()]);
                
                // Figure URL to entry edit page
                RollerContext rc = RollerContext.getRollerContext(request);
                String rootURL = rc.getAbsoluteContextUrl(request);
                if (rootURL == null || rootURL.trim().length()==0)
                {
                    rootURL = RequestUtils.serverURL(request) 
                                  + request.getContextPath();
                }               
                String editURL = rootURL 
                    + "/editor/weblog.do?method=edit&entryid=" + entry.getId();
                
                ResourceBundle resources = ResourceBundle.getBundle(
                    "ApplicationResources", request.getLocale());
                StringBuffer sb = new StringBuffer();
                sb.append(
                    MessageFormat.format(
                        resources.getString("weblogEntry.pendingEntrySubject"),
                        new Object[] {
                            entry.getWebsite().getName(), 
                            entry.getWebsite().getHandle()
                }));
                subject = sb.toString();
                sb = new StringBuffer();
                sb.append(
                    MessageFormat.format(
                        resources.getString("weblogEntry.pendingEntryContent"),
                        new Object[] { userName, userName, editURL })
                );
                content = sb.toString();
                MailUtil.sendTextMessage(
                        mailSession, from, to, cc, bcc, subject, content);
            }
        }
        catch (NamingException e)
        {
            mLogger.error("ERROR: Notification email(s) not sent, "
                    + "Roller's mail session not properly configured");
        }
        catch (MessagingException e)
        {
            mLogger.error("ERROR: Notification email(s) not sent, "
                    + "due to Roller configuration or mail server problem.");
        }
        catch (MalformedURLException e)
        {
            mLogger.error("ERROR: Notification email(s) not sent, "
                    + "Roller site URL is malformed?");
        }
        catch (RollerException e)
        {
            throw new RuntimeException(
                    "FATAL ERROR: unable to find Roller object");
        }
    }

    private boolean checkMediaCast(WeblogEntryData entry)
    {
        boolean valid = false;
        String url = entry.findEntryAttribute("att_mediacast_url");
        boolean empty = (url == null) || (url.trim().length() == 0);
        if (!empty)
        {
            // fetch MediaCast content type and length
            valid = false;
            try
            {
                mLogger.debug("Sending HTTP HEAD");
                HttpURLConnection con = (HttpURLConnection)
                    new URL(url).openConnection();
                mLogger.debug("Getting response code");
                con.setRequestMethod("HEAD");
                if (con.getResponseCode() != 200) 
                {
                    mLogger.debug("Response code indicates error");
                    mLogger.error("ERROR " 
                        + con.getResponseCode() + " return from MediaCast URL");
                    mLogger.debug(con.getContent().toString());
                }
                else if (con.getContentType()!=null 
                        && con.getContentLength()!=-1)
                {
                    mLogger.debug("Got good reponse and content info");
                    entry.putEntryAttribute(
                        "att_mediacast_type", con.getContentType());
                    entry.putEntryAttribute(
                        "att_mediacast_length", ""+con.getContentLength());
                    valid = true;
                }
            }
            catch (Exception e)
            {
                mLogger.error("ERROR checking MediaCast URL");
            }
        } 
        else 
        {
            mLogger.debug("No MediaCast specified, but that is OK");
            valid = true;
        }
        if (!valid || empty)
        {
            mLogger.debug("Removing MediaCast attributes");
            try 
            {
                entry.removeEntryAttribute("att_mediacast_url");
                entry.removeEntryAttribute("att_mediacast_type");
                entry.removeEntryAttribute("att_mediacast_length");
            }
            catch (RollerException e) 
            {
                mLogger.error("ERROR removing invalid MediaCast attributes");
            }
        }
        mLogger.debug("operation complete");
        return valid;
    }
    

    //-----------------------------------------------------------------------
    /**
     * Responds to request to remove weblog entry. Forwards user to page
     * that presents the 'are you sure?' question.
     */
    public ActionForward removeOk(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("weblogEntryRemove.page");
        try
        {
            Roller roller = RollerFactory.getRoller();
            WeblogEntryFormEx wf = (WeblogEntryFormEx)actionForm;
            WeblogEntryData wd = 
                roller.getWeblogManager().retrieveWeblogEntry(wf.getId());
            RollerSession rses = 
                RollerSession.getRollerSession(request);
            if (     rses.isUserAuthorizedToAuthor(wd.getWebsite()) 
                 || (rses.isUserAuthorized(wd.getWebsite()) && wd.isDraft()) )
            {
                wf.copyFrom(wd, request.getLocale());
                if (wd == null || wd.getId() == null)
                {
                    ResourceBundle resources = ResourceBundle.getBundle(
                        "ApplicationResources", request.getLocale());
                    request.setAttribute("javax.servlet.error.message", 
                        resources.getString("weblogEntry.notFound"));  
                    forward = mapping.findForward("error");
                }
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    /**
     * Responds to request from the 'are you sure you want to remove?' page.
     * Removes the specified weblog entry and flushes the cache.
     */
    public ActionForward remove(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        try
        {
            WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
            WeblogEntryData wd = 
                mgr.retrieveWeblogEntry(request.getParameter("id"));
            RollerSession rses = 
                RollerSession.getRollerSession(request);
            if (     rses.isUserAuthorizedToAuthor(wd.getWebsite()) 
                 || (rses.isUserAuthorized(wd.getWebsite()) && wd.isDraft()) )
            {
                // Flush the page cache
                PageCacheFilter.removeFromCache(request, wd.getWebsite());
                
				// remove the index for it
                wd.setStatus(WeblogEntryData.DRAFT);
		        reindexEntry(RollerFactory.getRoller(), wd);

                // remove entry itself
                wd.remove();
                RollerFactory.getRoller().commit();

				// flush caches
                PageCacheFilter.removeFromCache(request, wd.getWebsite());
                MainPageAction.flushMainPageCache();
                
                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(null, 
                    new ActionMessage("weblogEdit.entryRemoved"));
                saveMessages(request, uiMessages);
                
                RollerRequest.getRollerRequest().setWebsite(wd.getWebsite());
            }
            else
            {
                return mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
        
        // need to reset all values to empty (including 'id')
        actionForm = new WeblogEntryFormEx();
        request.setAttribute(mapping.getName(), actionForm);
        return create(mapping, actionForm, request, response);
    }

    //-----------------------------------------------------------------------
    public ActionForward correctSpelling(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        try
        {
            WeblogEntryFormEx wf = (WeblogEntryFormEx)actionForm;
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            WebsiteData site = umgr.retrieveWebsite(wf.getWebsiteId());
            RollerSession rses = 
                RollerSession.getRollerSession(request);
            if (rses.isUserAuthorizedToAuthor(site))
            {
                HttpSession session = request.getSession(true);
                // misspelt words have been submitted
                if (wf.getReplacementWords() != null &&
                    wf.getReplacementWords().length > 0)
                {
                    // replace misspelt words with chosen replacement
                    String[] replacementWords = wf.getReplacementWords();
                    StringBuffer entryText = new StringBuffer(wf.getText());

                    ArrayList events =
                        (ArrayList) session.getAttribute("spellCheckEvents");
                    SpellCheckEvent event = null;
                    String oldWord = null;
                    String newWord = null;
                    int start = -1;
                    int end = -1;
                    int count = replacementWords.length;
                    for(ListIterator it=events.listIterator(events.size());
                        it.hasPrevious();)
                    {
                        event = (SpellCheckEvent)it.previous();
                        oldWord = event.getInvalidWord();
                        newWord = replacementWords[ --count ];
                        if (!oldWord.equals(newWord))
                        {
                            start = event.getWordContextPosition();
                            end = start + oldWord.length();
                            entryText.replace( start, end, newWord );
                        }
                    }
                    wf.setText( entryText.toString() );

                    return save(mapping, wf, request, response);
                }
                else 
                {
                    return display(WeblogEntryPageModel.EDIT_MODE, 
                                mapping, actionForm, request, response);
                }
            }
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
        return mapping.findForward("access-denied");
    }    
    
    //-----------------------------------------------------------------------
    public ActionForward spellCheck(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("weblogEdit.page");
        try
        {
            WeblogEntryFormEx wf = (WeblogEntryFormEx)actionForm;
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            WebsiteData site = umgr.retrieveWebsite(wf.getWebsiteId());
            RollerSession rses = 
                RollerSession.getRollerSession(request);
            if ( rses.isUserAuthorizedToAuthor(site) )
            {
                HttpSession session = request.getSession(true);
                
                // we need to save any new entries before SpellChecking
                if (wf.getId() == null) 
                {
                    save(mapping, actionForm, request, response);
                }

                // pass the submitted entry text through the spellchecker
                ArrayList words =
                    RollerSpellCheck.getSpellingErrors( wf.getText() );
                session.setAttribute("spellCheckEvents", words);

                request.setAttribute("model", 
                    new WeblogEntryPageModel(
                       request, response, mapping, 
                       (WeblogEntryFormEx)actionForm,
                       WeblogEntryPageModel.SPELL_MODE, words));
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    /**
     * Update selected comments: delete and/or mark as spam.
     */
    public ActionForward updateComments(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("weblogEdit.page");
        ActionErrors errors = new ActionErrors();
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        RollerSession rollerSession = RollerSession.getRollerSession(request);
        try
        {
            WeblogEntryData wd = rreq.getWeblogEntry();
            if ( rollerSession.isUserAuthorizedToAuthor(wd.getWebsite()))
            {
                if (wd == null || wd.getId() == null)
                {
                    ResourceBundle resources = ResourceBundle.getBundle(
                        "ApplicationResources", request.getLocale());
                    request.setAttribute("javax.servlet.error.message", 
                        resources.getString("weblogEntry.notFound"));  
                    forward = mapping.findForward("error");                    
                }
                else 
                {
                    WeblogEntryFormEx form = (WeblogEntryFormEx)actionForm;

                    // If form indicates that comments should be deleted, delete
                    WeblogManager mgr= RollerFactory.getRoller().getWeblogManager();
                    String[] deleteIds = form.getDeleteComments();
                    if (deleteIds != null && deleteIds.length > 0)
                    {
                        mgr.removeComments( deleteIds );
                    }

                    List comments = mgr.getComments(wd.getId(), false); // spam too
                    if (form.getSpamComments() != null)
                    {
                        // comments marked as spam
                        List spamIds = Arrays.asList(form.getSpamComments());

                        // iterate over all comments, check each to see if
                        // is in the spamIds list.  If so, mark it as spam.
                        Iterator it = comments.iterator();
                        while (it.hasNext())
                        {
                            CommentData comment = (CommentData)it.next();
                            if (spamIds.contains(comment.getId()))
                            {
                                comment.setSpam(Boolean.TRUE);                            
                            }
                            else 
                            {
                                comment.setSpam(Boolean.FALSE);
                            }
                            comment.save();
                        }
                    }

                    RollerFactory.getRoller().commit();

                    reindexEntry(RollerFactory.getRoller(), wd);

                    request.setAttribute("model",
                            new WeblogEntryPageModel(request, response, mapping, 
                                    (WeblogEntryFormEx)actionForm,
                                    WeblogEntryPageModel.EDIT_MODE));
                }
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            forward = mapping.findForward("error");

            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.edit.comment", e.toString()));
            saveErrors(request,errors);

            mLogger.error(getResources(request).getMessage("error.edit.comment")
                + e.toString(),e);
        }
        return forward;
    }

    /**
    *
    */
   public ActionForward sendTrackback(
                   ActionMapping mapping, 
                   ActionForm actionForm,
                   HttpServletRequest request, 
                   HttpServletResponse response) throws RollerException
   {
       ActionForward forward = mapping.findForward("weblogEdit.page");
       ActionErrors errors = new ActionErrors();
       WeblogEntryData entry = null;
       try
       {
           WeblogEntryFormEx form = (WeblogEntryFormEx)actionForm;
           String entryid = form.getId();
           if ( entryid == null )
           {
               entryid = 
                   request.getParameter(RollerRequest.WEBLOGENTRYID_KEY);
           }

           RollerContext rctx= RollerContext.getRollerContext(request);
           WeblogManager wmgr= RollerFactory.getRoller().getWeblogManager();
           entry = wmgr.retrieveWeblogEntry(entryid);

           RollerSession rses = RollerSession.getRollerSession(request);
           if (rses.isUserAuthorizedToAuthor(entry.getWebsite()))
           {
               String title = entry.getTitle();

               // Run entry through registered PagePlugins
               PageHelper pageHelper = 
                   PageHelper.createPageHelper(request, response);
               pageHelper.setSkipFlag(true); // don't process ReadMorePlugin
               // we have to wrap the entry for rendering because the
               // page helper requires wrapped objects
               String excerpt = pageHelper.renderPlugins(WeblogEntryDataWrapper.wrap(entry));
               excerpt = StringUtils.left( Utilities.removeHTML(excerpt),255 );

               String url = rctx.createEntryPermalink(entry, request, true);
               String blog_name = entry.getWebsite().getName();

               if (form.getTrackbackUrl() != null)
               {
                   try
                   {
                       // Construct data

                       String data = URLEncoder.encode("title", "UTF-8")
                           +"="+URLEncoder.encode(title, "UTF-8");

                       data += ("&" + URLEncoder.encode("excerpt", "UTF-8")
                                 +"="+URLEncoder.encode(excerpt,"UTF-8"));

                       data += ("&" + URLEncoder.encode("url", "UTF-8")
                                 +"="+URLEncoder.encode(url,"UTF-8"));

                       data += ("&" + URLEncoder.encode("blog_name", "UTF-8")
                                 +"="+URLEncoder.encode(blog_name,"UTF-8"));

                       // Send data
                       URL tburl = new URL(form.getTrackbackUrl());
                       URLConnection conn = tburl.openConnection();
                       conn.setDoOutput(true);

                       OutputStreamWriter wr =
                           new OutputStreamWriter(conn.getOutputStream());
                       BufferedReader rd = null;
                       try
                       {
                           wr.write(data);
                           wr.flush();
    
                           // Get the response
                           rd = new BufferedReader(new InputStreamReader(
                               conn.getInputStream()));
    
                           String line;
                           StringBuffer resultBuff = new StringBuffer();
                           while ((line = rd.readLine()) != null)
                           {
                               resultBuff.append(
                                   Utilities.escapeHTML(line, true));
                               resultBuff.append("<br />");
                           }
                           
                           ActionMessages resultMsg = new ActionMessages();
                           resultMsg.add(ActionMessages.GLOBAL_MESSAGE,
                               new ActionMessage("weblogEdit.trackbackResults", 
                               resultBuff));
                           saveMessages(request, resultMsg);
                       }
                       finally
                       {
                           wr.close();
                           rd.close();
                       }
                   }
                   catch (IOException e)
                   {
                       errors.add(ActionErrors.GLOBAL_ERROR,
                           new ActionError("error.trackback",e));
                   }
               }
               else
               {
                   errors.add(ActionErrors.GLOBAL_ERROR,
                           new ActionError("error.noTrackbackUrlSpecified"));
               }
               
               form.setTrackbackUrl(null);
           }
           else
           {
               forward = mapping.findForward("access-denied");
           }
       }
       catch (Exception e) // unexpected
       {
           mLogger.error(e);
           String msg = e.getMessage();
           if ( msg == null )
           {
               msg = e.getClass().getName();
           }
           errors.add(ActionErrors.GLOBAL_ERROR,
               new ActionError("error.general",msg));
       }

       if ( !errors.isEmpty() )
       {
           saveErrors(request, errors);
       }
       
       request.setAttribute("model",
                       new WeblogEntryPageModel(request, response, mapping, 
                               (WeblogEntryFormEx)actionForm,
                               WeblogEntryPageModel.EDIT_MODE));

       return forward;
    }

    //-----------------------------------------------------------------------
    public ActionForward cancel(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        return (mapping.findForward("weblogEdit"));
    }

    //-----------------------------------------------------------------------
    public ActionForward unspecified(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        return create(mapping, actionForm, request, response);
    }
    
    /**
     * Attempts to remove the Entry from the Lucene index and
     * then re-index the Entry if it is Published.  If the Entry
     * is being deleted then mark it published = false.
     * @param entry
     */
    private void reindexEntry(Roller roller, WeblogEntryData entry) 
    throws RollerException
    {
         IndexManager manager = roller.getIndexManager();
        
        // remove entry before (re)adding it, or in case it isn't Published
        //manager.removeEntryIndexOperation(entry); 
        
        // if published, index the entry
        if (entry.isPublished()) 
        {
            manager.addEntryReIndexOperation(entry);
        }
    }

    public ActionErrors validateEntry(ActionErrors errors, WeblogEntryFormEx form) {
        if (errors == null) errors = new ActionErrors();
        if (StringUtils.isEmpty(form.getTitle())) {
            errors.add(null, new ActionError("weblogEdit.error.incompleteEntry"));
        }
        return errors;
    } 
                            

}

