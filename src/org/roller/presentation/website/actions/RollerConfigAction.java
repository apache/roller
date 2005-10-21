
package org.roller.presentation.website.actions;

import java.io.IOException;

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
import org.roller.RollerException;
import org.roller.RollerPermissionsException;
import org.roller.model.IndexManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.RollerConfigData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.website.formbeans.RollerConfigFormEx;

/////////////////////////////////////////////////////////////////////////////
/**
 * New user form action.
 * 
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
                RollerConfigData rollerConfig = RollerFactory.getRoller().getConfigManager().getRollerConfig();
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
                RollerConfigFormEx rcForm = (RollerConfigFormEx)actionForm;
                                    				
                // get current RollerConfig and set new values
                RollerConfigData rollerConfig = RollerFactory.getRoller().getConfigManager().getRollerConfig();
                rcForm.copyTo(rollerConfig, request.getLocale());
                
                // persist
                rreq.getRoller().getConfigManager().storeRollerConfig(rollerConfig);
                rreq.getRoller().getRefererManager().applyRefererFilters();
                rreq.getRoller().commit();
                
                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(null, new ActionMessage("weblogEdit.changesSaved"));
                saveMessages(request, uiMessages);
            }
            else
            {
                forward = mapping.findForward("access-denied");
            }
        }
        catch (RollerPermissionsException e)
        {
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
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
             RollerRequest rreq = RollerRequest.getRollerRequest(request);
			IndexManager manager = rreq.getRoller().getIndexManager();
										 
			manager.rebuildUserIndex();
			request.getSession().setAttribute(
				RollerSession.STATUS_MESSAGE,
					"Successfully scheduled rebuild of all users' indexes");

            // load up RollerConfig for UI
            RollerConfigData rollerConfig = RollerFactory.getRoller().getConfigManager().getRollerConfig();
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