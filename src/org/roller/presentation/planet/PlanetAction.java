package org.roller.presentation.planet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.RollerException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.Roller;
import org.roller.pojos.PlanetGroupData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;

/**
 * Main page action for Roller Planet.
 * @struts.action name="main" path="/planet" scope="request"
 * @struts.action-forward name="planet.page" path="/planet.jsp"
 */
public class PlanetAction extends Action
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(PlanetAction.class);
        
	/**
	 * Loads model and forwards to planet.page.
         */
	public ActionForward execute(
		ActionMapping mapping, ActionForm form,
		HttpServletRequest req, HttpServletResponse res)
		throws Exception
	{        
        RollerContext rctx = RollerContext.getRollerContext(req);		
        req.setAttribute("version",rctx.getRollerVersion());
        req.setAttribute("buildTime",rctx.getRollerBuildTime());
        req.setAttribute("baseURL", rctx.getContextUrl(req));
        req.setAttribute("data", new PlanetPageData(req));
        
        boolean allowNewUsers = 
                    RollerRuntimeConfig.getBooleanProperty("users.registration.enabled");

        java.security.Principal prince = req.getUserPrincipal();
        if (prince != null) 
        {
            req.setAttribute("loggedIn",Boolean.TRUE);
            req.setAttribute("userName",prince.getName());
        } 
        else if (allowNewUsers)
        {   
            req.setAttribute("allowNewUsers",Boolean.TRUE);
        }
        req.setAttribute("leftPage","/theme/status.jsp");
        
        return mapping.findForward("planet.page");
	}
        
    /**
     * Page model. 
     */
    public static class PlanetPageData 
    {
        private HttpServletRequest mRequest = null;
        
        public PlanetPageData(HttpServletRequest req) 
        {
            mRequest = req;
        }
        
        /** 
         * Get aggregation of entries in 'all' and 'external' groups
         */
        public List getAggregation(int num) throws RollerException
        {
            Roller roller = 
                RollerRequest.getRollerRequest(mRequest).getRoller();            
            return roller.getPlanetManager().getAggregation(num);
        }
        /** 
         * Get named group
         */
        public PlanetGroupData getGroup(String name) throws RollerException
        {
            PlanetGroupData group = null;
            try 
            {
                Roller roller = 
                    RollerRequest.getRollerRequest(mRequest).getRoller();  
                group = roller.getPlanetManager().getGroup(name);
            }
            catch (RollerException e) 
            {
                mLogger.error(e); 
            }
            return group;
        }
        /** 
         * Get aggregation of entries in named group
         */
        public List getAggregation(String name, int num) throws RollerException
        {
            List ret = new ArrayList();
            try 
            {
                Roller roller = 
                    RollerRequest.getRollerRequest(mRequest).getRoller();  
                PlanetGroupData group= roller.getPlanetManager().getGroup(name);
                ret = roller.getPlanetManager().getAggregation(group, num);
            }
            catch (RollerException e) 
            {
                mLogger.error(e); 
            }
            return ret;
        }
        /**
         * Get top blogs according to Technorati
         */
        public List getTopSubscriptions(int num) throws RollerException
        {
            List ret = new ArrayList();
            try 
            {
                Roller roller = 
                    RollerRequest.getRollerRequest(mRequest).getRoller();  
                ret = roller.getPlanetManager().getTopSubscriptions(num);
            }
            catch (RollerException e) 
            {
                mLogger.error(e); 
            }
            return ret;
        }
        /**
         * Get top blogs in a group according to Technorati
         */
        public List getTopSubscriptions(String name, int num) 
        throws RollerException
        {
            List ret = new ArrayList();
            try 
            {
                Roller roller = 
                    RollerRequest.getRollerRequest(mRequest).getRoller();  
                PlanetGroupData group= roller.getPlanetManager().getGroup(name);
                ret = roller.getPlanetManager().getTopSubscriptions(group,num);
            }
            catch (RollerException e) 
            {
                mLogger.error(e); 
            }
            return ret;
        }
        /** 
         * Get list of most popular websites in terms of day hits.
         */
        public List getPopularWebsites(int num) throws RollerException
        {
            Roller roller = 
                RollerRequest.getRollerRequest(mRequest).getRoller();            
            return roller.getRefererManager().getDaysPopularWebsites(num);
        }
    }
}

