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
