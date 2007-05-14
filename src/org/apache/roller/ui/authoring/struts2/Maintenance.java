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

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.search.IndexManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Allows user to perform maintenence operations such as flushing
 * the page cache or re-indexing the search index.
 */
public class Maintenance extends UIAction {
    
    private static Log log = LogFactory.getLog(Maintenance.class);
    
    
    public Maintenance() {
        this.actionName = "maintenance";
        this.desiredMenu = "editor";
        this.pageTitle = "maintenance.title";
    }
    
    
    // admin perms required
    public short requiredWeblogPermissions() {
        return PermissionsData.ADMIN;
    }
    
    
    public String execute() {
        return SUCCESS;
    }
    
    
    /**
     * Rebuild search index for weblog.
     */
    public String index() {
        
        try {
            IndexManager manager = RollerFactory.getRoller().getIndexManager();
            manager.rebuildWebsiteIndex(getActionWeblog());
            
            addMessage("maintenance.message.indexed");
        } catch (Exception ex) {
            log.error("Error doing index rebuild", ex);
            // TODO: i18n
            addError("Error rebuilding search index");
        }
        
        return SUCCESS;
    }

    
    /**
     * Flush page cache for weblog.
     */
    public String flushCache() {
        
        try {
            WebsiteData weblog = getActionWeblog();
            
            // some caches are based on weblog last-modified, so update it
            weblog.setLastModified(new Date());
            
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            umgr.saveWebsite(weblog);
            RollerFactory.getRoller().flush();
            
            // also notify cache manager
            CacheManager.invalidate(weblog);

            addMessage("maintenance.message.flushed");
            
        } catch (Exception ex) {
            log.error("Error saving weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error flushing page cache");
        }

        return SUCCESS;
    }
    
}
