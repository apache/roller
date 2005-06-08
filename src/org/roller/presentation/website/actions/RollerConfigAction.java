
package org.roller.presentation.website.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.pojos.RollerConfig;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.weblog.search.IndexManager;
import org.roller.presentation.weblog.search.operations.RebuildUserIndexOperation;
import org.roller.presentation.website.formbeans.RollerConfigFormEx;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/////////////////////////////////////////////////////////////////////////////
/**
 * New user form action.
 * @struts.action name="rollerConfigFormEx" path="/rollerConfig"
 *  scope="request" parameter="method"
 */
public final class RollerConfigAction extends DispatchAction
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerConfigAction.class);
        
    //-----------------------------------------------------------------------
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("rollerConfig.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() && rreq.isAdminUser() )
            {
                RollerContext rc = RollerContext.getRollerContext( request );
                RollerConfig rollerConfig = rc.getRollerConfig();
                RollerConfigFormEx rcForm = (RollerConfigFormEx)actionForm;
                rcForm.copyFrom(rollerConfig, request.getLocale());
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }

    //-----------------------------------------------------------------------
    public ActionForward update(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("rollerConfig.page");
        ActionErrors errors = new ActionErrors();
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( rreq.isUserAuthorizedToEdit() && rreq.isAdminUser() )
            {
                RollerContext rc = RollerContext.getRollerContext( request );
                RollerConfigFormEx rcForm = (RollerConfigFormEx)actionForm;
                                    				
                // get current RollerConfig and set new values
                RollerConfig rollerConfig = rc.getRollerConfig();
                rcForm.copyTo(rollerConfig, request.getLocale());
                
                // persist
                rreq.getRoller().getConfigManager().storeRollerConfig(rollerConfig);
                rreq.getRoller().commit();
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (RollerException e)
        {
            mLogger.error(e);
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
               "error.update.rollerConfig",e.getClass().getName()));
            saveErrors(request,errors);
        }
        return forward;
    }

	public ActionForward index(
		ActionMapping       mapping,
		ActionForm          actionForm,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws Exception
	{
		ActionForward forward = mapping.findForward("rollerConfig.page");
		try {
			// if admin requests an index be re-built, do it
			IndexManager manager =
				RollerContext.getRollerContext(RollerContext.getServletContext())
							 .getIndexManager();
										 
			manager.scheduleIndexOperation(new RebuildUserIndexOperation(null));
			request.getSession().setAttribute(
				RollerSession.STATUS_MESSAGE,
					"Successfully scheduled rebuild of all users' indexes");

            // load up RollerConfig for UI
            RollerContext rc = RollerContext.getRollerContext( request );
            RollerConfig rollerConfig = rc.getRollerConfig();
            RollerConfigFormEx rcForm = (RollerConfigFormEx)actionForm;
            rcForm.copyFrom(rollerConfig, request.getLocale());
		}
		catch (Exception e)
		{
			mLogger.error("ERROR in action",e);
			throw new ServletException(e);
		}
		return forward;
	}
}