
package org.roller.presentation.weblog.actions;

import java.util.List;

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
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.model.RefererManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;

/**
 * Display today's referers.
 * @struts.action name="refererForm" path="/editor/referers"
 *      scope="session" parameter="method"
 * 
 * @struts.action-forward name="referers.page" path=".referers"
 */
public class ReferersAction extends DispatchAction
{    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(ReferersAction.class);
        
    public ActionForward unspecified(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws Exception
    {
        return view(mapping, actionForm, request, response);
    }
    
    /**
     * execute
     */
    public ActionForward view(
        ActionMapping mapping, ActionForm form,
        HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        ActionForward forward = mapping.findForward("referers.page");
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        RollerSession rollerSession = RollerSession.getRollerSession(req);
        RefererManager refmgr = RollerFactory.getRoller().getRefererManager();
        try
        {
            if ( rollerSession.isUserAuthorizedToAuthor() )
            {   
                req.setAttribute("pageHits",
                    new Integer(refmgr.getDayHits(RollerSession.getRollerSession(req).getCurrentWebsite())));
                    
                req.setAttribute("totalHits",
                    new Integer(refmgr.getTotalHits(RollerSession.getRollerSession(req).getCurrentWebsite())));
                    
                List refs = refmgr.getTodaysReferers(RollerSession.getRollerSession(req).getCurrentWebsite());
                req.setAttribute("referers",refs);        
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        
        return forward;
    }
    
    public ActionForward reset(
        ActionMapping mapping, ActionForm form,
        HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        this.servlet.log("ReferersAction.reset()");
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        RollerSession rollerSession = RollerSession.getRollerSession(req);
        try
        {
            if ( rollerSession.isUserAuthorizedToAuthor() )
            {
                RefererManager refmgr = RollerFactory.getRoller().getRefererManager();
                WebsiteData website = RollerSession.getRollerSession(req).getCurrentWebsite();
                refmgr.forceTurnover(website.getId());
                RollerFactory.getRoller().commit();
            }
            this.servlet.log("ReferersAction.reset(): don't have permission");
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return view(mapping, form, req, res);
    }
    
    public ActionForward delete(
            ActionMapping mapping, ActionForm form,
            HttpServletRequest req, HttpServletResponse res)
        throws Exception
    {
        //this.servlet.log("ReferersAction.delete()");
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        RollerSession rollerSession = RollerSession.getRollerSession(req);
        try
        {
            if (rollerSession.isUserAuthorizedToAuthor() )
            {
                RefererManager refmgr = RollerFactory.getRoller().getRefererManager();
                WebsiteData website = RollerSession.getRollerSession(req).getCurrentWebsite();

                String[] deleteIds = req.getParameterValues("id");
                if (deleteIds != null)
                {
                    for (int i=0; i<deleteIds.length; i++)
                    {
                        refmgr.removeReferer(deleteIds[i]);
                    }
                    RollerFactory.getRoller().commit();
                    ActionMessages messages = new ActionMessages();
                    messages.add(null, new ActionMessage("referers.deletedReferers"));
                    saveMessages(req, messages);
                }
                else 
                {
                    ActionErrors errors = new ActionErrors();
                    errors.add(null, new ActionError("referers.noReferersSpecified"));
                    saveErrors(req, errors);
                }
            }
            //this.servlet.log("ReferersAction.delete(): don't have permission");
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return view(mapping, form, req, res);
    }
}

