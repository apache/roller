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
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.ui.authoring.struts.formbeans.WeblogCategoryFormEx;

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
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();

        WeblogCategoryData cd = null;
        if (null != form.getId() && !form.getId().trim().equals("")) 
        {
            cd = wmgr.getWeblogCategory(form.getId());
        }
        else 
        {
            cd = new WeblogCategoryData();
            String pid = form.getParentId();
            WeblogCategoryData parentCat = wmgr.getWeblogCategory(pid);
            cd.setWebsite(parentCat.getWebsite());
            cd.setParent(parentCat);
        }

        RollerSession rses = RollerSession.getRollerSession(request);
        if (cd.getWebsite().hasUserPermissions(
            rses.getAuthenticatedUser(), PermissionsData.AUTHOR))
        {
            form.copyTo(cd, request.getLocale());
            wmgr.saveWeblogCategory(cd);
            RollerFactory.getRoller().flush();
            
            // notify caches of object invalidation
            CacheManager.invalidate(cd);
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
