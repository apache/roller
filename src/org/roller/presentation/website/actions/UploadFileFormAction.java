
package org.roller.presentation.website.actions;

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
import org.roller.pojos.RollerConfig;
import org.roller.pojos.UserData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.website.formbeans.UploadFileForm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/////////////////////////////////////////////////////////////////////////////
/**
 * @struts.action
 *  name    ="uploadFiles"
 *  path    ="/uploadFiles"
 *  parameter="method"
 *  scope   ="request"
 *  validate="false"
 */
public final class UploadFileFormAction extends DispatchAction
{
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
        RollerRequest rreq = null;
        ActionForward fwd = mapping.findForward("uploadFiles.page");
        String username = "anonymous";
        try
        {
            rreq = RollerRequest.getRollerRequest(request);
            if ( !rreq.isUserAuthorizedToEdit() )
            {
                return mapping.findForward("access-denied");
            }
            UserData user = rreq.getUser();
            username = user.getUserName();
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
            RollerConfig rollerConfig = 
                RollerContext.getRollerContext( app ).getRollerConfig();

            boolean uploadEnabled = rollerConfig.getUploadEnabled().booleanValue();
            if ( !uploadEnabled )
            {
                errors.add(ActionErrors.GLOBAL_ERROR,
                    new ActionError("error.upload.disabled", ""));
                saveErrors(request, errors);
                return fwd;
            }

            // get the root of the /resources directory
            String dir = RollerContext.getUploadDir( app );

            // maximum size of directory contents
            BigDecimal maxDirMB = rollerConfig.getUploadMaxDirMB();
            int maxDirBytes = (int)(1024000 * maxDirMB.doubleValue());
            
            int userDirSize = 0;
            File d = new File(dir + username);
            if (d.mkdirs() || d.exists())
            {
                File[] files = d.listFiles();
                long dirSize = 0l;
                for (int i=0; i<files.length; i++)
                {
                    if (!files[i].isDirectory())
                    {
                        dirSize = dirSize + files[i].length();
                    }
                }
                userDirSize = new Long(dirSize).intValue();
            }

            //this line is here for when the input page is upload-utf8.jsp,
            //it sets the correct character encoding for the response
            String encoding = request.getCharacterEncoding();
            if ((encoding != null) && (encoding.equalsIgnoreCase("utf-8")))
            {
                response.setContentType("text/html; charset=utf-8");
            }

            // what's the maximum file-uploaded file size
            BigDecimal maxFileMB = rollerConfig.getUploadMaxFileMB();
            int maxFileBytes = (int)(1024000 * (maxFileMB.doubleValue()));

            //retrieve the file representation
            //FormFile[] files = theForm.getUploadedFiles();
            FormFile[] files = new FormFile[]{theForm.getUploadedFile()};
            int fileSize = 0;
            try
            {
                String[] allowFiles = rollerConfig.uploadAllowArray();
                String[] forbidFiles = rollerConfig.uploadForbidArray();
                for (int i=0; i<files.length; i++)
                {
                    if (files[i] == null) continue;

                    // retrieve the file name
                    String fileName= files[i].getFileName().toLowerCase();
                	int terminated = fileName.indexOf("\000");
                	if (terminated != -1) 
                    {
                		// disallow sneaky null terminated strings
                		fileName = fileName.substring(0, terminated).trim();
                    }

                    // retrieve the content type
                    // this would be useful if it could be trusted
                    // String contentType = files[i].getContentType();

                    // is the extension of this file okay?
                    boolean allowFile = true;
                    if (forbidFiles != null && forbidFiles.length > 0)
                    {
                        for (int x=0; x<forbidFiles.length; x++)
                        {
                            if (fileName.endsWith(forbidFiles[x].toLowerCase()))
                            {
                                allowFile = false;
                                break;
                            }
                        }
                    }
                    else if (allowFiles != null && allowFiles.length > 0)
                    {
                        allowFile = false;
                        for (int y=0; y<allowFiles.length; y++)
                        {
                            if (fileName.toLowerCase().endsWith(
                                    allowFiles[y].toLowerCase()))
                            {
                                allowFile = true;
                                break;
                            }
                        }
                    }
                    if ( !allowFile )
                    {
                        errors.add(ActionErrors.GLOBAL_ERROR,
                            new ActionError("error.upload.forbiddenFile", 
                                            rollerConfig.getUploadAllow()));                      continue;
                    }

                    //retrieve the file data
                    InputStream stream = files[i].getInputStream();

                    // only write files out that are less than maxMB
                    // and if file doesn't cause user's resource dir to
                    // exceed the maxDirMB
                    fileSize = files[i].getFileSize();
                    if (maxDirBytes < userDirSize + fileSize)
                    {
                        errors.add(ActionErrors.GLOBAL_ERROR,
                            new ActionError("error.upload.dirmax", 
                                            rollerConfig.getUploadMaxDirMB()));
                        saveErrors(request, errors);
                        return fwd;
                    }
                    if (fileSize < maxFileBytes)
                    {
                        byte[] buffer = new byte[8192];
                        int bytesRead = 0;

                        //write the file to the file specified
                        File dirPath = new File(dir + username);
                        if (!dirPath.exists())
                        {
                            dirPath.mkdirs();
                        }
                        OutputStream bos =
                            new FileOutputStream(dir + username+"/"+fileName);
                        while ((bytesRead = stream.read(buffer, 0, 8192)) != -1)
                        {
                            bos.write(buffer, 0, bytesRead);
                        }
                        bos.close();
                        
                        if (mLogger.isDebugEnabled())
                        {
                            mLogger.debug("The file has been written to \""
                                + dir + username + "\"");
                        }
                        
                        // increase userDirSize by current file's size
                        userDirSize = userDirSize + fileSize;
                    }
                    else
                    {
                        errors.add(ActionErrors.GLOBAL_ERROR,
                            new ActionError("error.upload.filemax", 
                                            rollerConfig.getUploadMaxFileMB()));
                    }

                    //close the stream
                    stream.close();

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

        ServletContext app = servlet.getServletConfig().getServletContext();

        // get the root of the /resource directory
        String dir = RollerContext.getUploadDir( app );

        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            UserData user = rreq.getUser();

            // delete files from this directory
            File dirPath = new File(dir + user.getUserName());
            if (!dirPath.exists())
            {
                dirPath.mkdirs();
            }

            File deleteMe = null;
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
	                deleteMe = new File(dirPath, deleteFiles[i]);
	                deleteMe.delete();
	                deleteMe = null;
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
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( !rreq.isUserAuthorizedToEdit() )
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

}

