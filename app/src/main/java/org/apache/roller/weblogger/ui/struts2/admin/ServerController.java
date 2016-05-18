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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.HashMap;
import java.util.Map;

import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.cache.CacheStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;

/**
 * Controller for weblogger backend tasks, e.g., cache and system runtime configuration.
 */
@RestController
@RequestMapping(path="/tb-ui/admin/rest/server")
public class ServerController {

    @Autowired
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public ServerController() {
    }

    @RequestMapping(value = "/caches", method = RequestMethod.GET)
    public Map<String, CacheStats> getCacheData() throws ServletException {
        return cacheManager.getStats();
    }

    @RequestMapping(value = "/cache/{cacheName}/clear", method = RequestMethod.POST)
    public Map<String, CacheStats> emptyOneCache(@PathVariable String cacheName) throws ServletException {
        cacheManager.clear(cacheName);
        Map<String, CacheStats> temp = new HashMap<>();
        temp.put(cacheName, cacheManager.getStats(cacheName));
        return temp;
    }

    @RequestMapping(value = "/caches/clear", method = RequestMethod.POST)
    public Map<String, CacheStats> emptyAllCaches() throws ServletException {
        cacheManager.clear();
        return getCacheData();
    }

}
