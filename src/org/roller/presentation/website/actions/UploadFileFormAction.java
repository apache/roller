
package org.roller.presentation.website.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;
import org.roller.RollerException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.FileManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
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
public final class UploadFileFormAction extends DispatchAction
{
    private static final String HANDLE = "fileupload.website.handle";
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(UploadFileFormAction.class);

    /**
     * Request to upload files
     */
    public ActionForward upload(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward fwd = mapping.findForward("uploadFiles.page");
        WebsiteData website = getWebsite(request);
        
        BasePageModel pageModel = new BasePageModel(
                "uploadFiles.title", request, response, mapping);
        request.setAttribute("model", pageModel);
        pageModel.setWebsite(website);
                
        RollerMessages msgs = new RollerMessages();
        try
        {            
            RollerSession rses = RollerSession.getRollerSession(request);            
            if ( !rses.isUserAuthorizedToAuthor(website) )
            {
                return mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            mLogger.warn("Unable to find user.");
            return fwd;
        }

        ActionErrors errors = new ActionErrors();
        UploadFileForm theForm = (UploadFileForm)actionForm;
        if ( theForm.getUploadedFile() != null )
        {
            ServletContext app = servlet.getServletConfig().getServletContext();

            boolean uploadEnabled = 
                    RollerRuntimeConfig.getBooleanProperty("uploads.enabled");
            
            if ( !uploadEnabled )
            {
                errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.upload.disabled", ""));
                saveErrors(request, errors);
                return fwd;
            }

            //this line is here for when the input page is upload-utf8.jsp,
            //it sets the correct character encoding for the response
            String encoding = request.getCharacterEncoding();
            if ((encoding != null) && (encoding.equalsIgnoreCase("utf-8")))
            {
                response.setContentType("text/html; charset=utf-8");
            }

            //retrieve the file representation
            //FormFile[] files = theForm.getUploadedFiles();
            FormFile[] files = new FormFile[]{theForm.getUploadedFile()};
            int fileSize = 0;
            try
            {
                for (int i=0; i<files.length; i++)
                {
                    if (files[i] == null) continue;

                    // retrieve the file name
                    String fileName= files[i].getFileName();
                	   int terminated = fileName.indexOf("\000");
                	   if (terminated != -1) 
                    {
                		  // disallow sneaky null terminated strings
                		  fileName = fileName.substring(0, terminated).trim();
                    }
                    
                    fileSize = files[i].getFileSize();
                    
                    //retrieve the file data
                    FileManager fmgr = RollerFactory.getRoller().getFileManager();
                    if (fmgr.canSave(website, fileName, fileSize, msgs))
                    {
                        InputStream stream = files[i].getInputStream();
                        fmgr.saveFile(website, fileName, fileSize, stream);
                    }

                    //destroy the temporary file created
                    files[i].destroy();
                }
            }
            catch (Exception e)
            {
                errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.upload.file",e.toString()));
            }
        }
        Iterator iter = msgs.getErrors();
        while (iter.hasNext())
        {
            RollerMessages.RollerMessage error = 
                (RollerMessages.RollerMessage) iter.next();
            errors.add(ActionErrors.GLOBAL_ERROR, 
                new ActionError(error.getKey(), error.getArgs()));
        }
        saveErrors(request, errors);
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
        throws IOException, ServletException
    {
        ActionErrors errors = new ActionErrors();
        UploadFileForm theForm = (UploadFileForm)actionForm;
        ActionForward fwd = mapping.findForward("uploadFiles.page");
        
        WebsiteData website = getWebsite(request);
        BasePageModel pageModel = 
            new BasePageModel("uploadFiles.title", request, response, mapping);
        pageModel.setWebsite(website);
        request.setAttribute("model", pageModel);

        try
        {
            FileManager fmgr = RollerFactory.getRoller().getFileManager();
            String[] deleteFiles = theForm.getDeleteFiles();
            for (int i=0; i<deleteFiles.length; i++)
            {
                if (    deleteFiles[i].trim().startsWith("/")
            	        || deleteFiles[i].trim().startsWith("\\")
                     || deleteFiles[i].indexOf("..") != -1)
            	   {
            		   // ignore absolute paths, or paths that contiain '..'
            	   }
            	   else 
            	   {
                    fmgr.deleteFile(website, deleteFiles[i]);
            	   }
            }
        }
        catch (Exception e)
        {
            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("error.upload.file",e.toString()));
            saveErrors(request,errors);
        }
        return fwd;
    }


    /**
     * Load file-listings page.
     */
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
        throws IOException, ServletException
    {       
        try
        {
            WebsiteData website = getWebsite(request);
            
            BasePageModel pageModel = new BasePageModel(
                "uploadFiles.title", request, response, mapping);
            pageModel.setWebsite(website);
            request.setAttribute("model", pageModel);
            
            RollerSession rses = RollerSession.getRollerSession(request);
            if ( !rses.isUserAuthorizedToAuthor(website) )
            {
                return mapping.findForward("access-denied");
            }
        }
        catch (RollerException re)
        {
            mLogger.error("Unexpected exception",re.getRootCause());
            throw new ServletException(re);
        }
        return mapping.findForward("uploadFiles.page"); 
    }

    /** 
     * Other actions can get the website handle from request params, but 
     * request params don't come accross in a file-upload post so we have to 
     * stash the website handle in the session.
     */
    public static WebsiteData getWebsite(HttpServletRequest request) 
        throws ServletException
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData website = rreq.getWebsite();
        if (website != null) 
        {
            request.getSession().setAttribute(HANDLE, website.getHandle());
        }
        else 
        {
            String handle = (String)request.getSession().getAttribute(HANDLE);
            Roller roller = RollerFactory.getRoller();
            try 
            {
                website = roller.getUserManager().getWebsiteByHandle(handle);
            }
            catch (RollerException e)
            {
                throw new ServletException(e);
            }
        }
        return website;
    }
}

