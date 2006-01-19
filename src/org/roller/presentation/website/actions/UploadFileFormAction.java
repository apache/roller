
package org.roller.presentation.website.actions;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.roller.RollerException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.FileManager;
import org.roller.model.PropertiesManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.website.formbeans.UploadFileForm;
import org.roller.util.RollerMessages;


/////////////////////////////////////////////////////////////////////////////
/**
 * @struts.action name="uploadFiles" path="/editor/uploadFiles"
 *  	parameter="method" scope="request" validate="false"
 *
 * @struts.action-forward name="uploadFiles.page" path=".upload-file"
 */
public final class UploadFileFormAction extends DispatchAction {
    private static final String HANDLE = "fileupload.website.handle";
    private static Log mLogger =
            LogFactory.getFactory().getInstance(UploadFileFormAction.class);
    
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
            UploadFilePageModel pageModel = new UploadFilePageModel(
                request, response, mapping, website.getHandle());
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

                        fileSize = files[i].getFileSize();

                        //retrieve the file data
                        if (fmgr.canSave(website.getHandle(), fileName, fileSize, rollerMessages)) {
                            InputStream stream = files[i].getInputStream();
                            fmgr.saveFile(website.getHandle(), fileName, fileSize, stream);
                            lastUploads.add(fileName);
                        }
                        
                        //destroy the temporary file created
                        files[i].destroy();
                    }
                } catch (Exception e) {
                    errors.add(ActionErrors.GLOBAL_ERROR,
                        new ActionError("error.upload.file",e.toString()));
                }
            }        
            UploadFilePageModel pageModel = new UploadFilePageModel(
                request, response, mapping, website.getHandle(), lastUploads);
            request.setAttribute("model", pageModel);
            pageModel.setWebsite(website);
            
            RollerContext rctx = RollerContext.getRollerContext();
		    String baseURL = rctx.getAbsoluteContextUrl(request);
            String resourcesBaseURL = baseURL + fmgr.getUploadUrl() + "/" + website.getHandle();
            Iterator uploads = lastUploads.iterator();
            while (uploads.hasNext()) {                
                messages.add(ActionMessages.GLOBAL_MESSAGE, 
                    new ActionMessage("uploadFiles.uploadedFile", 
                        resourcesBaseURL + "/" + (String)uploads.next()));
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
                        fmgr.deleteFile(website.getHandle(), deleteFiles[i]);
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
            
            UploadFilePageModel pageModel = new UploadFilePageModel(
                request, response, mapping, website.getHandle());
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
    public static WebsiteData getWebsite(HttpServletRequest request) throws RollerException {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData website = rreq.getWebsite();
        if (website != null) {
            request.getSession().setAttribute(HANDLE, website.getHandle());
        } else {
            String handle = (String)request.getSession().getAttribute(HANDLE);
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
        private String maxDirMB = null; // in megabytes
        private String maxFileMB = null; // in megabytes
        private List files = null;
        private long totalSize = 0;
        private List lastUploads = null;
        
        public UploadFilePageModel(
                HttpServletRequest req, 
                HttpServletResponse res, 
                ActionMapping mapping,
                String weblogHandle) throws RollerException {
            this(req, res, mapping, weblogHandle, null);
        }
       
        public UploadFilePageModel(
                HttpServletRequest req, 
                HttpServletResponse res, 
                ActionMapping mapping,
                String weblogHandle,
                List lastUploads) throws RollerException {
            
            super("uploadFiles.title", req, res, mapping);
            
            Roller roller = RollerFactory.getRoller();
            PropertiesManager pmgr = roller.getPropertiesManager();
            FileManager fmgr = roller.getFileManager();
            
            String dir = fmgr.getUploadDir();
            resourcesBaseURL = getBaseURL() + fmgr.getUploadUrl() + "/" + weblogHandle;
            
            RollerRequest rreq = RollerRequest.getRollerRequest(req);
            WebsiteData website = UploadFileFormAction.getWebsite(req);            
            maxDirMB = RollerRuntimeConfig.getProperty("uploads.dir.maxsize");
            maxFileMB = RollerRuntimeConfig.getProperty("uploads.file.maxsize");
                     
            overQuota = fmgr.overQuota(weblogHandle);
            uploadEnabled = RollerRuntimeConfig.getBooleanProperty("uploads.enabled");  
            
            files = new ArrayList();
            File[] rawFiles = fmgr.getFiles(weblogHandle);
            for (int i=0; i<rawFiles.length; i++) {
                files.add(new FileBean(rawFiles[i]));
                totalSize += rawFiles[i].length();
            }
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
    }
    
    /** 
     * If java.io.File followed bean conventions we wouldn't need this. (perhaps 
     * we shouldn't be using files directly here in the presentation layer) 
     */
    public class FileBean {
        private File file;
        public FileBean(File file) {
            this.file = file;
        }
        public String getName() { return file.getName(); }
        public long getLength() { return file.length(); }
    }
}







