/**
 * 
 */
package org.apache.roller.ui.core.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.config.RollerConfig;

/**
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 *
 */
public class AutoProvisioningHelper {

  private static Log log = LogFactory.getFactory().getInstance(AutoProvisioningHelper.class);

  public static boolean executeAutoProvisioning() {
    
    log.debug("AutoProvisionFactory::getInstance()");
    boolean enabled = RollerConfig.getBooleanProperty("users.sso.autoProvision.enabled");
    
    if(!enabled) {
      return false;
    }
    
    String clazzName = RollerConfig.getProperty("users.sso.autoProvision.class");
    
    if(null == clazzName) {
      return false;
    }
    
    Class clazz;
    try {
      clazz = Class.forName(clazzName);
    } catch (ClassNotFoundException e) {
      log.warn("Unable to found specified Auto Provision class.", e);
      return false;
    }
    
    if(null == clazz) {
      return false;
    }
    
    Class[] interfaces = clazz.getInterfaces();
    for (int i=0; i<interfaces.length; i++) {
        if (interfaces[i].equals(AutoProvision.class))
        {
          try {
            AutoProvision autoPrivision = (AutoProvision) clazz.newInstance();
            return autoPrivision.execute();
          } catch (InstantiationException e) {
            log.warn("InstantiationException while creating: " + clazzName, e);
          } catch (IllegalAccessException e) {
            log.warn("IllegalAccessException while creating: " + clazzName, e);
          }
        }
    }
    
    return false;
  }
  
}
