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

package org.apache.roller.ui.authoring.struts2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Action for removing a weblog.
 */
public class WeblogRemove extends UIAction {
    
    private static Log log = LogFactory.getLog(WeblogRemove.class);
    
    
    public WeblogRemove() {
        this.actionName = "weblogRemove";
        this.desiredMenu = "editor";
        this.pageTitle = "websiteRemove.title";
    }
    
    
    // admin perms required
    public short requiredWeblogPermissions() {
        return PermissionsData.ADMIN;
    }
    
    
    /** 
     * Show weblog remove confirmation.
     */
    public String execute() {
        return "confirm";
    }
    
    
    /**
     * Remove a weblog.
     */
    public String remove() {
        
        try {
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            
            // remove website
            umgr.removeWebsite(getActionWeblog());
            RollerFactory.getRoller().flush();
            
            CacheManager.invalidate(getActionWeblog());
            
            // TODO: i18n
            addMessage("Successfully removed weblog ["+getActionWeblog().getName()+"]");
            
            return SUCCESS;
            
        } catch (Exception ex) {
            log.error("Error removing weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error removing weblog");
        }
        
        return "confirm";
    }
    
}
