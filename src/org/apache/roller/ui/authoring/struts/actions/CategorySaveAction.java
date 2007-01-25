/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
/*
 * Created on Oct 21, 2003
 */
package org.apache.roller.ui.authoring.struts.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.ui.authoring.struts.formbeans.WeblogCategoryFormEx;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.WebsiteData;

/**
 * @struts.action path="/roller-ui/authoring/categorySave" name="weblogCategoryFormEx"
 *    validate="true" input="/roller-ui/authoring/categoryEdit.do"
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
        
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        
        WeblogCategoryData cd = null;
        if (null != form.getId() && !form.getId().trim().equals("")) {
            cd = wmgr.getWeblogCategory(form.getId());
            
            // update changeable properties
            if(!cd.getName().equals(form.getName())) {
                WeblogCategoryData parent = cd.getParent();
                
                // make sure new name is not a duplicate of an existing category
                if(parent.hasCategory(form.getName())) {
                    ActionErrors errors = new ActionErrors();
                    errors.add(ActionErrors.GLOBAL_ERROR,
                            new ActionError("categoryForm.error.duplicateName", form.getName()));
                    saveErrors(request, errors);
                    return mapping.findForward("categoryEdit");
                }
                
                // update the category name
                cd.setName(form.getName());
                
                // path includes name, so update path as well
                if("/".equals(parent.getPath())) {
                    cd.setPath("/"+cd.getName());
                } else {
                    cd.setPath(parent.getPath() + "/" + cd.getName());
                }
            }
            cd.setDescription(form.getDescription());
            cd.setImage(form.getImage());
            
        } else {
            WeblogCategoryData parentCat = wmgr.getWeblogCategory(form.getParentId());
            cd = new WeblogCategoryData(
                    parentCat.getWebsite(),
                    parentCat,
                    form.getName(),
                    form.getDescription(),
                    form.getImage());
            
            // make sure new cat is not a duplicate of an existing category
            if(parentCat.hasCategory(form.getName())) {
                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR,
                        new ActionError("categoryForm.error.duplicateName", form.getName()));
                saveErrors(request, errors);
                return mapping.findForward("categoryEdit");
            }
        }
        
        RollerSession rses = RollerSession.getRollerSession(request);
        if (cd.getWebsite().hasUserPermissions(
            rses.getAuthenticatedUser(), PermissionsData.AUTHOR))
        {
            try {
                wmgr.saveWeblogCategory(cd);
                RollerFactory.getRoller().flush();
                
                // notify caches of object invalidation
                CacheManager.invalidate(cd);
            } catch (RollerException re) {
                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.untranslated", re.getMessage())); 
                saveErrors(request, errors);
            }
            
            request.setAttribute(
                RequestConstants.WEBLOGCATEGORY_ID, cd.getParent().getId());
        }
        else
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        }
        
        return forward;
    }
}
