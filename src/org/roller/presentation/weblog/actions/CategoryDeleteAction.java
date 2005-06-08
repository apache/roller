/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.weblog.actions;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.weblog.formbeans.CategoryDeleteForm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @struts.action path="/categoryDelete" name="categoryDeleteForm"
 * @struts.action-forward name="Categories" path="/categories.do?method=selectCategory"
 * @struts.action-forward name="CategoryDeleteOK" path="/weblog/CategoryDeleteOK.jsp"
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
        WeblogManager wmgr = rreq.getRoller().getWeblogManager();
        
        if (rreq.isUserAuthorizedToEdit())
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
                WebsiteData website = rreq.getWebsite();
                WeblogCategoryData theCat = wmgr.retrieveWeblogCategory(catid);
                Iterator allCats = 
                    wmgr.getWeblogCategories(website).iterator();
                List destCats = new LinkedList();
                while (allCats.hasNext())
                {
                    WeblogCategoryData cat = (WeblogCategoryData)allCats.next();
                    if (cat.getId() != catid)
                    {
                        destCats.add(cat);
                    }                    
                }
                if (destCats.size() > 0)
                {
                    form.setName(theCat.getName());
                    form.setCatid(catid);
                    form.setCats(destCats);
                    form.setInUse(new Boolean(catToDelete.isInUse()));
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
                    forward = mapping.findForward("Categories");
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
                
                rreq.getRoller().commit();
                
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
