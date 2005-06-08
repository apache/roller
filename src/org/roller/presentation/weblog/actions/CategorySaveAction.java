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
import org.roller.presentation.RollerRequest;
import org.roller.presentation.weblog.formbeans.WeblogCategoryFormEx;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @struts.action path="/categorySave" name="weblogCategoryFormEx"
 *    validate="true" input="/categoryEdit.do"
 * @struts.action-forward name="Categories" path="/categories.do?method=selectCategory"
 * 
 * @author Dave Johnson
 */
public class CategorySaveAction extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        WeblogCategoryFormEx form = (WeblogCategoryFormEx)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WeblogManager wmgr = rreq.getRoller().getWeblogManager();
        
        WeblogCategoryData cd = null;
        if (null != form.getId() && !form.getId().trim().equals("")) 
        {
            cd = wmgr.retrieveWeblogCategory(form.getId());
        }
        else 
        {
            cd = wmgr.createWeblogCategory();
            cd.setWebsite(rreq.getWebsite());
            
            String parentId = form.getParentId();
            cd.setParent(wmgr.retrieveWeblogCategory(parentId));
        }
        form.copyTo(cd, request.getLocale());
        cd.save();
        rreq.getRoller().commit();
        
        request.setAttribute(
            RollerRequest.WEBLOGCATEGORYID_KEY, cd.getParent().getId());         
        return mapping.findForward("Categories");
    }

}
