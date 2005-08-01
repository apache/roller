/*
 * Created on Mar 31, 2004
 */
package org.roller.presentation.weblog.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.MainPageAction;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.presentation.weblog.formbeans.ImportEntriesForm;
import org.roller.util.StringUtils;

/**
 * TODO: revisit this class once Atom 1.0 support comes to Rome
 * @struts.action name="importEntries" path="/editor/importEntries"
 *                scope="request" parameter="method"
 * 
 * @struts.action-forward name="importEntries.page" path="/weblog/import-entries.jsp"
 *
 * @author lance.lavandowska
 */
public class ImportEntriesAction extends DispatchAction
{
    public ActionForward importEntries(
                              ActionMapping       mapping,
                              ActionForm          actionForm,
                              HttpServletRequest  request,
                              HttpServletResponse response)
    throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("importEntries.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = RollerSession.getRollerSession(rreq.getRequest());
            if ( !rollerSession.isUserAuthorizedToAdmin() )
            {
                forward = mapping.findForward("access-denied");
            }
            else
            {
			   getXmlFiles(actionForm, rreq);
                ImportEntriesForm form = (ImportEntriesForm)actionForm;
                if (StringUtils.isNotEmpty(form.getImportFileName()))
                {
                    // "default" values
                    WebsiteData website = RollerSession.getRollerSession(request).getCurrentWebsite();

                    // load selected file
                    ServletContext app = this.getServlet().getServletConfig().getServletContext();
                    String dir = RollerContext.getUploadDir( app );
                    File f = new File(dir + website.getHandle() +
                                      "/" + form.getImportFileName());

                    //ArchiveParser archiveParser =
                        //new ArchiveParser(RollerFactory.getRoller(), rreq.getWebsite(), f);
                    String parseMessages = null; // archiveParser.parse();

                    // buf will be non-zero if Entries were imported
                    if (parseMessages.length() > 0)
                    {
                        ActionMessages notices = new ActionMessages();
                        notices.add(ActionMessages.GLOBAL_MESSAGE, 
                                     new ActionMessage("weblogEntryImport.importFiles", 
                                                   parseMessages));
                        saveMessages(request, notices);

                        // Flush the page cache
                        PageCacheFilter.removeFromCache(request, website);
                        // refresh the front page cache
                        MainPageAction.flushMainPageCache();
                    }
                    else
                    {
                        ActionErrors errors = new ActionErrors();
                        errors.add(ActionErrors.GLOBAL_ERROR,
                                   new ActionError("error.importing.entries", ""));
                        saveErrors(request,errors);
                    }
                }
            }
        }
        catch (Exception e)
        {
            request.getSession().getServletContext().log("ERROR",e);
            throw new ServletException(e);
        }
        return forward;
    }

    /**
     * Load list of XML files available for import.
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward edit(
                              ActionMapping       mapping,
                              ActionForm          actionForm,
                              HttpServletRequest  request,
                              HttpServletResponse response)
    throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("importEntries.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = RollerSession.getRollerSession(
                    rreq.getRequest());
            if ( !rollerSession.isUserAuthorizedToAdmin() )
            {
                forward = mapping.findForward("access-denied");
            }
            else
            {
				getXmlFiles(actionForm, rreq);
            }
        }
        catch (Exception e)
        {
            request.getSession().getServletContext().log("ERROR",e);
            throw new ServletException(e);
        }
        return forward;
    }

    private void getXmlFiles(ActionForm actionForm, RollerRequest rreq)
    {
		ServletContext app = this.getServlet().getServletConfig().getServletContext();
		String dir = RollerContext.getUploadDir( app );
		File d = new File(dir + RollerSession.getRollerSession(rreq.getRequest()).getCurrentWebsite().getHandle());
		ArrayList xmlFiles = new ArrayList();
		if (d.mkdirs() || d.exists())
		{
			File[] files = d.listFiles();
			for (int i=0; i<files.length; i++)
			{
				if (!files[i].isDirectory() &&
					files[i].getName().toLowerCase().endsWith(".xml"))
					// TODO : later change detection to use FileInfo
				{
					xmlFiles.add(files[i].getName());
				}
			}
		}
		ImportEntriesForm form = (ImportEntriesForm)actionForm;
		form.setXmlFiles(xmlFiles);
	}
}
