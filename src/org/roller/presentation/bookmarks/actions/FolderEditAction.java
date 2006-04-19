/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.bookmarks.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.model.BookmarkManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.FolderData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.bookmarks.formbeans.FolderFormEx;

/**
 * @struts.action path="/editor/folderEdit" name="folderFormEx" validate="false"
 * @struts.action-forward name="FolderForm" path=".FolderForm"
 * 
 * @author Dave Johnson
 */
public class FolderEditAction extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        FolderFormEx form = (FolderFormEx)actionForm;
        
        FolderData parentFolder = null;
        if (null!=rreq.getFolder() && null==request.getParameter("correct")) 
        {
            // If request specifies folder and we are not correcting an 
            // already submitted form then load that folder into the form.
            request.setAttribute("state","edit"); 

            FolderData fd = rreq.getFolder();
            form.copyFrom(fd, request.getLocale());
            parentFolder = fd.getParent();
            
            BasePageModel pageModel = new BasePageModel(
                "folderForm.add.title", request, response, mapping);
            pageModel.setWebsite(parentFolder.getWebsite());
            request.setAttribute("model", pageModel);
        }
        else if (null != request.getParameter("correct"))
        {
            // We are correcting a previously submtted form.
            request.setAttribute("state","correcting"); 
            
            String parentId = request.getParameter(RollerRequest.PARENTID_KEY);
            parentFolder = bmgr.getFolder(parentId);
            
            BasePageModel pageModel = new BasePageModel(
                "folderForm.correct.title", request, response, mapping);
            pageModel.setWebsite(parentFolder.getWebsite());
            request.setAttribute("model", pageModel);
        }
        else
        {
            // We are adding a new bookmark
            request.setAttribute("state","add");
            
            String parentId = request.getParameter(RollerRequest.PARENTID_KEY);
            parentFolder = bmgr.getFolder(parentId);
            
            BasePageModel pageModel = new BasePageModel(
                "folderForm.add.title", request, response, mapping);
            pageModel.setWebsite(parentFolder.getWebsite());
            request.setAttribute("model", pageModel);
        }
        
        request.setAttribute(RollerRequest.PARENTID_KEY, parentFolder.getId());
        request.setAttribute("parentFolder", parentFolder);
        
        return mapping.findForward("FolderForm");
    }

}
