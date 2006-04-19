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
import org.roller.model.BookmarkManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.FolderData;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.bookmarks.formbeans.FolderFormEx;
import org.roller.presentation.cache.CacheManager;

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
        RollerSession rses = RollerSession.getRollerSession(request);
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        WebsiteData website = null;
                
        FolderData fd = null;
        if (null != form.getId() && !form.getId().trim().equals("")) 
        {
            fd = bmgr.getFolder(form.getId());
            website = fd.getWebsite();
        }
        else 
        {
            fd = new FolderData();
            String parentId = request.getParameter(RollerRequest.PARENTID_KEY);
            FolderData parent = bmgr.getFolder(parentId);
            website = parent.getWebsite();
            fd.setParent(parent);
            fd.setWebsite(website);
        }
        
        if (fd.getWebsite().hasUserPermissions(
                rses.getAuthenticatedUser(), PermissionsData.AUTHOR))
        {
            // Copy form values to object
            form.copyTo(fd, request.getLocale());
            bmgr.saveFolder(fd);
            RollerFactory.getRoller().flush();
            
            CacheManager.invalidate(fd);
        }
        else
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        }         
        if (null != fd.getParent()) 
        {
            request.setAttribute(
               RollerRequest.FOLDERID_KEY, fd.getParent().getId());
        }         
        return forward;
    }
}
