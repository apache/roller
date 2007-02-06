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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.authoring.struts.formbeans.FolderFormEx;
import org.apache.roller.ui.core.RequestConstants;

/**
 * @struts.action path="/roller-ui/authoring/folderEdit" name="folderFormEx" validate="false"
 * @struts.action-forward name="FolderForm" path=".FolderForm"
 * 
 * @author Dave Johnson
 */
public class FolderEditAction extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        FolderFormEx form = (FolderFormEx)actionForm;
        
        FolderData parentFolder = null;
        if (null!=rreq.getFolder() && null==request.getParameter("correct")) 
        {
            // If request specifies folder and we are not correcting an 
            // already submitted form then load that folder into the form.
            request.setAttribute("state","edit"); 

            FolderData fd = rreq.getFolder();
            form.copyFrom(fd, request.getLocale());
            parentFolder = fd.getParent();
            
            BasePageModel pageModel = new BasePageModel(
                "folderForm.add.title", request, response, mapping);
            pageModel.setWebsite(parentFolder.getWebsite());
            request.setAttribute("model", pageModel);
        }
        else if (null != request.getParameter("correct"))
        {
            // We are correcting a previously submtted form.
            request.setAttribute("state","correcting"); 
            
            String parentId = request.getParameter(RequestConstants.PARENT_ID);
            parentFolder = bmgr.getFolder(parentId);
            
            BasePageModel pageModel = new BasePageModel(
                "folderForm.correct.title", request, response, mapping);
            pageModel.setWebsite(parentFolder.getWebsite());
            request.setAttribute("model", pageModel);
        }
        else
        {
            // We are adding a new bookmark
            request.setAttribute("state","add");
            
            String parentId = request.getParameter(RequestConstants.PARENT_ID);
            parentFolder = bmgr.getFolder(parentId);
            
            BasePageModel pageModel = new BasePageModel(
                "folderForm.add.title", request, response, mapping);
            pageModel.setWebsite(parentFolder.getWebsite());
            request.setAttribute("model", pageModel);
        }
        
        request.setAttribute(RequestConstants.PARENT_ID, parentFolder.getId());
        request.setAttribute("parentFolder", parentFolder);
        
        return mapping.findForward("FolderForm");
    }

}
