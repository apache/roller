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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.FileManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.WeblogResource;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.authoring.struts.formbeans.UploadFileForm;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.util.RollerMessages;
import org.apache.roller.util.URLUtilities;


/**
 * @struts.action name="uploadFiles" path="/roller-ui/authoring/uploadFiles"
 *  	parameter="method" scope="request" validate="false"
 *
 * @struts.action-forward name="uploadFiles.page" path=".upload-file"
 */
public final class UploadFileFormAction extends DispatchAction {
    
    private static Log mLogger = LogFactory.getLog(UploadFileFormAction.class);
    
    /**
     * Display upload file page.
     */
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        
        ActionForward fwd =  mapping.findForward("access-denied");
        RollerSession rses = RollerSession.getRollerSession(request);
        WebsiteData website = getWebsite(request);
        
        if (rses.isUserAuthorizedToAuthor(website)) {
            // this is the path in the uploads area that is being used
            UploadFileForm uploadForm = (UploadFileForm) actionForm;
            String path = uploadForm.getPath();
            
            UploadFilePageModel pageModel = new UploadFilePageModel(
                    request, response, mapping, website, path);
            pageModel.setWebsite(website);
            request.setAttribute("model", pageModel);
            fwd = mapping.findForward("uploadFiles.page");
        }
        
