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

package org.apache.roller.weblogger.ui.struts2.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.ui.struts2.common.PingTargetsBase;


/**
 * Admin action for managing global ping targets.
 */
public class CommonPingTargets extends PingTargetsBase {
    
    private static Log log = LogFactory.getLog(CommonPingTargets.class);
    
    
    public CommonPingTargets() {
        this.actionName = "commonPingTargets";
        this.desiredMenu = "admin";
        this.pageTitle = "commonPingTargets.commonPingTargets";
    }
    
    
    public String requiredUserRole() {
        return "admin";
    }
    
    // no weblog required
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    protected Log getLogger() {
        return log;
    }
    
    
    public void loadPingTargets() {
        try {
            PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
            setPingTargets(pingTargetMgr.getCommonPingTargets());
        } catch (WebloggerException ex) {
            log.error("Error loading common ping targets", ex);
            // TODO: i18n
            addError("Error loading common ping targets");
        }
    }
    
    
    /**
     * Set a ping target auto enabled to true.
     */
    public String enable() {
        
        if(getPingTarget() != null) {
            try {
                getPingTarget().setAutoEnabled(true);
                
                PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
                pingTargetMgr.savePingTarget(getPingTarget());
                WebloggerFactory.getWeblogger().flush();
            } catch (Exception ex) {
                getLogger().error("Error saving ping target", ex);
                // TODO: i18n
                addError("Error saving ping target.");
            }
        } else {
            // TODO: i18n
            addError("Cannot enable ping target - "+getPingTargetId());
        }
        
        return LIST;
    }
    
    
    /**
     * Set a ping target auto-enable to false.
     */
    public String disable() {
        
        if(getPingTarget() != null) {
            try {
                getPingTarget().setAutoEnabled(false);
                
                PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
                pingTargetMgr.savePingTarget(getPingTarget());
                WebloggerFactory.getWeblogger().flush();
            } catch (Exception ex) {
                getLogger().error("Error saving ping target", ex);
                // TODO: i18n
                addError("Error saving ping target.");
            }
        } else {
            // TODO: i18n
            addError("Cannot disable ping target - "+getPingTargetId());
        }
        
        return LIST;
    }
    
}
