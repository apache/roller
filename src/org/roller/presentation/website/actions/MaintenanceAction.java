/*
 * Created on May 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.roller.presentation.website.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.model.IndexManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerSession;
import org.roller.presentation.pagecache.PageCacheFilter;

/**
 * Allows user to perform Website maintenence operations such as flushing
 * the website page cache or re-indexing the website search index.
 * 
 * @struts.action path="/editor/maintenance" name="maintenanceForm" scope="request" parameter="method"
 * 
 * @struts.action-forward name="maintenance.page" path="/website/Maintenance.jsp"
 */
public class MaintenanceAction extends DispatchAction 
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(MaintenanceAction.class);


	/* (non-Javadoc)
	 * @see org.apache.struts.actions.DispatchAction#unspecified(
	 * 	org.apache.struts.action.ActionMapping, 
	 *  org.apache.struts.action.ActionForm, 
	 *  javax.servlet.http.HttpServletRequest, 
	 *  javax.servlet.http.HttpServletResponse)
	 */
	protected ActionForward unspecified(
			ActionMapping       mapping,
			ActionForm          actionForm,
			HttpServletRequest  request,
			HttpServletResponse response)
			throws ServletException
	{
		return mapping.findForward("maintenance.page");
	}

	//-----------------------------------------------------------------------
    /**
     * Respond to user's request to rebuild search index.
     */
	public ActionForward index(
		ActionMapping       mapping,
		ActionForm          actionForm,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws IOException, ServletException
	{
		try
		{
            RollerSession rollerSession = RollerSession.getRollerSession(request);
			if (rollerSession.isUserAuthorizedToEdit() )
			{
				WebsiteData website = RollerSession.getRollerSession(request).getCurrentWebsite();
				IndexManager manager = RollerFactory.getRoller().getIndexManager();
				manager.rebuildWebsiteIndex(website);
				
                ActionMessages messages = new ActionMessages();
                messages.add(null, new ActionMessage("maintenance.message.indexed"));
                saveMessages(request, messages);
			}
		}
		catch (RollerException re)
		{
			mLogger.error("Unexpected exception",re.getRootCause());
			throw new ServletException(re);
		}
		catch (Exception e)
		{
			mLogger.error("Unexpected exception",e);
			throw new ServletException(e);
		}
		return mapping.findForward("maintenance.page");
	}

    //-----------------------------------------------------------------------
    /**
     * Respond to request to flush a user's page cache.
     */
    public ActionForward flushCache(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        try
        {
            RollerSession rollerSession = RollerSession.getRollerSession(request);
			if ( rollerSession.isUserAuthorizedToEdit() )
			{
	            PageCacheFilter.removeFromCache(request, RollerSession.getRollerSession(request).getCurrentWebsite());

                 ActionMessages messages = new ActionMessages();
                 messages.add(null, new ActionMessage("maintenance.message.flushed"));
                 saveMessages(request, messages);
            }
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
		return mapping.findForward("maintenance.page");
    }
}
