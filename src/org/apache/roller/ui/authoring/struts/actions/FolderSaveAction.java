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

package org.apache.roller.ui.authoring.struts.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.roller.RollerException;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.ui.authoring.struts.formbeans.FolderFormEx;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/**
 * Process a folder save action.
 *
 * @struts.action path="/roller-ui/authoring/folderSave" name="folderFormEx"
 *     validate="true" input="/roller-ui/authoring/folderEdit.do"
 * @struts.action-forward name="Bookmarks" path="/roller-ui/authoring/bookmarks.do?method=selectFolder"
 */
public class FolderSaveAction extends Action {
    
    public ActionForward execute(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        
        ActionForward forward = mapping.findForward("Bookmarks");
        FolderFormEx form = (FolderFormEx)actionForm;
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        FolderData fd = null;
        if (null != form.getId() && !form.getId().trim().equals("")) {
            fd = bmgr.getFolder(form.getId());
        } else {
            String parentId = request.getParameter(RequestConstants.PARENT_ID);
            FolderData parent = bmgr.getFolder(parentId);
            fd = new FolderData(
                    parent,
                    form.getName(),
                    form.getDescription(),
                    parent.getWebsite());
        }
        
        RollerSession rses = RollerSession.getRollerSession(request);
        if (fd.getWebsite().hasUserPermissions(
                rses.getAuthenticatedUser(), PermissionsData.AUTHOR)) {
            try {
                bmgr.saveFolder(fd);
                RollerFactory.getRoller().flush();
                
                // notify caches of object invalidation
                CacheManager.invalidate(fd);
            } catch (RollerException re) {
                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR,
                        new ActionError("error.untranslated", re.getMessage()));
                saveErrors(request, errors);
            }
            
            request.setAttribute(
                    RequestConstants.WEBLOGCATEGORY_ID, fd.getParent().getId());
        } else {
            ActionErrors errors = new ActionErrors();
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
        }
        
        if (null != fd.getParent()) {
            request.setAttribute(
                    RequestConstants.FOLDER_ID, fd.getParent().getId());
        }
        
        return forward;
    }
    
}
