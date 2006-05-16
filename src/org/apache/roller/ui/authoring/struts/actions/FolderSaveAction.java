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
import org.apache.roller.model.BookmarkManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.authoring.struts.formbeans.FolderFormEx;
import org.apache.roller.util.cache.CacheManager;

/**
 * @struts.action path="/editor/folderSave" name="folderFormEx" 
 *     validate="true" input="/editor/folderEdit.do"
 * @struts.action-forward name="Bookmarks" path="/editor/bookmarks.do?method=selectFolder"
 * 
 * @author Dave Johnson
 */
public class FolderSaveAction extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        ActionForward forward = mapping.findForward("Bookmarks");
        FolderFormEx form = (FolderFormEx)actionForm;
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        RollerSession rses = RollerSession.getRollerSession(request);
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        WebsiteData website = null;
                
        FolderData fd = null;
        if (null != form.getId() && !form.getId().trim().equals("")) 
        {
            fd = bmgr.getFolder(form.getId());
            website = fd.getWebsite();
        }
        else 
        {
            fd = new FolderData();
            String parentId = request.getParameter(RollerRequest.PARENTID_KEY);
            FolderData parent = bmgr.getFolder(parentId);
            website = parent.getWebsite();
            fd.setParent(parent);
            fd.setWebsite(website);
        }
        
        if (fd.getWebsite().hasUserPermissions(
                rses.getAuthenticatedUser(), PermissionsData.AUTHOR))
        {
            // Copy form values to object
            form.copyTo(fd, request.getLocale());
            bmgr.saveFolder(fd);
            RollerFactory.getRoller().flush();
            
            CacheManager.invalidate(fd);
        }
        else
        {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        }         
        if (null != fd.getParent()) 
        {
            request.setAttribute(
               RollerRequest.FOLDERID_KEY, fd.getParent().getId());
        }         
        return forward;
    }
}
