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
 */

package org.apache.roller.weblogger.ui.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheHandler;
import org.apache.roller.weblogger.util.cache.CacheManager;

import java.util.HashMap;
import java.util.Map;

public class RollerLoginSessionManager {
   private static final Log log = LogFactory.getLog(RollerLoginSessionManager.class);
   private static final String CACHE_ID = "roller.session.cache";
   private final Cache sessionCache;

   public static RollerLoginSessionManager getInstance() {
      return RollerLoginSessionManager.SingletonHolder.INSTANCE;
   }

   private static class SingletonHolder {
      private static final RollerLoginSessionManager INSTANCE = new RollerLoginSessionManager();
   }

   class SessionCacheHandler implements CacheHandler {
     @Override
     public void invalidate(User user) {
         if (user != null && user.getUserName() != null) {
            sessionCache.remove(user.getUserName());
         }
      }
   }

   /** Testing purpose only */
   RollerLoginSessionManager(Cache cache) {
      this.sessionCache = cache;
      CacheManager.registerHandler(new SessionCacheHandler());
   }

   private RollerLoginSessionManager() {
      Map<String, String> cacheProps = new HashMap<>();
      cacheProps.put("id", CACHE_ID);
      cacheProps.put("size", "1000");  // Cache up to 1000 sessions
      cacheProps.put("timeout", "3600"); // Session timeout in seconds (1 hour)
      this.sessionCache = CacheManager.constructCache(null, cacheProps);
      CacheManager.registerHandler(new SessionCacheHandler());
   }

   public void register(String userName, RollerSession session) {
      if (userName != null && session != null) {
         this.sessionCache.put(userName, session);
         log.debug("Registered session for user: " + userName);
      }
   }

   public RollerSession get(String userName) {
      if (userName != null) {
         return (RollerSession) this.sessionCache.get(userName);
      }
      return null;
   }

   public void invalidate(String userName) {
      if (userName != null) {
         this.sessionCache.remove(userName);
         log.debug("Invalidated session for user: " + userName);
      }
   }
}
