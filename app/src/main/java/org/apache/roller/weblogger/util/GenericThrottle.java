/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * A tool used to provide throttling support.
 *
 * The basic idea is that if the # of hits from a client within a certain
 * interval of time is greater than the threshold value then the client is
 * considered to be abusive.
 */
public class GenericThrottle {
    private static Log log = LogFactory.getLog(GenericThrottle.class);
    
    // threshold and interval to determine who is abusive
    private int threshold = 1;
    private long interval = 0;

    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // a cache to maintain the data
    Cache clientHistoryCache = null;

    public GenericThrottle(int thresh, int interSec, int maxEntries) {
        
        // threshold can't be negative, that would mean everyone is abusive
        if(thresh > -1) {
            this.threshold = thresh;
        }
        
        // interval must be a positive value
        if(interSec > 0) {
            this.interval = interSec * 1000;
        }
        
        // max entries must be a positive value
        if(maxEntries < 0) {
            maxEntries = 1;
        }

        // get cache instance.  handler is null cuz we don't want to register it
        this.clientHistoryCache = cacheManager.constructCache("throttle", maxEntries, interval);
    }
    
    
    /**
     * Process a new hit from the client.
     *
     * Each call to this method increments the hit count for the client and
     * then returns a boolean value indicating if the hit has pushed the client
     * over the threshold.
     *
     * @return true if client is abusive, false otherwise
     */
    public boolean processHit(String clientId) {
        
        if(clientId == null) {
            return false;
        }
        
        // see if we have any info about this client yet
        ClientInfo client = (ClientInfo) this.clientHistoryCache.get(clientId);
        if(client != null) {
            log.debug("HIT " + clientId);
        } else {
            log.debug("MISS " + clientId);
        }

        // if we already know this client then update their hit count and 
        // see if they have surpassed the threshold
        if(client != null) {
            client.hits++;
            
            log.debug("STATUS "+clientId+" - "+client.hits+" hits since "+client.start);
            
            // abusive client
            if(client.hits > this.threshold) {
                return true;
            }
            
        } else {
            log.debug("NEW "+clientId);
            
            // first timer
            ClientInfo newClient = new ClientInfo();
            newClient.hits = 1;

            this.clientHistoryCache.put(clientId, newClient);
        }
        
        return false;
    }

    /**
     * Check the current status of a client.
     *
     * A client is considered abusive if the number of hits from the client
     * within the configured interval is greater than the set threshold.
     *
     * @return true if client is abusive, false otherwise.
     */
    public boolean isAbusive(String clientId) {
        
        if(clientId == null) {
            return false;
        }
        
        // see if we have any info about this client
        ClientInfo client = (ClientInfo) this.clientHistoryCache.get(clientId);

        if(client != null) {
            log.debug("HIT " + clientId);
        } else {
            log.debug("MISS " + clientId);
        }

        return client != null && client.hits > this.threshold;
    }
    
    
    // just something to keep a few properties in
    private class ClientInfo {
        
        public int hits = 0;
        public java.util.Date start = new java.util.Date();
        
    }
    
}
