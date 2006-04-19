/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.weblog.actions;

import java.util.LinkedList;

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
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.weblog.formbeans.WeblogCategoryFormEx;

/**
 * @struts.action path="/editor/categoryEdit" name="weblogCategoryFormEx" validate="false"
 * @struts.action-forward name="CategoryForm" path=".CategoryForm"
 * 
 * @author Dave Johnson
 */
public class CategoryEditAction extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        WeblogCategoryFormEx form = (WeblogCategoryFormEx)actionForm;
        
        BasePageModel pageModel = null;
        WeblogCategoryData parentCat = null;
        if (null!=rreq.getWeblogCategory() && null==request.getParameter("correct")) 
        {
            // If request specifies Category and we are not correcting an 
            // already submitted form then load that Category into the form.
            WeblogCategoryData cd = rreq.getWeblogCategory();
            form.copyFrom(cd, request.getLocale());
            request.setAttribute("state","edit"); 
                             
            parentCat = cd.getParent();            
            pageModel = new BasePageModel(
                "categoryForm.edit.title", request, response, mapping);
            pageModel.setWebsite(cd.getWebsite());
        }
        else if (null != request.getParameter("correct"))
        {
            // We are correcting a previously submtted form.
            // already submitted form then load that Category into the form.
            WeblogCategoryData cd = rreq.getWeblogCategory();
            request.setAttribute("state","correcting");    
            
            parentCat = wmgr.getWeblogCategory(cd.getId());          
            pageModel = new BasePageModel(
                "categoryForm.correct.title", request, response, mapping);
            pageModel.setWebsite(cd.getWebsite());
        }
        else
        {
            // We are adding a new Category
            request.setAttribute("state","add");
            
            String pid = request.getParameter(RollerRequest.PARENTID_KEY);
            parentCat = wmgr.getWeblogCategory(pid);             
            form.setParentId(parentCat.getId()); 
            
            pageModel = new BasePageModel(
                "categoryForm.add.title", request, response, mapping);
            pageModel.setWebsite(parentCat.getWebsite());
        }
        
        // Build cat path for display on page
        if (null != parentCat)
        {
            LinkedList categoryPath = new LinkedList();
            categoryPath.add(0, parentCat);
            WeblogCategoryData parent = parentCat.getParent();
            while (parent != null) 
            {
                categoryPath.add(0, parent);
                parent = parent.getParent();   
            }
            request.setAttribute("parentCategory", parentCat);
            request.setAttribute("categoryPath", categoryPath);
        }
        
        request.setAttribute("model", pageModel);
        return mapping.findForward("CategoryForm");
    }
    
}
