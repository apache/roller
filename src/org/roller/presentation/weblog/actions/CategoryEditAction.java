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
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.weblog.formbeans.WeblogCategoryFormEx;

/**
 * @struts.action path="/editor/categoryEdit" name="weblogCategoryFormEx" validate="false"
 * @struts.action-forward name="CategoryForm" path="/weblog/CategoryForm.jsp"
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
        WebsiteData wd = RollerSession.getRollerSession(request).getCurrentWebsite();
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        WeblogCategoryFormEx form = (WeblogCategoryFormEx)actionForm;
        
        WeblogCategoryData parentCat = null;
        if (null!=rreq.getWeblogCategory() && null==request.getParameter("correct")) 
        {
            // If request specifies Category and we are not correcting an 
            // already submitted form then load that Category into the form.
            WeblogCategoryData cd = rreq.getWeblogCategory();
            form.copyFrom(cd, request.getLocale());
            request.setAttribute("state","edit"); 
                
            // Pass Category's parent id on as attribute.                 
            parentCat = cd.getParent();
            request.setAttribute(
                RollerRequest.WEBLOGCATEGORYID_KEY, parentCat.getId());
        }
        else if (null != request.getParameter("correct"))
        {
            // We are correcting a previously submtted form.
            request.setAttribute("state","correcting"); 
                
            // Cat is specified by request param, pass it on as attribute.                 
            parentCat = wmgr.retrieveWeblogCategory(rreq.getWeblogCategory().getId());
            request.setAttribute(
                RollerRequest.WEBLOGCATEGORYID_KEY, parentCat.getId());                
        }
        else
        {
            // We are adding a new Category
            request.setAttribute("state","add");
            
            // Cat is specified by request param, pass it on as attribute. 
            String parentId = request.getParameter(RollerRequest.PARENTID_KEY);
            form.setParentId(parentId);                            
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

        return mapping.findForward("CategoryForm");
    }
    
}
