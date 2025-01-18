package org.apache.roller.weblogger.ui.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheHandlerAdapter;
import org.apache.roller.weblogger.util.cache.CacheManager;

import java.util.HashMap;
import java.util.Map;

public class RollerSessionManager {
   private static final Log log = LogFactory.getLog(RollerSessionManager.class);
   private static final String CACHE_ID = "roller.session.cache";

   private final Cache sessionCache;

   public static RollerSessionManager getInstance() {
      return RollerSessionManager.SingletonHolder.INSTANCE;
   }

   private static class SingletonHolder {
      private static final RollerSessionManager INSTANCE = new RollerSessionManager();
   }

   private class SessionCacheHandler extends CacheHandlerAdapter {
     public void invalidateUser(User user) {
         if (user != null && user.getUserName() != null) {
            sessionCache.remove(user.getUserName());
         }
      }
   }

   private RollerSessionManager() {
      Map<String, String> cacheProps = new HashMap<>();
      cacheProps.put("id", CACHE_ID);
      this.sessionCache = CacheManager.constructCache(null, cacheProps);
      SessionCacheHandler cacheHandler = new SessionCacheHandler();
      CacheManager.registerHandler(cacheHandler);
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
