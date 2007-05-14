
package org.apache.roller.examples.plugins.pagemodel;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.wrapper.UserDataWrapper;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.rendering.model.Model;


public class AuthenticatedUserModel implements Model {
    private static Log log = LogFactory.getLog(AuthenticatedUserModel.class); 
    private HttpServletRequest request = null;
    
    public String getModelName() {
        return "authenticated";
    }

    public void init(Map params) throws RollerException {
        this.request = (HttpServletRequest)params.get("request");
    }
    
    public UserDataWrapper getUser() {
        try {
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses != null && rses.getAuthenticatedUser() != null) {
                return UserDataWrapper.wrap(rses.getAuthenticatedUser());
            }
        } catch (Exception e) {
            log.warn("ERROR: checking user authorization", e);
        }
        return null;
    }
}
