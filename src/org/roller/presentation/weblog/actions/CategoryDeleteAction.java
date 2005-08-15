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
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.weblog.formbeans.CategoryDeleteForm;

/**
 * @struts.action path="/editor/categoryDelete" name="categoryDeleteForm"
 * @struts.action-forward name="CategoryDeleteOK" path=.CategoryDeleteOK"
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
        ActionForward forward = null;
        CategoryDeleteForm form = (CategoryDeleteForm)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        RollerSession rollerSession = RollerSession.getRollerSession(
                rreq.getRequest());
        if (rollerSession.isUserAuthorizedToAuthor())
        {
            String catid = request.getParameter("catid");
            WeblogCategoryData catToDelete = 
                wmgr.retrieveWeblogCategory(catid);
            String returnId = null;
            if (catToDelete.getParent() != null)
            {
                returnId = catToDelete.getParent().getId();
            }
            if (form.isDelete() == null)
            {
                // Present CategoryDeleteOK? page to user
                WebsiteData website = RollerSession.getRollerSession(request).getCurrentWebsite();
                WeblogCategoryData theCat = wmgr.retrieveWeblogCategory(catid);
                Iterator allCats = 
                    wmgr.getWeblogCategories(website).iterator();
                List destCats = new LinkedList();
                while (allCats.hasNext())
                {
                    WeblogCategoryData cat = (WeblogCategoryData)allCats.next();
                    // Allow entries to be moved to any other category except 
                    // root and the sub-cats of the category being deleted.
                    if (!cat.getId().equals(catid) 
                        && cat.getParent()!=null
                        && !cat.descendentOf(catToDelete))
                    {
                        destCats.add(cat);
                    }                    
                }
                if (destCats.size() > 0)
                {
                    form.setName(theCat.getName());
                    form.setCatid(catid);
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
                    forward = mapping.findForward("categories");
                }
            }
            else if (form.isDelete().booleanValue()) 
            {
                // User clicked YES to delete
                WeblogCategoryData destCat = null;
                if (form.getMoveToWeblogCategoryId() != null) 
                {
                    destCat = wmgr.retrieveWeblogCategory(form.getMoveToWeblogCategoryId());
                }
                  
                // move entries to destCat and remove catToDelete
                catToDelete.remove(destCat);
                
                RollerFactory.getRoller().commit();
                
                if (null != returnId) 
                {
                    request.setAttribute(
                        RollerRequest.WEBLOGCATEGORYID_KEY, returnId);
                }               
                forward = mapping.findForward("Categories");
            }
            else 
            {
                // User clicked NO to delete, do back to categories form
                if (null != returnId) 
                {
                    request.setAttribute(
                       RollerRequest.WEBLOGCATEGORYID_KEY, returnId);
                }               
                forward = mapping.findForward("Categories");   
            }
        }
        else
        {
            forward = mapping.findForward("access-denied");
        }
        return forward;
    }

}
