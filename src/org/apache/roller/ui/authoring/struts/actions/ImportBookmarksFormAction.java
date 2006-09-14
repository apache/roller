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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.apache.roller.model.BookmarkManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.authoring.struts.formbeans.FolderFormEx;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;

/////////////////////////////////////////////////////////////////////////////
/**
 * @struts.action name="folderFormEx" path="/roller-ui/authoring/importBookmarks"
 *  scope="request" input=".import" validate="false"
 *
 * @struts.action-forward name="importBookmarks.page" path=".import"
 *
 * TODO Should import into folder with same name as imported file
 */
public final class ImportBookmarksFormAction extends Action {
      
    private static Log mLogger =
            LogFactory.getFactory().getInstance(RollerRequest.class);
    
    /**
     * Request to import bookmarks
     */
    public ActionForward execute(
            ActionMapping 		mapping,
            ActionForm 			actionForm,
            HttpServletRequest 	request,
            HttpServletResponse response)
            throws Exception {
        
        ActionErrors errors = new ActionErrors();
        FolderFormEx theForm = (FolderFormEx)actionForm;
        ActionForward fwd = mapping.findForward("importBookmarks.page");
        
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        RollerSession rses = RollerSession.getRollerSession(request);        
        BookmarkManager bm = RollerFactory.getRoller().getBookmarkManager();
        
        BasePageModel pageModel = new BasePageModel(
            "bookmarksImport.title", request, response, mapping);
        request.setAttribute("model", pageModel);

        WebsiteData website = getWebsite(request);
        pageModel.setWebsite(website);
        
        // if user authorized and a file is being uploaded
        if (rses.isUserAuthorizedToAuthor(website) && theForm.getBookmarksFile() != null) {
            
            // this line is here for when the input page is upload-utf8.jsp,
            // it sets the correct character encoding for the response
            String encoding = request.getCharacterEncoding();
            if ((encoding != null) && (encoding.equalsIgnoreCase("utf-8"))) {
                response.setContentType("text/html; charset=utf-8");
            }            
            boolean writeFile = false;
            
            //retrieve the file representation
            FormFile file = theForm.getBookmarksFile();
            String data = null;
            InputStream stream = null;
            try {
               
                //retrieve the file data
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                stream = file.getInputStream();
                if (!writeFile) {
                    //only write files out that are less than 1MB
                    if (file.getFileSize() < (4*1024000)) {
                        
                        byte[] buffer = new byte[8192];
                        int bytesRead = 0;
                        while ((bytesRead=stream.read(buffer,0,8192)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        data = new String(baos.toByteArray());
                        
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("yyyyMMddHHmmss");
                        Date now = new Date();
                        String folderName = "imported-" + formatter.format(now);
                        
                        // Use Roller BookmarkManager to import bookmarks

                        bm.importBookmarks(website, folderName, data);  
                        RollerFactory.getRoller().flush();
                        CacheManager.invalidate(website);
                        
                        ActionMessages messages = new ActionMessages();
                        messages.add(ActionMessages.GLOBAL_MESSAGE,
                           new ActionMessage("bookmarksImport.imported", folderName));
                        saveMessages(request, messages);
                    } 
                    else {
                        data = "The file is greater than 4MB, "
                                +" and has not been written to stream."
                                +" File Size: "+file.getFileSize()+" bytes. "
                                +" This is a limitation of this particular "
                                +" web application, hard-coded in "
                                +" org.apache.struts.webapp.upload.UploadAction";
                        errors.add(ActionErrors.GLOBAL_ERROR,
                           new ActionError("bookmarksImport.error",data));
                    }
                }
                
            } 
            catch (Exception e) {
                errors.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("bookmarksImport.error",e.toString()));
                saveErrors(request,errors);
                mLogger.error("ERROR: importing bookmarks",e);
            } 
            finally {
                if ( stream!=null ) {
                    try { stream.close(); } 
                    catch (Exception e) { mLogger.error("Closing stream",e); };
                }
            }            
            //destroy the temporary file created
            file.destroy();
        }
        else if (!rses.isUserAuthorizedToAuthor(website)) {
            fwd = mapping.findForward("access-denied");
        }        
        return fwd;
    }
    
    /**
     * Other actions can get the website handle from request params, but
     * request params don't come accross in a file-upload post so we have to
     * stash the website handle in the session.
     */
    public static WebsiteData getWebsite(HttpServletRequest request) throws RollerException {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData website = rreq.getWebsite();
        String folderid = request.getParameter(RequestConstants.FOLDER_ID);
        if (website == null && folderid != null) { 
            BookmarkManager bm = RollerFactory.getRoller().getBookmarkManager();
            FolderData folder = bm.getFolder(folderid);     
            website = folder.getWebsite();
        }           
        if (website != null) {
            request.getSession().setAttribute(RequestConstants.WEBLOG_SESSION_STASH, website.getHandle());
        } 
        else {
            String handle = (String)request.getSession().getAttribute(RequestConstants.WEBLOG_SESSION_STASH);
            Roller roller = RollerFactory.getRoller();
            website = roller.getUserManager().getWebsiteByHandle(handle);
        }
        return website;
    }
}

