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
import org.roller.RollerPermissionsException;
import org.roller.model.BookmarkManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.bookmarks.formbeans.BookmarkFormEx;

/**
 * @struts.action path="/editor/bookmarkSave" name="bookmarkFormEx"
 *    validate="true" input="/editor/bookmarkEdit.do"
 * @struts.action-forward name="Bookmarks" path="/editor/bookmarks.do?method=selectFolder"
 * 
 * @author Dave Johnson
 */
public class BookmarkSaveAction extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        ActionForward forward = mapping.findForward("Bookmarks");
        try
        {
            BookmarkFormEx form = (BookmarkFormEx)actionForm;
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
            
            BookmarkData bd = null;
            if (null != form.getId() && !form.getId().trim().equals("")) 
            {
                bd = bmgr.retrieveBookmark(form.getId());
                bd.startEditing(); // should throw if save not permitted
            }
            else 
            {
                bd = bmgr.createBookmark();
                
                // Existing bookmarks already have folders, but this is a new one.
                FolderData fd = bmgr.retrieveFolder(
                    request.getParameter(RollerRequest.FOLDERID_KEY));
                bd.setFolder(fd);
            }
            form.copyTo(bd, request.getLocale());
            bd.save();
            RollerFactory.getRoller().commit();
            
            request.setAttribute(
                RollerRequest.FOLDERID_KEY,bd.getFolder().getId());  
        }
        catch (RollerPermissionsException e)
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        }
        return forward;
        
    }

}
