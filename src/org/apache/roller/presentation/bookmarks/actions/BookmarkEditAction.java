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
package org.apache.roller.presentation.bookmarks.actions;

import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.roller.model.BookmarkManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.presentation.BasePageModel;
import org.apache.roller.presentation.RollerRequest;
import org.apache.roller.presentation.RollerSession;
import org.apache.roller.presentation.bookmarks.formbeans.BookmarkFormEx;

/**
 * @struts.action path="/editor/bookmarkEdit" name="bookmarkFormEx" validate="false"
 * @struts.action-forward name="BookmarkForm" path=".BookmarkForm"
 * 
 * @author Dave Johnson
 */
public class BookmarkEditAction extends Action
{
    public ActionForward execute(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        RollerSession rses = RollerSession.getRollerSession(request);
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        BookmarkFormEx form = (BookmarkFormEx)actionForm;
        
        FolderData parentFolder = null;
        if (null!=rreq.getBookmark() && null==request.getParameter("correct")) 
        {
            // If request specifies bookmark and we are not correcting an 
            // already submitted form then load that bookmark into the form.
            BookmarkData bd = rreq.getBookmark();
            form.copyFrom(bd, request.getLocale());
            request.setAttribute("state","edit"); 
                
            // Pass bookmark's Folder on as attribute.                 
            parentFolder = bd.getFolder();

            request.setAttribute("model", new BasePageModel(
                "bookmarkForm.edit.title", request, response, mapping));
        }
        else if (null != request.getParameter("correct"))
        {
            // We are correcting a previously submtted form.
            request.setAttribute("state","correcting"); 
                
            // Folder is specified by request param, pass it on as attribute.                 
            parentFolder = bmgr.getFolder(rreq.getFolder().getId());        
            
            request.setAttribute("model", new BasePageModel(
                "bookmarkForm.correct.title", request, response, mapping));
        }
        else
        {
            // We are adding a new bookmark
            request.setAttribute("state","add");
            
            // Folder is specified by request param, pass it on as attribute.                 
            parentFolder = bmgr.getFolder(rreq.getFolder().getId()); 
            
            request.setAttribute("model", new BasePageModel(
                "bookmarkForm.add.title", request, response, mapping));
        }
        
        // Build folder path for display on page
        if (null != parentFolder)
        {
            request.setAttribute(
                RollerRequest.FOLDERID_KEY, parentFolder.getId());
            
            LinkedList folderPath = new LinkedList();
            folderPath.add(0, parentFolder);
            FolderData parent = parentFolder.getParent();
            while (parent != null) 
            {
                folderPath.add(0, parent);
                parent = parent.getParent();   
            }
            request.setAttribute("parentFolder", parentFolder);
            request.setAttribute("folderPath", folderPath);
        }        
        return mapping.findForward("BookmarkForm");
    }
    
}
