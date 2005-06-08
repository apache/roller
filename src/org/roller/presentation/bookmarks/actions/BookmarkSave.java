/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.bookmarks.actions;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.model.BookmarkManager;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.bookmarks.formbeans.BookmarkFormEx;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @struts.action path="/bookmarkSave" name="bookmarkFormEx"
 *    validate="true" input="/bookmarkEdit.do"
 * @struts.action-forward name="Bookmarks" path="/bookmarks.do?method=selectFolder"
 * 
 * @author Dave Johnson
 */
public class BookmarkSave extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        BookmarkFormEx form = (BookmarkFormEx)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        BookmarkManager bmgr = rreq.getRoller().getBookmarkManager();
        
        BookmarkData bd = null;
        if (null != form.getId() && !form.getId().trim().equals("")) 
        {
            bd = bmgr.retrieveBookmark(form.getId());
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
        rreq.getRoller().commit();
        
        request.setAttribute(
            RollerRequest.FOLDERID_KEY,bd.getFolder().getId());         
        return mapping.findForward("Bookmarks");
    }

}
