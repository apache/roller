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

import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.authoring.struts.formbeans.WeblogCategoryFormEx;
import org.apache.roller.ui.core.RequestConstants;

/**
 * @struts.action path="/roller-ui/authoring/categoryEdit" name="weblogCategoryFormEx" validate="false"
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
            
            String pid = request.getParameter(RequestConstants.PARENT_ID);
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
