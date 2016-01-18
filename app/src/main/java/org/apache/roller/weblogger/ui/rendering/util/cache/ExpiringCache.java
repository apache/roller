/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.ui.rendering.util.cache;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class ExpiringCache {
    protected static Log log = LogFactory.getLog(ExpiringCache.class);

    protected String cacheHandlerId;

    public void setCacheHandlerId(String cacheHandlerId) {
        this.cacheHandlerId = cacheHandlerId;
    }

    protected boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected int size;

    public void setSize(int size) {
        this.size = size;
    }

    protected long timeoutInMS;

    public void setTimeoutSec(int timeoutSec) {
        this.timeoutInMS = timeoutSec * DateUtils.MILLIS_PER_SECOND;
    }

    protected Cache contentCache = null;

    @Autowired
    protected CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void init() {
        if (enabled) {
            contentCache = cacheManager.constructCache(cacheHandlerId, size, timeoutInMS);
        } else {
            log.warn("Cache " + cacheHandlerId + " has been DISABLED");
        }
    }

    public Object get(String key) {
        Object entry = null;
        if (enabled) {
            entry = contentCache.get(key);
            if (entry == null) {
                log.debug("MISS "+key);
            } else {
                log.debug("HIT "+key);
            }
        }
        return entry;
    }

    public void put(String key, Object value) {
        if (enabled) {
            contentCache.put(key, value);
            log.debug("PUT "+key);
        }
    }

    public void remove(String key) {
        if (enabled) {
            contentCache.remove(key);
            log.debug("REMOVE "+key);
        }
    }

    public void clear() {
        if (enabled) {
            contentCache.clear();
            log.debug("CLEAR");
        }
    }
}
