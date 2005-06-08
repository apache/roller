
package org.roller.presentation.bookmarks.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.roller.model.BookmarkManager;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.bookmarks.formbeans.FolderFormEx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/////////////////////////////////////////////////////////////////////////////
/**
 * @struts.action 
 *  name    ="folderFormEx" 
 *  path    ="/importBookmarks" 
 *  scope   ="request"
 *  input   ="/bookmarks/import.jsp"
 *  validate="false"
 * 
 * TODO Should import into folder with same name as imported file
 */
public final class ImportBookmarksFormAction extends Action
{
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
		throws IOException, ServletException 
	{
        ActionErrors errors = new ActionErrors();
	    FolderFormEx theForm = (FolderFormEx)actionForm;
		ActionForward fwd = mapping.findForward("importBookmarks.page");
	    if ( theForm.getBookmarksFile() != null )
        {
            //this line is here for when the input page is upload-utf8.jsp,
            //it sets the correct character encoding for the response
            String encoding = request.getCharacterEncoding();
            if ((encoding != null) && (encoding.equalsIgnoreCase("utf-8")))
            {
                response.setContentType("text/html; charset=utf-8");
            }

            boolean writeFile = false; //theForm.getWriteFile();

            //retrieve the file representation
            FormFile file = theForm.getBookmarksFile();
        /*
            //retrieve the file name
            String fileName= file.getFileName();

            //retrieve the content type
            String contentType = file.getContentType();

            //retrieve the file size
            String size = (file.getFileSize() + " bytes");
        */
            String data = null;

            InputStream stream = null;
            try 
            {
                //retrieve the file data
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                stream = file.getInputStream();
                if (!writeFile) 
                {
                    //only write files out that are less than 1MB
                    if (file.getFileSize() < (4*1024000)) {

                        byte[] buffer = new byte[8192];
                        int bytesRead = 0;
                        while ((bytesRead=stream.read(buffer,0,8192)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        data = new String(baos.toByteArray());

                        // Use Roller BookmarkManager to import bookmarks
                        RollerRequest rreq = 
                            RollerRequest.getRollerRequest(request);
                        BookmarkManager bm = 
                            rreq.getRoller().getBookmarkManager();    
                        bm.importBookmarks(rreq.getWebsite(), "unfiled", data);
                        
                        rreq.getRoller().commit();
                    }
                    else 
                    {
                        data = new String("The file is greater than 4MB, " 
                        +" and has not been written to stream." 
                        +" File Size: "+file.getFileSize()+" bytes. This is a" 
                        +" limitation of this particular web application,"
                        +" hard-coded in "
                        +" org.apache.struts.webapp.upload.UploadAction");
                    }
                }
                else 
                {
                    //write the file to the file specified
                    /*OutputStream bos = 
                        new FileOutputStream(theForm.getFilePath());
                    int bytesRead = 0;
                    byte[] buffer = new byte[8192];
                    while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                    bos.close();
                    data = "The file has been written to \"" 
                        + theForm.getFilePath() + "\"";
                    */
                }
            }
            catch (Exception e) 
            {
            	errors.add(ActionErrors.GLOBAL_ERROR,
				    new ActionError("error.importing.bookmarks",e.toString()));
			    saveErrors(request,errors);
                mLogger.error("ERROR: importing bookmarks",e);
            }
            finally 
            {
                if ( stream!=null )
                {
                    try { stream.close(); } 
                    catch (Exception e) { mLogger.error("Closing stream",e); };
                }
            }

            //destroy the temporary file created
            file.destroy();
        }
		return fwd; 
	}
}

