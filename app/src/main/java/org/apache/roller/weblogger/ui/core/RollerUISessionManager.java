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

package org.apache.roller.weblogger.ui.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheHandlerAdapter;
import org.apache.roller.weblogger.util.cache.CacheManager;

import java.util.HashMap;
import java.util.Map;

public class RollerUISessionManager implements UISessionManager {
   private static final Log log = LogFactory.getLog(RollerUISessionManager.class);
   private static final String CACHE_ID = "roller.session.cache";

   private final Cache sessionCache;

   public RollerUISessionManager() {
      Map<String, String> cacheProps = new HashMap<>();
      cacheProps.put("id", CACHE_ID);
      cacheProps.put("size", "1000");  // Default cache size
      cacheProps.put("timeout", "3600"); // Default timeout in seconds
      this.sessionCache = CacheManager.constructCache(null, cacheProps);
      CacheManager.registerHandler(new SessionCacheHandler());
   }

   public void register(String userName, RollerUISession session) {
      if (userName != null && session != null) {
         try {
            this.sessionCache.put(userName, session);
            log.debug("Registered session for user: " + userName);
         } catch (Exception e) {
            log.error("Failed to register session for user: " + userName, e);
         }
      }
   }

   public RollerUISession get(String userName) {
      if (userName != null) {
         try {
            return (RollerUISession) this.sessionCache.get(userName);
         } catch (Exception e) {
            log.error("Failed to retrieve session for user: " + userName, e);
         }
      }
      return null;
   }

   public void invalidate(String userName) {
      if (userName != null) {
         try {
            this.sessionCache.remove(userName);
            log.debug("Invalidated session for user: " + userName);
         } catch (Exception e) {
            log.error("Failed to invalidate session for user: " + userName, e);
         }
      }
   }

   class SessionCacheHandler extends CacheHandlerAdapter {
      @Override
      public void invalidate(User user) {
         if (user != null && user.getUserName() != null) {
            try {
               sessionCache.remove(user.getUserName());
               log.debug("Cache handler invalidated session for user: " + user.getUserName());
            } catch (Exception e) {
               log.error("Cache handler failed to invalidate session for user: " + user.getUserName(), e);
            }
         }
      }
   }
}