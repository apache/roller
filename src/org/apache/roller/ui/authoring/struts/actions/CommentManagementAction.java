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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.RequestUtils;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.ui.rendering.servlets.CommentServlet;
import org.apache.roller.ui.authoring.struts.formbeans.CommentManagementForm;
import org.apache.roller.util.Utilities;

/**
 * Action for quering, approving, marking as spam and deleting comments.
 *
 * @struts.action path="/roller-ui/authoring/commentManagement" name="commentManagementForm" 
 *     scope="request" parameter="method"
 *
 * @struts.action path="/roller-ui/authoring/commentQuery" name="commentQueryForm" 
 *     scope="request" parameter="method"
 *
 * @struts.action path="/roller-ui/admin/commentManagement" name="commentManagementForm" 
 *     scope="request" parameter="method"
 *
 * @struts.action path="/roller-ui/admin/commentQuery" name="commentQueryForm" 
 *     scope="request" parameter="method"
 *
 * @struts.action-forward name="commentManagement.page" path=".CommentManagement"
 * @struts.action-forward name="commentManagementGlobal.page" path=".CommentManagementGlobal"
 */
public final class CommentManagementAction extends DispatchAction {
    
    private static Log logger =
        LogFactory.getFactory().getInstance(CommentManagementAction.class);
    
    public ActionForward query(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response) 
            throws IOException, ServletException, RollerException {
        
        CommentManagementForm queryForm = (CommentManagementForm)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        RollerSession rses = RollerSession.getRollerSession(request);
        
        ActionForward fwd = null;
        // Ensure user is authorized to view comments in weblog
        if (rreq.getWebsite() != null && rses.isUserAuthorized(rreq.getWebsite())) {
            fwd =  mapping.findForward("commentManagement.page");
        }
        // Ensure only global admins can see all comments
        else if (rses.isGlobalAdminUser()) {
            fwd =  mapping.findForward("commentManagementGlobal.page");
        } 
        else {
            // And everybody else gets...
            return mapping.findForward("access-denied");
        }        
        
        if (rreq.getWeblogEntry() != null) {
            queryForm.setEntryid(rreq.getWeblogEntry().getId());
            queryForm.setWeblog(rreq.getWeblogEntry().getWebsite().getHandle());
        }        
        else if (rreq.getWebsite() != null) {
            queryForm.setWeblog(rreq.getWebsite().getHandle());
        }        
        request.setAttribute("model", new CommentManagementPageModel(
           "commentManagement.title", request, response, mapping, queryForm));
        if (request.getAttribute("commentManagementForm") == null) {
            request.setAttribute("commentManagementForm", actionForm);
        }
        return fwd;
    }

