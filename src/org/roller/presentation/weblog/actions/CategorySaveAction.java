/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.weblog.actions;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.RollerPermissionsException;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogCategoryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.weblog.formbeans.WeblogCategoryFormEx;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @struts.action path="/editor/categorySave" name="weblogCategoryFormEx"
 *    validate="true" input="/editor/categoryEdit.do"
 * 
 * @struts.action-forward name="Categories" path="/editor/categories.do?method=selectCategory"
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
        ActionForward forward = mapping.findForward("Categories");
        try 
        {
            WeblogCategoryFormEx form = (WeblogCategoryFormEx)actionForm;
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WeblogManager wmgr = rreq.getRoller().getWeblogManager();
            
            WeblogCategoryData cd = null;
            if (null != form.getId() && !form.getId().trim().equals("")) 
            {
                cd = wmgr.retrieveWeblogCategory(form.getId());
                cd.save(); // should throw if save not permitted
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
