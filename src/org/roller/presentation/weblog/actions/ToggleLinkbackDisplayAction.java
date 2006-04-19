package org.roller.presentation.weblog.actions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.model.RefererManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.RefererData;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.cache.CacheManager;

/**
 * Toggle display of a linkback.
 * @struts.action path="/editor/toggleLinkback" name="toggleLinkback"
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
        WeblogEntryData entry = null;
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        RollerSession rollerSession = RollerSession.getRollerSession(req);
        try
        {
            if (rreq.getWebsite() != null 
                 && rollerSession.isUserAuthorizedToAuthor(rreq.getWebsite()) )
            {
                String refid = req.getParameter(RollerRequest.REFERERID_KEY);
                if ( refid != null )
                {
                    RefererManager refmgr = 
                        RollerFactory.getRoller().getRefererManager();                        
                    RefererData ref = refmgr.getReferer(refid); 
                    entry = ref.getWeblogEntry();
                    boolean was = ref.getVisible()==null ? 
                                  false : ref.getVisible().booleanValue(); 
                    ref.setVisible(Boolean.valueOf( !was )); // what up, dog?                     
                    refmgr.saveReferer(ref);
                    
                    RollerFactory.getRoller().flush();
                    
                    //PageCacheFilter.removeFromCache( req, rreq.getWebsite() );
                    CacheManager.invalidate(rreq.getWebsite());
                }                
            }
        }
        catch (Exception e)
        {
            mLogger.error("Toggling linkback display",e);
            throw new ServletException(e);
        }
        
        // forward back to entry or to blog if we have no entry
		String url = null;
		try
		{
			RollerContext rctx = RollerContext.getRollerContext();
            if (entry != null) {
                url = rctx.createEntryPermalink(entry, req, true);
            } else {
    			url = rctx.getContextUrl(req, rreq.getWebsite());
            }
            res.sendRedirect(url);
		}
		catch (Exception e)
		{
			mLogger.error("Unexpected exception",e);
		}

        return null;
	}
}