        return fwd;
    }
    
    
    /**
     * Request to upload files
     */
    public ActionForward upload(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        
        ActionForward fwd = mapping.findForward("access-denied");
        WebsiteData website = getWebsite(request);
        RollerMessages rollerMessages = new RollerMessages();
        RollerSession rses = RollerSession.getRollerSession(request);
        List lastUploads = new ArrayList();
        
        if ( rses.isUserAuthorizedToAuthor(website)) {
            
            FileManager fmgr = RollerFactory.getRoller().getFileManager();
            fwd = mapping.findForward("uploadFiles.page");
            ActionMessages messages = new ActionMessages();
            ActionErrors errors = new ActionErrors();
            UploadFileForm theForm = (UploadFileForm)actionForm;
            String path = theForm.getPath();
            if (theForm.getUploadedFiles().length > 0) {
                ServletContext app = servlet.getServletConfig().getServletContext();
                
                boolean uploadEnabled =
                        RollerRuntimeConfig.getBooleanProperty("uploads.enabled");
                
                if ( !uploadEnabled ) {
                    errors.add(ActionErrors.GLOBAL_ERROR,
                            new ActionError("error.upload.disabled", ""));
                    saveErrors(request, errors);
                    return fwd;
                }
                
                //this line is here for when the input page is upload-utf8.jsp,
                //it sets the correct character encoding for the response
                String encoding = request.getCharacterEncoding();
                if ((encoding != null) && (encoding.equalsIgnoreCase("utf-8"))) {
                    response.setContentType("text/html; charset=utf-8");
                }
                
                //retrieve the file representation
                FormFile[] files = theForm.getUploadedFiles();
                int fileSize = 0;
                try {
                    for (int i=0; i<files.length; i++) {
                        if (files[i] == null) continue;
                        
                        // retrieve the file name
                        String fileName= files[i].getFileName();
                        int terminated = fileName.indexOf("\000");
                        if (terminated != -1) {
                            // disallow sneaky null terminated strings
                            fileName = fileName.substring(0, terminated).trim();
                        }
                        if(path != null) {
                            fileName = path + "/" + fileName;
                        }
                        
                        fileSize = files[i].getFileSize();
                        
                        InputStream stream = files[i].getInputStream();
                        fmgr.saveFile(website, fileName,
                                files[i].getContentType(), fileSize, stream);
                        lastUploads.add(fileName);

                        //destroy the temporary file created
                        files[i].destroy();
                    }
                } catch (Exception e) {
                    errors.add(ActionErrors.GLOBAL_ERROR,
                            new ActionError("error.upload.file",e.toString()));
                    mLogger.error("Error saving uploaded file", e);
                }
            }
            
            UploadFilePageModel pageModel = new UploadFilePageModel(
                    request, response, mapping, website, path, lastUploads);
            request.setAttribute("model", pageModel);
            pageModel.setWebsite(website);
            
            Iterator uploads = lastUploads.iterator();
            if (uploads.hasNext()) {
                messages.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("uploadFiles.uploadedFiles"));
            }
            while (uploads.hasNext()) {
                messages.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("uploadFiles.uploadedFile",
                        URLUtilities.getWeblogResourceURL(website, (String)uploads.next(), true)));
            }
            saveMessages(request, messages);
            
            Iterator iter = rollerMessages.getErrors();
            while (iter.hasNext()) {
                RollerMessages.RollerMessage error =
                        (RollerMessages.RollerMessage)iter.next();
                errors.add(ActionErrors.GLOBAL_ERROR,
                        new ActionError(error.getKey(), error.getArgs()));
            }
            saveErrors(request, errors);
        }
        return fwd;
    }
    
    
    /**
     * Request to delete files
     */
    public ActionForward delete(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws Exception {
        
        ActionMessages messages = new ActionMessages();
        ActionErrors errors = new ActionErrors();
        UploadFileForm theForm = (UploadFileForm)actionForm;
        ActionForward fwd = mapping.findForward("access-denied");
        WebsiteData website = getWebsite(request);
        
        int count = 0;
        RollerSession rses = RollerSession.getRollerSession(request);
        if (rses.isUserAuthorizedToAuthor(website)) {
            fwd = mapping.findForward("uploadFiles.page");
            try {
                FileManager fmgr = RollerFactory.getRoller().getFileManager();
                String[] deleteFiles = theForm.getDeleteFiles();
                for (int i=0; i<deleteFiles.length; i++) {
                    if (    deleteFiles[i].trim().startsWith("/")
                    || deleteFiles[i].trim().startsWith("\\")
                    || deleteFiles[i].indexOf("..") != -1) {
                        // ignore absolute paths, or paths that contiain '..'
                    } else {
                        fmgr.deleteFile(website, deleteFiles[i]);
                        count++;
                    }
                }
            } catch (Exception e) {
                errors.add(ActionErrors.GLOBAL_ERROR,
                        new ActionError("error.upload.file",e.toString()));
                saveErrors(request,errors);
            }
            
            messages.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("uploadFiles.deletedFiles", new Integer(count)));
            saveMessages(request, messages);
            
            String path = theForm.getPath();
            UploadFilePageModel pageModel = new UploadFilePageModel(
                    request, response, mapping, website, path);
            pageModel.setWebsite(website);
            request.setAttribute("model", pageModel);
        }
        return fwd;
    }
    
    
    /**
     * Other actions can get the website handle from request params, but
     * request params don't come accross in a file-upload post so we have to
     * stash the website handle in the session.
     */
    private WebsiteData getWebsite(HttpServletRequest request) throws RollerException {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData website = rreq.getWebsite();
        if (website != null) {
            request.getSession().setAttribute(RequestConstants.WEBLOG_SESSION_STASH, website.getHandle());
        } else {
            String handle = (String)request.getSession().getAttribute(RequestConstants.WEBLOG_SESSION_STASH);
            Roller roller = RollerFactory.getRoller();
            website = roller.getUserManager().getWebsiteByHandle(handle);
        }
        return website;
    }
    
    
    /** All information we'll need on the UploadFile page */
    public class UploadFilePageModel extends BasePageModel {
        private String resourcesBaseURL = null;
        private boolean uploadEnabled = true;
        private boolean overQuota = false;
        private boolean showingRoot = true;
        private String path = null;
        private String parentPath = null;
        private String maxDirMB = null; // in megabytes
        private String maxFileMB = null; // in megabytes
        private List files = null;
        private long totalSize = 0;
        private List lastUploads = null;
        
        public UploadFilePageModel(
                HttpServletRequest req,
                HttpServletResponse res,
                ActionMapping mapping,
                WebsiteData weblog,
                String uploadsPath) throws RollerException {
            this(req, res, mapping, weblog, uploadsPath, null);
        }
        
        public UploadFilePageModel(
                HttpServletRequest req,
                HttpServletResponse res,
                ActionMapping mapping,
                WebsiteData weblog,
                String uploadsPath,
                List lastUploads) throws RollerException {
            
            super("uploadFiles.title", req, res, mapping);
            
            FileManager fmgr = RollerFactory.getRoller().getFileManager();
            
            path = uploadsPath;
            
            // are we showing the root of the weblog's upload area?
            // are we in a subdirectory which has a parent path?
            if(uploadsPath != null && !uploadsPath.trim().equals("")) {
                showingRoot = false;
                if(uploadsPath.indexOf("/") != -1) {
                    parentPath = uploadsPath.substring(0, uploadsPath.lastIndexOf("/"));
                }
            }
            
            resourcesBaseURL = URLUtilities.getWeblogResourceURL(weblog, "", false);
            
            maxDirMB = RollerRuntimeConfig.getProperty("uploads.dir.maxsize");
            maxFileMB = RollerRuntimeConfig.getProperty("uploads.file.maxsize");
            
            overQuota = fmgr.overQuota(weblog);
            uploadEnabled = RollerRuntimeConfig.getBooleanProperty("uploads.enabled");
            
            WeblogResource[] resources = fmgr.getFiles(weblog, uploadsPath);
            for (int i=0; i<resources.length; i++) {
                totalSize += resources[i].getLength();
            }
            
            files = Arrays.asList(resources);
            Collections.sort(files, new WeblogResourceComparator());
        }
        
        public boolean isUploadEnabled() {
            return uploadEnabled;
        }
        public boolean isOverQuota() {
            return overQuota;
        }
        public String getMaxDirMB() {
            return maxDirMB;
        }
        public String getMaxFileMB() {
            return maxFileMB;
        }
        public String getResourcesBaseURL() {
            return resourcesBaseURL;
        }
        public long getTotalSize() {
            return totalSize;
        }
        public List getFiles() {
            return files;
        }
        public List getLastUploads() {
            return lastUploads;
        }
        public String getParentPath() {
            return parentPath;
        }
        public boolean isShowingRoot() {
            return showingRoot;
        }

        public String getPath() {
            return path;
        }
    }
    
    
    public class WeblogResourceComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            WeblogResource r1 = (WeblogResource)o1;
            WeblogResource r2 = (WeblogResource)o2;
            
            // consider directories so they go to the top of the list
            if(r1.isDirectory() && r2.isDirectory()) {
                // if we have 2 directories then just go by name
                return r1.getPath().compareTo(r2.getPath());
            } else if(r1.isDirectory()) {
                // directories go before files
                return -1;
            } else if(r2.isDirectory()) {
                // directories go before files
                return 1;
            } else {
                // if we have 2 files then just go by name
                return r1.getPath().compareTo(r2.getPath());
            }
        }
        public boolean equals(Object o1, Object o2) {
            WeblogResource r1 = (WeblogResource)o1;
            WeblogResource r2 = (WeblogResource)o2;
            
            // need to be same type to be equals, i.e both files or directories
            if((r1.isDirectory() && !r2.isDirectory()) ||
                    (r1.isFile() && !r2.isFile())) {
                return false;
            }
            
            // after that it's just a matter of comparing paths
            return r1.getPath().equals(r2.getPath());
        }
    }
    
}
