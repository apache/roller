
package org.apache.roller.examples.plugins.pagemodel;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.wrapper.UserWrapper;
import org.apache.roller.weblogger.ui.rendering.model.Model;


public class AuthenticatedUserModel implements Model {
    private static Log log = LogFactory.getLog(AuthenticatedUserModel.class); 
    private HttpServletRequest request = null;
    
    public String getModelName() {
        return "authenticated";
    }

    public void init(Map params) throws WebloggerException {
        this.request = (HttpServletRequest)params.get("request");
    }
    
    public UserWrapper getUser() {
        try {
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses != null && rses.getAuthenticatedUser() != null) {
                return UserWrapper.wrap(rses.getAuthenticatedUser());
            }
        } catch (Exception e) {
            log.warn("ERROR: checking user authorization", e);
        }
        return null;
    }
}
