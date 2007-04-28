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

package org.apache.roller.ui.admin.struts2;

import java.util.Collections;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Action for displaying rendering cache info.
 */
public class CacheInfo extends UIAction {
    
    private static Log log = LogFactory.getLog(CacheInfo.class);
    
    // map of stats to display
    private Map stats = Collections.EMPTY_MAP;
    
    // cache which we would clear when clear() is called
    private String cache = null;
    
    
    public CacheInfo() {
        this.actionName = "cacheInfo";
        this.desiredMenu = "admin";
        this.pageTitle = "";
    }
    
    
    public String requiredUserRole() {
        return "admin";
    }
    
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    public void myPrepare() {
        Map cacheStats = CacheManager.getStats();
        setStats(cacheStats);
    }
    
    
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

    
    public Map getStats() {
        return stats;
    }

    public void setStats(Map stats) {
        this.stats = stats;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

}
