package org.roller.presentation.website.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.roller.pojos.RollerConfig;
import org.roller.pojos.UserData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;

import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @jsp.tag name="FileUpload"
 */
public class FileUploadTag extends TagSupport
{
    private ResourceBundle bundle = 
        ResourceBundle.getBundle("org.roller.presentation.ApplicationResources");
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(FileUploadTag.class);

    //-------------------------------------------------------------
    /**
     * Process start tag.
     * @return EVAL_SKIP_BODY
     */
    public int doStartTag() throws JspException
    {
        JspWriter pw = pageContext.getOut();
        try
        {
            ServletContext app = pageContext.getServletContext();
            RollerConfig rollerConfig = 
                RollerContext.getRollerContext( app ).getRollerConfig();

            // get the root of the /resource directory
            String dir = RollerContext.getUploadDir( app );

            HttpServletRequest request =
                (HttpServletRequest)pageContext.getRequest();
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            UserData user = rreq.getUser();
            BigDecimal maxDirMB = rollerConfig.getUploadMaxDirMB();
            int maxDirBytes = (int)(1024000 * maxDirMB.doubleValue());

            // determine the number of bytes in user's directory
            int userDirSize = 0;
            File d = new File(dir + user.getUserName());
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

            boolean uploadEnabled = rollerConfig.getUploadEnabled().booleanValue();
            if ( !uploadEnabled )
            {
                pw.print("<span class=\"error\">");
                pw.print(bundle.getString("uploadFiles.uploadDisabled")); 
                pw.println("</span>");
                
            }
            // if user has exceeded maximum do not allow upload
            else if ( userDirSize > maxDirBytes )
            {
                pw.print("<span class=\"error\">");
                pw.print(bundle.getString("uploadFiles.exceededQuota"));
                pw.println("</span><br />");
            }
            else
            {
                BigDecimal maxFileMB = rollerConfig.getUploadMaxFileMB();
                Hashtable params = new Hashtable();

                String edit = RequestUtils.computeURL( pageContext,
                    "uploadFiles", null, null, null, params,null,false);
                pw.print("<form name=\"uploadFiles\" method=\"post\" ");
                pw.print("action=\"" +  edit + "\" ");
                pw.println("enctype=\"multipart/form-data\">");
                pw.println(bundle.getString("uploadFiles.uploadPrompt")+"<br />");
                pw.print("<input type=\"file\" ");
                pw.println("name=\"uploadedFile\" size=\"30\" />");
                pw.println("&nbsp;");
                pw.println("<input type=\"submit\" value=\""
                    +bundle.getString("uploadFiles.upload")+"\" />");
                pw.println("<input type=\"hidden\" name=\"method\" value=\"upload\" /><br /><br />");
                pw.println(MessageFormat.format(
                    bundle.getString("uploadFiles.quotaNote"),
                    (Object[])new String[] {maxFileMB.toString(), maxDirMB.toString()}));
                pw.println("</form>");
            }
        }
        catch (Exception e)
        {
            mLogger.error("Exception writing file upload tag",e);
            throw new JspException(e);
        }
        return Tag.SKIP_BODY;
    }
}
