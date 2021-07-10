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

package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.convention.annotation.AllowedMethods;


/**
 * Action for displaying rendering cache info.
 */
// TODO: make this work @AllowedMethods({"execute"})
public class CacheInfo extends UIAction {
    
    // map of stats to display
    private Map<String, Map<String, Object>> stats = Collections.emptyMap();
    
    // cache which we would clear when clear() is called
    private String cache = null;
    
    
    public CacheInfo() {
        this.actionName = "cacheInfo";
        this.desiredMenu = "admin";
        this.pageTitle = "cacheInfo.title";
    }
    
    
    @Override
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
    }
    
    @Override
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    @Override
    public void myPrepare() {
        setStats(CacheManager.getStats());
    }
    
    
    @Override
    public String execute() {
        return SUCCESS;
    }

    
    /**
     * clear action.
     *
     * this is triggered when someone has indicated that they want to clear
     * one or all of the caches.
     */
    public String clear() {
        
        // see if a specific cache was specified
        String handlerClass = getCache();
        if(handlerClass != null && handlerClass.length() > 0) {
            CacheManager.clear(handlerClass);
        } else {
            CacheManager.clear();
        }
        
        // update stats after clear
        myPrepare();
        
        return SUCCESS;
    }

    
    public Map<String, Map<String, Object>> getStats() {
        return stats;
    }

    public void setStats(Map<String, Map<String, Object>> stats) {
        this.stats = stats;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

}
