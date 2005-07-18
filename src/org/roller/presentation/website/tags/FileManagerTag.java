package org.roller.presentation.website.tags;

import java.io.File;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;

/**
 * @jsp.tag name="FileManager"
 */
public class FileManagerTag extends TagSupport
{
    static final long serialVersionUID = 5118479809543177187L;
    
    private transient ResourceBundle bundle = 
        ResourceBundle.getBundle("ApplicationResources");
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerRequest.class);

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

            // get the root of the /resource directory
            String dir = RollerContext.getUploadDir( app );

            HttpServletRequest request =
                (HttpServletRequest)pageContext.getRequest();
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            //UserData user = RollerSession.getRollerSession(request).getAuthenticatedUser();
            WebsiteData website = RollerSession.getRollerSession(request).getCurrentWebsite();

            // for formatting the file size
            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
            format.setMaximumFractionDigits(1);
            format.setMinimumFractionDigits(0);

            pw.println("<table class=\"rollertable\">");
            pw.println("<tr class=\"rHeaderTr\">");
            pw.println("   <th class=\"rollertable\" width=\"95%\">Filename</th>");
            pw.println("   <th class=\"rollertable\">Size</td>");
            pw.println("   <th class=\"rollertable\">Delete</td>");
            pw.println("</tr>");

            // get the files
            String[] files = new String[0];
            int numFiles = 0;
            File d = new File(dir + website.getHandle());
            if (d.mkdirs() || d.exists())
            {
                files = this.fileList(d);

                // print the file list
                if (files != null && 0 < files.length)
                {
                    numFiles = files.length;

                    /*
                    * Force images array to sort alphabetically, ignoring case.
                    * We have to do this as some servers (Bluestone)
                    * returns them sorted according to upload time.
                    */
                    //Get the Collator for US English and set its strength
                    // to PRIMARY
                    java.text.Collator locCollator =
                        java.text.Collator.getInstance(
                            RollerSession.getRollerSession(request).getCurrentWebsite().getLocaleInstance());
                    locCollator.setStrength(Collator.PRIMARY);
                    java.util.Arrays.sort(files, locCollator);

                    File checkSize = null;
                    float totalSize = 0;
                    String fileSize = null;
                    String fileLink = null;
                    for (int i = 0; i < files.length; i++)
                    {
                        checkSize  = new File(d, files[i]);
                        totalSize += new Float(checkSize.length()).floatValue();
                        fileSize   = format.format(
                          new Float(checkSize.length()).floatValue() / 1024);

                        if (i+1 % 2 == 0) pw.print("<tr class=\"rollertable_even\">");
                        else              pw.print("<tr class=\"rollertable_odd\">");

                        fileLink = RequestUtils.printableURL(
                            RequestUtils.absoluteURL( request,
                                RollerContext.getUploadPath( app ) +
                                "/" + website.getHandle() + "/" + files[i] ) );
                        pw.print("<td class=\"rollertable\"><a href=\"" +
                            fileLink + "\">" + files[i] + "</a></td>");
                        pw.print("<td class=\"rollertable\" align=\"right\">" +
                            fileSize + " kb</td>");
                        pw.print("<td class=\"rollertable\" align=\"center\">" +
                            "<input type=\"checkbox\" name=\"deleteFiles\" " +
                            "value=\"" + files[i] + "\"></td>");

                        pw.println("</tr>");
                    }
                    
                    // print the total of file sizes
                    fileSize = format.format(totalSize/1024);
                    pw.print("<tr><td align=\"right\">Total:</td>");
                    pw.print("<td align=\"right\">" + 
                            fileSize + "&nbsp;kb</td>");
                }
            }

            if (numFiles == 0)
            {
                pw.print("<tr class=\"rollertable\">");
                pw.print("<td class=\"rollertable\" colspan=\"3\">");
                pw.print(bundle.getString("uploadFiles.noFiles")+"</td>");
                pw.println("</tr>");
            }
            pw.println("</table>");
        }
        catch (Exception e)
        {
            mLogger.error("Displaying files",e);
            throw new JspException(e);
        }
        return Tag.SKIP_BODY;
    }

    /**
     * Returns a String[] list of files only (excludes directories).
    **/
    public String[] fileList(File d)
    {
        File[] files = null; //list of files & directories in the image directory
        java.util.ArrayList images = new java.util.ArrayList();

        files = d.listFiles();
        for (int i=0; i<files.length; i++)
        {
            if (!files[i].isDirectory()) images.add(files[i].getName());
        }

        String[] myFiles = new String[0];
        return (String[]) images.toArray(myFiles);

    }
}

