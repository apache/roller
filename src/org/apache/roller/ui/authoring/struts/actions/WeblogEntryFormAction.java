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

package org.apache.roller.ui.authoring.struts.actions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
import org.apache.roller.RollerException;
import org.apache.roller.RollerPermissionsException;
import org.apache.roller.business.search.IndexManager;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.ui.authoring.struts.formbeans.WeblogEntryFormEx;
import org.apache.roller.util.MailUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.util.RollerMessages;
import org.apache.roller.util.RollerMessages.RollerMessage;



/////////////////////////////////////////////////////////////////////////////
/**
 * Supports Weblog Entry form actions edit, remove, update, etc.
 *
 * @struts.action name="weblogEntryFormEx" path="/roller-ui/authoring/weblog"
 *     scope="request" parameter="method"
 *
 * @struts.action-forward name="weblogEdit.page" path=".WeblogEdit"
 * @struts.action-forward name="weblogEntryRemove.page" path=".WeblogEntryRemove"
 */
public final class WeblogEntryFormAction extends DispatchAction {
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
            throws IOException, ServletException {
        ActionForward forward = mapping.findForward("weblogEdit.page");
        try {
            RollerSession rses = RollerSession.getRollerSession(request);
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (rreq.getWebsite() != null
                    && rses.isUserAuthorized(rreq.getWebsite())) {
                WeblogEntryFormEx form = (WeblogEntryFormEx)actionForm;
                form.initNew(request, response);
                form.setCreatorId(rses.getAuthenticatedUser().getId());
                form.setWebsiteId(rreq.getWebsite().getId());
                form.setAllowComments(rreq.getWebsite().getDefaultAllowComments());
                form.setCommentDays(new Integer(rreq.getWebsite().getDefaultCommentDays()));
                
                request.setAttribute("model",
                        new WeblogEntryPageModel(request, response, mapping,
                        (WeblogEntryFormEx)actionForm,
                        WeblogEntryPageModel.EDIT_MODE));
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
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
            throws IOException, ServletException {
        ActionForward forward = mapping.findForward("weblogEdit.page");
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            WeblogEntryData entry = rreq.getWeblogEntry();
            WeblogEntryFormEx form = (WeblogEntryFormEx)actionForm;
            if (entry == null && form.getId() != null) {
                entry = wmgr.getWeblogEntry(form.getId());
            }
            if (entry == null) {
                ResourceBundle resources = ResourceBundle.getBundle(
                        "ApplicationResources", request.getLocale());
                request.setAttribute("javax.servlet.error.message",
                        resources.getString("weblogEntry.notFound"));
                forward = mapping.findForward("error");
            } else if (rses.isUserAuthorized(entry.getWebsite())
            || (rses.isUserAuthorized(entry.getWebsite()) && !entry.isPublished())) {
                form.copyFrom(entry, request.getLocale());
                WeblogEntryPageModel pageModel = new WeblogEntryPageModel(
                        request, response, mapping, form,
                        WeblogEntryPageModel.EDIT_MODE);
                pageModel.setWebsite(entry.getWebsite());
                request.setAttribute("model", pageModel);
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
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
            throws IOException, ServletException {
        // we need to save any new entries before Previewing
        WeblogEntryFormEx form = (WeblogEntryFormEx)actionForm;
        if (form.getId() == null) {
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
            throws IOException, ServletException {
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
            throws IOException, ServletException {
        ActionForward forward = mapping.findForward("weblogEdit.page");
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession =
                    RollerSession.getRollerSession(request);
            WeblogEntryPageModel pageModel = new WeblogEntryPageModel(
                    request, response, mapping, (WeblogEntryFormEx)actionForm, mode);
            if (rollerSession.isUserAuthorized(
                    pageModel.getWeblogEntry().getWebsite())) {
                request.setAttribute("model", pageModel);
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
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
            
            UserData            ud  = userMgr.getUser(form.getCreatorId());
            WebsiteData        site = userMgr.getWebsite(form.getWebsiteId());
            
            WeblogEntryData   entry = null;
            
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
                    entry.setWebsite(site);
                } else {
                    entry = weblogMgr.getWeblogEntry(form.getId());
                }
                
                mLogger.debug("setting update time now");
                form.setUpdateTime(new Timestamp(new Date().getTime()));                
                if ("PUBLISHED".equals(form.getStatus()) &&
                        "0/0/0".equals(form.getDateString())) {
                    mLogger.debug("setting pubtime now");                    
                    // NOTE: the form .copyTo() method will override this value
                    // based on data submitted with the form if that data is
                    // not null.  Check the method to verify. This means that 
                    // setting the pubtime here only takes effect if the entry 
                    // is being published for the first time.
                    form.setPubTime(form.getUpdateTime());
                }
                
                mLogger.debug("copying submitted form data to entry object");
                form.copyTo(entry, request.getLocale(), request.getParameterMap());
                
                // Fetch MediaCast content type and length
                mLogger.debug("Checking MediaCast attributes");
                if (!checkMediaCast(entry, uiMessages)) {
                    mLogger.debug("Invalid MediaCast attributes");
                } else {
                    mLogger.debug("Validated MediaCast attributes");
                }
                                
                mLogger.debug("Saving entry");
                weblogMgr.saveWeblogEntry(entry);
                RollerFactory.getRoller().flush();
                
                mLogger.debug("Populating form");
                form.copyFrom(entry, request.getLocale());
                
                request.setAttribute(
                        RequestConstants.WEBLOGENTRY_ID, entry.getId());
                
                // Reindex entry, flush caches, etc.
                reindexEntry(RollerFactory.getRoller(), entry);
                mLogger.debug("Removing from cache");
                RollerRequest rreq = RollerRequest.getRollerRequest(request);
                //PageCacheFilter.removeFromCache(request, entry.getWebsite());
                CacheManager.invalidate(entry);
                
                // Queue applicable pings for this update.
                if(entry.isPublished()) {
                    RollerFactory.getRoller().getAutopingManager().queueApplicableAutoPings(entry);
                }
                
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
                            new ActionMessage("weblogEdit.submittedForReview"));
                    
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
        } catch (RollerPermissionsException e) {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        } catch (Exception e) {
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
            HttpServletRequest request, WeblogEntryData entry) {
        try {
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            javax.naming.Context ctx = (javax.naming.Context)
            new InitialContext().lookup("java:comp/env");
            Session mailSession =
                    (Session)ctx.lookup("mail/Session");
            if (mailSession != null) {
                String userName = entry.getCreator().getUserName();
                String from = entry.getCreator().getEmailAddress();
                String cc[] = new String[] {from};
                String bcc[] = new String[0];
                String to[];
                String subject;
                String content;
                
                // list of enabled website authors and admins
                ArrayList reviewers = new ArrayList();
                List websiteUsers = umgr.getUsers(
                     entry.getWebsite(), Boolean.TRUE, null, null, 0, -1);
                
                // build list of reviewers (website users with author permission)
                Iterator websiteUserIter = websiteUsers.iterator();
                while (websiteUserIter.hasNext()) {
                    UserData websiteUser = (UserData)websiteUserIter.next();
                    if (entry.getWebsite().hasUserPermissions(
                            websiteUser, PermissionsData.AUTHOR)
                            && websiteUser.getEmailAddress() != null) {
                        reviewers.add(websiteUser.getEmailAddress());
                    }
                }
                to = (String[])reviewers.toArray(new String[reviewers.size()]);
                
                // Figure URL to entry edit page
                RollerContext rc = RollerContext.getRollerContext();
                String rootURL = RollerRuntimeConfig.getAbsoluteContextURL();
                if (rootURL == null || rootURL.trim().length()==0) {
                    rootURL = RequestUtils.serverURL(request)
                    + request.getContextPath();
                }
                String editURL = rootURL
                        + "/roller-ui/authoring/weblog.do?method=edit&entryId=" + entry.getId();
                
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
        } catch (NamingException e) {
            mLogger.error("ERROR: Notification email(s) not sent, "
                    + "Roller's mail session not properly configured");
        } catch (MessagingException e) {
            mLogger.error("ERROR: Notification email(s) not sent, "
                    + "due to Roller configuration or mail server problem.");
        } catch (MalformedURLException e) {
            mLogger.error("ERROR: Notification email(s) not sent, "
                    + "Roller site URL is malformed?");
        } catch (RollerException e) {
            throw new RuntimeException(
                    "FATAL ERROR: unable to find Roller object");
        }
    }
    
    private boolean checkMediaCast(WeblogEntryData entry, ActionMessages uiMessages) {
        boolean valid = false;
        String url = entry.findEntryAttribute("att_mediacast_url");
        boolean empty = (url == null) || (url.trim().length() == 0);
        if (!empty) {
            // fetch MediaCast content type and length
            valid = false;
            try {
                mLogger.debug("Sending HTTP HEAD");
                HttpURLConnection con = (HttpURLConnection)
                new URL(url).openConnection();
                mLogger.debug("Getting response code");
                con.setRequestMethod("HEAD");
                if (con.getResponseCode() != 200) {
                    mLogger.debug("Response code indicates error");
                    mLogger.error("ERROR " + con.getResponseCode() + " return from MediaCast URL");
                    uiMessages.add(null,new ActionMessage("weblogEdit.mediaCastResponseError"));
                } else if (con.getContentType()==null || con.getContentLength()==-1) {
                    mLogger.debug("Content type + (" + con.getContentType()  + " is null or content length (" +
                            con.getContentLength() + ") is -1 (indeterminate).");
                    uiMessages.add(null,new ActionMessage("weblogEdit.mediaCastLacksContentTypeOrLength"));
                } else {
                    if (mLogger.isDebugEnabled()) {
                        mLogger.debug("Got good response: Content type " +
                                con.getContentType() + " [length = " + con.getContentLength() + "]" );
                    }
                    entry.putEntryAttribute(
                            "att_mediacast_type", con.getContentType());
                    entry.putEntryAttribute(
                            "att_mediacast_length", ""+con.getContentLength());
                    valid = true;
                }
            } catch (MalformedURLException mfue) {
                mLogger.debug("Malformed MediaCast url: " + url);
                uiMessages.add(null, new ActionMessage("weblogEdit.mediaCastUrlMalformed"));
            } catch (Exception e) {
                mLogger.error("ERROR while checking MediaCast URL: " + url + ": " + e.getMessage());
                uiMessages.add(null, new ActionMessage("weblogEdit.mediaCastFailedFetchingInfo"));
            }
        } else {
            mLogger.debug("No MediaCast specified, but that is OK");
            valid = true;
        }
        if (!valid) {
            mLogger.debug("Removing MediaCast attributes");
            WeblogManager weblogManager = RollerFactory.getRoller().getWeblogManager();
            try {
                weblogManager.removeWeblogEntryAttribute("att_mediacast_url", entry);
                weblogManager.removeWeblogEntryAttribute("att_mediacast_type", entry);
                weblogManager.removeWeblogEntryAttribute("att_mediacast_length", entry);
            } catch (Exception e) {
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
            throws IOException, ServletException {
        ActionForward forward = mapping.findForward("weblogEntryRemove.page");
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogEntryFormEx wf = (WeblogEntryFormEx)actionForm;
            WeblogEntryData wd =
                    roller.getWeblogManager().getWeblogEntry(wf.getId());
            RollerSession rses =
                    RollerSession.getRollerSession(request);
            if (     rses.isUserAuthorizedToAuthor(wd.getWebsite())
            || (rses.isUserAuthorized(wd.getWebsite()) && wd.isDraft()) ) {
                wf.copyFrom(wd, request.getLocale());
                if (wd == null || wd.getId() == null) {
                    ResourceBundle resources = ResourceBundle.getBundle(
                            "ApplicationResources", request.getLocale());
                    request.setAttribute("javax.servlet.error.message",
                            resources.getString("weblogEntry.notFound"));
                    forward = mapping.findForward("error");
                }
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
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
            throws IOException, ServletException {
        try {
            WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
            WeblogEntryData wd =
                    mgr.getWeblogEntry(request.getParameter("id"));
            RollerSession rses =
                    RollerSession.getRollerSession(request);
            if (     rses.isUserAuthorizedToAuthor(wd.getWebsite())
            || (rses.isUserAuthorized(wd.getWebsite()) && wd.isDraft()) ) {
                
                // remove the index for it
                // TODO: can we do this in a better way?
                wd.setStatus(WeblogEntryData.DRAFT);
                reindexEntry(RollerFactory.getRoller(), wd);
                
                // remove entry itself
                mgr.removeWeblogEntry(wd);
                RollerFactory.getRoller().flush();
                
                // flush caches
                CacheManager.invalidate(wd);
                
                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(null,
                        new ActionMessage("weblogEdit.entryRemoved"));
                saveMessages(request, uiMessages);
                
                RollerRequest.getRollerRequest().setWebsite(wd.getWebsite());
            } else {
                return mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            throw new ServletException(e); 
        }
        
        // need to reset all values to empty (including 'id')
        actionForm = new WeblogEntryFormEx();
        actionForm.reset(mapping, request);
        request.setAttribute(mapping.getName(), actionForm);
        return create(mapping, actionForm, request, response);
    }
    
    /**
     *
     */
    public ActionForward sendTrackback(
            ActionMapping mapping,
            ActionForm actionForm,
            HttpServletRequest request,
            HttpServletResponse response) throws RollerException {
        
        ActionMessages resultMsg = new ActionMessages();
        ActionForward forward = mapping.findForward("weblogEdit.page");
        ActionErrors errors = new ActionErrors();
        WeblogEntryData entry = null;
        try {
            WeblogEntryFormEx form = (WeblogEntryFormEx)actionForm;
            String entryid = form.getId();
            if ( entryid == null ) {
                entryid = request.getParameter(RequestConstants.WEBLOGENTRY_ID);
            }
            Roller roller = RollerFactory.getRoller();
            RollerContext rctx= RollerContext.getRollerContext();
            WeblogManager wmgr= roller.getWeblogManager();
            entry = wmgr.getWeblogEntry(entryid);
            
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses.isUserAuthorizedToAuthor(entry.getWebsite())) {                                
                if (form.getTrackbackUrl() != null) {
                                    
                    RollerMessages messages = wmgr.sendTrackback(entry, form.getTrackbackUrl());
                    
                    for (Iterator mit = messages.getMessages(); mit.hasNext();) {
                        RollerMessage msg = (RollerMessage) mit.next();
                        resultMsg.add(null, new ActionMessage(msg.getKey(), msg.getArgs())); 
                    }
                    for (Iterator eit = messages.getErrors(); eit.hasNext();) {
                        RollerMessage err = (RollerMessage) eit.next();
                        errors.add(null, new ActionMessage(err.getKey(), err.getArgs())); 
                    }
                    form.setTrackbackUrl(null);
                }
                                                
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) // unexpected
        {
            mLogger.error(e);
            String msg = e.getMessage();
            if ( msg == null ) {
                msg = e.getClass().getName();
            }
            errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.general",msg));
        }
        
        saveErrors(request, errors);
        saveMessages(request, resultMsg);
        
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
            throws IOException, ServletException {
        return (mapping.findForward("weblogEdit"));
    }
    
    //-----------------------------------------------------------------------
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        return create(mapping, actionForm, request, response);
    }
    
    /**
     * Attempts to remove the Entry from the Lucene index and
     * then re-index the Entry if it is Published.  If the Entry
     * is being deleted then mark it published = false.
     * @param entry
     */
    private void reindexEntry(Roller roller, WeblogEntryData entry)
    throws RollerException {
        IndexManager manager = roller.getIndexManager();
        
        // remove entry before (re)adding it, or in case it isn't Published
        //manager.removeEntryIndexOperation(entry);
        
        // if published, index the entry
        if (entry.isPublished()) {
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


