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
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.bookmarks.formbeans.BookmarkFormEx;
import org.roller.presentation.forms.BookmarkForm;

import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @struts.action path="/editor/bookmarkEdit" name="bookmarkFormEx" validate="false"
 * @struts.action-forward name="BookmarkForm" path="/bookmarks/BookmarkForm.jsp"
 * 
 * @author Dave Johnson
 */
public class BookmarkEditAction extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData wd = rreq.getWebsite();
        BookmarkManager bmgr = rreq.getRoller().getBookmarkManager();
        BookmarkFormEx form = (BookmarkFormEx)actionForm;
        
        FolderData parentFolder = null;
        if (null!=rreq.getBookmark() && null==request.getParameter("correct")) 
        {
            // If request specifies bookmark and we are not correcting an 
            // already submitted form then load that bookmark into the form.
            BookmarkData bd = rreq.getBookmark();
            form.copyFrom(bd, request.getLocale());
            request.setAttribute("state","edit"); 
                
            // Pass bookmark's Folder on as attribute.                 
            parentFolder = bd.getFolder();
        }
        else if (null != request.getParameter("correct"))
        {
            // We are correcting a previously submtted form.
            request.setAttribute("state","correcting"); 
                
            // Folder is specified by request param, pass it on as attribute.                 
            parentFolder = bmgr.retrieveFolder(rreq.getFolder().getId());        
        }
        else
        {
            // We are adding a new bookmark
            request.setAttribute("state","add");
            
            // Folder is specified by request param, pass it on as attribute.                 
            parentFolder = bmgr.retrieveFolder(rreq.getFolder().getId());        
        }
        
        // Build folder path for display on page
        if (null != parentFolder)
        {
            request.setAttribute(
                RollerRequest.FOLDERID_KEY, parentFolder.getId());
            
            LinkedList folderPath = new LinkedList();
            folderPath.add(0, parentFolder);
            FolderData parent = parentFolder.getParent();
            while (parent != null) 
            {
                folderPath.add(0, parent);
                parent = parent.getParent();   
            }
            request.setAttribute("parentFolder", parentFolder);
            request.setAttribute("folderPath", folderPath);
        }

        return mapping.findForward("BookmarkForm");
    }
    
}
