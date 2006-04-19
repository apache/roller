/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.weblog.actions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogCategoryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.cache.CacheManager;
import org.roller.presentation.weblog.formbeans.CategoryDeleteForm;

/**
 * @struts.action path="/editor/categoryDelete" name="categoryDeleteForm"
 * @struts.action-forward name="CategoryDeleteOK" path=".CategoryDeleteOK"
 *
 * @author Dave Johnson
 */
public class CategoryDeleteAction extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        ActionForward forward = mapping.findForward("categories");
        CategoryDeleteForm form = (CategoryDeleteForm)actionForm;
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();

        String catid = request.getParameter(RollerRequest.WEBLOGCATEGORYID_KEY);
        WeblogCategoryData catToDelete = 
                wmgr.getWeblogCategory(catid);
        RollerSession rses = RollerSession.getRollerSession(request);
        if (rses.isUserAuthorizedToAuthor(catToDelete.getWebsite()))
        {
            String returnId = null;
            if (catToDelete.getParent() != null)
            {
                returnId = catToDelete.getParent().getId();
            }
            if (form.isDelete() == null)
            {
                // Present CategoryDeleteOK? page to user
                RollerRequest rreq = RollerRequest.getRollerRequest(request);
                WeblogCategoryData theCat = wmgr.getWeblogCategory(catid);
                Iterator allCats = 
                    wmgr.getWeblogCategories(theCat.getWebsite()).iterator();
                List destCats = new LinkedList();
                while (allCats.hasNext())
                {
                    WeblogCategoryData cat = (WeblogCategoryData)allCats.next();
                    // Allow entries to be moved to any other category except 
                    // root and the sub-cats of the category being deleted.
                    if (!cat.getId().equals(catid) 
                        && cat.getParent()!=null
                        && !cat.descendentOf(catToDelete)
                        && cat.retrieveWeblogEntries(true).size() < 1)
                    {
                        destCats.add(cat);
                    }                    
                }
                if (destCats.size() > 0)
                {
                    form.setName(theCat.getName());
                    form.setCategoryId(catid); 
                    form.setCats(destCats);
                    form.setInUse(Boolean.valueOf(catToDelete.isInUse()));
                    forward = mapping.findForward("CategoryDeleteOK");
                }
                else
                {
                    // Can't delete last category, send 'em back!
                    if (null != returnId) 
                    {
                        request.setAttribute(
                                RollerRequest.WEBLOGCATEGORYID_KEY, returnId);
                    }               
                }
            }
            else if (form.isDelete().booleanValue()) {
                
                // User clicked YES to delete
                // remove cat to delete
                wmgr.removeWeblogCategory(catToDelete);
                RollerFactory.getRoller().flush();
                
                // notify caches of invalidated object
                CacheManager.invalidate(catToDelete);
                
                if (null != returnId) {
                    request.setAttribute(RollerRequest.WEBLOGCATEGORYID_KEY, returnId);
                }               
            }
            else 
            {
                // User clicked NO to delete, do back to categories form
                if (null != returnId) 
                {
                    request.setAttribute(
                       RollerRequest.WEBLOGCATEGORYID_KEY, returnId);
                }               
            }
        }
        else
        {
            forward = mapping.findForward("access-denied");
        }
        return forward;
    }

}
