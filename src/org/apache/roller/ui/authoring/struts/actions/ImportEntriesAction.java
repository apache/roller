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
 * Created on Mar 31, 2004
 */
package org.apache.roller.ui.authoring.struts.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.ui.authoring.struts.formbeans.ImportEntriesForm;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.business.FileManager;
import org.apache.roller.pojos.WeblogResource;

/**
 * TODO: revisit this class once Atom 1.0 support comes to Rome
 * @struts.action name="importEntries" path="/roller-ui/authoring/importEntries"
 *                scope="request" parameter="method"
 * 
 * @struts.action-forward name="importEntries.page" path=".import-entries"
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
            if ( rreq.getWebsite() == null 
                  || !rollerSession.isUserAuthorizedToAdmin(rreq.getWebsite()))
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
                    WebsiteData website = rreq.getWebsite();

                    // load selected file
                    FileManager fMgr = RollerFactory.getRoller().getFileManager();
                    WeblogResource f = fMgr.getFile(website, form.getImportFileName());

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
                        //PageCacheFilter.removeFromCache(request, website);
                        CacheManager.invalidate(website);
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
            RollerSession rses = RollerSession.getRollerSession(request);
            if ( rreq.getWebsite() == null 
                 || !rses.isUserAuthorizedToAdmin(rreq.getWebsite()) )
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
		String dir = null;
                
		File d = new File(dir + rreq.getWebsite().getHandle());
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
