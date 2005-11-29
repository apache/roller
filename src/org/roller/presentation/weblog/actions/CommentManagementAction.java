package org.roller.presentation.weblog.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.weblog.formbeans.CommentManagementForm;
import org.roller.presentation.weblog.formbeans.CommentQueryForm;
import org.roller.util.Utilities;

/**
 * Action for quering, approving, marking as spam and deleting comments.
 *
 * @struts.action path="/editor/commentManagement" name="commentManagementForm" 
 *     scope="session" parameter="method"
 *
 * @struts.action path="/editor/commentQuery" name="commentQueryForm" 
 *     scope="session" parameter="method"
 *
 * @struts.action-forward name="commentManagement.page" path=".CommentManagement"
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

        if (rreq.getWeblogEntry() != null) {
            queryForm.setEntryid(rreq.getWeblogEntry().getId());
            queryForm.setWeblog(rreq.getWeblogEntry().getWebsite().getHandle());
        }        
        else if (rreq.getWebsite() != null) {
            queryForm.setWeblog(rreq.getWebsite().getHandle());
        }        
        request.setAttribute("model", new CommentManagementPageModel(
           "commentManagement.title", request, response, mapping, queryForm)); 
        return mapping.findForward("commentManagement.page");
    }

    public ActionForward update(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response) 
            throws IOException, ServletException, RollerException {
        
        CommentManagementForm queryForm = (CommentManagementForm)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        RollerSession rollerSession = RollerSession.getRollerSession(request);
        try {
            if (rollerSession.isUserAuthorizedToAuthor(rreq.getWebsite())) { 
                WeblogManager mgr= RollerFactory.getRoller().getWeblogManager();
                String[] deleteIds = queryForm.getDeleteComments();
                List deletedList = Arrays.asList(deleteIds); 
                if (deleteIds != null && deleteIds.length > 0) {
                    mgr.removeComments(deleteIds);
                }                
                String[] ids = Utilities.stringToStringArray(queryForm.getIds(),",");
                for (int i=0; i<ids.length; i++) { 
                    if (deletedList.contains(ids[i])) continue;
                    CommentData comment = mgr.retrieveComment(ids[i]);
                    List spamIds = Arrays.asList(queryForm.getSpamComments());
                    List approvedIds = Arrays.asList(queryForm.getApprovedComments());
                    if (spamIds.contains(ids[i])) {
                        comment.setSpam(Boolean.TRUE);
                    } else {
                        comment.setSpam(Boolean.FALSE);
                    }
                    if (approvedIds.contains(ids[i])) {
                        comment.setPending(Boolean.FALSE);
                    } else {
                        comment.setPending(Boolean.TRUE);
                    }
                    comment.save();
                }               
                RollerFactory.getRoller().commit();
                ActionMessages msgs = new ActionMessages();
                msgs.add(ActionMessages.GLOBAL_MESSAGE, 
                    new ActionMessage("commentManagement.updateSuccess"));
                saveMessages(request, msgs);
            }
        } catch (Exception e) {
            ActionMessages errors = new ActionMessages();
            errors.add(ActionErrors.GLOBAL_MESSAGE,
                new ActionMessage("commentManagement.updateError", e.toString()));
            saveErrors(request, errors);
            logger.error("ERROR updating comments", e);       
        }
        CommentManagementPageModel model = new CommentManagementPageModel(
           "commentManagement.title", request, response, mapping, queryForm);
        request.setAttribute("model", model); 
        return mapping.findForward("commentManagement.page");
    }
    
    public class CommentManagementPageModel extends BasePageModel {
        private List comments = new ArrayList();
        private WeblogEntryData weblogEntry = null;
        private CommentManagementForm queryForm = null;
        private boolean more = false;
        
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
                queryForm.getOffset(), 
                queryForm.getCount() + 1); 
            if (comments.size() > queryForm.getCount()) {
                more = true;
                comments.remove(comments.size()-1);
            }
            this.queryForm.loadCheckboxes(comments);
        }    
        
        public List getComments() {
            return comments;
        }
        
        public int getCommentCount() {
            int ret = comments.size();            
            return ret > queryForm.getCount() ? queryForm.getCount() : ret;
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
            sb.append("/editor/commentManagement.do"); // TODO: get path from Struts
            sb.append("?method=query");
            if (getWebsite() != null) {
                sb.append("&weblog=");
                sb.append(getWebsite().getHandle());
            }
            sb.append("&count=");
            sb.append(queryForm.getCount());
            return sb.toString();
        }
    }
}



