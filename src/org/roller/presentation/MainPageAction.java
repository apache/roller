package org.roller.presentation;

import java.util.Date;
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
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogEntryData;


/**
 * Main page action for Roller.
 * @struts.action name="main" path="/main" scope="request"
 * @struts.action-forward name="main.page" path=".main"
 */
public class MainPageAction extends Action
{
    // TODO: make timeouts configurable
    private static TimedHolder mPopularWebsites = new TimedHolder(30 * 60 * 1000);
    private static TimedHolder mRecentEntries = new TimedHolder(120 * 60 * 1000);
    private static TimedHolder mPinnedEntries = new TimedHolder(120 * 60 * 1000);
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(MainPageAction.class);
        
	/**
	 * Loads model and forwards to main.page.
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
        req.setAttribute("data", new MainPageData(req));
        
        // Determines if register new sers
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
        
        return mapping.findForward("main.page");
	}
    
    public static void flushMainPageCache()
    {
        mLogger.debug("Flushing recent and pinned entries");
        mRecentEntries.expire();    
        mPinnedEntries.expire();    
    }
    
    /**
     * Page model. 
     */
    public static class MainPageData 
    {
        private HttpServletRequest mRequest = null;
        
        public MainPageData(HttpServletRequest req) 
        {
            mRequest = req;
        }
        
        /** 
         * Get list of most popular websites in terms of day hits.
         * @param num Number of entries to return (takes effect on next cache refresh)
         */
        public List getPopularWebsites(int num) throws RollerException
        {
            List list = (List)mPopularWebsites.getObject();
            if (list == null)
            {
                mLogger.debug("Refreshing popular websites list");
                Roller roller = RollerFactory.getRoller();            
                list = roller.getRefererManager().getDaysPopularWebsites(num);
                mPopularWebsites.setObject(list);
            }
            return list;
       }
        
        /** 
         * Get list of recent weblog entries.
         * @param num Number of entries to return (takes effect on next cache refresh)
         */
        public List getRecentWeblogEntries(int num) throws RollerException
        {
            List list = (List)mRecentEntries.getObject();
            try {
            if (list == null)
            {
                mLogger.debug("Refreshing recent entries list");
                Roller roller = RollerFactory.getRoller();		                      
                list = roller.getWeblogManager().getWeblogEntries(
                    null,                   // userName
                    null,                   // startDate
                    new Date(),             // endDate
                    null,                   // catName
                    WeblogEntryData.PUBLISHED, // status
                    new Integer(num));       // maxEntries
                mRecentEntries.setObject(list);
            }
            } 
            catch (Exception e) 
            {
                mLogger.error(e);
            }
            return list;
        }
        
        /** 
         * Get list of recent weblog pinned entries 
         * @param num Number of entries to return (takes effect on next cache refresh)
         */
        public List getWeblogEntriesPinnedToMain(int num) throws RollerException
        {
            List list = (List)mPinnedEntries.getObject();
            if (list == null)
            {
                mLogger.debug("Refreshing pinned entries list");
                Roller roller = RollerFactory.getRoller();
                list = roller.getWeblogManager()
                    .getWeblogEntriesPinnedToMain(new Integer(num));  
                mPinnedEntries.setObject(list);
            }
            return list;
        }
    }
    
    /** Hold object and expire after timeout passes. */
    public static class TimedHolder 
    {
        private Object obj = null;
        private long updated = 0L;
        private long timeout = 3000L;  // 3 seconds ?? -Lance
        
        /** Create holder with timeout */
        public TimedHolder(long timeout)
        {
            this.timeout = timeout;
        }
        /** Set object and reset the timeout clock */
        public synchronized void setObject(Object obj)
        {
            this.obj = obj;
            this.updated = new Date().getTime();
        }
        /** Force object to expire */
        public synchronized void expire()
        {
            this.obj = null;
        }
        /** Get object or null if object has expired */
        public Object getObject()
        {
            long currentTime = new Date().getTime();
            if ((currentTime - this.updated) > this.timeout)
            {
                return null;
            }
            else 
            {
                return this.obj;
            }
        }
    }
}

