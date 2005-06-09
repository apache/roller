/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.bookmarks.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.RollerException;
import org.roller.RollerPermissionsException;
import org.roller.model.BookmarkManager;
import org.roller.pojos.FolderData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.bookmarks.formbeans.FolderFormEx;

/**
 * @struts.action path="/editor/folderSave" name="folderFormEx" 
 *     validate="true" input="/editor/folderEdit.do"
 * @struts.action-forward name="Bookmarks" path="/editor/bookmarks.do?method=selectFolder"
 * 
 * @author Dave Johnson
 */
public class FolderSaveAction extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        ActionForward forward = mapping.findForward("Bookmarks");
        FolderFormEx form = (FolderFormEx)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData wd = rreq.getWebsite();
        BookmarkManager bmgr = rreq.getRoller().getBookmarkManager();
        
        FolderData fd = null;
        if (null != form.getId() && !form.getId().trim().equals("")) 
        {
            fd = bmgr.retrieveFolder(form.getId());
            fd.save(); // should throw if save not permitted
        }
        else 
        {
            fd = bmgr.createFolder();
        
            String parentId = request.getParameter(RollerRequest.PARENTID_KEY);
            FolderData parent = null;
            if (null != parentId && !parentId.trim().equalsIgnoreCase("null"))
            {
                parent = bmgr.retrieveFolder(parentId);
            }
            else 
            {
                parent = bmgr.getRootFolder(wd);
            }
            fd.setParent(parent);
            fd.setWebsite(wd);
        }
        
        // Copy form values to object
        form.copyTo(fd, request.getLocale());
            
        try 
        {
            // Store object and commit
            fd.save();
            rreq.getRoller().commit();
        }
        catch (RollerPermissionsException e)
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        }
        catch (RollerException re)
        {
            rreq.getRoller().rollback();
            ActionErrors errors = new ActionErrors();
            String msg = (null != re.getRootCause())
                ? re.getRootCause().toString()
                : re.toString();
            errors.add(ActionErrors.GLOBAL_ERROR, 
               new ActionError("folderForm.save.exception", msg));
            saveErrors(request,errors);            
        }
         
        if (null != fd.getParent()) 
        {
            request.setAttribute(
               RollerRequest.FOLDERID_KEY, fd.getParent().getId());
        }         
        return forward;
    }
}
