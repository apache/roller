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
public class MainPageAction extends Action {
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(MainPageAction.class);
    
    /**
     * Loads model and forwards to main.page.
     */
    public ActionForward execute(
            ActionMapping mapping, ActionForm form,
            HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        RollerContext rctx = RollerContext.getRollerContext(req);
        
        req.setAttribute("version",rctx.getRollerVersion());
        req.setAttribute("buildTime",rctx.getRollerBuildTime());
        req.setAttribute("baseURL", rctx.getContextUrl(req));
        
        MainPageData model = new MainPageData(req, res, mapping);
        req.setAttribute("model", model);
        req.setAttribute("data", model);
        
        // Determines if register new sers
        boolean allowNewUsers =
                RollerRuntimeConfig.getBooleanProperty("users.registration.enabled");
        
        java.security.Principal prince = req.getUserPrincipal();
        if (prince != null) {
            req.setAttribute("loggedIn",Boolean.TRUE);
            req.setAttribute("userName",prince.getName());
        } else if (allowNewUsers) {
            req.setAttribute("allowNewUsers",Boolean.TRUE);
        }
        req.setAttribute("leftPage","/theme/status.jsp");
        
        return mapping.findForward("main.page");
    }
        
    /**
     * Page model.
     */
    public static class MainPageData extends BasePageModel {
        private HttpServletRequest mRequest = null;
        
        public MainPageData(
                HttpServletRequest req,
                HttpServletResponse res,
                ActionMapping mapping) {
            super("dummyTitleKey", req, res, mapping);
            mRequest = req;
        }
        
        /**
         * Get list of most popular websites in terms of day hits.
         * @param num Number of entries to return (takes effect on next cache refresh)
         */
        public List getPopularWebsites(int num) throws RollerException {
            List list = null;
            mLogger.debug("Refreshing popular websites list");
            Roller roller = RollerFactory.getRoller();
            list = roller.getRefererManager().getDaysPopularWebsites(num);
            return list;
        }
        
        /**
         * Get list of recent weblog entries.
         * @param num Number of entries to return (takes effect on next cache refresh)
         */
        public List getRecentWeblogEntries(int num) throws RollerException {
            List list = null;
            try {
                mLogger.debug("Refreshing recent entries list");
                Roller roller = RollerFactory.getRoller();
                list = roller.getWeblogManager().getWeblogEntries(
                        null,                   // userName
                        null,                   // startDate
                        new Date(),             // endDate
                        null,                   // catName
                        WeblogEntryData.PUBLISHED, // status
                        new Integer(num));       // maxEntries
            } catch (Exception e) {
                mLogger.error(e);
            }
            return list;
        }
        
        /**
         * Get list of recent weblog pinned entries
         * @param num Number of entries to return (takes effect on next cache refresh)
         */
        public List getWeblogEntriesPinnedToMain(int num) throws RollerException {
            List list = null;
            mLogger.debug("Refreshing pinned entries list");
            Roller roller = RollerFactory.getRoller();
            list = roller.getWeblogManager()
                .getWeblogEntriesPinnedToMain(new Integer(num));
            return list;
        }
    }
}

