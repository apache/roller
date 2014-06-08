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

package org.apache.roller.weblogger.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A utility class for handling i18n messaging.
 */
public final class I18nMessages {
    
    private static final Log LOG = LogFactory.getLog(I18nMessages.class);
    
    // locale and bundle we are using for messaging
    private final Locale locale;
    private final ResourceBundle bundle;
    
    // a map of cached messages instances, keyed by locale
    private static Map<Locale, I18nMessages> messagesMap = 
            Collections.synchronizedMap(new HashMap());
    
    
    private I18nMessages(String locale) {
        Locale loc = I18nUtils.toLocale(locale);
        this.locale = loc;
        this.bundle = ResourceBundle.getBundle("ApplicationResources", loc);
    }
    
    private I18nMessages(Locale locale) {
        this.locale = locale;
        this.bundle = ResourceBundle.getBundle("ApplicationResources", locale);
    }
    
    
    /**
     * Get an instance for a given locale.
     */
    public static I18nMessages getMessages(String locale) {
        
        LOG.debug("request for messages in locale = " + locale);
        
        // check if we already have a message utils created for that locale
        I18nMessages messages = messagesMap.get(I18nUtils.toLocale(locale));
        
        // if no utils for that language yet then construct
        if(messages == null) {
            messages = new I18nMessages(locale);
            
            // keep a reference to it
            messagesMap.put(messages.getLocale(), messages);
        }
        
        return messages;
    }
    
    
    /**
     * Get an instance for a given locale.
     */
    public static I18nMessages getMessages(Locale locale) {
        
        LOG.debug("request for messages in locale = " + locale.toString());
        
        // check if we already have a message utils created for that locale
        I18nMessages messages = messagesMap.get(locale);
        
        // if no utils for that language yet then construct
        if(messages == null) {
            messages = new I18nMessages(locale);
            
            // keep a reference to it
            messagesMap.put(messages.getLocale(), messages);
        }
        
        return messages;
    }
    
    
    /**
     * The locale representing this message utils.
     */
    public Locale getLocale() {
        return this.locale;
    }
    
    
    /**
     * Get a message from the bundle.
     */
    public String getString(String key) {
        
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            // send a warning in the logs
            LOG.warn("Error getting key " + key);
            return key;
        }
    }
    
    
    /**
     * Get a message from the bundle and substitute the given args into
     * the message contents.
     */
    public String getString(String key, List args) {
        
        try {
            String msg = bundle.getString(key);
            return MessageFormat.format(msg, args.toArray());
        } catch (Exception e) {
            // send a warning in the logs
            LOG.warn("Error getting key " + key, e);
            return key;
        }
    }
    
    
    /**
     * Get a message from the bundle and substitute the given args into
     * the message contents.
     */
    public String getString(String key, Object[] args) {
        
        try {
            String msg = bundle.getString(key);
            return MessageFormat.format(msg, args);
        } catch (Exception e) {
            // send a warning in the logs
            LOG.warn("Error getting key " + key, e);
            return key;
        }
    }
    
	/**
	 * Reload bundle.
	 * 
	 * @param key
	 *            the key
	 */
	public static void reloadBundle(Locale key) {

		try {

			Class type = ResourceBundle.class;
			Field cacheList = type.getDeclaredField("cacheList");

			synchronized (cacheList) {
				cacheList.setAccessible(true);
				((Map) cacheList.get(ResourceBundle.class)).clear();
			}

			clearTomcatCache();

			// Remove cached bundle
			messagesMap.remove(key);

		} catch (Exception e) {
			LOG.error("Error clearing message resource bundles", e);
		}

	}

	/**
	 * Clear tomcat cache.
	 * 
	 * @see com.opensymphony.xwork2.util.LocalizedTextUtil
	 */
	private static void clearTomcatCache() {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		// no need for compilation here.
		Class cl = loader.getClass();

		try {
			if ("org.apache.catalina.loader.WebappClassLoader".equals(cl
					.getName())) {
				clearMap(cl, loader, "resourceEntries");
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("class loader " + cl.getName()
							+ " is not tomcat loader.");
				}
			}
		} catch (Exception e) {
			LOG.warn("couldn't clear tomcat cache", e);
		}
	}

	private static void clearMap(Class cl, Object obj, String name)
			throws NoSuchFieldException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		Field field = cl.getDeclaredField(name);
		field.setAccessible(true);

		Object cache = field.get(obj);

		synchronized (cache) {
			Class ccl = cache.getClass();
			Method clearMethod = ccl.getMethod("clear");
			clearMethod.invoke(cache);
		}

	}
}
