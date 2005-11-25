/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.weblog.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.weblog.formbeans.WeblogCategoryFormEx;

/**
 * @struts.action path="/editor/categorySave" name="weblogCategoryFormEx"
 *    validate="true" input="/editor/categoryEdit.do"
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
        ActionForward forward = mapping.findForward("categories");
        WeblogCategoryFormEx form = (WeblogCategoryFormEx)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();

        WeblogCategoryData cd = null;
        if (null != form.getId() && !form.getId().trim().equals("")) 
        {
            cd = wmgr.retrieveWeblogCategory(form.getId());
        }
        else 
        {
            cd = wmgr.createWeblogCategory();
            String pid = form.getParentId();
            WeblogCategoryData parentCat = wmgr.retrieveWeblogCategory(pid);
            cd.setWebsite(parentCat.getWebsite());
            cd.setParent(parentCat);
        }

        RollerSession rses = RollerSession.getRollerSession(request);
        if (cd.getWebsite().hasUserPermissions(
            rses.getAuthenticatedUser(), PermissionsData.AUTHOR))
        {
            form.copyTo(cd, request.getLocale());
            cd.save();
            RollerFactory.getRoller().commit();
        }
        else
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        }
        request.setAttribute(
            RollerRequest.WEBLOGCATEGORYID_KEY, cd.getParent().getId());         
        return forward;
    }
}
