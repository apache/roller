/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.roller.ui.authoring.struts.actions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.roller.business.referrers.RefererManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.ui.core.RequestConstants;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.cache.CacheManager;

/**
 * Toggle display of a linkback.
 * @struts.action path="/roller-ui/authoring/toggleLinkback" name="toggleLinkback"
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
                String refid = req.getParameter(RequestConstants.REFERRER_ID);
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
                url = entry.getPermalink();
            } else {
    			url = rreq.getWebsite().getURL();
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