    public ActionForward bulkDelete(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response) 
            throws Exception {
        if ("POST".equals(request.getMethod())) {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rreq.getWebsite() != null && rses.isUserAuthorized(rreq.getWebsite())
                || rses.isGlobalAdminUser()) {
                WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
                CommentManagementForm queryForm = (CommentManagementForm)actionForm;
                wmgr.removeMatchingComments(
                    rreq.getWebsite(),
                    rreq.getWeblogEntry(), 
                    queryForm.getSearchString(),
                    queryForm.getStartDate(request.getLocale()),  
                    queryForm.getEndDate(request.getLocale()), 
                    queryForm.getPending(),
                    queryForm.getApproved(),
                    queryForm.getSpam());
            }  
            CommentManagementForm queryForm = (CommentManagementForm)actionForm;
        }
        return query(mapping, actionForm, request, response);
    }
        
    public ActionForward update(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response) 
            throws IOException, ServletException, RollerException {
        
        CommentManagementForm queryForm = (CommentManagementForm)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        if (rreq.getWeblogEntry() != null) {
            queryForm.setEntryid(rreq.getWeblogEntry().getId());
            queryForm.setWeblog(rreq.getWeblogEntry().getWebsite().getHandle());
        }        
        else if (rreq.getWebsite() != null) {
            queryForm.setWeblog(rreq.getWebsite().getHandle());
        } 
        else {
            // user needs Global Admin rights to access site-wide comments
            RollerSession rses = RollerSession.getRollerSession(request);
            if (!rses.isGlobalAdminUser()) {
                return mapping.findForward("access-denied");
            }
        }
        RollerSession rses = RollerSession.getRollerSession(request);
        try {
            if (rses.isGlobalAdminUser() 
                || (rreq.getWebsite()!=null && rses.isUserAuthorizedToAuthor(rreq.getWebsite())) ) { 
                WeblogManager mgr= RollerFactory.getRoller().getWeblogManager();
                
                // delete all comments with delete box checked
                CommentData deleteComment = null;
                String[] deleteIds = queryForm.getDeleteComments();
                List deletedList = Arrays.asList(deleteIds);
                if (deleteIds != null && deleteIds.length > 0) {
                    for(int j=0; j < deleteIds.length; j++) {
                        deleteComment = mgr.getComment(deleteIds[j]);
                        
                        mgr.removeComment(deleteComment);
                    }
                }
                
                // Collect comments approved for first time, so we can send
                // out comment approved notifications later
                List approvedComments = new ArrayList();
                
                // loop through IDs of all comments displayed on page
                String[] ids = Utilities.stringToStringArray(queryForm.getIds(),",");
                List flushList = new ArrayList();
                for (int i=0; i<ids.length; i++) {                    
                    if (deletedList.contains(ids[i])) continue;                    
                    CommentData comment = mgr.getComment(ids[i]);
                    
                    // apply spam checkbox 
                    List spamIds = Arrays.asList(queryForm.getSpamComments());
                    if (spamIds.contains(ids[i])) {
                        comment.setSpam(Boolean.TRUE);
                    } else {
                        comment.setSpam(Boolean.FALSE);
                    }
                    
                    // Only participate in comment review workflow if we're
                    // working within one specfic weblog. Global admins should
                    // be able to mark-as-spam and delete comments without 
                    // interfering with moderation by bloggers.
                    if (rreq.getWebsite() != null) {
                        
                        // all comments reviewed, so they're no longer pending
                        if (comment.getPending() != null && comment.getPending().booleanValue()) {
                            comment.setPending(Boolean.FALSE);
                            approvedComments.add(comment);
                        }
                        
                        // apply pending checkbox
                        List approvedIds = 
                            Arrays.asList(queryForm.getApprovedComments());
                        if (approvedIds.contains(ids[i])) {
                            comment.setApproved(Boolean.TRUE);
                            
                        } else {
                            comment.setApproved(Boolean.FALSE);
                        }
                    }
                    mgr.saveComment(comment);
                    flushList.add(comment);
                }
                
                RollerFactory.getRoller().flush();
                for (Iterator comments=flushList.iterator(); comments.hasNext();) {
                    CacheManager.invalidate((CommentData)comments.next());
                }
                
                sendCommentNotifications(request, approvedComments);
                
                ActionMessages msgs = new ActionMessages();
                msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                    new ActionMessage("commentManagement.updateSuccess"));
                saveMessages(request, msgs);
            }
        } catch (Exception e) {
            ActionMessages errors = new ActionMessages();
            errors.add(ActionErrors.GLOBAL_MESSAGE,
                new ActionMessage("commentManagement.updateError",e.toString()));
            saveErrors(request, errors);
            logger.error("ERROR updating comments", e);       
        }
        CommentManagementPageModel model = new CommentManagementPageModel(
           "commentManagement.title", request, response, mapping, queryForm);
        request.setAttribute("model", model); 
        if (request.getAttribute("commentManagementForm") == null) {
            request.setAttribute("commentManagementForm", actionForm);
        }
        
        if (rreq.getWebsite() != null) {
            return mapping.findForward("commentManagement.page");
        }
        return mapping.findForward("commentManagementGlobal.page");
    }
    
    private void sendCommentNotifications(
        HttpServletRequest req, List comments) throws RollerException {
        
        RollerContext rc = RollerContext.getRollerContext();                             
        String rootURL = RollerRuntimeConfig.getAbsoluteContextURL();
        try {
            if (rootURL == null || rootURL.trim().length()==0) {
                rootURL = RequestUtils.serverURL(req) + req.getContextPath();
            } 
        } catch (MalformedURLException e) {
            logger.error("ERROR: determining URL of site");
            return;
        }

        Iterator iter = comments.iterator();
        while (iter.hasNext()) {
            CommentData comment = (CommentData)iter.next();
            
            // Send email notifications because a new comment has been approved
            CommentServlet.sendEmailNotification(comment, rootURL);
            
            // Send approval notification to author of approved comment
            CommentServlet.sendEmailApprovalNotification(comment, rootURL);
        }
    }
    
    public class CommentManagementPageModel extends BasePageModel {
        private List                  comments = new ArrayList();
        private WeblogEntryData       weblogEntry = null;
        private CommentManagementForm queryForm = null;
        private boolean               more = false;
        private int                   totalMatchingCommentCount = 0;
        private boolean               showBulkDeleteLink = false;
        
        public CommentManagementPageModel(
                String titleKey,
                HttpServletRequest request,
                HttpServletResponse response,
                ActionMapping mapping, 
                CommentManagementForm queryForm)  throws RollerException {
            
            super(titleKey, request, response, mapping);
            this.queryForm = queryForm;
            
            Roller roller = RollerFactory.getRoller();
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if (rreq.getWeblogEntry() != null) {
                website = rreq.getWeblogEntry().getWebsite();
                weblogEntry = rreq.getWeblogEntry();
            }
            else if (rreq.getWebsite() != null) {
                website = rreq.getWebsite();
            }
            WeblogManager blogmgr = roller.getWeblogManager();
         
            int offset = queryForm.getOffset();
            comments = blogmgr.getComments(
                website,
                weblogEntry, 
                queryForm.getSearchString(),
                queryForm.getStartDate(request.getLocale()), 
                queryForm.getEndDate(request.getLocale()), 
                queryForm.getPending(),
                queryForm.getApproved(),
                queryForm.getSpam(),
                true, // reverse  chrono order
                queryForm.getOffset(), 
                queryForm.getCount() + 1); 
            if (comments.size() > queryForm.getCount()) {
                more = true;
                comments.remove(comments.size()-1);
            }
            this.queryForm.loadCheckboxes(comments);
            
            // If we have a query POST, then we know we're responding to a query so 
            // we need to decide whether or not to show the bulk comment prompt.
            if ("POST".equals(request.getMethod()) 
                && "query".equals(request.getParameter("method"))) {
                
                // So we run the query again, except this time with no limit.
                List allMatchingComments = blogmgr.getComments( 
                    website,
                    weblogEntry, 
                    queryForm.getSearchString(),
                    queryForm.getStartDate(request.getLocale()), 
                    queryForm.getEndDate(request.getLocale()), 
                    queryForm.getPending(),
                    queryForm.getApproved(),
                    queryForm.getSpam(),
                    true, 
                    0, -1);                
                totalMatchingCommentCount = allMatchingComments.size();
                
                // If there are more comments than can be shown on one 
                // page, then present the bulk-comment delete prompt.
                showBulkDeleteLink = totalMatchingCommentCount > queryForm.getCount();
            }
        }    
        
        public List getComments() {
            return comments;
        }
        
        public int getCommentCount() {
            int ret = comments.size();            
            return ret > queryForm.getCount() ? queryForm.getCount() : ret;
        }
        
        /**
         * Number of pending entries on current page of results.
         * Returns zero when managing comments across entire site, because 
         * we don't want global admins to change pending status of posts.
         */        
        public int getPendingCommentCount() {
            int count = 0;
            if (getWebsite() != null) { 
                for (Iterator iter = comments.iterator(); iter.hasNext();) {
                    CommentData cd = (CommentData)iter.next();
                    if (cd.getPending().booleanValue()) count++;
                }
            }
            return count;
        }
        
        public Date getEarliestDate() {
            Date date = null;
            if (comments.size() > 0) {
                CommentData earliest = (CommentData)comments.get(comments.size()-1);
                date = earliest.getPostTime();
            }
            return date;
        }
        
        public Date getLatestDate() {
            Date date = null;
            if (comments.size() > 0) {
                CommentData latest = (CommentData)comments.get(0);
                date = latest.getPostTime();
            }
            return date;
        }
        
        public WeblogEntryData getWeblogEntry() {
            return weblogEntry;
        }
        
        public String getLink() {
            return getQueryLink() + "&offset=" + queryForm.getOffset();
        }
        
        public String getNextLink() {
            if (more) {
                int offset = queryForm.getOffset() + queryForm.getCount();
                offset = (offset < 0) ? 0 : offset;
                return getQueryLink() + "&offset=" + offset;
            } else {
                return null;
            }
        }
        
        public String getPrevLink() {
            if (queryForm.getOffset() > 0) {
                int offset = queryForm.getOffset() - queryForm.getCount();
                offset = (offset < 0) ? 0 : offset;
                return getQueryLink() + "&offset=" + offset;
            } else {
                return null;
            }
        }
        
        private String getQueryLink() {
            StringBuffer sb = new StringBuffer();
            sb.append(request.getContextPath());
            if (getWebsite() != null) {
                sb.append("/roller-ui/authoring/commentManagement.do"); // TODO: get path from Struts
                sb.append("?method=query");
                sb.append("&weblog=");
                sb.append(getWebsite().getHandle());
            } else {
                sb.append("/roller-ui/admin/commentManagement.do"); // TODO: get path from Struts
                sb.append("?method=query");
            }
            sb.append("&count=");
            sb.append(queryForm.getCount());
            return sb.toString();
        }

        public int getTotalMatchingCommentCount() {
            return totalMatchingCommentCount;
        }

        public boolean isShowBulkDeleteLink() {
            return showBulkDeleteLink;
        }
    }
}



