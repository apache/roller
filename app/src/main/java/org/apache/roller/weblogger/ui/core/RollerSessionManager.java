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

   class SessionCacheHandler extends CacheHandlerAdapter {
     @Override
     public void invalidate(User user) {
         if (user != null && user.getUserName() != null) {
            sessionCache.remove(user.getUserName());
         }
      }
   }

   /** Testing purpose only */
   RollerSessionManager(Cache cache) {
      this.sessionCache = cache;
      CacheManager.registerHandler(new SessionCacheHandler());
   }

   private RollerSessionManager() {
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
