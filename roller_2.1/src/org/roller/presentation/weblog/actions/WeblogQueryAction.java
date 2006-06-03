
package org.roller.presentation.weblog.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.weblog.formbeans.WeblogQueryForm;


/////////////////////////////////////////////////////////////////////////////
/**
 * Query weblog entries and display the results in tabular form.
 *
 * @struts.action path="/editor/weblogQuery" name="weblogQueryForm" 
 *     scope="session" parameter="method"
 * 
 * @struts.action-forward name="weblogQuery.page" path=".WeblogQuery"
 */
public final class WeblogQueryAction extends DispatchAction
{
    //-----------------------------------------------------------------------
    /**
     * Respond to request to add a new or edit an existing weblog entry.
     * Loads the appropriate model objects and forwards the request to
     * the edit weblog page.
     */
    public ActionForward query(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException, RollerException
    {
        WeblogQueryForm form = (WeblogQueryForm)actionForm;
        RollerRequest   rreq = RollerRequest.getRollerRequest(request);
        WeblogManager   wmgr = RollerFactory.getRoller().getWeblogManager();           
        RollerSession   rses = RollerSession.getRollerSession(request);
        
        // ensure that weblog is specfied and user has permission to work there
        if (rreq.getWebsite() != null && rses.isUserAuthorized(rreq.getWebsite())) {
            String status= form.getStatus().equals("ALL") ? null : form.getStatus();        
            request.setAttribute("model", new WeblogQueryPageModel(
               request, 
               response, 
               mapping,
               rreq.getWebsite(),
               form.getCategoryId(),
               form.getStartDateString(),
               form.getEndDateString(),
               status,
               form.getMaxEntries())); 
        } 
        else 
        {
            return mapping.findForward("access-denied");
        }
        return mapping.findForward("weblogQuery.page");
    }
}
