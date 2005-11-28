/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.bookmarks.actions;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.roller.RollerException;
import org.roller.model.BookmarkManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.bookmarks.formbeans.BookmarksForm;
import org.roller.presentation.cache.CacheManager;

/**
 * Actions that are initiated from the BookmarksForm.
 *
 * @struts.action name="bookmarksForm" path="/editor/bookmarks" parameter="method"
 * @struts.action-forward name="BookmarksForm" path=".BookmarksForm"
 *
 * @author Dave Johnson
 */
public class BookmarksAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(BookmarksAction.class);

    /**
     * Present the BookmarksForm loaded with folder specified by request.
     * @param mapping Action mapping.
     * @param actionForm Form bean.
     * @param request Request.
     * @param response Response.
     * @return Forward to BookmarksForm or access-denied.
     * @throws RollerException
     */
    public ActionForward selectFolder(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws RollerException
    {
        BookmarksPageModel pageModel = new BookmarksPageModel(
            request, response, mapping, (BookmarksForm)actionForm);
        if (RollerSession.getRollerSession(request).isUserAuthorizedToAuthor(
                pageModel.getFolder().getWebsite()))
        {
            request.setAttribute("model", pageModel);
            return mapping.findForward("BookmarksForm");
        }
        else
        {
            return mapping.findForward("access-denied");
        }
    }

    /**
     * Delete folders and bookmarks indicated by BookmarksForm bean.
     * @param mapping Action mapping.
     * @param actionForm Form bean.
     * @param request Request.
     * @param response Response.
     * @return Forward to BookmarksForm or access-denied.
     * @throws RollerException
     */
    public ActionForward deleteSelected(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws RollerException
    {
        Roller roller = RollerFactory.getRoller();
        BookmarksPageModel pageModel = new BookmarksPageModel(
            request, response, mapping, (BookmarksForm)actionForm);
        WebsiteData website = pageModel.getFolder().getWebsite();
        if (RollerSession.getRollerSession(request).isUserAuthorizedToAuthor(website))
        {
            BookmarkManager bmgr = roller.getBookmarkManager();
            BookmarksForm form = (BookmarksForm)actionForm;

            mLogger.debug("Deleting folders and bookmarks.");

            String folders[] = form.getSelectedFolders();
            if (null != folders)
            {
                for (int i = 0; i < folders.length; i++)
                {
                    FolderData fd = bmgr.retrieveFolder(folders[i]);
                    fd.remove(); // removes child folders and bookmarks too
                }
            }

            String bookmarks[] = form.getSelectedBookmarks();
            if (null != bookmarks)
            {
                for (int j = 0; j < bookmarks.length; j++)
                {
                    bmgr.removeBookmark(bookmarks[j]);
                }
            }
            roller.commit();
                
            CacheManager.invalidate(website);

            // recreate model now that folder  is deleted
            pageModel = new BookmarksPageModel(
                request, response, mapping, (BookmarksForm)actionForm);
            request.setAttribute("model", pageModel);
            return mapping.findForward("BookmarksForm");
        }
        else
        {
            return mapping.findForward("access-denied");
        }
    }

    /**
     * Move folders and bookmarks indicated by BookmarksForm bean.
     * @param mapping Action mapping.
     * @param actionForm  Form bean.
     * @param request Request.
     * @param response Response.
     * @return Forward to BookmarksForm or access-denied.
     * @throws RollerException
     */
    public ActionForward moveSelected(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws RollerException
    {
        ActionMessages messages = new ActionMessages();
        ActionForward forward = mapping.findForward("BookmarksForm");
        Roller roller = RollerFactory.getRoller();
        BookmarksPageModel pageModel = new BookmarksPageModel(
            request, response, mapping, (BookmarksForm)actionForm);
        request.setAttribute("model", pageModel);
        WebsiteData website = pageModel.getFolder().getWebsite();

        if (RollerSession.getRollerSession(request).isUserAuthorizedToAuthor(website))
        {
            try 
            {
                BookmarkManager bmgr = roller.getBookmarkManager();
                BookmarksForm form = (BookmarksForm)actionForm;
    
                mLogger.debug("Moving folders and bookmarks to folder, id="
                    + form.getMoveToFolderId());
    
                // Move subfolders to new folder.
                String folders[] = form.getSelectedFolders();
                FolderData parent = bmgr.retrieveFolder(form.getMoveToFolderId());
                if (null != folders)
                {
                    for (int i = 0; i < folders.length; i++)
                    {
                        FolderData fd = bmgr.retrieveFolder(folders[i]);
    
                        // Don't move folder into itself.
                        if (    !fd.getId().equals(parent.getId())
                             && !parent.descendentOf(fd))
                        {
                            fd.setParent(parent);
                            fd.save();
                        }
                        else 
                        {
                            messages.add(null, new ActionMessage(
                                "bookmarksForm.warn.notMoving",fd.getName()));
                        }
                    }
                }
    
                // Move bookmarks.
                String bookmarks[] = form.getSelectedBookmarks();
                if (null != bookmarks)
                {
                    for (int j = 0; j < bookmarks.length; j++)
                    {
                        BookmarkData bd = bmgr.retrieveBookmark(bookmarks[j]);
                        bd.setFolder(parent);
                        bd.save();
                    }
                }
                roller.commit();

                CacheManager.invalidate(website);
                
                saveMessages(request, messages);
            }
            catch (RollerException e)
            {
                ActionErrors errors = new ActionErrors();
                errors.add(null, new ActionError("bookmarksForm.error.move"));
                saveErrors(request, errors);       
            }
        }
        else
        {
            forward = mapping.findForward("access-denied");
        }
        return forward;
    }

    private static final class FolderPathComparator implements Comparator
    {
        public int compare(Object o1, Object o2) {
            FolderData f1 = (FolderData)o1;
            FolderData f2 = (FolderData)o2;
            int res = 0;
            try
            {
                res = f1.getPath().compareTo(f2.getPath());
            }
            catch (RollerException e)
            {
                mLogger.error("ERROR: sorting folders");
            }
            return res;
        }
    }
    
    public class BookmarksPageModel extends BasePageModel
    {
        private List folderPath = null;
        private TreeSet allFolders = null;
        private FolderData folder = null;   
        
        public BookmarksPageModel(
                HttpServletRequest request,
                HttpServletResponse response,
                ActionMapping mapping,
                BookmarksForm form) throws RollerException
        {
            super("",  request, response, mapping);
            
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();

            allFolders = new TreeSet(new FolderPathComparator());

            // Find folderid wherever it may be
            String folderId = (String)
                request.getAttribute(RollerRequest.FOLDERID_KEY);
            if (null == folderId)
            {
                folderId = request.getParameter(RollerRequest.FOLDERID_KEY);
            }
            if (null == folderId)
            {
                folderId = form.getFolderId();
            }

            if (null == folderId || folderId.equals("null"))
            {
                website = rreq.getWebsite();
                folder = bmgr.getRootFolder(website);
                folderId = folder.getId();
            }
            else
            {
                folder = bmgr.retrieveFolder(folderId);
                website = folder.getWebsite();
            }
            form.setFolderId(folder.getId());

            if (null != folder.getParent())
            {
                folderPath = new LinkedList();
                folderPath.add(0, folder);
                FolderData parent = folder.getParent();
                while (parent != null)
                {
                    folderPath.add(0, parent);
                    parent = parent.getParent();
                }
                request.setAttribute(
                    RollerRequest.PARENTID_KEY, folder.getParent().getId());
            }

            // Build list of all folders, except for current one, sorted by path.
            Iterator iter = bmgr.getAllFolders(website).iterator();
            while (iter.hasNext())
            {
                FolderData fd = (FolderData) iter.next();
                if (!fd.getId().equals(folderId))
                {
                    allFolders.add(fd);
                }
            }
            if (allFolders.size() > 0) {
                request.setAttribute("allFolders", allFolders); // for Struts tags
            } else {
                allFolders = null;
            }
            request.setAttribute("folder", folder); // for Struts tags          
        }
        
        public String getTitle()
        {
            if (folderPath == null || folderPath.isEmpty()) 
            {
                return bundle.getString("bookmarksForm.rootTitle");
            }
            else 
            {
                return MessageFormat.format(
                    bundle.getString("bookmarksForm.folderTitle"),
                    new String[] {folder.getName()});
            }
        }
        public List getFolderPath() 
        {
            return folderPath;
        }
        public Set getAllFolders() 
        {
            return allFolders;
        }
        public FolderData getFolder()
        {
            return folder;
        }
                

    }
}
