package org.roller.presentation.weblog.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.model.RefererManager;
import org.roller.pojos.RefererData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.pagecache.PageCacheFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Toggle display of a linkback.
 * @struts.action name="toggleLinkback" path="/editor/toggleLinkback" scope="session"
 */
public class ToggleLinkbackDisplayAction extends Action
{
    private static Log mLogger = LogFactory.getFactory().getInstance(
        ToggleLinkbackDisplayAction.class);
        
	/**
	 * execute
     */
	public ActionForward execute(
		ActionMapping mapping, ActionForm form,
		HttpServletRequest req, HttpServletResponse res)
		throws Exception
	{
         
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        try
        {
            if ( rreq.isUserAuthorizedToEdit() )
            {
                String refid = req.getParameter(RollerRequest.REFERERID_KEY);
                if ( refid != null )
                {
                    RefererManager refmgr = 
                        rreq.getRoller().getRefererManager();                        
                    RefererData ref = refmgr.retrieveReferer(refid); 
                    boolean was = ref.getVisible()==null ? 
                                  false : ref.getVisible().booleanValue(); 
                    ref.setVisible(Boolean.valueOf( !was )); // what up, dog?                     
                    ref.save();
                    
                    rreq.getRoller().commit();
                    
                    PageCacheFilter.removeFromCache( req, rreq.getCurrentWebsite() );
                }                
            }
        }
        catch (Exception e)
        {
            mLogger.error("Toggling linkback display",e);
            throw new ServletException(e);
        }
        
        // forward to user's website URL
		String url = null;
		try
		{
			RollerContext rctx = RollerContext.getRollerContext(
				rreq.getServletContext());
			url = rctx.getContextUrl( req, rreq.getCurrentWebsite());
			res.sendRedirect(url);
		}
		catch (Exception e)
		{
			mLogger.error("Unexpected exception",e);
		}

        return null;
	}
}

