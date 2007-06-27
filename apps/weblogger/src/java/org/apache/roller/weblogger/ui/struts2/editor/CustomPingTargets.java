/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.config.PingConfig;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.common.PingTargetsBase;


/**
 * Action for managing weblog custom ping targets.
 */
public class CustomPingTargets extends PingTargetsBase {
    
    private static Log log = LogFactory.getLog(CustomPingTargets.class);
    
    
    public CustomPingTargets() {
        this.actionName = "customPingTargets";
        this.desiredMenu = "editor";
        this.pageTitle = "customPingTargets.customPingTargets";
    }
    
    
    // no weblog required
    public boolean isWeblogRequired() {
        return true;
    }
    
    public short requiredWeblogPermissions() {
        return WeblogPermission.ADMIN;
    }
    
    
    protected Log getLogger() {
        return log;
    }
    
    
    public void loadPingTargets() {
        
        if(!PingConfig.getDisallowCustomTargets()) try {
            PingTargetManager pingTargetMgr = WebloggerFactory.getRoller().getPingTargetManager();
            setPingTargets(pingTargetMgr.getCustomPingTargets(getActionWeblog()));
        } catch (WebloggerException ex) {
            log.error("Error loading common ping targets", ex);
            // TODO: i18n
            addError("Error loading common ping targets");
        }
    }
    
}
