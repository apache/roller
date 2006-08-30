/**
 * 
 */
package org.apache.roller.ui.core.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.UserData;

/**
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 * 
 */
public class BasicUserAutoProvision implements AutoProvision {
  
  private static Log log = LogFactory.getFactory().getInstance(BasicUserAutoProvision.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.roller.ui.core.security.AutoProvision#execute()
   */
  public boolean execute() {
    UserData ud = CustomUserRegistry.getUserDetailsFromAuthentication();
    
    if(ud != null) {
      UserManager mgr;
      try {
        mgr = RollerFactory.getRoller().getUserManager();
        mgr.addUser(ud);
        RollerFactory.getRoller().flush();
      } catch (RollerException e) {
        log.warn("Error while auto-provisioning user from SSO.", e);
      }
    }

    return true;
  }

}
